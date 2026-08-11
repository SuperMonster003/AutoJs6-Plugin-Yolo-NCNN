package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import android.os.ParcelFileDescriptor
import java.io.Closeable

internal class OwnedParcelFileDescriptors private constructor(
    private val descriptors: Array<ParcelFileDescriptor?>,
) : Closeable {
    private val consumed = BooleanArray(descriptors.size)
    private var closed = false

    val count: Int
        get() = descriptors.size

    fun <T> consume(index: Int, block: (ParcelFileDescriptor) -> T): T {
        val descriptor = synchronized(this) {
            check(!closed) { "Descriptor owner is closed" }
            require(index in descriptors.indices) { "Descriptor index is out of range" }
            check(!consumed[index]) { "Descriptor was already consumed" }
            consumed[index] = true
            descriptors[index].also { descriptors[index] = null }
                ?: error("Descriptor is unavailable")
        }
        return descriptor.use(block)
    }

    override fun close() {
        val remaining = synchronized(this) {
            if (closed) return
            closed = true
            descriptors.mapIndexedNotNull { index, descriptor ->
                descriptor?.also {
                    consumed[index] = true
                    descriptors[index] = null
                }
            }
        }
        remaining.forEach { runCatching { it.close() } }
    }

    companion object {
        /**
         * Binder ingress must duplicate every descriptor before returning control
         * to a caller while asynchronous work may still reference it.
         */
        fun duplicateBeforeAsync(
            incoming: Array<out ParcelFileDescriptor>,
        ): OwnedParcelFileDescriptors {
            val copies = arrayOfNulls<ParcelFileDescriptor>(incoming.size)
            try {
                incoming.forEachIndexed { index, descriptor ->
                    copies[index] = ParcelFileDescriptor.dup(descriptor.fileDescriptor)
                }
                return OwnedParcelFileDescriptors(copies)
            } catch (error: Throwable) {
                copies.forEach { runCatching { it?.close() } }
                throw error
            } finally {
                closeIncoming(incoming)
            }
        }

        fun closeIncoming(incoming: Array<out ParcelFileDescriptor>?) {
            incoming?.forEach { runCatching { it.close() } }
        }

        fun duplicateOneBeforeAsync(incoming: ParcelFileDescriptor): ParcelFileDescriptor = try {
            ParcelFileDescriptor.dup(incoming.fileDescriptor)
        } finally {
            runCatching { incoming.close() }
        }

        fun closeIncoming(incoming: ParcelFileDescriptor?) {
            runCatching { incoming?.close() }
        }
    }
}
