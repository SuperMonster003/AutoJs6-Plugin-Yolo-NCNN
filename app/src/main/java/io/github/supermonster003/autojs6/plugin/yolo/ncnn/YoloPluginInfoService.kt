package io.github.supermonster003.autojs6.plugin.yolo.ncnn

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.autojs.plugin.common.api.IPluginInfoProvider

class YoloPluginInfoService : Service() {
    private val binder = object : IPluginInfoProvider.Stub() {
        override fun getInfo() = YoloPlugin.pluginInfo(this@YoloPluginInfoService)
    }

    // Explicit-component binding intentionally carries no action.
    override fun onBind(intent: Intent?): IBinder = binder
}
