package com.kieronquinn.app.smartspacer.plugin.googlehome.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.googlehome.R
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity

class ConfigurationActivity: BaseConfigurationActivity() {

    //Mapping of activity-aliases to their respective Nav Graph resources
    enum class NavGraphMapping(
        override val className: String,
        @NavigationRes override val graph: Int
    ): NavGraphRepository.NavGraphMapping {
        TARGET(
            ".ui.activities.GoogleHomeTargetConfigurationActivity",
            R.navigation.nav_graph_target
        ),
        COMPLICATION(
            ".ui.activities.GoogleHomeComplicationConfigurationActivity",
            R.navigation.nav_graph_complication
        )
    }

}