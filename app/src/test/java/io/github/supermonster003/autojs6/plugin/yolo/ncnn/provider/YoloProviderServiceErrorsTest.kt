package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import org.autojs.plugin.yolo.api.YoloContractException
import org.autojs.plugin.yolo.api.YoloContractViolation
import org.autojs.plugin.yolo.api.YoloErrorCode
import org.autojs.plugin.yolo.api.YoloOpenSessionFailureCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class YoloProviderServiceErrorsTest {

    @Test
    fun capabilityAndProtocolIncompatibilityKeepStableOpenFailureCodes() {
        listOf(
            YoloContractViolation.CAPABILITY_INCOMPATIBLE to YoloErrorCode.UNSUPPORTED_CAPABILITY,
            YoloContractViolation.PROTOCOL_INCOMPATIBLE to YoloErrorCode.UNSUPPORTED_PROTOCOL,
        ).forEach { (violation, expectedCode) ->
            val mapped = YoloProviderServiceErrors.mapOpenFailure(
                YoloContractException(violation, "unsupported test request"),
            ) as IllegalArgumentException
            val decoded = YoloOpenSessionFailureCodec.decode(mapped)

            assertNotNull(decoded)
            assertEquals(expectedCode, decoded?.code)
            assertEquals("unsupported test request", decoded?.message)
        }
    }
}
