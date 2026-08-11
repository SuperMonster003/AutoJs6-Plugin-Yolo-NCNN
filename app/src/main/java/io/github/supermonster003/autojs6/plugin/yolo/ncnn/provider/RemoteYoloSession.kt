package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import org.autojs.plugin.yolo.api.IYoloCallback
import org.autojs.plugin.yolo.api.IYoloSession
import org.autojs.plugin.yolo.api.YoloCancellation
import org.autojs.plugin.yolo.api.YoloCancellationReason
import org.autojs.plugin.yolo.api.YoloCancelRequest
import org.autojs.plugin.yolo.api.YoloCapabilities
import org.autojs.plugin.yolo.api.YoloCodec
import org.autojs.plugin.yolo.api.YoloDetectRequest
import org.autojs.plugin.yolo.api.YoloDetectResult
import org.autojs.plugin.yolo.api.YoloError
import org.autojs.plugin.yolo.api.YoloErrorCode
import org.autojs.plugin.yolo.api.YoloFailurePhase
import org.autojs.plugin.yolo.api.YoloSessionLifecyclePolicy
import org.autojs.plugin.yolo.api.YoloValidation
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

internal class RemoteYoloSession(
    private val ownerUid: Int,
    private val engine: YoloInferenceEngine,
    private val model: MaterializedModel,
    private val callback: IYoloCallback,
    private val callerVerifier: HostCallerVerifier,
    private val capabilities: YoloCapabilities,
    private val worker: ExecutorService,
    private val callbackLane: SerialCallbackLane,
    private val onFinished: (RemoteYoloSession) -> Unit,
) : IYoloSession.Stub() {
    private val lifecycle = YoloSessionLifecyclePolicy()
    private val controlLane = SerialControlLane()
    private val callbackBinder = callback.asBinder()
    private val finished = AtomicBoolean(false)
    private val closeRequested = AtomicBoolean(false)
    private val closeIntent = AtomicReference<CloseIntent?>(null)
    private val pendingCancel = AtomicReference<YoloCancelRequest?>(null)
    private val cancelTaskScheduled = AtomicBoolean(false)
    private val emergencyAdmissionTaskScheduled = AtomicBoolean(false)
    private val workerOccupied = AtomicBoolean(false)
    private val activeStartedAt = AtomicLong(0L)
    private var closing = false // control-lane only
    private var identityFailureSequenceFloor = 0L // control-lane only

    private val deathRecipient = IBinder.DeathRecipient {
        scheduleClose(YoloCancellationReason.PROVIDER_SHUTDOWN, notifyCallback = false)
    }

    val isFinished: Boolean
        get() = finished.get()

    init {
        callbackBinder.linkToDeath(deathRecipient, 0)
    }

    override fun detect(request: ByteArray?, imageFd: ParcelFileDescriptor?) {
        try {
            callerVerifier.enforceSessionOwner(ownerUid)
        } catch (error: SecurityException) {
            OwnedParcelFileDescriptors.closeIncoming(imageFd)
            throw error
        }
        val metadata = request ?: run {
            OwnedParcelFileDescriptors.closeIncoming(imageFd)
            return
        }
        val decoded = try {
            YoloCodec.decodeDetectRequest(metadata.copyOf())
        } catch (error: Throwable) {
            OwnedParcelFileDescriptors.closeIncoming(imageFd)
            DetectRequestIdentityExtractor.extractOrNull(metadata)?.let { identity ->
                submitIdentityAdmissionFailure(
                    identity = identity,
                    code = YoloErrorCode.INVALID_REQUEST,
                    message = error.message ?: "YOLO detect request metadata is invalid",
                )
            }
            return
        }
        try {
            YoloValidation.validateDetectRequestAgainst(decoded, capabilities)
        } catch (error: Throwable) {
            OwnedParcelFileDescriptors.closeIncoming(imageFd)
            submitAdmissionFailure(
                request = decoded,
                code = YoloErrorCode.INVALID_REQUEST,
                message = error.message ?: "YOLO detect request is incompatible with this provider",
            )
            return
        }
        val incomingImage = imageFd ?: run {
            submitAdmissionFailure(
                decoded,
                YoloErrorCode.IMAGE_REJECTED,
                "YOLO image descriptor is missing",
            )
            return
        }
        val ownedImage = try {
            OwnedParcelFileDescriptors.duplicateOneBeforeAsync(incomingImage)
        } catch (error: Throwable) {
            submitAdmissionFailure(
                request = decoded,
                code = YoloErrorCode.IMAGE_REJECTED,
                message = error.message ?: "YOLO image descriptor could not be duplicated",
            )
            return
        }

        when (controlLane.tryDispatch { handleDetect(decoded, ownedImage) }) {
            SerialControlLane.DispatchResult.ACCEPTED -> Unit
            SerialControlLane.DispatchResult.SATURATED -> {
                ownedImage.close()
                submitEmergencyAdmissionFailure(
                    request = decoded,
                    code = YoloErrorCode.RESOURCE_EXHAUSTED,
                    message = "YOLO session control lane is saturated",
                )
            }
            SerialControlLane.DispatchResult.CLOSED -> {
                ownedImage.close()
            }
        }
    }

    override fun cancel(request: ByteArray?) {
        val decoded = try {
            callerVerifier.enforceSessionOwner(ownerUid)
            YoloCodec.decodeCancelRequest(
                requireNotNull(request) { "YOLO cancellation metadata is missing" }.copyOf(),
            )
        } catch (error: SecurityException) {
            throw error
        } catch (_: Throwable) {
            return
        }
        submitCancel(decoded)
    }

    override fun close() {
        callerVerifier.enforceSessionOwner(ownerUid)
        scheduleClose(YoloCancellationReason.SESSION_CLOSED, notifyCallback = true)
    }

    fun serviceDestroyed() {
        scheduleClose(YoloCancellationReason.PROVIDER_SHUTDOWN, notifyCallback = true)
    }

    private fun handleDetect(request: YoloDetectRequest, imageFd: ParcelFileDescriptor) {
        if (closeRequested.get() || closing || finished.get()) {
            imageFd.close()
            handleAdmissionFailure(
                request,
                YoloErrorCode.SESSION_CLOSED,
                "YOLO session is closing",
            )
            requestCloseIfNeeded()
            return
        }
        if (request.sequence <= identityFailureSequenceFloor) {
            imageFd.close()
            return
        }
        val activeRequestIdBefore = lifecycle.activeRequestId
        val activeSequenceBefore = lifecycle.lastSeenSequence.takeIf { activeRequestIdBefore != null }
        when (lifecycle.beginDetect(request)) {
            YoloSessionLifecyclePolicy.BeginDisposition.ACCEPTED -> dispatchDetect(request, imageFd)
            YoloSessionLifecyclePolicy.BeginDisposition.BUSY -> {
                imageFd.close()
                if (
                    request.requestId != activeRequestIdBefore ||
                    request.sequence != activeSequenceBefore
                ) {
                    publishRejected(
                        request,
                        YoloErrorCode.BUSY,
                        "YOLO session already has an active detection",
                    )
                }
            }
            YoloSessionLifecyclePolicy.BeginDisposition.CLOSED -> {
                imageFd.close()
                publishRejected(request, YoloErrorCode.SESSION_CLOSED, "YOLO session is closed")
            }
            YoloSessionLifecyclePolicy.BeginDisposition.REPLAYED_OR_OUT_OF_ORDER -> {
                imageFd.close()
            }
        }
    }

    private fun submitCancel(request: YoloCancelRequest) {
        if (closeRequested.get() || finished.get()) return
        val activeRequestId = lifecycle.activeRequestId
        if (activeRequestId != null && activeRequestId != request.requestId) return
        // First-wins prevents a later malformed sequence from replacing an admitted cancel.
        pendingCancel.compareAndSet(null, request)
        enqueuePendingCancel()
    }

    private fun enqueuePendingCancel() {
        if (!cancelTaskScheduled.compareAndSet(false, true)) return
        if (!controlLane.dispatchReserved { drainPendingCancel() }) {
            cancelTaskScheduled.set(false)
            pendingCancel.set(null)
        }
    }

    private fun drainPendingCancel() {
        try {
            pendingCancel.getAndSet(null)?.let(::handleCancel)
        } finally {
            cancelTaskScheduled.set(false)
            if (pendingCancel.get() != null) enqueuePendingCancel()
        }
    }

    private fun handleCancel(request: YoloCancelRequest) {
        if (closing || finished.get()) return
        if (!lifecycle.requestCancel(request)) return
        runCatching { engine.cancel(request.sequence) }
        val cancellation = YoloCancellation(
            requestId = request.requestId,
            sequence = request.sequence,
            reason = request.reason,
            elapsedMillis = elapsedMillis(),
        )
        if (lifecycle.onCancelled(cancellation)) {
            publish { callback.onCancelled(YoloCodec.encodeCancellation(cancellation)) }
        }
    }

    private fun dispatchDetect(request: YoloDetectRequest, imageFd: ParcelFileDescriptor) {
        if (!workerOccupied.compareAndSet(false, true)) {
            imageFd.close()
            val error = errorFor(
                request,
                YoloErrorCode.BUSY,
                YoloFailurePhase.ADMISSION,
                "YOLO worker is occupied; protocol v1 has no queue",
                retryableBeforeDispatch = true,
            )
            if (lifecycle.onFailed(error)) publishError(error)
            return
        }
        activeStartedAt.set(SystemClock.elapsedRealtime())
        try {
            worker.execute {
                val outcome = try {
                    imageFd.use { descriptor ->
                        DetectionOutcome.Success(engine.detect(request, descriptor))
                    }
                } catch (error: Throwable) {
                    DetectionOutcome.Failure(error)
                }
                // The control lane remains alive until workerOccupied is cleared here.
                if (!controlLane.dispatchCritical { completeDetect(request, outcome) }) {
                    runCatching { engine.cancel(request.sequence) }
                }
            }
        } catch (_: RejectedExecutionException) {
            workerOccupied.set(false)
            activeStartedAt.set(0L)
            imageFd.close()
            val error = errorFor(
                request,
                YoloErrorCode.RESOURCE_EXHAUSTED,
                YoloFailurePhase.ADMISSION,
                "YOLO worker rejected the detection",
                retryableBeforeDispatch = true,
            )
            if (lifecycle.onFailed(error)) publishError(error)
            requestCloseIfNeeded()
        }
    }

    private fun completeDetect(request: YoloDetectRequest, outcome: DetectionOutcome) {
        try {
            if (!closing && !closeRequested.get()) {
                when (outcome) {
                    is DetectionOutcome.Success -> {
                        YoloValidation.validateDetectResultAgainst(outcome.result, request)
                        if (lifecycle.onDetected(outcome.result)) {
                            publish { callback.onDetected(YoloCodec.encodeDetectResult(outcome.result)) }
                        }
                    }
                    is DetectionOutcome.Failure -> {
                        val error = errorFor(
                            request,
                            YoloErrorCode.INFERENCE_FAILED,
                            YoloFailurePhase.INFERENCE,
                            outcome.error.message ?: "YOLO inference failed",
                            retryableBeforeDispatch = false,
                        )
                        if (lifecycle.onFailed(error)) publishError(error)
                    }
                }
            }
        } catch (error: Throwable) {
            val terminal = errorFor(
                request,
                YoloErrorCode.INFERENCE_FAILED,
                YoloFailurePhase.DECODE,
                error.message ?: "YOLO result validation failed",
                retryableBeforeDispatch = false,
            )
            if (lifecycle.onFailed(terminal)) publishError(terminal)
        } finally {
            workerOccupied.set(false)
            activeStartedAt.set(0L)
            requestCloseIfNeeded()
        }
    }

    private fun scheduleClose(reason: YoloCancellationReason, notifyCallback: Boolean) {
        while (true) {
            val current = closeIntent.get()
            if (current != null) {
                // First close fixes the reason; a later caller may only upgrade notification.
                if (notifyCallback && !current.notifyCallback) {
                    closeIntent.compareAndSet(current, current.copy(notifyCallback = true))
                }
                return
            }
            if (closeIntent.compareAndSet(null, CloseIntent(reason, notifyCallback))) break
        }
        closeRequested.set(true)
        if (!controlLane.dispatchCritical {
                closing = true
                requestCloseIfNeeded()
            }
        ) {
            lifecycle.lastSeenSequence?.let { sequence ->
                runCatching { engine.cancel(sequence) }
            }
        }
    }

    /** Must only run on the control lane. */
    private fun requestCloseIfNeeded() {
        if (!closeRequested.get() || finished.get()) return
        if (!closing) closing = true
        val intent = closeIntent.get() ?: return
        val close = lifecycle.close()
        close.cancelledSequence?.let { runCatching { engine.cancel(it) } }
        val cancelledRequestId = close.cancelledRequestId
        val cancelledSequence = close.cancelledSequence
        if (
            intent.notifyCallback &&
            cancelledRequestId != null &&
            cancelledSequence != null
        ) {
            val cancellation = YoloCancellation(
                requestId = cancelledRequestId,
                sequence = cancelledSequence,
                reason = intent.reason,
                elapsedMillis = elapsedMillis(),
            )
            publish { callback.onCancelled(YoloCodec.encodeCancellation(cancellation)) }
        }
        if (workerOccupied.get()) return
        finalizeClose()
    }

    /** Native/model resources are released only after the inference worker is idle. */
    private fun finalizeClose() {
        if (!finished.compareAndSet(false, true)) return
        runCatching { callbackBinder.unlinkToDeath(deathRecipient, 0) }
        runCatching { engine.close() }
        model.close()
        onFinished(this)
        controlLane.close()
    }

    private fun submitAdmissionFailure(
        request: YoloDetectRequest,
        code: YoloErrorCode,
        message: String,
    ) {
        when (controlLane.tryDispatch { handleAdmissionFailure(request, code, message) }) {
            SerialControlLane.DispatchResult.ACCEPTED -> Unit
            SerialControlLane.DispatchResult.SATURATED ->
                submitEmergencyAdmissionFailure(request, code, message)
            SerialControlLane.DispatchResult.CLOSED -> Unit
        }
    }

    private fun submitIdentityAdmissionFailure(
        identity: DetectRequestIdentity,
        code: YoloErrorCode,
        message: String,
    ) {
        val task = { handleIdentityAdmissionFailure(identity, code, message) }
        when (controlLane.tryDispatch(task)) {
            SerialControlLane.DispatchResult.ACCEPTED -> Unit
            SerialControlLane.DispatchResult.SATURATED -> submitEmergencyAdmissionTask(task)
            SerialControlLane.DispatchResult.CLOSED -> Unit
        }
    }

    private fun submitEmergencyAdmissionFailure(
        request: YoloDetectRequest,
        code: YoloErrorCode,
        message: String,
    ) {
        submitEmergencyAdmissionTask { handleAdmissionFailure(request, code, message) }
    }

    /** At most one overflow rejection is retained; further hostile flood is dropped fail-closed. */
    private fun submitEmergencyAdmissionTask(task: () -> Unit) {
        if (!emergencyAdmissionTaskScheduled.compareAndSet(false, true)) return
        if (!controlLane.dispatchReserved {
                try {
                    task()
                } finally {
                    emergencyAdmissionTaskScheduled.set(false)
                }
            }
        ) {
            emergencyAdmissionTaskScheduled.set(false)
        }
    }

    /** Must run on the control lane so an admission error cannot complete another active request. */
    private fun handleAdmissionFailure(
        request: YoloDetectRequest,
        code: YoloErrorCode,
        message: String,
    ) {
        if (request.sequence <= identityFailureSequenceFloor) return
        val activeRequestIdBefore = lifecycle.activeRequestId
        val activeSequenceBefore = lifecycle.lastSeenSequence.takeIf { activeRequestIdBefore != null }
        val disposition = try {
            lifecycle.beginDetect(request)
        } catch (_: Throwable) {
            handleIdentityAdmissionFailure(
                DetectRequestIdentity(request.requestId, request.sequence),
                code,
                message,
            )
            return
        }
        when (disposition) {
            YoloSessionLifecyclePolicy.BeginDisposition.ACCEPTED -> {
                val error = errorFor(
                    request,
                    code,
                    YoloFailurePhase.ADMISSION,
                    message,
                    retryableBeforeDispatch = true,
                )
                if (lifecycle.onFailed(error)) publishError(error)
            }
            YoloSessionLifecyclePolicy.BeginDisposition.BUSY -> {
                if (
                    request.requestId != activeRequestIdBefore ||
                    request.sequence != activeSequenceBefore
                ) {
                    publishRejected(request, code, message)
                }
            }
            YoloSessionLifecyclePolicy.BeginDisposition.CLOSED ->
                publishRejected(request, YoloErrorCode.SESSION_CLOSED, "YOLO session is closed")
            YoloSessionLifecyclePolicy.BeginDisposition.REPLAYED_OR_OUT_OF_ORDER -> Unit
        }
    }

    /** Handles an envelope whose full request cannot safely be reconstructed. */
    private fun handleIdentityAdmissionFailure(
        identity: DetectRequestIdentity,
        code: YoloErrorCode,
        message: String,
    ) {
        val activeRequestId = lifecycle.activeRequestId
        val activeSequence = lifecycle.lastSeenSequence.takeIf { activeRequestId != null }
        val lastConsumedSequence = maxOf(
            identityFailureSequenceFloor,
            lifecycle.lastSeenSequence ?: 0L,
        )
        if (
            identity.sequence <= lastConsumedSequence ||
            (identity.requestId == activeRequestId && identity.sequence == activeSequence)
        ) return
        identityFailureSequenceFloor = identity.sequence
        publishIdentityError(identity, code, message)
    }

    private fun publishRejected(request: YoloDetectRequest, code: YoloErrorCode, message: String) {
        publishError(
            errorFor(
                request,
                code,
                YoloFailurePhase.ADMISSION,
                message,
                retryableBeforeDispatch = true,
            ),
        )
    }

    private fun publishIdentityError(
        identity: DetectRequestIdentity,
        code: YoloErrorCode,
        message: String,
    ) {
        publishError(
            YoloError(
                requestId = identity.requestId,
                sequence = identity.sequence,
                code = code,
                phase = YoloFailurePhase.INPUT_VALIDATION,
                message = message.take(1_024).ifBlank { "YOLO detect request is invalid" },
                retryableBeforeDispatch = true,
            ),
        )
    }

    private fun errorFor(
        request: YoloDetectRequest,
        code: YoloErrorCode,
        phase: YoloFailurePhase,
        message: String,
        retryableBeforeDispatch: Boolean,
    ) = YoloError(
        requestId = request.requestId,
        sequence = request.sequence,
        code = code,
        phase = phase,
        message = message.take(1_024).ifBlank { "YOLO provider failure" },
        retryableBeforeDispatch = retryableBeforeDispatch,
    )

    private fun publishError(error: YoloError) {
        publish { callback.onFailed(YoloCodec.encodeError(error)) }
    }

    private fun publish(block: () -> Unit) {
        callbackLane.dispatch(
            callback = block,
            onFailure = {
                scheduleClose(YoloCancellationReason.PROVIDER_SHUTDOWN, notifyCallback = false)
            },
        )
    }

    private fun elapsedMillis(): Long {
        val startedAt = activeStartedAt.get()
        return if (startedAt == 0L) 0L else (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0L)
    }

    private sealed class DetectionOutcome {
        data class Success(val result: YoloDetectResult) : DetectionOutcome()
        data class Failure(val error: Throwable) : DetectionOutcome()
    }

    private data class CloseIntent(
        val reason: YoloCancellationReason,
        val notifyCallback: Boolean,
    )
}
