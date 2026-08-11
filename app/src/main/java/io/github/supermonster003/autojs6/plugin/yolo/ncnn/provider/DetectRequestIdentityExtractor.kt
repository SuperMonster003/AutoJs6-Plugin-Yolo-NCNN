package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import org.autojs.plugin.protocol.wire.TaggedWireDocument
import org.autojs.plugin.protocol.wire.TaggedWireLimits
import org.autojs.plugin.yolo.api.YoloContract
import org.autojs.plugin.yolo.api.YoloRequestId

internal data class DetectRequestIdentity(
    val requestId: YoloRequestId,
    val sequence: Long,
)

/**
 * Recovers only the two terminal-correlation fields from a structurally valid
 * detect envelope. This lets the provider reject an otherwise invalid request
 * without fabricating an identity or disturbing another active request.
 */
internal object DetectRequestIdentityExtractor {
    private const val REQUEST_ID_TAG = 1
    private const val SEQUENCE_TAG = 2

    fun extractOrNull(bytes: ByteArray): DetectRequestIdentity? = runCatching {
        val document = TaggedWireDocument.decode(
            bytes,
            TaggedWireLimits(
                maxDocumentBytes = YoloContract.MAX_METADATA_BYTES,
                maxFieldBytes = YoloContract.MAX_FIELD_BYTES,
                maxFields = YoloContract.MAX_WIRE_FIELDS,
            ),
        ).requireSchema(YoloContract.SCHEMA_DETECT_REQUEST, YoloContract.SCHEMA_MAJOR)
        val sequence = document.requireInt64(SEQUENCE_TAG)
        require(sequence > 0L)
        DetectRequestIdentity(
            requestId = YoloRequestId.fromBytes(document.requireBytes(REQUEST_ID_TAG)),
            sequence = sequence,
        )
    }.getOrNull()
}
