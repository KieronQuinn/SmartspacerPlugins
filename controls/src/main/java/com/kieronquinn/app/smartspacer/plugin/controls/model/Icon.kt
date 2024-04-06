package com.kieronquinn.app.smartspacer.plugin.controls.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.core.graphics.drawable.toBitmap
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.niceName
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.googlematerial.OutlinedGoogleMaterial
import com.mikepenz.iconics.typeface.library.googlematerial.RoundedGoogleMaterial
import kotlinx.parcelize.Parcelize
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon as SmartspacerIcon

sealed class Icon(
    @SerializedName(NAME_TYPE)
    val type: Type
): Parcelable {

    companion object {
        const val NAME_TYPE = "type"

        fun default(): Font {
            return Font(
                IconFont.MATERIAL_ROUNDED.name,
                RoundedGoogleMaterial.Icon.gmr_event.name
            )
        }

        private fun createErrorSmartspacerIcon(
            context: Context,
            iconSize: Int,
            iconPadding: Int
        ): SmartspacerIcon {
            return IconicsDrawable(context, OutlinedGoogleMaterial.Icon.gmo_broken_image).apply {
                paddingPx = iconPadding
            }.toSmartspacerIcon(iconSize, iconSize, true)
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

    abstract fun describe(context: Context): CharSequence

    @Parcelize
    data class Font(
        @SerializedName("font_name")
        val fontName: String,
        @SerializedName("icon_name")
        val iconName: String
    ): Icon(Type.FONT) {

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
                it.name == fontName
            }
            return iconFont?.getIcon(iconName)
        }

        fun loadIcon(context: Context): SmartspacerIcon {
            val iconSize = context.resources.getDimensionPixelSize(R.dimen.target_icon_size)
            val iconPadding = context.resources.getDimensionPixelSize(R.dimen.icon_padding)
            val icon = getIcon()?.also {
            } ?: return createErrorSmartspacerIcon(context, iconSize, iconPadding)
            return IconicsDrawable(context, icon).apply {
                paddingPx = iconPadding
            }.toSmartspacerIcon(iconSize, iconSize, true)
        }

    }

    @Parcelize
    data class File(
        @SerializedName("uri")
        val uri: String,
        @SerializedName("name")
        val name: String?,
        @SerializedName("tint")
        val tint: Boolean
    ): Icon(Type.FILE) {

        override fun describe(context: Context): CharSequence {
            return name?.let {
                context.getString(R.string.configuration_icon_description, it)
            } ?: context.getString(R.string.configuration_icon_description_error)
        }

    }

    enum class Type {
        FONT, FILE
    }

}