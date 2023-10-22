package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.R

class ConfigurationActivity: BaseConfigurationActivity() {

    //Mapping of activity-aliases to their respective Nav Graph resources
    enum class NavGraphMapping(
        override val className: String,
        @NavigationRes override val graph: Int
    ): NavGraphRepository.NavGraphMapping {
        COMPLICATION_SUNRISE(
            ".ui.activities.SunriseConfigurationActivity",
            R.navigation.nav_graph_configuration_sunrise
        ),
        COMPLICATION_SUNSET(
            ".ui.activities.SunsetConfigurationActivity",
            R.navigation.nav_graph_configuration_sunset
        )
    }

}