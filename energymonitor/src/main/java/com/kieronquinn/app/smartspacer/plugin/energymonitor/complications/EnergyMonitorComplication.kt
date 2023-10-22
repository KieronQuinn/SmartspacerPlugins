package com.kieronquinn.app.smartspacer.plugin.energymonitor.complications

import android.app.PendingIntent
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.energymonitor.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.energymonitor.R
import com.kieronquinn.app.smartspacer.plugin.energymonitor.receivers.EnergyMonitorClickReceiver.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugin.energymonitor.repositories.StateRepository
import com.kieronquinn.app.smartspacer.plugin.energymonitor.ui.activities.ReconfigureTrampolineActivity
import com.kieronquinn.app.smartspacer.plugin.energymonitor.widgets.EnergyMonitorWidget
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class EnergyMonitorComplication: SmartspacerComplicationProvider() {

    private val stateRepository by inject<StateRepository>()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val state = stateRepository.getState(smartspacerId) ?: return emptyList()
        return listOf(
            ComplicationTemplate.Basic(
                "energy_monitor_${state.deviceName}_$smartspacerId",
                Icon(AndroidIcon.createWithBitmap(state.icon)),
                Text(state.getContent()),
                onClick = TapAction(pendingIntent = getClickPendingIntent(smartspacerId))
            ).create()
        )
    }

    private fun StateRepository.State.getContent(): String {
        return if(isCharging){
            resources.getString(R.string.complication_charging, batteryLevel)
        }else batteryLevel
    }

    private fun getClickPendingIntent(smartspacerId: String): PendingIntent {
        val intent = createIntent(provideContext(), smartspacerId)
        return PendingIntent.getBroadcast(
            provideContext(),
            smartspacerId.hashCode(),
            intent,
            PendingIntent_MUTABLE_FLAGS
        )
    }

    override fun onProviderRemoved(smartspacerId: String) {
        stateRepository.deleteState(smartspacerId)
    }

    override fun getConfig(smartspacerId: String?): Config {
        val state = smartspacerId?.let { stateRepository.getState(it) }
        val description = if(state != null){
            resources.getString(R.string.complication_description, state.deviceName)
        }else{
            resources.getString(R.string.complication_description_unset)
        }
        return Config(
            resources.getString(R.string.complication_title),
            description,
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_energy_monitor),
            compatibilityState = getCompatibilityState(),
            allowAddingMoreThanOnce = true,
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.energymonitor",
            configActivity = Intent(provideContext(), ReconfigureTrampolineActivity::class.java)
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(EnergyMonitorWidget.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(resources.getString(R.string.complication_incompatible))
        }else CompatibilityState.Compatible
    }

}