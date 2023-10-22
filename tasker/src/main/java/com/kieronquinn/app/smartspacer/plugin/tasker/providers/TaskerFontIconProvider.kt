package com.kieronquinn.app.smartspacer.plugin.tasker.providers

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugin.tasker.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.takeIfNotBlank
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerBitmapProvider
import com.kieronquinn.app.smartspacer.sdk.utils.createSmartspacerProxyUri

class TaskerFontIconProvider: SmartspacerBitmapProvider() {

    companion object {
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider.font"
        private const val ARG_TINT = "tint"
        private const val ARG_CONTENT_DESCRIPTION = "desc"

        fun createUri(icon: Icon.Font): Uri {
            val uri = Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .appendPath(icon.iconFontName)
                .appendPath(icon.iconName)
                .appendQueryParameter(ARG_TINT, icon.shouldTint.toString())
                .appendQueryParameter(ARG_CONTENT_DESCRIPTION, icon.contentDescription ?: "")
                .build()
            return createSmartspacerProxyUri(uri)
        }
    }

    override fun getBitmap(uri: Uri): Bitmap? {
        val fontName = uri.pathSegments.getOrNull(0) ?: return null
        val iconName = uri.pathSegments.getOrNull(1) ?: return null
        val shouldTint = uri.getQueryParameter(ARG_TINT)
        val contentDescription = uri.getQueryParameter(ARG_CONTENT_DESCRIPTION)?.takeIfNotBlank()
        val icon = Icon.Font(shouldTint, contentDescription, fontName, iconName)
        return icon.loadIcon(provideContext()).icon.loadDrawable(provideContext())?.toBitmap()
    }

}