package com.kieronquinn.app.smartspacer.plugin.tasker.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.core.graphics.drawable.toBitmap
import androidx.documentfile.provider.DocumentFile
import com.bumptech.glide.Glide
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.providers.TaskerDefaultIconProvider
import com.kieronquinn.app.smartspacer.plugin.tasker.providers.TaskerFontIconProvider
import com.kieronquinn.app.smartspacer.plugin.tasker.providers.TaskerProxyProvider
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.downloadToFile
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.niceName
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerProxyProvider
import com.kieronquinn.app.smartspacer.sdk.utils.createSmartspacerProxyUri
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.typeface.library.googlematerial.OutlinedGoogleMaterial
import kotlinx.parcelize.Parcelize
import android.graphics.Bitmap as AndroidBitmap
import android.graphics.drawable.Icon as AndroidIcon
import android.net.Uri as AndroidUri
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon as SmartspacerIcon

sealed class Icon(
    @Transient
    @SerializedName(NAME_SHOULD_TINT)
    open val _shouldTint: String? = null,
    @Transient
    @SerializedName(NAME_CONTENT_DESCRIPTION)
    open val contentDescription: String? = null,
    @SerializedName(NAME_TYPE)
    val type: IconType
): Manipulative<Icon>, Parcelable {

    val shouldTint
        get() = _shouldTint?.toBooleanStrictOrNull() ?: true

    companion object {
        const val NAME_TYPE = "type"
        private const val NAME_SHOULD_TINT = "should_tint"
        private const val NAME_CONTENT_DESCRIPTION = "content_description"

        private fun createErrorSmartspacerIcon(
            context: Context,
            iconSize: Int,
            iconPadding: Int
        ): SmartspacerIcon {
            return IconicsDrawable(context, OutlinedGoogleMaterial.Icon.gmo_broken_image).apply {
                paddingPx = iconPadding
            }.toSmartspacerIcon(iconSize, iconSize, true)
        }

        fun createDefaultImage(context: Context): Bitmap {
            return Bitmap(
                _shouldTint = "false",
                contentDescription = context.getString(
                    R.string.configuration_icon_description_default
                ),
                uri = TaskerDefaultIconProvider.getUri().toString()
            )
        }

        private fun createGenericWebIcon(): Font {
            return Font(
                "true",
                null,
                IconFont.COMMUNITY_MATERIAL.name,
                CommunityMaterial.Icon3.cmd_web.name
            )
        }

        private fun Drawable.toSmartspacerIcon(
            width: Int = intrinsicWidth,
            height: Int = intrinsicHeight,
            shouldTint: Boolean
        ): SmartspacerIcon {
            return SmartspacerIcon(
                AndroidIcon.createWithBitmap(toBitmap(width, height)),
                shouldTint = shouldTint
            )
        }
    }

    abstract fun toIcon(context: Context, width: Int? = null, height: Int? = null): SmartspacerIcon
    abstract fun describe(context: Context): CharSequence

    fun loadBitmap(context: Context): AndroidBitmap? {
        return toIcon(context).icon.loadDrawable(context)?.toBitmap()
    }

    @Parcelize
    data class Bitmap(
        @SerializedName(NAME_SHOULD_TINT)
        override val _shouldTint: String? = null,
        @SerializedName(NAME_CONTENT_DESCRIPTION)
        override val contentDescription: String? = null,
        @SerializedName("uri")
        val uri: String
    ): Icon(_shouldTint, contentDescription, IconType.BITMAP) {

        override fun getVariables(): Array<String> {
            return emptyArray()
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Bitmap {
            //Path replacement is not supported due to SAF
            return this
        }

        override fun toIcon(context: Context, width: Int?, height: Int?): SmartspacerIcon {
            return SmartspacerIcon(
                icon = AndroidIcon.createWithContentUri(uri),
                shouldTint = shouldTint,
                contentDescription = contentDescription
            )
        }

        override fun describe(context: Context): CharSequence {
            return contentDescription?.let {
                context.getString(R.string.configuration_icon_description, it)
            } ?: context.getString(R.string.configuration_icon_description_error)
        }

    }

    @Parcelize
    data class Url(
        @SerializedName(NAME_SHOULD_TINT)
        override val _shouldTint: String? = null,
        @SerializedName(NAME_CONTENT_DESCRIPTION)
        override val contentDescription: String? = null,
        @SerializedName("url")
        val _url: String,
        @SerializedName("authentication")
        val authentication: String? = null
    ): Icon(_shouldTint, contentDescription, IconType.URL) {

        val url
            get() = try {
                AndroidUri.parse(_url)
            }catch (e: Exception){
                null
            }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *_shouldTint?.getVariables() ?: emptyArray(),
                *contentDescription?.getVariables() ?: emptyArray(),
                *_url.getVariables(),
                *authentication?.getVariables() ?: emptyArray()
            )
        }

        override suspend fun copyWithManipulations(
            context: Context,
            replacements: Map<String, String>
        ): Icon {
            val url = url ?: return createGenericWebIcon()
            val downloadedFile = Glide.with(context).downloadToFile(url.toString(), authentication)
                ?: return createDefaultImage(context)
            val documentFile = DocumentFile.fromFile(downloadedFile)
            val taskerProxyUri = SmartspacerProxyProvider.proxy(
                documentFile.uri, TaskerProxyProvider.AUTHORITY
            )
            val proxyUri = createSmartspacerProxyUri(taskerProxyUri)
            return Bitmap(
                _shouldTint = _shouldTint?.replace(replacements),
                contentDescription = url.toString(),
                uri = proxyUri.toString()
            )
        }

        override fun toIcon(context: Context, width: Int?, height: Int?): SmartspacerIcon {
            //Should never be loaded raw, only converted to Bitmap
            return createGenericWebIcon().toIcon(context, width, height)
        }

        override fun describe(context: Context): CharSequence {
            return context.getString(R.string.configuration_icon_url_description, _url)
        }

    }

    @Parcelize
    data class Font(
        @SerializedName(NAME_SHOULD_TINT)
        override val _shouldTint: String? = "true",
        @SerializedName(NAME_CONTENT_DESCRIPTION)
        override val contentDescription: String? = null,
        @SerializedName("icon_font_name")
        val iconFontName: String,
        @SerializedName("icon_name")
        val iconName: String
    ): Icon(_shouldTint, contentDescription, IconType.FONT) {

        override fun toIcon(context: Context, width: Int?, height: Int?): SmartspacerIcon {
            return SmartspacerIcon(
                icon = AndroidIcon.createWithContentUri(TaskerFontIconProvider.createUri(this)),
                shouldTint = shouldTint
            )
        }

        fun loadIcon(context: Context): SmartspacerIcon {
            val iconSize = context.resources.getDimensionPixelSize(R.dimen.target_icon_size)
            val iconPadding = context.resources.getDimensionPixelSize(R.dimen.icon_padding)
            val icon = getIcon() ?: return createErrorSmartspacerIcon(context, iconSize, iconPadding)
            return IconicsDrawable(context, icon).apply {
                paddingPx = iconPadding
            }.toSmartspacerIcon(iconSize, iconSize, shouldTint)
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Icon {
            return this
        }

        override fun describe(context: Context): CharSequence {
            return getIcon()?.let {
                context.getString(
                    R.string.configuration_icon_font_description,
                    it.niceName,
                    it.typeface.fontName
                )
            } ?: context.getString(R.string.configuration_icon_font_description_error)
        }

        private fun getIcon(): IIcon? {
            val iconFont = IconFont.values().firstOrNull {
                it.name == iconFontName
            }
            return iconFont?.getIcon(iconName)
        }

        override fun getVariables(): Array<String> {
            return emptyArray()
        }

    }

    enum class IconType {
        BITMAP,
        URL,
        FONT
    }

}
