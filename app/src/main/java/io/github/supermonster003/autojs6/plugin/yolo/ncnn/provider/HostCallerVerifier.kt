package io.github.supermonster003.autojs6.plugin.yolo.ncnn.provider

import android.content.Context
import android.content.pm.PackageManager
import android.os.Binder
import io.github.supermonster003.autojs6.plugin.yolo.ncnn.YoloPlugin

internal class HostCallerVerifier(context: Context) {
    private val packageManager = context.applicationContext.packageManager
    private val providerPackageName = context.applicationContext.packageName

    fun enforceAllowedCaller(): Int = Binder.getCallingUid().also(::enforceAllowedUid)

    fun enforceSessionOwner(expectedUid: Int) {
        val callingUid = Binder.getCallingUid()
        if (callingUid != expectedUid) {
            throw SecurityException("YOLO session UID does not match its owner")
        }
        enforceAllowedUid(callingUid)
    }

    @Suppress("DEPRECATION")
    private fun enforceAllowedUid(uid: Int) {
        val installedHostUid = try {
            packageManager.getApplicationInfo(YoloPlugin.HOST_PACKAGE_NAME, 0).uid
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
        val packages = packageManager.getPackagesForUid(uid)?.toSet().orEmpty()
        val signaturesMatch = packageManager.checkSignatures(
            providerPackageName,
            YoloPlugin.HOST_PACKAGE_NAME,
        ) == PackageManager.SIGNATURE_MATCH

        val rejection = when {
            YoloPlugin.HOST_PACKAGE_NAME !in packages -> "HOST_PACKAGE_NOT_OWNED"
            installedHostUid == null -> "HOST_NOT_INSTALLED"
            uid != installedHostUid -> "HOST_UID_MISMATCH"
            !signaturesMatch -> "SIGNATURE_MISMATCH"
            else -> null
        }
        if (rejection != null) {
            throw SecurityException("YOLO provider caller rejected: $rejection")
        }
    }
}
