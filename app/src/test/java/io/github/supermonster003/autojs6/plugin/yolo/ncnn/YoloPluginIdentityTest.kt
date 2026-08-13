package io.github.supermonster003.autojs6.plugin.yolo.ncnn

import org.junit.Assert.assertEquals
import org.junit.Test

class YoloPluginIdentityTest {
    @Test
    fun releaseIdentityRemainsPinnedForOfflineIndexGeneration() {
        assertEquals("io.github.supermonster003.autojs6.plugin.yolo.ncnn", YoloPlugin.APPLICATION_ID)
        assertEquals("yolo-ncnn", YoloPlugin.PLUGIN_ID)
        assertEquals("yolo", YoloPlugin.ENGINE)
        assertEquals("ncnn", YoloPlugin.VARIANT)
        assertEquals(5_274L, YoloPlugin.REQUIRED_HOST_VERSION)
    }
}
