package com.kieronquinn.app.smartspacer.plugin.tasker.requirements

import android.graphics.drawable.Icon
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Requirement
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerRequirementProvider
import org.koin.android.ext.android.inject

class TaskerRequirement: SmartspacerRequirementProvider() {

    private val databaseRepository by inject<DatabaseRepository>()

    override fun isRequirementMet(smartspacerId: String): Boolean {
        return getRequirement(smartspacerId)?.isMet ?: false
    }

    override fun getConfig(smartspacerId: String?): Config {
        val data = smartspacerId?.let { getRequirement(it) }
        val description = if(data?.name != null){
            resources.getString(R.string.requirement_description, data.name)
        }else{
            resources.getString(R.string.requirement_description_unset)
        }
        return Config(
            resources.getString(R.string.requirement_label),
            description,
            Icon.createWithResource(provideContext(), R.drawable.ic_tasker),
            configActivity = createIntent(provideContext(), NavGraphMapping.REQUIREMENT_SETUP)
        )
    }

    private fun getRequirement(smartspacerId: String): Requirement? {
        return databaseRepository.getRequirementSync(smartspacerId)
    }

}