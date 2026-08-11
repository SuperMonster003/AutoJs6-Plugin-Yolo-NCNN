package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import android.os.ParcelFileDescriptor
import android.os.SystemClock
import org.autojs.plugin.yolo.api.YoloBounds
import org.autojs.plugin.yolo.api.YoloDetectRequest
import org.autojs.plugin.yolo.api.YoloDetectResult
import org.autojs.plugin.yolo.api.YoloDetection
import java.util.concurrent.atomic.AtomicLong

internal class NativeYoloEngine(
    nativeHandle: Long,
    private val labels: List<String>,
) : YoloInferenceEngine {
    private val handle = AtomicLong(nativeHandle)

    override fun detect(request: YoloDetectRequest, imageFd: ParcelFileDescriptor): YoloDetectResult {
        val activeHandle = handle.get()
        check(activeHandle != 0L) { "YOLO detector is closed" }
        val startedAt = SystemClock.elapsedRealtime()
        val values = NativeYoloRuntime.detect(
            handle = activeHandle,
            sequence = request.sequence,
            fd = imageFd.fd,
            width = request.image.width,
            height = request.image.height,
            rowStride = request.image.rowStride,
            sizeBytes = request.image.sizeBytes,
            confidenceThreshold = request.confidenceThreshold.toFloat(),
            iouThreshold = request.iouThreshold.toFloat(),
            maxDetections = request.maxDetections,
        )
        check(values.size % DETECTION_FIELD_COUNT == 0) { "Native YOLO result is malformed" }
        check(values.size / DETECTION_FIELD_COUNT <= request.maxDetections) {
            "Native YOLO result exceeds maxDetections"
        }
        val detections = ArrayList<YoloDetection>(values.size / DETECTION_FIELD_COUNT)
        values.indices.step(DETECTION_FIELD_COUNT).forEach { offset ->
            val encodedClassId = values[offset]
            val classId = encodedClassId.toInt()
            check(encodedClassId.isFinite() && encodedClassId == classId.toFloat()) {
                "Native YOLO class ID is invalid"
            }
            check(classId in labels.indices) { "Native YOLO class ID is outside the manifest labels" }
            val confidence = values[offset + 1].toDouble()
            val left = values[offset + 2].toDouble()
            val top = values[offset + 3].toDouble()
            val right = values[offset + 4].toDouble()
            val bottom = values[offset + 5].toDouble()
            check(confidence.isFinite() && confidence in 0.0..1.0) {
                "Native YOLO confidence is invalid"
            }
            check(listOf(left, top, right, bottom).all { it.isFinite() }) {
                "Native YOLO bounds are invalid"
            }
            detections += YoloDetection(
                classId = classId,
                label = labels[classId],
                confidence = confidence,
                bounds = YoloBounds(left, top, right, bottom),
            )
        }
        return YoloDetectResult(
            requestId = request.requestId,
            sequence = request.sequence,
            imageWidth = request.image.width,
            imageHeight = request.image.height,
            elapsedMillis = (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0L),
            detections = detections,
        )
    }

    override fun cancel(sequence: Long) {
        handle.get().takeIf { it != 0L }?.let { NativeYoloRuntime.cancel(it, sequence) }
    }

    override fun close() {
        handle.getAndSet(0L).takeIf { it != 0L }?.let(NativeYoloRuntime::destroy)
    }

    private companion object {
        const val DETECTION_FIELD_COUNT = 6
    }
}
