package com.kieronquinn.app.smartspacer.plugins.bbcweather.complications

import android.app.PendingIntent
import androidx.core.text.util.LocalePreferences
import androidx.core.text.util.LocalePreferences.TemperatureUnit
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import com.kieronquinn.app.smartspacer.plugins.bbcweather.R
import com.kieronquinn.app.smartspacer.plugins.bbcweather.model.ComplicationState
import com.kieronquinn.app.smartspacer.plugins.bbcweather.receivers.ComplicationClickReceiver
import com.kieronquinn.app.smartspacer.plugins.bbcweather.repositories.BBCWeatherRepository
import com.kieronquinn.app.smartspacer.plugins.bbcweather.widgets.BBCWeatherComplicationWidget
import com.kieronquinn.app.smartspacer.sdk.annotations.LimitedNativeSupport
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.model.weather.WeatherData
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class BBCWeatherComplication: SmartspacerComplicationProvider() {

    private val bbcWeatherRepository by inject<BBCWeatherRepository>()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val state = bbcWeatherRepository.getComplicationState() ?: return emptyList()
        return listOf(state.toSmartspaceAction(smartspacerId))
    }

    @OptIn(LimitedNativeSupport::class)
    private fun ComplicationState.toSmartspaceAction(smartspacerId: String): SmartspaceAction {
        return ComplicationTemplate.Basic(
            "bbc_weather_${smartspacerId}_at_${System.currentTimeMillis()}",
            Icon(
                AndroidIcon.createWithBitmap(icon),
                shouldTint = false,
                contentDescription = contentDescription
            ),
            Text(temperature),
            smartspacerId.getTapAction()
        ).create().apply {
            weatherData = WeatherData(
                this@toSmartspaceAction.contentDescription,
                weatherStateIcon ?: return@apply,
                shouldUseCelsius(),
                temperature.parseTemperature() ?: return@apply
            )
        }
    }

    private fun String.parseTemperature(): Int? {
        return replace("°", "").toIntOrNull()
    }

    private fun shouldUseCelsius(): Boolean {
        return LocalePreferences.getTemperatureUnit() == TemperatureUnit.CELSIUS
    }

    private fun String.getTapAction(): TapAction {
        val intent = ComplicationClickReceiver.createIntent(provideContext(), this)
        val pendingIntent = PendingIntent.getBroadcast(
            provideContext(),
            hashCode(),
            intent,
            PendingIntent_MUTABLE_FLAGS
        )
        return TapAction(pendingIntent = pendingIntent)
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(R.string.complication_label),
            resources.getString(R.string.complication_description),
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_bbc_weather),
            widgetProvider = BBCWeatherComplicationWidget.AUTHORITY,
            compatibilityState = getCompatibilityState(),
            allowAddingMoreThanOnce = true
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(BBCWeatherComplicationWidget.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(resources.getString(R.string.complication_incompatible))
        }else CompatibilityState.Compatible
    }

}