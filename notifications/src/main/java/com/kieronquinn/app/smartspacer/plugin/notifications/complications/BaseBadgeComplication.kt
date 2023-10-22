package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration.ConfigurationFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration.ConfigurationFragment.Config as ConfigurationConfig

abstract class BaseBadgeComplication: BaseComplication() {

    abstract val idPrefix: String
    abstract val icon: Int
    abstract val configurationConfig: ConfigurationConfig

    private val databaseRepository by inject<DatabaseRepository>()

    final override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val badgeCount = databaseRepository.getBadgeCount(packageName)
        if(badgeCount == 0) return emptyList()
        return listOf(ComplicationTemplate.Basic(
            id = "${idPrefix}_$smartspacerId",
            icon = Icon(AndroidIcon.createWithResource(provideContext(), icon)),
            content = Text(badgeCount.toString()),
            onClick = getLaunchIntent()?.let { TapAction(intent = it) }
        ).create())
    }

    protected fun getConfigIntent(): Intent {
        return BaseConfigurationActivity.createIntent(
            provideContext(), NavGraphMapping.BADGE
        ).also {
            ConfigurationFragment.setConfig(it, configurationConfig)
        }
    }

}