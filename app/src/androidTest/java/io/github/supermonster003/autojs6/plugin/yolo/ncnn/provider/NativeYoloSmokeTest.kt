package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.autojs.plugin.yolo.api.YoloBackend
import org.autojs.plugin.yolo.api.YoloContract
import org.autojs.plugin.yolo.api.YoloDetectRequest
import org.autojs.plugin.yolo.api.YoloDevice
import org.autojs.plugin.yolo.api.YoloImageReference
import org.autojs.plugin.yolo.api.YoloModelArtifact
import org.autojs.plugin.yolo.api.YoloModelFormat
import org.autojs.plugin.yolo.api.YoloOpenSessionRequest
import org.autojs.plugin.yolo.api.YoloPixelFormat
import org.autojs.plugin.yolo.api.YoloProtocolVersion
import org.autojs.plugin.yolo.api.YoloRequestId
import org.autojs.plugin.yolo.api.YoloSha256
import org.autojs.plugin.yolo.api.YoloTask
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class NativeYoloSmokeTest {
    @Test
    fun yolo11nBusFixtureProducesReferenceDetections() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val testContext = instrumentation.context
        val targetContext = instrumentation.targetContext
        val resultFile = File(targetContext.filesDir, "r1-native-result.json")
        resultFile.delete()
        val workingDirectory = File(targetContext.cacheDir, "yolo-r1-${UUID.randomUUID()}")
        check(workingDirectory.mkdir()) { "Cannot create YOLO smoke directory" }

        var model: MaterializedModel? = null
        var engine: YoloInferenceEngine? = null
        try {
            val manifest = copyAsset(testContext, workingDirectory, "model-manifest-v1.json")
            val param = copyAsset(testContext, workingDirectory, "model.ncnn.param")
            val bin = copyAsset(testContext, workingDirectory, "model.ncnn.bin")
            val image = copyAsset(testContext, workingDirectory, "bus.jpg")
            assertEquals(
                "c02019c4979c191eb739ddd944445ef408dad5679acab6fd520ef9d434bfbc63",
                sha256(image).toHexString(),
            )

            val artifactFiles = listOf(
                Triple(YoloContract.REQUIRED_MANIFEST_ROLE, "model-manifest-v1.json", manifest),
                Triple("ncnn-param", "model.ncnn.param", param),
                Triple("ncnn-bin", "model.ncnn.bin", bin),
            )
            val artifacts = artifactFiles.mapIndexed { index, (role, logicalName, file) ->
                YoloModelArtifact(
                    role = role,
                    logicalName = logicalName,
                    sizeBytes = file.length(),
                    sha256 = sha256(file),
                    descriptorIndex = index,
                )
            }
            val openRequest = YoloOpenSessionRequest(
                requestId = requestId(),
                protocolVersion = YoloProtocolVersion(YoloContract.PROTOCOL_MAJOR, YoloContract.PROTOCOL_MINOR),
                task = YoloTask.DETECT,
                modelFormat = YoloModelFormat.NCNN,
                device = YoloDevice.CPU,
                cpuThreads = 4,
                decoderId = "ultralytics-detect",
                timeoutMillis = 120_000L,
                artifacts = artifacts,
            )
            val incoming = artifactFiles.map { (_, _, file) ->
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            }.toTypedArray()
            val owned = OwnedParcelFileDescriptors.duplicateBeforeAsync(incoming)
            val deadline = YoloSessionOpenDeadline.start(openRequest.timeoutMillis)
            model = MaterializedModel.create(
                context = targetContext,
                request = openRequest,
                descriptors = owned,
                maximumArtifactBytes = 512L * 1024L * 1024L,
                deadline = deadline,
            )
            engine = NativeYoloRuntime.open(openRequest, model, deadline)
            assertEquals("ncnn/1.0.20260526", NativeYoloRuntime.backendVersion)

            val bitmap = checkNotNull(BitmapFactory.decodeFile(image.absolutePath)) {
                "Cannot decode bus.jpg"
            }
            assertEquals(810, bitmap.width)
            assertEquals(1080, bitmap.height)
            val rawImage = File(workingDirectory, "bus.rgba")
            val rowStride = bitmap.width * 4
            FileOutputStream(rawImage).use { output ->
                val pixels = IntArray(bitmap.width)
                val row = ByteArray(rowStride)
                repeat(bitmap.height) { y ->
                    bitmap.getPixels(pixels, 0, bitmap.width, 0, y, bitmap.width, 1)
                    pixels.forEachIndexed { x, color ->
                        val offset = x * 4
                        row[offset] = Color.red(color).toByte()
                        row[offset + 1] = Color.green(color).toByte()
                        row[offset + 2] = Color.blue(color).toByte()
                        row[offset + 3] = Color.alpha(color).toByte()
                    }
                    output.write(row)
                }
                output.fd.sync()
            }
            bitmap.recycle()

            val detectRequest = YoloDetectRequest(
                requestId = requestId(),
                sequence = 1L,
                image = YoloImageReference(
                    pixelFormat = YoloPixelFormat.RGBA_8888,
                    width = 810,
                    height = 1080,
                    rowStride = rowStride,
                    sizeBytes = rawImage.length(),
                ),
                confidenceThreshold = 0.25,
                iouThreshold = 0.45,
                maxDetections = 100,
                timeoutMillis = 30_000L,
            )
            val result = ParcelFileDescriptor.open(rawImage, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                engine.detect(detectRequest, fd)
            }
            val expected = loadExpected(testContext)
            assertEquals(810, result.imageWidth)
            assertEquals(1080, result.imageHeight)
            assertEquals(expected.size, result.detections.size)
            expected.zip(result.detections).forEachIndexed { index, (reference, actual) ->
                assertEquals("classId[$index]", reference.classId, actual.classId)
                assertEquals("label[$index]", reference.label, actual.label)
                assertTrue(
                    "confidence[$index] expected=${reference.confidence} actual=${actual.confidence}",
                    kotlin.math.abs(reference.confidence - actual.confidence) <= 0.02,
                )
                val actualBounds = doubleArrayOf(
                    actual.bounds.left,
                    actual.bounds.top,
                    actual.bounds.right,
                    actual.bounds.bottom,
                )
                assertTrue(
                    "bounds[$index] IoU=${iou(reference.bounds, actualBounds)}",
                    iou(reference.bounds, actualBounds) >= 0.95,
                )
            }

            val receipt = JSONObject()
                .put("backendVersion", NativeYoloRuntime.backendVersion)
                .put("imageWidth", result.imageWidth)
                .put("imageHeight", result.imageHeight)
                .put("elapsedMillis", result.elapsedMillis)
                .put(
                    "detections",
                    JSONArray().apply {
                        result.detections.forEach { detection ->
                            put(
                                JSONObject()
                                    .put("classId", detection.classId)
                                    .put("label", detection.label)
                                    .put("confidence", detection.confidence)
                                    .put(
                                        "bounds",
                                        JSONArray(
                                            listOf(
                                                detection.bounds.left,
                                                detection.bounds.top,
                                                detection.bounds.right,
                                                detection.bounds.bottom,
                                            ),
                                        ),
                                    ),
                            )
                        }
                    },
                )
            resultFile.writeText(receipt.toString(), Charsets.UTF_8)
        } finally {
            runCatching { engine?.close() }
            runCatching { model?.close() }
            workingDirectory.deleteRecursively()
        }
    }

    private fun copyAsset(context: android.content.Context, directory: File, name: String): File =
        File(directory, name).also { destination ->
            context.assets.open(name).use { input ->
                FileOutputStream(destination).use(input::copyTo)
            }
        }

    private fun sha256(file: File): YoloSha256 {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return YoloSha256.fromBytes(digest.digest())
    }

    private fun requestId(): YoloRequestId = YoloRequestId.fromUuid(UUID.randomUUID())

    private fun loadExpected(context: android.content.Context): List<ExpectedDetection> {
        val text = context.assets.open("expected-bus-detections.json")
            .bufferedReader()
            .use { it.readText() }
        val values = JSONObject(text).getJSONArray("detections")
        return List(values.length()) { index ->
            val value = values.getJSONObject(index)
            val bounds = value.getJSONArray("bounds")
            ExpectedDetection(
                classId = value.getInt("classId"),
                label = value.getString("label"),
                confidence = value.getDouble("confidence"),
                bounds = DoubleArray(4) { bounds.getDouble(it) },
            )
        }
    }

    private fun iou(first: DoubleArray, second: DoubleArray): Double {
        val left = maxOf(first[0], second[0])
        val top = maxOf(first[1], second[1])
        val right = minOf(first[2], second[2])
        val bottom = minOf(first[3], second[3])
        val intersection = maxOf(0.0, right - left) * maxOf(0.0, bottom - top)
        val firstArea = maxOf(0.0, first[2] - first[0]) * maxOf(0.0, first[3] - first[1])
        val secondArea = maxOf(0.0, second[2] - second[0]) * maxOf(0.0, second[3] - second[1])
        val union = firstArea + secondArea - intersection
        return if (union > 0.0) intersection / union else 0.0
    }

    private data class ExpectedDetection(
        val classId: Int,
        val label: String,
        val confidence: Double,
        val bounds: DoubleArray,
    )
}
