package com.kieronquinn.app.smartspacer.plugin.tasker.complications

import android.graphics.drawable.Icon
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.ConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.isTaskerInstalled
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import org.koin.android.ext.android.inject

class TaskerComplication: SmartspacerComplicationProvider() {

    private val databaseRepository by inject<DatabaseRepository>()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val complication = databaseRepository.getComplicationSync(smartspacerId) ?: return emptyList()
        if(!complication.isVisible) return emptyList()
        val current = complication.current ?: return emptyList()
        val id = "tasker_$smartspacerId"
        return listOf(current.toComplication(provideContext(), id))
    }

    override fun getConfig(smartspacerId: String?): Config {
        val complication = smartspacerId?.let { databaseRepository.getComplicationSync(it) }
        val description = complication?.let {
            resources.getString(R.string.complication_description, it.name)
        } ?: resources.getString(R.string.complication_description_unset)
        return Config(
            resources.getString(R.string.complication_label),
            description,
            Icon.createWithResource(provideContext(), R.drawable.ic_tasker),
            setupActivity = BaseConfigurationActivity.createIntent(
                provideContext(),
                ConfigurationActivity.NavGraphMapping.COMPLICATION_SETUP
            ),
            configActivity = BaseConfigurationActivity.createIntent(
                provideContext(),
                ConfigurationActivity.NavGraphMapping.COMPLICATION_SETUP
            ),
            refreshIfNotVisible = complication?.refreshIfNotVisible ?: false,
            refreshPeriodMinutes = complication?.refreshPeriod ?: 0,
            allowAddingMoreThanOnce = true,
            compatibilityState = getCompatibilityState()
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(!provideContext().isTaskerInstalled()) {
            CompatibilityState.Incompatible(resources.getString(R.string.complication_incompatible))
        } else CompatibilityState.Compatible
    }

    override fun onProviderRemoved(smartspacerId: String) {
        databaseRepository.deleteComplication(smartspacerId)
    }

}