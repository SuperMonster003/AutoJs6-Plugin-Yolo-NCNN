package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import java.util.concurrent.atomic.AtomicInteger

/** Keeps the release runtime's logical native-handle count exact across every destroy path. */
internal object NativeHandleCountPolicy {

    fun onCreated(counter: AtomicInteger): Int {
        while (true) {
            val current = counter.get()
            check(current >= 0) { "Native YOLO handle count is corrupt" }
            check(current < Int.MAX_VALUE) { "Native YOLO handle count overflow" }
            val next = current + 1
            if (counter.compareAndSet(current, next)) return next
        }
    }

    fun destroy(
        counter: AtomicInteger,
        handle: Long,
        nativeDestroy: (Long) -> Unit,
    ) {
        require(handle != 0L) { "Native YOLO handle must be non-zero" }
        check(counter.get() > 0) { "Native YOLO handle count underflow" }
        try {
            nativeDestroy(handle)
        } finally {
            onDestroyed(counter)
        }
    }

    private fun onDestroyed(counter: AtomicInteger): Int {
        while (true) {
            val current = counter.get()
            check(current > 0) { "Native YOLO handle count underflow" }
            val next = current - 1
            if (counter.compareAndSet(current, next)) return next
        }
    }
}
