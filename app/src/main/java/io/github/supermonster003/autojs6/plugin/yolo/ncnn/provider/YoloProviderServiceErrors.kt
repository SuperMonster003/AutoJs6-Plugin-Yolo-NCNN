package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import org.autojs.plugin.yolo.api.YoloErrorCode
import org.autojs.plugin.yolo.api.YoloOpenSessionFailureCodec

internal object YoloProviderServiceErrors {
    val INVALID_REQUEST = YoloErrorCode.INVALID_REQUEST.wireCode
    val NATIVE_RUNTIME_NOT_READY = YoloErrorCode.UNSUPPORTED_CAPABILITY.wireCode
    val PROVIDER_BUSY = YoloErrorCode.BUSY.wireCode
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

    fun sessionOpenFailed(message: String): IllegalArgumentException =
        YoloOpenSessionFailureCodec.exception(YoloErrorCode.INTERNAL, bounded(message))

    fun sessionOpenTimedOut(message: String): IllegalArgumentException =
        YoloOpenSessionFailureCodec.exception(YoloErrorCode.TIMEOUT, bounded(message))

    private fun bounded(message: String): String =
        message.take(1_024).ifBlank { "YOLO session open failed" }
}
