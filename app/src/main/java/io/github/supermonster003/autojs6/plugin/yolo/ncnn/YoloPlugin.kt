package io.github.supermonster003.autojs6.plugin.yolo.ncnn

import android.content.Context
import android.os.Build
import android.os.Bundle
import io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider.NativeYoloRuntime
import io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider.YoloDecoderRegistry
import org.autojs.plugin.common.api.PluginCapabilityKeys
import org.autojs.plugin.common.api.PluginInfo
import org.autojs.plugin.yolo.api.YoloBackend
import org.autojs.plugin.yolo.api.YoloCapabilities
import org.autojs.plugin.yolo.api.YoloContract
import org.autojs.plugin.yolo.api.YoloDevice
import org.autojs.plugin.yolo.api.YoloModelFormat
import org.autojs.plugin.yolo.api.YoloPixelFormat
import org.autojs.plugin.yolo.api.YoloProtocolVersion
import org.autojs.plugin.yolo.api.YoloProviderInfo
import org.autojs.plugin.yolo.api.YoloResourceLimits
import org.autojs.plugin.yolo.api.YoloTask

internal object YoloPlugin {
    const val HOST_PACKAGE_NAME = "org.autojs.autojs6"
    const val APPLICATION_ID = "io.github.supermonster003.autojs6.plugin.yolo.ncnn"
    const val PLUGIN_ID = "yolo-ncnn"
    const val ENGINE = YoloContract.ENGINE_ID
    const val VARIANT = "ncnn"
    const val PROVIDER_ID = "autojs6-yolo-ncnn"
    const val REQUIRED_HOST_VERSION = 5_274L
    const val DECODER_ID = YoloDecoderRegistry.ULTRALYTICS_DETECT

    val supportedAbis = listOf("arm64-v8a")

    val capabilities: YoloCapabilities
        get() = YoloCapabilities(
            protocolMin = YoloProtocolVersion(YoloContract.PROTOCOL_MAJOR, 0),
            protocolMax = YoloProtocolVersion(YoloContract.PROTOCOL_MAJOR, YoloContract.PROTOCOL_MINOR),
            backend = YoloBackend.NCNN,
            backendVersion = NativeYoloRuntime.backendVersion,
            tasks = listOf(YoloTask.DETECT),
            modelFormats = listOf(YoloModelFormat.NCNN),
            devices = listOf(YoloDevice.CPU),
            pixelFormats = listOf(YoloPixelFormat.RGBA_8888),
            decoderIds = YoloDecoderRegistry.decoderIds,
            artifactRoles = listOf(
                YoloContract.REQUIRED_MANIFEST_ROLE,
                "ncnn-param",
                "ncnn-bin",
            ),
            limits = YoloResourceLimits(
                maxModelDescriptors = 3,
                maxModelArtifactBytes = 512L * 1024L * 1024L,
                maxTotalModelBytes = 1024L * 1024L * 1024L,
                maxImageDimension = 16_384,
                maxImageBytes = 256L * 1024L * 1024L,
                maxDetections = YoloContract.MAX_DETECTIONS,
                maxCpuThreads = 16,
                maxTimeoutMillis = 2L * 60L * 1_000L,
                maxConcurrentSessions = 1,
                maxQueuedDetections = 0,
            ),
        )

    fun providerInfo(context: Context): YoloProviderInfo {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }
        return YoloProviderInfo(
            providerId = PROVIDER_ID,
            displayName = context.getString(R.string.app_name),
            author = context.getString(R.string.plugin_author),
            versionName = packageInfo.versionName.orEmpty(),
            versionCode = versionCode,
            backend = YoloBackend.NCNN,
            supportedAbis = supportedAbis,
            minHostVersionCode = REQUIRED_HOST_VERSION,
        )
    }

    fun pluginInfo(context: Context): PluginInfo {
        val provider = providerInfo(context)
        return PluginInfo().apply {
            name = provider.displayName
            description = context.getString(R.string.plugin_description)
            instruction = context.resources.openRawResource(R.raw.plugin_instruction)
                .bufferedReader()
                .use { it.readText() }
            author = provider.author
            collaborators = null
            versionName = provider.versionName
            versionCode = provider.versionCode
            versionDate = BuildConfig.VERSION_DATE
            id = PLUGIN_ID
            engine = ENGINE
            variant = VARIANT
            supportedAbis = YoloPlugin.supportedAbis.toTypedArray()
            capabilities = Bundle().apply {
                putLong(PluginCapabilityKeys.REQUIRES_HOST_VERSION, REQUIRED_HOST_VERSION)
                putBoolean("runtimeReady", NativeYoloRuntime.isReady)
                putString("backend", "ncnn")
                putString("decoder", DECODER_ID)
                putBoolean("supportsDetect", true)
                putBoolean("supportsVulkan", false)
            }
        }
    }
}
