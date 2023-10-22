package com.kieronquinn.app.smartspacer.plugin.googlefinance.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.googlefinance.R
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity

class ConfigurationActivity: BaseConfigurationActivity() {

    //Mapping of activity-aliases to their respective Nav Graph resources
    enum class NavGraphMapping(
        override val className: String,
        @NavigationRes override val graph: Int
    ): NavGraphRepository.NavGraphMapping {
        TARGET_GOOGLE_FINANCE(
            ".ui.activities.GoogleFinanceConfigurationActivity",
            R.navigation.nav_graph_configuration
        )
    }

}