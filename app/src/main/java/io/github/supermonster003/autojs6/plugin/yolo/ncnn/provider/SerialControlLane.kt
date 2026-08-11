package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import java.io.Closeable
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.Semaphore
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/** Serializes session admission, cancellation, completion, and cleanup decisions. */
internal class SerialControlLane : Closeable {
    enum class DispatchResult {
        ACCEPTED,
        SATURATED,
        CLOSED,
    }

    private val queue = LinkedBlockingDeque<Runnable>(TOTAL_QUEUE_CAPACITY)
    private val normalPermits = Semaphore(NORMAL_TASK_CAPACITY)
    private val executor = ThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.MILLISECONDS,
        queue,
        { runnable -> Thread(runnable, "yolo-control").apply { isDaemon = true } },
        ThreadPoolExecutor.AbortPolicy(),
    ).apply {
        prestartCoreThread()
    }

    /**
     * Host-originated traffic has a small fixed admission budget. A rejected detect task therefore
     * cannot retain an unbounded number of duplicated image descriptors.
     */
    fun tryDispatch(block: () -> Unit): DispatchResult {
        if (executor.isShutdown) return DispatchResult.CLOSED
        if (!normalPermits.tryAcquire()) return DispatchResult.SATURATED
        return try {
            executor.execute {
                try {
                    block()
                } finally {
                    normalPermits.release()
                }
            }
            DispatchResult.ACCEPTED
        } catch (_: RejectedExecutionException) {
            normalPermits.release()
            if (executor.isShutdown) DispatchResult.CLOSED else DispatchResult.SATURATED
        }
    }

    /**
     * Internally generated close/completion events use reserved, priority slots. The session
     * coalesces each reserved event kind, so this path stays bounded.
     */
    fun dispatchCritical(block: () -> Unit): Boolean {
        if (executor.isShutdown) return false
        return queue.offerFirst(Runnable(block))
    }

    /** Bounded, coalesced cancel/admission work runs after already admitted control traffic. */
    fun dispatchReserved(block: () -> Unit): Boolean {
        if (executor.isShutdown) return false
        return queue.offerLast(Runnable(block))
    }

    /** Existing control events drain; new events are rejected. */
    override fun close() {
        executor.shutdown()
    }

    private companion object {
        const val NORMAL_TASK_CAPACITY = 4
        const val RESERVED_CRITICAL_CAPACITY = 4
        const val TOTAL_QUEUE_CAPACITY = NORMAL_TASK_CAPACITY + RESERVED_CRITICAL_CAPACITY
    }
}
