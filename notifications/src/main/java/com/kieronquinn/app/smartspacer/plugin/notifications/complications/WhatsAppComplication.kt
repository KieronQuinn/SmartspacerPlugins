package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import com.kieronquinn.app.smartspacer.plugin.notifications.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration.ConfigurationFragment
import android.graphics.drawable.Icon as AndroidIcon

class WhatsAppComplication: BaseBadgeComplication() {

    companion object {
        const val PACKAGE_NAME = "com.whatsapp"
    }

    override val packageName = PACKAGE_NAME
    override val idPrefix = "whatsapp"
    override val icon = R.drawable.ic_complication_whatsapp
    override val configurationConfig = ConfigurationFragment.Config.WHATSAPP

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = resources.getString(R.string.complication_whatsapp_label),
            description = resources.getString(R.string.complication_whatsapp_description),
            icon = AndroidIcon.createWithResource(provideContext(), icon),
            broadcastProvider = "${BuildConfig.APPLICATION_ID}.receivers.whatsapp",
            compatibilityState = getCompatibilityState(R.string.complication_whatsapp_incompatible),
            configActivity = getConfigIntent()
        )
    }

}