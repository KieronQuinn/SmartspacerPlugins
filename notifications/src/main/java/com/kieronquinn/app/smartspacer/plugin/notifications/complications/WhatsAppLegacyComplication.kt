package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import com.kieronquinn.app.smartspacer.plugin.notifications.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration.ConfigurationFragment
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import android.graphics.drawable.Icon as AndroidIcon

class WhatsAppLegacyComplication: BaseBadgeComplication() {

    companion object {
        const val PACKAGE_NAME = "com.whatsapp"
    }

    override val packageName = PACKAGE_NAME
    override val idPrefix = "whatsapp"
    override val icon = R.drawable.ic_complication_whatsapp
    override val configurationConfig = ConfigurationFragment.Config.WHATSAPP

    override fun getConfig(smartspacerId: String?): Config {
        val whatsAppCompatibilityState =
            getCompatibilityState(R.string.complication_whatsapp_incompatible)
        val compatibilityState = if(whatsAppCompatibilityState == CompatibilityState.Compatible
            && smartspacerId != null) {
            //Already added, prompt the user to replace
            CompatibilityState.Compatible
        }else{
            //Not added, user will be prevented from adding
            CompatibilityState.Incompatible(
                resources.getString(R.string.complication_whatsapp_description_legacy)
            )
        }
        return Config(
            label = resources.getString(R.string.complication_whatsapp_label_deprecated),
            description = resources.getString(R.string.complication_whatsapp_description_deprecated),
            icon = AndroidIcon.createWithResource(provideContext(), icon),
            broadcastProvider = "${BuildConfig.APPLICATION_ID}.receivers.whatsapp",
            compatibilityState = compatibilityState,
            configActivity = getConfigIntent()
        )
    }

}