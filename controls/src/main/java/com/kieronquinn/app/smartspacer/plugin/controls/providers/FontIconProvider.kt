package com.kieronquinn.app.smartspacer.plugin.controls.providers

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugin.controls.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.controls.model.Icon
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerBitmapProvider
import com.kieronquinn.app.smartspacer.sdk.utils.createSmartspacerProxyUri

class FontIconProvider: SmartspacerBitmapProvider() {

    companion object {
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider.font"

        fun createUri(icon: Icon.Font): Uri {
            val uri = Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .appendPath(icon.fontName)
                .appendPath(icon.iconName)
                .build()
            return createSmartspacerProxyUri(uri)
        }
    }

    override fun getBitmap(uri: Uri): Bitmap? {
        val fontName = uri.pathSegments.getOrNull(0) ?: return null
        val iconName = uri.pathSegments.getOrNull(1) ?: return null
        val icon = Icon.Font(fontName, iconName)
        return icon.loadIcon(provideContext()).icon.loadDrawable(provideContext())?.toBitmap()
    }

}