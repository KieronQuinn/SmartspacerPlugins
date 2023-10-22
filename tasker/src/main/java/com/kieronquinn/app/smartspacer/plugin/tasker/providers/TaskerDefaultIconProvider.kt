package com.kieronquinn.app.smartspacer.plugin.tasker.providers

import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugin.tasker.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerBitmapProvider
import com.kieronquinn.app.smartspacer.sdk.utils.createSmartspacerProxyUri

class TaskerDefaultIconProvider: SmartspacerBitmapProvider() {

    companion object {
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider.defaulticon"

        fun getUri(): Uri {
            val uri = Uri.parse("content://$AUTHORITY")
            return createSmartspacerProxyUri(uri)
        }
    }

    override fun getBitmap(uri: Uri): Bitmap? {
        val icon = Icon.createWithResource(provideContext(), R.mipmap.ic_launcher)
        return icon.loadDrawable(context)?.toBitmap(512, 512)
    }

}