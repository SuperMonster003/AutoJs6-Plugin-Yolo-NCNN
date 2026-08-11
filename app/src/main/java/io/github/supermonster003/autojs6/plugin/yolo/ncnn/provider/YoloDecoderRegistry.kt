package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

internal object YoloDecoderRegistry {
    const val ULTRALYTICS_DETECT = "ultralytics-detect"

    private val decoders = linkedMapOf(
        ULTRALYTICS_DETECT to Decoder(ULTRALYTICS_DETECT),
    )

    val decoderIds: List<String> = decoders.keys.toList()

    fun require(decoderId: String): Decoder = decoders[decoderId]
        ?: throw YoloModelRejectedException(
            detailCode = "MANIFEST_DECODER_UNSUPPORTED",
            detail = "Unsupported YOLO decoder: $decoderId",
        )

    internal class Decoder internal constructor(val id: String) {
        fun validate(manifest: YoloModelManifest) {
            if (manifest.outputCoordinates != "cxcywh") {
                YoloDecoderRegistry.reject(
                    "MANIFEST_OUTPUT_INVALID",
                    "ultralytics-detect coordinates must be cxcywh",
                )
            }
            if (manifest.outputHasObjectness) {
                YoloDecoderRegistry.reject(
                    "MANIFEST_OUTPUT_INVALID",
                    "ultralytics-detect must not declare objectness",
                )
            }
            val expectedRows = BOX_FIELD_COUNT + manifest.labels.size
            val expectedShape = listOf(1, expectedRows, OUTPUT_COLUMNS)
            if (manifest.outputShape != expectedShape) {
                YoloDecoderRegistry.reject(
                    "MANIFEST_SHAPE_INVALID",
                    "ultralytics-detect shape must be [1,$expectedRows,$OUTPUT_COLUMNS] for " +
                        "${manifest.labels.size} labels",
                )
            }
        }
    }

    private fun reject(code: String, detail: String): Nothing =
        throw YoloModelRejectedException(code, detail)

    const val MAX_LABELS = 256
    const val BOX_FIELD_COUNT = 4
    const val OUTPUT_COLUMNS = 8_400
}
