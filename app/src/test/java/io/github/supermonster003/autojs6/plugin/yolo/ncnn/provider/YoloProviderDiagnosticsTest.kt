package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class YoloProviderDiagnosticsTest {

    @Test
    fun formatterIsOneDeterministicNonSensitiveJsonLine() {
        val line = YoloProviderDiagnosticsSnapshot(
            pid = 1234,
            activeSessions = 1,
            sessionGate = true,
            activeNativeHandles = 1,
            selfFdCount = 27,
            selfRssKb = 98_765L,
            modelDirCount = 1,
        ).formatLine()

        assertEquals(
            "AUTOJS6_YOLO_DIAGNOSTICS_V1=" +
                "{\"schemaVersion\":1,\"pid\":1234,\"activeSessions\":1," +
                "\"sessionGate\":true,\"activeNativeHandles\":1,\"selfFdCount\":27," +
                "\"selfRssKb\":98765,\"modelDirCount\":1}",
            line,
        )
        assertTrue(line.none { it == '\n' || it == '\r' })
        assertTrue("model paths must not be present", !line.contains('/') && !line.contains('\\'))
    }

    @Test
    fun nativeHandleCounterTracksCreateAndDestroy() {
        val counter = AtomicInteger(0)
        assertEquals(1, NativeHandleCountPolicy.onCreated(counter))
        assertEquals(1, counter.get())

        var destroyedHandle = 0L
        NativeHandleCountPolicy.destroy(counter, 42L) { destroyedHandle = it }

        assertEquals(42L, destroyedHandle)
        assertEquals(0, counter.get())
    }

    @Test
    fun nativeHandleCounterDecrementsWhenDestroyThrows() {
        val counter = AtomicInteger(0)
        NativeHandleCountPolicy.onCreated(counter)

        assertThrows(IllegalStateException::class.java) {
            NativeHandleCountPolicy.destroy(counter, 7L) { error("synthetic destroy failure") }
        }
        assertEquals(0, counter.get())
    }

    @Test
    fun nativeHandleCounterRejectsUnderflowWithoutMutation() {
        val counter = AtomicInteger(0)
        var destroyCalled = false

        assertThrows(IllegalStateException::class.java) {
            NativeHandleCountPolicy.destroy(counter, 9L) { destroyCalled = true }
        }
        assertEquals(0, counter.get())
        assertTrue(!destroyCalled)
    }
}
