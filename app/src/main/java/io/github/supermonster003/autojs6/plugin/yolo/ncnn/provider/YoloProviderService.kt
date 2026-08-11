package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.ParcelFileDescriptor
import io.github.supermonster003.autojs6.plugin.yolo.ncnn.YoloPlugin
import org.autojs.plugin.yolo.api.IYoloCallback
import org.autojs.plugin.yolo.api.IYoloProvider
import org.autojs.plugin.yolo.api.IYoloSession
import org.autojs.plugin.yolo.api.YoloCodec
import org.autojs.plugin.yolo.api.YoloContract
import org.autojs.plugin.yolo.api.YoloOpenSessionFailureCodec
import org.autojs.plugin.yolo.api.YoloValidation
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class YoloProviderService : Service() {
    private lateinit var callerVerifier: HostCallerVerifier
    private lateinit var worker: ExecutorService
    private lateinit var callbackLane: SerialCallbackLane
    private val sessionGate = AtomicBoolean(false)
    private val sessions = ConcurrentHashMap.newKeySet<RemoteYoloSession>()

    override fun onCreate() {
        super.onCreate()
        callerVerifier = HostCallerVerifier(this)
        worker = ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            SynchronousQueue<Runnable>(),
            { runnable -> Thread(runnable, "yolo-inference").apply { isDaemon = true } },
            ThreadPoolExecutor.AbortPolicy(),
        )
        callbackLane = SerialCallbackLane()
    }

    // AidlPluginHost later binds an exact component without an Intent action.
    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        sessions.toList().forEach(RemoteYoloSession::serviceDestroyed)
        sessions.clear()
        sessionGate.set(false)
        worker.shutdownNow()
        callbackLane.close()
        super.onDestroy()
    }

    private val binder = object : IYoloProvider.Stub() {
        override fun getProviderInfo(): ByteArray {
            callerVerifier.enforceAllowedCaller()
            return YoloCodec.encodeProviderInfo(YoloPlugin.providerInfo(this@YoloProviderService))
        }

        override fun getCapabilities(): ByteArray {
            callerVerifier.enforceAllowedCaller()
            NativeYoloRuntime.requireReady()
            val info = YoloPlugin.providerInfo(this@YoloProviderService)
            val capabilities = YoloPlugin.capabilities
            YoloValidation.validateCapabilitiesAgainstInfo(capabilities, info)
            return YoloCodec.encodeCapabilities(capabilities)
        }

        override fun openSession(
            request: ByteArray?,
            modelDescriptors: Array<out ParcelFileDescriptor>?,
            callback: IYoloCallback?,
        ): IYoloSession {
            val ownerUid: Int
            val metadata: ByteArray
            val incoming: Array<out ParcelFileDescriptor>
            val safeCallback: IYoloCallback
            val decoded: org.autojs.plugin.yolo.api.YoloOpenSessionRequest
            val deadline: YoloSessionOpenDeadline
            val capabilities = YoloPlugin.capabilities
            try {
                ownerUid = callerVerifier.enforceAllowedCaller()
                metadata = requireNotNull(request) { "YOLO session metadata is missing" }.copyOf()
                require(metadata.size <= YoloContract.MAX_METADATA_BYTES) {
                    "YOLO session metadata exceeds the envelope limit"
                }
                incoming = requireNotNull(modelDescriptors) { "YOLO model descriptors are missing" }
                safeCallback = requireNotNull(callback) { "YOLO callback is missing" }
                decoded = YoloCodec.decodeOpenSessionRequest(metadata, incoming.size)
                require(
                    decoded.protocolVersion >= capabilities.protocolMin &&
                        decoded.protocolVersion <= capabilities.protocolMax,
                ) { "YOLO protocol version is outside the provider range" }
                YoloValidation.validateOpenSessionRequestAgainst(
                    request = decoded,
                    descriptorCount = incoming.size,
                    capabilities = capabilities,
                    negotiatedVersion = decoded.protocolVersion,
                )
                deadline = YoloSessionOpenDeadline.start(decoded.timeoutMillis)
                NativeYoloRuntime.requireReady()
                if (!sessionGate.compareAndSet(false, true)) {
                    throw YoloProviderServiceErrors.providerBusy()
                }
            } catch (error: Throwable) {
                OwnedParcelFileDescriptors.closeIncoming(modelDescriptors)
                throw mapOpenFailure(error)
            }

            val ownedDescriptors = try {
                val owned = OwnedParcelFileDescriptors.duplicateBeforeAsync(incoming)
                try {
                    deadline.requireRemaining()
                    owned
                } catch (error: Throwable) {
                    owned.close()
                    throw error
                }
            } catch (error: Throwable) {
                sessionGate.set(false)
                throw if (error is YoloSessionOpenTimeoutException) {
                    YoloProviderServiceErrors.sessionOpenTimedOut(error.message.orEmpty())
                } else {
                    YoloProviderServiceErrors.sessionOpenFailed(
                        error.message ?: "Cannot duplicate YOLO model descriptors",
                    )
                }
            }

            var model: MaterializedModel? = null
            var engine: YoloInferenceEngine? = null
            try {
                model = MaterializedModel.create(
                    context = this@YoloProviderService,
                    request = decoded,
                    descriptors = ownedDescriptors,
                    maximumArtifactBytes = capabilities.limits.maxModelArtifactBytes,
                    deadline = deadline,
                )
                deadline.requireRemaining()
                engine = NativeYoloRuntime.open(decoded, model, deadline)
                // Native construction must honor the same deadline. Even if a future JNI call
                // returns late, no session may be published after the negotiated timeout.
                deadline.requireRemaining()
                val session = RemoteYoloSession(
                    ownerUid = ownerUid,
                    engine = engine,
                    model = model,
                    callback = safeCallback,
                    callerVerifier = callerVerifier,
                    capabilities = capabilities,
                    worker = worker,
                    callbackLane = callbackLane,
                    onFinished = { finished ->
                        sessions.remove(finished)
                        sessionGate.set(false)
                    },
                )
                sessions += session
                if (session.isFinished) {
                    sessions.remove(session)
                    throw YoloProviderServiceErrors.sessionOpenFailed(
                        "YOLO callback died while the session was opening",
                    )
                }
                return session
            } catch (error: Throwable) {
                runCatching { engine?.close() }
                model?.close()
                ownedDescriptors.close()
                sessionGate.set(false)
                throw mapOpenFailure(error)
            }
        }
    }

    private fun mapOpenFailure(error: Throwable): Throwable = when (error) {
        is SecurityException -> error
        is YoloSessionOpenTimeoutException ->
            YoloProviderServiceErrors.sessionOpenTimedOut(error.message.orEmpty())
        is YoloModelRejectedException ->
            YoloProviderServiceErrors.modelRejected(error.message.orEmpty())
        is IllegalArgumentException -> {
            if (YoloOpenSessionFailureCodec.decode(error) != null) {
                error
            } else {
                YoloProviderServiceErrors.invalidRequest(
                    error.message ?: "Invalid YOLO session request",
                )
            }
        }
        else -> YoloProviderServiceErrors.sessionOpenFailed(
            error.message ?: "YOLO session could not be opened",
        )
    }
}
