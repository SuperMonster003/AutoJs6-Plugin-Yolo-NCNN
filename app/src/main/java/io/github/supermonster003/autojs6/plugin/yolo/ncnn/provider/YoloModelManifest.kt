package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

internal data class YoloModelManifest(
    val inputName: String,
    val outputName: String,
    val labels: List<String>,
) {
    companion object {
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
            require(file.isFile) { "YOLO manifest artifact is not a regular file" }
            val length = file.length()
            require(length in 1L..(256L * 1024L)) { "YOLO manifest length is invalid" }
            val bytes = file.readBytes()
            val decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            val text = decoder.decode(ByteBuffer.wrap(bytes)).toString()
            require('\u0000' !in text) { "YOLO manifest contains a NUL character" }

            val tokener = JSONTokener(text)
            val root = tokener.nextValue() as? JSONObject
                ?: throw IllegalArgumentException("YOLO manifest root must be an object")
            require(tokener.nextClean() == 0.toChar()) { "YOLO manifest has trailing content" }
            requireKeys(root, TOP_LEVEL_KEYS, "manifest")

            require(root.requireInt("schemaVersion") == 1) { "Unsupported YOLO manifest schema" }
            require(root.requireString("engine") == "yolo") { "Manifest engine must be yolo" }
            require(root.requireString("backend") == "ncnn") { "Manifest backend must be ncnn" }
            require(root.requireString("family") == "ultralytics") {
                "Manifest family must be ultralytics"
            }
            require(root.requireString("variant") == "yolo11") { "Manifest variant must be yolo11" }
            require(root.requireString("task") == "detect") { "Manifest task must be detect" }

            val input = root.requireObject("input")
            requireKeys(input, INPUT_KEYS, "manifest.input")
            require(input.requireString("name") == "in0") { "Manifest input name must be in0" }
            require(input.requireInt("width") == 640 && input.requireInt("height") == 640) {
                "Manifest input dimensions must be 640 x 640"
            }
            require(input.requireString("color") == "RGB") { "Manifest input color must be RGB" }
            require(input.requireString("layout") == "NCHW") { "Manifest input layout must be NCHW" }
            require(input.requireBoolean("letterbox")) { "Manifest input must use centered letterbox" }
            require(kotlin.math.abs(input.requireDouble("scale") - (1.0 / 255.0)) <= 1e-12) {
                "Manifest input scale must be 1/255"
            }
            require(input.requireInt("paddingValue") == 114) {
                "Manifest input padding value must be 114"
            }

            val output = root.requireObject("output")
            requireKeys(output, OUTPUT_KEYS, "manifest.output")
            require(output.requireString("name") == "out0") { "Manifest output name must be out0" }
            require(output.requireString("decoder") == "ultralytics-detect") {
                "Manifest decoder must be ultralytics-detect"
            }
            require(output.requireString("coordinates") == "cxcywh") {
                "Manifest coordinates must be cxcywh"
            }
            require(!output.requireBoolean("objectness")) {
                "Manifest output must not contain a separate objectness score"
            }
            requireIntArray(output, "shape", intArrayOf(1, 84, 8_400))

            val labelsArray = root.requireArray("labels")
            require(labelsArray.length() == 80) { "Manifest must contain exactly 80 labels" }
            val labels = ArrayList<String>(labelsArray.length())
            repeat(labelsArray.length()) { index ->
                val label = labelsArray.opt(index) as? String
                    ?: throw IllegalArgumentException("Manifest label $index must be a string")
                require(label.isNotBlank()) { "Manifest label $index must not be blank" }
                require(label.toByteArray(StandardCharsets.UTF_8).size <= 256) {
                    "Manifest label $index exceeds the UTF-8 limit"
                }
                labels += label
            }
            require(labels.distinct().size == labels.size) { "Manifest labels must be unique" }

            return YoloModelManifest(
                inputName = input.requireString("name"),
                outputName = output.requireString("name"),
                labels = labels.toList(),
            )
        }

        private fun requireKeys(value: JSONObject, expected: Set<String>, path: String) {
            val actual = value.keys().asSequence().toSet()
            require(actual == expected) {
                "$path keys are incompatible: expected ${expected.sorted()}, found ${actual.sorted()}"
            }
        }

        private fun requireIntArray(value: JSONObject, name: String, expected: IntArray) {
            val array = value.requireArray(name)
            require(array.length() == expected.size) { "Manifest output shape is invalid" }
            expected.forEachIndexed { index, item ->
                val actual = array.opt(index)
                require(actual is Int && actual == item) { "Manifest output shape is invalid" }
            }
        }

        private fun JSONObject.requireString(name: String): String =
            (opt(name) as? String) ?: throw IllegalArgumentException("$name must be a string")

        private fun JSONObject.requireInt(name: String): Int =
            (opt(name) as? Int) ?: throw IllegalArgumentException("$name must be an integer")

        private fun JSONObject.requireDouble(name: String): Double {
            val value = opt(name)
            require(value is Number && value !is Int && value !is Long) { "$name must be a decimal number" }
            return value.toDouble().also { require(it.isFinite()) { "$name must be finite" } }
        }

        private fun JSONObject.requireBoolean(name: String): Boolean =
            (opt(name) as? Boolean) ?: throw IllegalArgumentException("$name must be a boolean")

        private fun JSONObject.requireObject(name: String): JSONObject =
            (opt(name) as? JSONObject) ?: throw IllegalArgumentException("$name must be an object")

        private fun JSONObject.requireArray(name: String): JSONArray =
            (opt(name) as? JSONArray) ?: throw IllegalArgumentException("$name must be an array")
    }
}
