package com.kieronquinn.app.smartspacer.plugins.yahoosport.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity
import com.kieronquinn.app.smartspacer.plugins.yahoosport.R

class ConfigurationActivity: BaseConfigurationActivity() {

    //Mapping of activity-aliases to their respective Nav Graph resources
    enum class NavGraphMapping(
        override val className: String,
        @NavigationRes override val graph: Int
    ): NavGraphRepository.NavGraphMapping {
        CONFIGURATION(
            ".ui.activities.YahooSportConfigurationActivity",
            R.navigation.nav_graph_configuration
        )
    }

}