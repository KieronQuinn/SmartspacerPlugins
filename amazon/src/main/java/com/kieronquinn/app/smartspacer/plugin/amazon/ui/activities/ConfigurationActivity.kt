package com.kieronquinn.app.smartspacer.plugin.amazon.ui.activities

import android.app.Activity
import android.content.Intent
import androidx.annotation.NavigationRes
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity

class ConfigurationActivity: BaseConfigurationActivity() {

    companion object {
        private const val KEY_IS_SETTINGS = "is_settings"

        fun setIsSettings(intent: Intent): Intent {
            intent.putExtra(KEY_IS_SETTINGS, true)
            return intent
        }

        fun getIsSettings(activity: Activity): Boolean {
            return activity.intent.getBooleanExtra(KEY_IS_SETTINGS, false)
        }
    }

    //Mapping of activity-aliases to their respective Nav Graph resources
    enum class NavGraphMapping(
        override val className: String,
        @NavigationRes override val graph: Int
    ): NavGraphRepository.NavGraphMapping {
        TARGET_AMAZON(
            ".ui.activities.AmazonConfigurationActivity",
            R.navigation.nav_graph_configure_target_amazon
        )
    }

}