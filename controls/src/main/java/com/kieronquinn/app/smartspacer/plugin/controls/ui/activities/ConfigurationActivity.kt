package com.kieronquinn.app.smartspacer.plugin.controls.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity

class ConfigurationActivity: BaseConfigurationActivity() {

    //Mapping of activity-aliases to their respective Nav Graph resources
    enum class NavGraphMapping(
        override val className: String,
        @NavigationRes override val graph: Int
    ): NavGraphRepository.NavGraphMapping {
        CONFIGURATION_COMPLICATION(
            ".ui.activities.ComplicationConfigurationActivity",
            R.navigation.nav_graph_configuration_complication
        ),
        CONFIGURATION_TARGET(
            ".ui.activities.TargetConfigurationActivity",
            R.navigation.nav_graph_configuration_target
        ),
        CONFIGURATION_REQUIREMENT(
            ".ui.activities.RequirementConfigurationActivity",
            R.navigation.nav_graph_configuration_requirement
        )
    }

}