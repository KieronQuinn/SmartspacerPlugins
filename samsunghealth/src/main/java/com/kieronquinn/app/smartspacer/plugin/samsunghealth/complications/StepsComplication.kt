package com.kieronquinn.app.smartspacer.plugin.samsunghealth.complications

import android.content.Intent
import android.net.Uri
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.R
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.SamsungHealthPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories.SamsungHealthSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.widgets.StepsWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class StepsComplication: SmartspacerComplicationProvider() {

    companion object {
        private const val DEEP_LINK_URL =
            "https://shealth.samsung.com/deepLink?sc_id=tracker.pedometer&action=view&destination=track&launch_dashboard=true"
    }

    private val settings by inject<SamsungHealthSettingsRepository>()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val steps = settings.steps.getSync()
        if(steps.isBlank()) return emptyList()
        val onClick = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(DEEP_LINK_URL)
            `package` = PACKAGE_NAME
        }
        return listOf(ComplicationTemplate.Basic(
            "samsung_health_steps_$smartspacerId",
            Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_complication_steps)),
            Text(steps),
            onClick = TapAction(intent = onClick)
        ).create())
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = resources.getString(R.string.complication_steps_title),
            description = resources.getString(R.string.complication_steps_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_complication_steps),
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.steps",
            compatibilityState = getCompatibilityState(),
            allowAddingMoreThanOnce = true
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(StepsWidgetProvider.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(resources.getString(R.string.complication_incompatible))
        }else CompatibilityState.Compatible
    }

}