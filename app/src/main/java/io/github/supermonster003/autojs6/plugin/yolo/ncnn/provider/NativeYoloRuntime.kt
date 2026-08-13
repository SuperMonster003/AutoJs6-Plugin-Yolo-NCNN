package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import io.github.supermonster003.autojs6.plugin.yolo.ncnn.BuildConfig
import org.autojs.plugin.yolo.api.YoloOpenSessionRequest
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * The source boundary that will own JNI in the next R1 slice.
 * No native library is loaded while [BuildConfig.NCNN_RUNTIME_STAGED] is false.
 */
internal object NativeYoloRuntime {
    private val loadFailure = AtomicReference<Throwable?>(null)
    private val activeNativeHandles = AtomicInteger(0)

    init {
        if (BuildConfig.NCNN_RUNTIME_STAGED) {
            runCatching { System.loadLibrary("autojs_yolo") }
                .onFailure(loadFailure::set)
        }
    }

    val isReady: Boolean
        get() = BuildConfig.NCNN_RUNTIME_STAGED && loadFailure.get() == null

    val backendVersion: String
        get() = if (isReady) nativeBackendVersion() else "not-staged"

    val activeHandleCount: Int
        get() = activeNativeHandles.get()

    fun requireReady() {
        if (!isReady) {
            throw YoloProviderServiceErrors.nativeRuntimeNotReady()
        }
    }

    fun open(
        request: YoloOpenSessionRequest,
        model: MaterializedModel,
        deadline: YoloSessionOpenDeadline,
    ): YoloInferenceEngine {
        deadline.requireRemaining()
        requireReady()
        val decoder = YoloDecoderRegistry.require(request.decoderId)

        val byRole = model.artifacts.groupBy(MaterializedModelArtifact::role)
        require(byRole.keys == setOf("manifest", "ncnn-param", "ncnn-bin")) {
            "YOLO NCNN models require exactly manifest, ncnn-param, and ncnn-bin artifacts"
        }
        require(byRole.values.all { it.size == 1 }) { "YOLO model artifact roles must be unique" }

        val manifest = YoloModelManifest.parse(byRole.getValue("manifest").single().file)
        if (decoder.id != manifest.decoderId) {
            throw YoloModelRejectedException(
                "MANIFEST_DECODER_MISMATCH",
                "Session decoder ${decoder.id} does not match manifest decoder ${manifest.decoderId}",
            )
        }
        deadline.requireRemaining()

        val handle = try {
            registerNativeHandle(
                nativeCreate(
                    paramPath = byRole.getValue("ncnn-param").single().file.absolutePath,
                    binPath = byRole.getValue("ncnn-bin").single().file.absolutePath,
                    inputName = manifest.inputName,
                    outputName = manifest.outputName,
                    cpuThreads = request.cpuThreads,
                    classCount = manifest.labels.size,
                    outputRows = manifest.outputRows,
                ).also { check(it != 0L) { "NCNN returned an invalid detector handle" } },
            )
        } catch (error: IllegalArgumentException) {
            throw YoloModelRejectedException(
                "MODEL_GRAPH_REJECTED",
                error.message ?: "NCNN rejected the model graph",
                error,
            )
        } catch (error: IllegalStateException) {
            throw YoloModelRejectedException(
                "MODEL_GRAPH_REJECTED",
                error.message ?: "NCNN rejected the model graph",
                error,
            )
        }
        return try {
            deadline.requireRemaining()
            NativeYoloEngine(handle, manifest.labels)
        } catch (error: Throwable) {
            destroy(handle)
            throw error
        }
    }

    internal fun detect(
        handle: Long,
        sequence: Long,
        fd: Int,
        width: Int,
        height: Int,
        rowStride: Int,
        sizeBytes: Long,
        confidenceThreshold: Float,
        iouThreshold: Float,
        maxDetections: Int,
    ): FloatArray = nativeDetect(
        handle,
        sequence,
        fd,
        width,
        height,
        rowStride,
        sizeBytes,
        confidenceThreshold,
        iouThreshold,
        maxDetections,
    )

    internal fun cancel(handle: Long, sequence: Long) = nativeCancel(handle, sequence)

    internal fun destroy(handle: Long) = NativeHandleCountPolicy.destroy(
        counter = activeNativeHandles,
        handle = handle,
        nativeDestroy = ::nativeDestroy,
    )

    private fun registerNativeHandle(handle: Long): Long {
        try {
            NativeHandleCountPolicy.onCreated(activeNativeHandles)
            return handle
        } catch (error: Throwable) {
            runCatching { nativeDestroy(handle) }
            throw error
        }
    }

    private external fun nativeDetect(
        handle: Long,
        sequence: Long,
        fd: Int,
        width: Int,
        height: Int,
        rowStride: Int,
        sizeBytes: Long,
        confidenceThreshold: Float,
        iouThreshold: Float,
        maxDetections: Int,
    ): FloatArray

    private external fun nativeCancel(handle: Long, sequence: Long)

    private external fun nativeDestroy(handle: Long)

    private external fun nativeBackendVersion(): String

    private external fun nativeCreate(
        paramPath: String,
        binPath: String,
        inputName: String,
        outputName: String,
        cpuThreads: Int,
        classCount: Int,
        outputRows: Int,
    ): Long
}
