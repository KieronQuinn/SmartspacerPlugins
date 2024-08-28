package com.kieronquinn.app.smartspacer.plugins.bbcweather.targets

import android.app.PendingIntent
import android.content.ComponentName
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import com.kieronquinn.app.smartspacer.plugins.bbcweather.R
import com.kieronquinn.app.smartspacer.plugins.bbcweather.model.TargetState
import com.kieronquinn.app.smartspacer.plugins.bbcweather.receivers.TargetClickReceiver
import com.kieronquinn.app.smartspacer.plugins.bbcweather.repositories.BBCWeatherRepository
import com.kieronquinn.app.smartspacer.plugins.bbcweather.widgets.BBCWeatherTargetWidget
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.CarouselTemplateData.CarouselItem
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class BBCWeatherTarget: SmartspacerTargetProvider() {

    private val bbcWeatherRepository by inject<BBCWeatherRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val state = bbcWeatherRepository.getTargetState() ?: return emptyList()
        return listOf(state.toTarget(smartspacerId))
    }

    private fun TargetState.toTarget(smartspacerId: String): SmartspaceTarget {
        val tapAction = smartspacerId.getTapAction()
        return TargetTemplate.Carousel(
            id = "bbc_weather_${smartspacerId}_at_${System.currentTimeMillis()}",
            componentName = ComponentName(provideContext(), BBCWeatherTarget::class.java),
            icon = Icon(
                AndroidIcon.createWithBitmap(icon),
                shouldTint = false,
                contentDescription = contentDescription
            ),
            title = Text(location),
            subtitle = Text("$temperature $contentDescription"),
            items = toCarouselItems(tapAction),
            onClick = tapAction,
            onCarouselClick = tapAction,
            subComplication = ComplicationTemplate.blank().create()
        ).create().apply {
            canBeDismissed = false
        }
    }

    private fun TargetState.toCarouselItems(tapAction: TapAction): List<CarouselItem> {
        return items.map {
            CarouselItem(
                Text(it.temperature),
                Text(" ${it.day} "),
                Icon(
                    AndroidIcon.createWithBitmap(it.icon),
                    shouldTint = false,
                    contentDescription = contentDescription
                ),
                tapAction
            )
        }
    }

    private fun String.getTapAction(): TapAction {
        val intent = TargetClickReceiver.createIntent(provideContext(), this)
        val pendingIntent = PendingIntent.getBroadcast(
            provideContext(),
            hashCode(),
            intent,
            PendingIntent_MUTABLE_FLAGS
        )
        return TapAction(pendingIntent = pendingIntent)
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(R.string.target_label),
            resources.getString(R.string.target_description),
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_bbc_weather),
            widgetProvider = BBCWeatherTargetWidget.AUTHORITY,
            compatibilityState = getCompatibilityState(),
            allowAddingMoreThanOnce = true
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(BBCWeatherTargetWidget.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(resources.getString(R.string.target_incompatible))
        }else CompatibilityState.Compatible
    }
    
}