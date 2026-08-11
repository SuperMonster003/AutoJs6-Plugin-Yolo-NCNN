package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import io.github.supermonster003.autojs6.plugin.yolo.ncnn.BuildConfig
import io.github.supermonster003.autojs6.plugin.yolo.ncnn.YoloPlugin
import org.autojs.plugin.yolo.api.YoloOpenSessionRequest
import java.util.concurrent.atomic.AtomicReference

/**
 * The source boundary that will own JNI in the next R1 slice.
 * No native library is loaded while [BuildConfig.NCNN_RUNTIME_STAGED] is false.
 */
internal object NativeYoloRuntime {
    private val loadFailure = AtomicReference<Throwable?>(null)

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
        require(request.decoderId == YoloPlugin.DECODER_ID) { "Unsupported YOLO decoder" }

        val byRole = model.artifacts.groupBy(MaterializedModelArtifact::role)
        require(byRole.keys == setOf("manifest", "ncnn-param", "ncnn-bin")) {
            "YOLO11n requires exactly manifest, ncnn-param, and ncnn-bin artifacts"
        }
        require(byRole.values.all { it.size == 1 }) { "YOLO model artifact roles must be unique" }

        val manifest = YoloModelManifest.parse(byRole.getValue("manifest").single().file)
        require(request.decoderId == "ultralytics-detect") {
            "Session decoder does not match the model manifest"
        }
        deadline.requireRemaining()

        val handle = nativeCreate(
            paramPath = byRole.getValue("ncnn-param").single().file.absolutePath,
            binPath = byRole.getValue("ncnn-bin").single().file.absolutePath,
            inputName = manifest.inputName,
            outputName = manifest.outputName,
            cpuThreads = request.cpuThreads,
        )
        check(handle != 0L) { "NCNN returned an invalid detector handle" }
        return try {
            deadline.requireRemaining()
            NativeYoloEngine(handle, manifest.labels)
        } catch (error: Throwable) {
            nativeDestroy(handle)
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

    internal fun destroy(handle: Long) = nativeDestroy(handle)

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
    ): Long
}
