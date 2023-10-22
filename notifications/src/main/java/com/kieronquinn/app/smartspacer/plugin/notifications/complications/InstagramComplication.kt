package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import com.kieronquinn.app.smartspacer.plugin.notifications.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration.ConfigurationFragment
import android.graphics.drawable.Icon as AndroidIcon

class InstagramComplication: BaseBadgeComplication() {

    companion object {
        const val PACKAGE_NAME = "com.instagram.android"
    }

    override val packageName = PACKAGE_NAME
    override val idPrefix = "instagram"
    override val icon = R.drawable.ic_complication_instagram
    override val configurationConfig = ConfigurationFragment.Config.INSTAGRAM

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = resources.getString(R.string.complication_instagram_label),
            description = resources.getString(R.string.complication_instagram_description),
            icon = AndroidIcon.createWithResource(provideContext(), icon),
            broadcastProvider = "${BuildConfig.APPLICATION_ID}.receivers.instagram",
            compatibilityState = getCompatibilityState(R.string.complication_instagram_incompatible),
            configActivity = getConfigIntent()
        )
    }

}