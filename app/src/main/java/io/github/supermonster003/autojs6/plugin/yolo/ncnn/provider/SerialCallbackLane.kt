package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import java.io.Closeable
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class SerialCallbackLane : Closeable {
    private val executor = ThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.MILLISECONDS,
        ArrayBlockingQueue(8),
        { runnable -> Thread(runnable, "yolo-callback").apply { isDaemon = true } },
        ThreadPoolExecutor.AbortPolicy(),
    )

    fun dispatch(callback: () -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            executor.execute { runCatching(callback).onFailure(onFailure) }
        } catch (error: RejectedExecutionException) {
            onFailure(error)
        }
    }

    override fun close() {
        executor.shutdownNow()
    }
}
