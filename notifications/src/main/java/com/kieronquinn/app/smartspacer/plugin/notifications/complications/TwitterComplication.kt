package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import com.kieronquinn.app.smartspacer.plugin.notifications.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration.ConfigurationFragment
import android.graphics.drawable.Icon as AndroidIcon

class TwitterComplication: BaseBadgeComplication() {

    companion object {
        const val PACKAGE_NAME = "com.twitter.android"
    }

    override val packageName = PACKAGE_NAME
    override val idPrefix = "twitter"
    override val icon = R.drawable.ic_complication_twitter
    override val configurationConfig = ConfigurationFragment.Config.TWITTER

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = resources.getString(R.string.complication_twitter_label),
            description = resources.getString(R.string.complication_twitter_description),
            icon = AndroidIcon.createWithResource(provideContext(), icon),
            broadcastProvider = "${BuildConfig.APPLICATION_ID}.receivers.twitter",
            compatibilityState = getCompatibilityState(R.string.complication_twitter_incompatible),
            configActivity = getConfigIntent()
        )
    }

}