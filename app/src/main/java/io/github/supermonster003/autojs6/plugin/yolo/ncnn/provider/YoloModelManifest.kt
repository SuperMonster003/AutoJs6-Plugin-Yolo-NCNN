package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

internal data class YoloModelManifest(
    val decoderId: String,
    val inputName: String,
    val inputWidth: Int,
    val inputHeight: Int,
    val outputName: String,
    val outputShape: List<Int>,
    val outputCoordinates: String,
    val outputHasObjectness: Boolean,
    val labels: List<String>,
) {
    val outputRows: Int
        get() = outputShape[1]

    companion object {
        private const val MAX_MANIFEST_BYTES = 256L * 1024L
        private const val MAX_LABEL_BYTES = 256

        private val TOP_LEVEL_KEYS = setOf(
            "schemaVersion",
            "engine",
            "backend",
            "family",
            "variant",
            "task",
            "input",
            "output",
            "labels",
        )
        private val INPUT_KEYS = setOf(
            "name",
            "width",
            "height",
            "color",
            "layout",
            "letterbox",
            "scale",
            "paddingValue",
        )
        private val OUTPUT_KEYS = setOf(
            "name",
            "decoder",
            "shape",
            "coordinates",
            "objectness",
        )

        fun parse(file: File): YoloModelManifest {
            if (!file.isFile) reject("MANIFEST_FILE_INVALID", "Manifest artifact is not a regular file")
            val length = file.length()
            if (length !in 1L..MAX_MANIFEST_BYTES) {
                reject("MANIFEST_FILE_INVALID", "Manifest length must be within 1..$MAX_MANIFEST_BYTES bytes")
            }
            val bytes = file.readBytes()
            val text = try {
                StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString()
            } catch (error: Exception) {
                throw YoloModelRejectedException(
                    "MANIFEST_ENCODING_INVALID",
                    "Manifest must be valid UTF-8",
                    error,
                )
            }
            if ('\u0000' in text) reject("MANIFEST_ENCODING_INVALID", "Manifest contains a NUL character")

            val root = try {
                val tokener = JSONTokener(text)
                val value = tokener.nextValue() as? JSONObject
                    ?: reject("MANIFEST_JSON_INVALID", "Manifest root must be an object")
                if (tokener.nextClean() != 0.toChar()) {
                    reject("MANIFEST_JSON_INVALID", "Manifest has trailing content")
                }
                value
            } catch (error: YoloModelRejectedException) {
                throw error
            } catch (error: Exception) {
                throw YoloModelRejectedException(
                    "MANIFEST_JSON_INVALID",
                    "Manifest is not valid JSON",
                    error,
                )
            }
            requireKeys(root, TOP_LEVEL_KEYS, "manifest")

            if (root.requireInt("schemaVersion") != 1) {
                reject("MANIFEST_SCHEMA_UNSUPPORTED", "Only manifest schemaVersion 1 is supported")
            }
            requireProfile(root.requireString("engine") == "yolo", "engine must be yolo")
            requireProfile(root.requireString("backend") == "ncnn", "backend must be ncnn")
            requireProfile(root.requireString("family") == "ultralytics", "family must be ultralytics")
            requireProfile(root.requireString("variant") == "yolo11", "variant must be yolo11")
            requireProfile(root.requireString("task") == "detect", "task must be detect")

            val input = root.requireObject("input")
            requireKeys(input, INPUT_KEYS, "manifest.input")
            requireNode(input.requireString("name") == "in0", "input node must be in0")
            requireProfile(
                input.requireInt("width") == 640 && input.requireInt("height") == 640,
                "input dimensions must be 640 x 640",
            )
            requireProfile(input.requireString("color") == "RGB", "input color must be RGB")
            requireProfile(input.requireString("layout") == "NCHW", "input layout must be NCHW")
            requireProfile(input.requireBoolean("letterbox"), "input must use centered letterbox")
            requireProfile(
                input.requireDouble("scale") == (1.0 / 255.0),
                "input scale must be 1/255",
            )
            requireProfile(input.requireInt("paddingValue") == 114, "input paddingValue must be 114")

            val output = root.requireObject("output")
            requireKeys(output, OUTPUT_KEYS, "manifest.output")
            requireNode(output.requireString("name") == "out0", "output node must be out0")

            val labelsArray = root.requireArray("labels")
            if (labelsArray.length() !in 1..YoloDecoderRegistry.MAX_LABELS) {
                reject(
                    "MANIFEST_LABELS_INVALID",
                    "labels must contain 1..${YoloDecoderRegistry.MAX_LABELS} entries",
                )
            }
            val labels = ArrayList<String>(labelsArray.length())
            repeat(labelsArray.length()) { index ->
                val label = labelsArray.opt(index) as? String
                    ?: reject("MANIFEST_LABELS_INVALID", "label $index must be a string")
                if (label.isBlank()) reject("MANIFEST_LABELS_INVALID", "label $index must not be blank")
                if (label.toByteArray(StandardCharsets.UTF_8).size > MAX_LABEL_BYTES) {
                    reject("MANIFEST_LABELS_INVALID", "label $index exceeds the UTF-8 byte limit")
                }
                labels += label
            }
            if (labels.distinct().size != labels.size) {
                reject("MANIFEST_LABELS_INVALID", "labels must be unique")
            }

            val manifest = YoloModelManifest(
                decoderId = output.requireString("decoder"),
                inputName = input.requireString("name"),
                inputWidth = input.requireInt("width"),
                inputHeight = input.requireInt("height"),
                outputName = output.requireString("name"),
                outputShape = output.requireIntArray("shape"),
                outputCoordinates = output.requireString("coordinates"),
                outputHasObjectness = output.requireBoolean("objectness"),
                labels = labels.toList(),
            )
            YoloDecoderRegistry.require(manifest.decoderId).validate(manifest)
            return manifest
        }

        private fun requireKeys(value: JSONObject, expected: Set<String>, path: String) {
            val actual = value.keys().asSequence().toSet()
            if (actual != expected) {
                reject(
                    "MANIFEST_KEYS_INVALID",
                    "$path keys are incompatible: expected ${expected.sorted()}, found ${actual.sorted()}",
                )
            }
        }

        private fun JSONObject.requireString(name: String): String =
            (opt(name) as? String) ?: reject("MANIFEST_FIELD_INVALID", "$name must be a string")

        private fun JSONObject.requireInt(name: String): Int =
            (opt(name) as? Int) ?: reject("MANIFEST_FIELD_INVALID", "$name must be an integer")

        private fun JSONObject.requireDouble(name: String): Double {
            val value = opt(name)
            if (value !is Number || value is Int || value is Long) {
                reject("MANIFEST_FIELD_INVALID", "$name must be a decimal number")
            }
            return value.toDouble().also {
                if (!it.isFinite()) reject("MANIFEST_FIELD_INVALID", "$name must be finite")
            }
        }

        private fun JSONObject.requireBoolean(name: String): Boolean =
            (opt(name) as? Boolean) ?: reject("MANIFEST_FIELD_INVALID", "$name must be a boolean")

        private fun JSONObject.requireObject(name: String): JSONObject =
            (opt(name) as? JSONObject) ?: reject("MANIFEST_FIELD_INVALID", "$name must be an object")

        private fun JSONObject.requireArray(name: String): JSONArray =
            (opt(name) as? JSONArray) ?: reject("MANIFEST_FIELD_INVALID", "$name must be an array")

        private fun JSONObject.requireIntArray(name: String): List<Int> {
            val array = requireArray(name)
            if (array.length() != 3) reject("MANIFEST_SHAPE_INVALID", "output shape must have three axes")
            return List(array.length()) { index ->
                (array.opt(index) as? Int)
                    ?: reject("MANIFEST_SHAPE_INVALID", "output shape axis $index must be an integer")
            }
        }

        private fun requireProfile(condition: Boolean, detail: String) {
            if (!condition) reject("MANIFEST_PROFILE_UNSUPPORTED", detail)
        }

        private fun requireNode(condition: Boolean, detail: String) {
            if (!condition) reject("MANIFEST_NODE_UNSUPPORTED", detail)
        }

        private fun reject(code: String, detail: String): Nothing =
            throw YoloModelRejectedException(code, detail)
    }
}
