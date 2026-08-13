package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

internal data class YoloProviderDiagnosticsSnapshot(
    val pid: Int,
    val activeSessions: Int,
    val sessionGate: Boolean,
    val activeNativeHandles: Int,
    val selfFdCount: Int,
    val selfRssKb: Long,
    val modelDirCount: Int,
) {
    init {
        require(pid > 0) { "Provider diagnostics PID must be positive" }
        require(activeSessions >= 0) { "Provider diagnostics session count must be non-negative" }
        require(activeNativeHandles >= 0) { "Provider diagnostics native handle count must be non-negative" }
        require(selfFdCount >= UNAVAILABLE_INT) { "Provider diagnostics FD count is invalid" }
        require(selfRssKb >= UNAVAILABLE_LONG) { "Provider diagnostics RSS is invalid" }
        require(modelDirCount >= UNAVAILABLE_INT) { "Provider diagnostics model directory count is invalid" }
    }

    fun formatLine(): String = buildString {
        append(PREFIX)
        append('{')
        append("\"schemaVersion\":1")
        append(",\"pid\":").append(pid)
        append(",\"activeSessions\":").append(activeSessions)
        append(",\"sessionGate\":").append(sessionGate)
        append(",\"activeNativeHandles\":").append(activeNativeHandles)
        append(",\"selfFdCount\":").append(selfFdCount)
        append(",\"selfRssKb\":").append(selfRssKb)
        append(",\"modelDirCount\":").append(modelDirCount)
        append('}')
    }

    companion object {
        const val PREFIX = "AUTOJS6_YOLO_DIAGNOSTICS_V1="
        const val UNAVAILABLE_INT = -1
        const val UNAVAILABLE_LONG = -1L
    }
}
