package io.github.supermonster003.autojs6.plugin.yolo.ncnn

import io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider.YoloProviderService
import org.autojs.plugin.yolo.api.YoloBackend
import org.autojs.plugin.yolo.api.YoloTask
import org.junit.Assert.assertEquals
import org.junit.Test

class YoloPluginIdentityTest {
    @Test
    fun releaseIdentityRemainsPinnedForOfflineIndexGeneration() {
        assertEquals("io.github.supermonster003.autojs6.plugin.yolo.ncnn", YoloPlugin.APPLICATION_ID)
        assertEquals("yolo-ncnn", YoloPlugin.PLUGIN_ID)
        assertEquals("yolo", YoloPlugin.ENGINE)
        assertEquals("ncnn", YoloPlugin.VARIANT)
        assertEquals(5_275L, YoloPlugin.REQUIRED_HOST_VERSION)
        assertEquals(YoloPlugin.RELEASE_RUNTIME_SERVICE, YoloProviderService::class.java.name)
        assertEquals(
            "${YoloPlugin.APPLICATION_ID}/${YoloPlugin.RELEASE_RUNTIME_SERVICE}",
            YoloPlugin.RELEASE_RUNTIME_COMPONENT,
        )

        val capabilities = YoloPlugin.capabilities
        assertEquals(
            "${capabilities.protocolMin.major}.${capabilities.protocolMin.minor}",
            YoloPlugin.RELEASE_PROTOCOL_API_MIN,
        )
        assertEquals(
            "${capabilities.protocolMax.major}.${capabilities.protocolMax.minor}",
            YoloPlugin.RELEASE_PROTOCOL_API_MAX,
        )
        assertEquals(YoloBackend.NCNN, capabilities.backend)
        assertEquals(capabilities.backend.name.lowercase(), YoloPlugin.RELEASE_BACKEND)
        assertEquals(listOf(YoloTask.DETECT), capabilities.tasks)
        assertEquals(capabilities.tasks.single().name.lowercase(), YoloPlugin.RELEASE_TASK)
        assertEquals(listOf(YoloPlugin.RELEASE_DECODER), capabilities.decoderIds)
        assertEquals(YoloPlugin.RELEASE_DECODER, YoloPlugin.DECODER_ID)
        assertEquals(listOf(YoloPlugin.RELEASE_SUPPORTED_ABI), YoloPlugin.supportedAbis)
    }
}
