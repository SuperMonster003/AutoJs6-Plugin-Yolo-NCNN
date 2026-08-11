package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import android.os.SystemClock

/** One monotonic deadline shared by descriptor verification and native session construction. */
internal class YoloSessionOpenDeadline private constructor(
    private val deadlineElapsedRealtimeMillis: Long,
) {
    fun requireRemaining() {
        if (SystemClock.elapsedRealtime() >= deadlineElapsedRealtimeMillis) {
            throw YoloSessionOpenTimeoutException()
        }
    }

    fun remainingMillis(): Long {
        val remaining = deadlineElapsedRealtimeMillis - SystemClock.elapsedRealtime()
        if (remaining <= 0L) throw YoloSessionOpenTimeoutException()
        return remaining
    }

    companion object {
        fun start(timeoutMillis: Long): YoloSessionOpenDeadline {
            require(timeoutMillis > 0L) { "YOLO session-open timeout must be positive" }
            val now = SystemClock.elapsedRealtime()
            val deadline = if (timeoutMillis > Long.MAX_VALUE - now) {
                Long.MAX_VALUE
            } else {
                now + timeoutMillis
            }
            return YoloSessionOpenDeadline(deadline)
        }
    }
}

internal class YoloSessionOpenTimeoutException : RuntimeException(
    "YOLO session open exceeded its declared deadline",
)
