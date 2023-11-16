package com.kieronquinn.app.smartspacer.plugin.tasker.targets

import android.content.ComponentName
import android.graphics.drawable.Icon
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetDismissInput
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.TargetDismissEventActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.isTaskerInstalled
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import org.koin.android.ext.android.inject

class TaskerTarget: SmartspacerTargetProvider() {

    private val databaseRepository by inject<DatabaseRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val target = databaseRepository.getTargetSync(smartspacerId) ?: return emptyList()
        if(!target.isVisible) return emptyList()
        val current = target.current ?: return emptyList()
        val componentName = ComponentName(provideContext(), this::class.java)
        val id = "tasker_$smartspacerId"
        return listOf(current.toTarget(provideContext(), componentName, id))
    }

    override fun getConfig(smartspacerId: String?): Config {
        val target = smartspacerId?.let { databaseRepository.getTargetSync(it) }
        val description = target?.let {
            resources.getString(R.string.target_description, it.name)
        } ?: resources.getString(R.string.target_description_unset)
        return Config(
            resources.getString(R.string.target_label),
            description,
            Icon.createWithResource(provideContext(), R.drawable.ic_tasker),
            setupActivity = createIntent(provideContext(), NavGraphMapping.TARGET_SETUP),
            configActivity = createIntent(provideContext(), NavGraphMapping.TARGET_SETUP),
            refreshIfNotVisible = target?.refreshIfNotVisible ?: false,
            refreshPeriodMinutes = target?.refreshPeriod ?: 0,
            allowAddingMoreThanOnce = true,
            compatibilityState = getCompatibilityState()
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(!provideContext().isTaskerInstalled()) {
            CompatibilityState.Incompatible(resources.getString(R.string.target_incompatible))
        } else CompatibilityState.Compatible
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        val target = databaseRepository.getTargetSync(smartspacerId) ?: return false
        val canBeDismissed = target.current?.targetExtras?.canBeDismissed ?: false
        if(canBeDismissed){
            TargetDismissEventActivity::class.java.requestQuery(
                provideContext(), SmartspacerTargetDismissInput(smartspacerId)
            )
        }
        return canBeDismissed
    }

    override fun onProviderRemoved(smartspacerId: String) {
        databaseRepository.deleteTarget(smartspacerId)
    }

}