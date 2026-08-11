package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import android.os.ParcelFileDescriptor
import org.autojs.plugin.yolo.api.YoloDetectRequest
import org.autojs.plugin.yolo.api.YoloDetectResult
import java.io.Closeable

internal interface YoloInferenceEngine : Closeable {
    fun detect(request: YoloDetectRequest, imageFd: ParcelFileDescriptor): YoloDetectResult

    fun cancel(sequence: Long)
}
