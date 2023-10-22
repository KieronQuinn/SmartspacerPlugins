package com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities

import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.R

open class ConfigurationActivity: BaseConfigurationActivity() {

    //Mapping of activity-aliases to their respective Nav Graph resources
    enum class NavGraphMapping(
        override val className: String,
        @NavigationRes override val graph: Int
    ): NavGraphRepository.NavGraphMapping {
        TARGET_SETUP(
            ".ui.activities.TargetSetup",
            R.navigation.nav_graph_target_setup
        ),
        TARGET_CONFIGURATION(
            ".ui.activities.TargetConfigurationActivity",
            R.navigation.nav_graph_target_configuration
        ),
        TARGET_VISIBILITY(
            ".ui.activities.TargetVisibilityActivity",
            R.navigation.nav_graph_target_visibility
        ),
        TAP_ACTION_EVENT(
            ".ui.activities.TapActionEventActivity",
            R.navigation.nav_graph_tap_action_event
        ),
        TARGET_DISMISSED(
            ".ui.activities.TargetDismissEventActivity",
            R.navigation.nav_graph_target_dismiss_event
        ),
        TARGET_UPDATED(
            ".ui.activities.TargetUpdateEventActivity",
            R.navigation.nav_graph_target_update_event
        ),
        COMPLICATION_SETUP(
            ".ui.activities.ComplicationSetup",
            R.navigation.nav_graph_complication_setup
        ),
        COMPLICATION_CONFIGURATION(
            ".ui.activities.ComplicationConfigurationActivity",
            R.navigation.nav_graph_complication_configuration
        ),
        COMPLICATION_VISIBILITY(
            ".ui.activities.ComplicationVisibilityActivity",
            R.navigation.nav_graph_complication_visibility
        ),
        COMPLICATION_UPDATED(
            ".ui.activities.ComplicationUpdateEventActivity",
            R.navigation.nav_graph_complication_update_event
        ),
        REQUIREMENT_SETUP(
            ".ui.activities.RequirementSetup",
            R.navigation.nav_graph_requirement_setup
        ),
        REQUIREMENT_UPDATE(
            ".ui.activities.RequirementUpdateActivity",
            R.navigation.nav_graph_requirement_update
        )
    }

}