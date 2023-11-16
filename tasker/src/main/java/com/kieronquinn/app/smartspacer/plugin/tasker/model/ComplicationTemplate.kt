package com.kieronquinn.app.smartspacer.plugin.tasker.model

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.UiSurface_validSurfaces
import com.kieronquinn.app.smartspacer.sdk.annotations.DisablingTrim
import com.kieronquinn.app.smartspacer.sdk.annotations.LimitedNativeSupport
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.UiSurface
import com.kieronquinn.app.smartspacer.sdk.model.weather.WeatherData.WeatherStateIcon
import com.kieronquinn.app.smartspacer.sdk.utils.TrimToFit
import kotlinx.parcelize.Parcelize
import com.kieronquinn.app.smartspacer.sdk.model.weather.WeatherData as SmartspacerWeatherData
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate.Basic as ComplicationTemplateBasic

sealed class ComplicationTemplate(
    @Transient
    @SerializedName(NAME_ICON)
    open val icon: Icon?,
    @Transient
    @SerializedName(NAME_CONTENT)
    open val content: Text,
    @Transient
    @SerializedName(NAME_ON_CLICK)
    open val onClick: TapAction,
    @Transient
    @SerializedName(NAME_COMPLICATION_EXTRAS)
    open val complicationExtras: ComplicationExtras,
    @Transient
    @SerializedName(NAME_DISABLE_TRIM)
    open val disableTrim: Boolean,
    @SerializedName(NAME_COMPLICATION_TYPE)
    val templateType: TemplateType
): Manipulative<ComplicationTemplate> {

    companion object {
        const val NAME_COMPLICATION_TYPE = "complication_type"
        private const val NAME_ICON = "icon"
        private const val NAME_CONTENT = "content"
        private const val NAME_ON_CLICK = "on_click"
        private const val NAME_COMPLICATION_EXTRAS = "complication_extras"
        private const val NAME_DISABLE_TRIM = "disable_trim"
    }

    abstract fun toComplication(
        context: Context,
        id: String
    ): SmartspaceAction

    fun copyCompat(
        icon: Icon? = this.icon,
        content: Text = this.content,
        complicationExtras: ComplicationExtras = this.complicationExtras,
        onClick: TapAction = this.onClick,
        disableTrim: Boolean = this.disableTrim
    ): ComplicationTemplate {
        return when(this) {
            is Basic -> copy(icon, content, onClick, complicationExtras, disableTrim)
        }
    }

    data class Basic(
        @SerializedName(NAME_ICON)
        override val icon: Icon?,
        @SerializedName(NAME_CONTENT)
        override val content: Text,
        @SerializedName(NAME_ON_CLICK)
        override val onClick: TapAction,
        @SerializedName(NAME_COMPLICATION_EXTRAS)
        override val complicationExtras: ComplicationExtras,
        @SerializedName(NAME_DISABLE_TRIM)
        override val disableTrim: Boolean = false
    ): ComplicationTemplate(
        icon,
        content,
        onClick,
        complicationExtras,
        disableTrim,
        TemplateType.BASIC
    ) {

        @OptIn(DisablingTrim::class)
        override fun toComplication(
            context: Context, id: String
        ): SmartspaceAction {
            return ComplicationTemplateBasic(
                id = "${id}_at_${System.currentTimeMillis()}",
                icon = icon?.toIcon(context),
                content = content.toText(),
                onClick = onClick.toTapAction(context),
                extras = Bundle.EMPTY,
                trimToFit = if(disableTrim) TrimToFit.Disabled else TrimToFit.Enabled
            ).create().apply {
                complicationExtras.applyToComplication(this)
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *(icon?.getVariables() ?: emptyArray()),
                *content.getVariables(),
                *onClick.getVariables(),
                *complicationExtras.getVariables()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Basic {
            return copy(
                icon = icon?.copyWithManipulations(context, replacements),
                content = content.copyWithManipulations(context, replacements),
                onClick = onClick.copyWithManipulations(context, replacements),
                complicationExtras = complicationExtras.copyWithManipulations(context, replacements)
            )
        }

    }

    data class ComplicationExtras(
        @SerializedName("limit_to_surfaces")
        val limitToSurfaces: Set<UiSurface> = UiSurface_validSurfaces().toSet(),
        @SerializedName("weather_data")
        val weatherData: WeatherData? = null
    ): Manipulative<ComplicationExtras> {

        @OptIn(LimitedNativeSupport::class)
        fun applyToComplication(smartspaceAction: SmartspaceAction) {
            smartspaceAction.weatherData = weatherData?.toWeatherData()
            smartspaceAction.limitToSurfaces = limitToSurfaces
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *weatherData?.getVariables() ?: emptyArray()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): ComplicationExtras {
            return copy(
                weatherData = weatherData?.copyWithManipulations(context, replacements)
            )
        }

    }

    @Parcelize
    data class WeatherData(
        @SerializedName("content_description")
        val contentDescription: String = "",
        @SerializedName("weather_state_icon")
        val _weatherStateIcon: String = WeatherStateIcon.UNKNOWN_ICON.id.toString(),
        @SerializedName("use_celsius")
        val useCelsius: Boolean = true,
        @SerializedName("temperature")
        val _temperature: String? = null
    ): Manipulative<WeatherData>, Parcelable {

        val weatherStateIcon
            get() = _weatherStateIcon.toIntOrNull() ?: WeatherStateIcon.UNKNOWN_ICON.id

        val temperature
            get() = _temperature?.toIntOrNull() ?: 0

        fun toWeatherData(): SmartspacerWeatherData {
            return SmartspacerWeatherData(
                contentDescription,
                WeatherStateIcon.fromId(weatherStateIcon),
                useCelsius,
                temperature
            )
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *_weatherStateIcon.getVariables(),
                *_temperature?.getVariables() ?: emptyArray()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): WeatherData {
            return copy(
                _weatherStateIcon = _weatherStateIcon.replace(replacements),
                _temperature = _temperature?.replace(replacements)
            )
        }

    }

    enum class TemplateType {
        BASIC
    }

}