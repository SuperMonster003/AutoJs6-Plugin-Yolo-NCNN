package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

internal class YoloModelRejectedException(
    val detailCode: String,
    detail: String,
    cause: Throwable? = null,
) : IllegalArgumentException("$detailCode: $detail", cause)

