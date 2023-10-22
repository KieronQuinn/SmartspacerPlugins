package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.googlewallet.R
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity

class ConfigurationActivity: BaseConfigurationActivity() {

    //Mapping of activity-aliases to their respective Nav Graph resources
    enum class NavGraphMapping(
        override val className: String,
        @NavigationRes override val graph: Int
    ): NavGraphRepository.NavGraphMapping {
        TARGET_WALLET_STATIC(
            ".ui.activities.StaticConfigurationActivity",
            R.navigation.nav_graph_configuration_static
        ),
        TARGET_WALLET_DYNAMIC(
            ".ui.activities.DynamicConfigurationActivity",
            R.navigation.nav_graph_configuration_dynamic
        )
    }

}