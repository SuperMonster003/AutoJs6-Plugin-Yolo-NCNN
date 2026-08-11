package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.autojs.plugin.yolo.api.YoloErrorCode
import org.autojs.plugin.yolo.api.YoloOpenSessionFailureCodec
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class YoloModelManifestTest {
    @Test
    fun publishedProfileAcceptsOneThroughMaximumLabels() {
        val single = parse(labels = listOf("target"))
        assertEquals(1, single.labels.size)
        assertEquals(5, single.outputRows)

        val maximumLabels = List(YoloDecoderRegistry.MAX_LABELS) { index -> "class-$index" }
        val maximum = parse(labels = maximumLabels)
        assertEquals(YoloDecoderRegistry.MAX_LABELS, maximum.labels.size)
        assertEquals(260, maximum.outputRows)
    }

    @Test
    fun outputShapeMustMatchLabelCount() {
        val error = rejected {
            parse(labels = listOf("first", "second"), outputRows = 5)
        }
        assertEquals("MANIFEST_SHAPE_INVALID", error.detailCode)
    }

    @Test
    fun decoderRegistryHasNoUnknownFallback() {
        assertEquals(listOf("ultralytics-detect"), YoloDecoderRegistry.decoderIds)
        val error = rejected { YoloDecoderRegistry.require("unknown-decoder") }
        assertEquals("MANIFEST_DECODER_UNSUPPORTED", error.detailCode)
    }

    @Test
    fun modelRejectionsUseTheTypedProtocolCategory() {
        val encoded = YoloProviderServiceErrors.modelRejected(
            YoloModelRejectedException("MANIFEST_SHAPE_INVALID", "shape mismatch").message.orEmpty(),
        )
        val decoded = checkNotNull(YoloOpenSessionFailureCodec.decode(encoded))
        assertEquals(YoloErrorCode.MODEL_REJECTED, decoded.code)
    }

    private fun parse(
        labels: List<String>,
        outputRows: Int = YoloDecoderRegistry.BOX_FIELD_COUNT + labels.size,
    ): YoloModelManifest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File.createTempFile("yolo-manifest-", ".json", context.cacheDir)
        return try {
            file.writeText(manifestJson(labels, outputRows).toString(), Charsets.UTF_8)
            YoloModelManifest.parse(file)
        } finally {
            file.delete()
        }
    }

    private fun manifestJson(labels: List<String>, outputRows: Int): JSONObject = JSONObject()
        .put("schemaVersion", 1)
        .put("engine", "yolo")
        .put("backend", "ncnn")
        .put("family", "ultralytics")
        .put("variant", "yolo11")
        .put("task", "detect")
        .put(
            "input",
            JSONObject()
                .put("name", "in0")
                .put("width", 640)
                .put("height", 640)
                .put("color", "RGB")
                .put("layout", "NCHW")
                .put("letterbox", true)
                .put("scale", 1.0 / 255.0)
                .put("paddingValue", 114),
        )
        .put(
            "output",
            JSONObject()
                .put("name", "out0")
                .put("decoder", "ultralytics-detect")
                .put("shape", JSONArray(listOf(1, outputRows, 8_400)))
                .put("coordinates", "cxcywh")
                .put("objectness", false),
        )
        .put("labels", JSONArray(labels))

    private fun rejected(block: () -> Unit): YoloModelRejectedException = try {
        block()
        fail("Expected YoloModelRejectedException")
        throw AssertionError("unreachable")
    } catch (error: YoloModelRejectedException) {
        error
    }
}
