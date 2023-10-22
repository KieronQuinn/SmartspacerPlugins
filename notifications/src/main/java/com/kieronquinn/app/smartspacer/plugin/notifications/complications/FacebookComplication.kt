package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import com.kieronquinn.app.smartspacer.plugin.notifications.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration.ConfigurationFragment
import android.graphics.drawable.Icon as AndroidIcon

class FacebookComplication: BaseBadgeComplication() {

    companion object {
        const val PACKAGE_NAME = "com.facebook.katana"
    }

    override val packageName = PACKAGE_NAME
    override val idPrefix = "facebook"
    override val icon = R.drawable.ic_complication_facebook
    override val configurationConfig = ConfigurationFragment.Config.FACEBOOK

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = resources.getString(R.string.complication_facebook_label),
            description = resources.getString(R.string.complication_facebook_description),
            icon = AndroidIcon.createWithResource(provideContext(), icon),
            broadcastProvider = "${BuildConfig.APPLICATION_ID}.receivers.facebook",
            compatibilityState = getCompatibilityState(R.string.complication_facebook_incompatible),
            configActivity = getConfigIntent()
        )
    }

}