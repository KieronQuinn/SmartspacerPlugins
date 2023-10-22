package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import com.kieronquinn.app.smartspacer.plugin.notifications.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration.ConfigurationFragment
import android.graphics.drawable.Icon as AndroidIcon

class ThreadsComplication: BaseBadgeComplication() {

    companion object {
        const val PACKAGE_NAME = "com.instagram.barcelona"
    }

    override val packageName = PACKAGE_NAME
    override val idPrefix = "threads"
    override val icon = R.drawable.ic_complication_threads
    override val configurationConfig = ConfigurationFragment.Config.THREADS

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = resources.getString(R.string.complication_threads_label),
            description = resources.getString(R.string.complication_threads_description),
            icon = AndroidIcon.createWithResource(provideContext(), icon),
            broadcastProvider = "${BuildConfig.APPLICATION_ID}.receivers.threads",
            compatibilityState = getCompatibilityState(R.string.complication_threads_incompatible),
            configActivity = getConfigIntent()
        )
    }

}