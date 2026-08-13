package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import org.autojs.plugin.yolo.api.YoloContractException
import org.autojs.plugin.yolo.api.YoloContractViolation
import org.autojs.plugin.yolo.api.YoloErrorCode
import org.autojs.plugin.yolo.api.YoloOpenSessionFailureCodec

internal object YoloProviderServiceErrors {
    val INVALID_REQUEST = YoloErrorCode.INVALID_REQUEST.wireCode
    val NATIVE_RUNTIME_NOT_READY = YoloErrorCode.UNSUPPORTED_CAPABILITY.wireCode
    val PROVIDER_BUSY = YoloErrorCode.BUSY.wireCode
    val MODEL_REJECTED = YoloErrorCode.MODEL_REJECTED.wireCode
    val SESSION_OPEN_FAILED = YoloErrorCode.INTERNAL.wireCode

    fun invalidRequest(message: String): IllegalArgumentException =
        YoloOpenSessionFailureCodec.exception(YoloErrorCode.INVALID_REQUEST, bounded(message))

    fun nativeRuntimeNotReady(): IllegalArgumentException =
        YoloOpenSessionFailureCodec.exception(
            YoloErrorCode.UNSUPPORTED_CAPABILITY,
            "NCNN runtime is unavailable",
        )

    fun providerBusy(): IllegalArgumentException =
        YoloOpenSessionFailureCodec.exception(
            YoloErrorCode.BUSY,
            "YOLO provider already owns its single session",
        )

    fun modelRejected(message: String): IllegalArgumentException =
        YoloOpenSessionFailureCodec.exception(YoloErrorCode.MODEL_REJECTED, bounded(message))

    fun sessionOpenFailed(message: String): IllegalArgumentException =
        YoloOpenSessionFailureCodec.exception(YoloErrorCode.INTERNAL, bounded(message))

    fun sessionOpenTimedOut(message: String): IllegalArgumentException =
        YoloOpenSessionFailureCodec.exception(YoloErrorCode.TIMEOUT, bounded(message))

    fun unsupportedCapability(message: String): IllegalArgumentException =
        YoloOpenSessionFailureCodec.exception(
            YoloErrorCode.UNSUPPORTED_CAPABILITY,
            bounded(message),
        )

    fun unsupportedProtocol(message: String): IllegalArgumentException =
        YoloOpenSessionFailureCodec.exception(
            YoloErrorCode.UNSUPPORTED_PROTOCOL,
            bounded(message),
        )

    fun mapOpenFailure(error: Throwable): Throwable = when (error) {
        is SecurityException -> error
        is YoloSessionOpenTimeoutException -> sessionOpenTimedOut(error.message.orEmpty())
        is YoloModelRejectedException -> modelRejected(error.message.orEmpty())
        is YoloContractException -> when (error.violation) {
            YoloContractViolation.CAPABILITY_INCOMPATIBLE ->
                unsupportedCapability(error.message.orEmpty())
            YoloContractViolation.PROTOCOL_INCOMPATIBLE ->
                unsupportedProtocol(error.message.orEmpty())
            else -> invalidRequest(error.message ?: "Invalid YOLO session request")
        }
        is IllegalArgumentException -> {
            if (YoloOpenSessionFailureCodec.decode(error) != null) {
                error
            } else {
                invalidRequest(error.message ?: "Invalid YOLO session request")
            }
        }
        else -> sessionOpenFailed(error.message ?: "YOLO session could not be opened")
    }

    private fun bounded(message: String): String =
        message.take(1_024).ifBlank { "YOLO session open failed" }
}
