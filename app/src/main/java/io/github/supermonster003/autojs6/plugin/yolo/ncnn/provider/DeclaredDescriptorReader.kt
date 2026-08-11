package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import org.autojs.plugin.yolo.api.YoloModelArtifact
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest

internal object DeclaredDescriptorReader {
    private const val BUFFER_BYTES = 64 * 1024

    fun copyAndVerify(
        input: InputStream,
        output: OutputStream,
        declaration: YoloModelArtifact,
        maximumLengthBytes: Long,
        deadline: YoloSessionOpenDeadline,
        verifyProducerCompletion: () -> Unit,
    ) {
        require(declaration.sizeBytes in 1L..maximumLengthBytes) {
            "Model artifact exceeds its bounded reader limit"
        }
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(BUFFER_BYTES)
        var remaining = declaration.sizeBytes
        while (remaining > 0L) {
            deadline.requireRemaining()
            val count = input.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
            require(count >= 0) { "Model artifact ended before its declared length" }
            if (count == 0) {
                val single = input.read()
                require(single >= 0) { "Model artifact ended before its declared length" }
                output.write(single)
                digest.update(single.toByte())
                remaining -= 1L
            } else {
                output.write(buffer, 0, count)
                digest.update(buffer, 0, count)
                remaining -= count.toLong()
            }
        }
        deadline.requireRemaining()
        require(input.read() == -1) { "Model artifact exceeds its declared length" }
        verifyProducerCompletion()
        require(
            MessageDigest.isEqual(
                digest.digest(),
                declaration.sha256.toByteArray(),
            ),
        ) { "Model artifact SHA-256 does not match its declaration" }
        deadline.requireRemaining()
    }
}
