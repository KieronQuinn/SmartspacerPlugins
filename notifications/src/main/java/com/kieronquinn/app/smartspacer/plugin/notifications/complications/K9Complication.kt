package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import android.graphics.drawable.Icon
import com.kieronquinn.app.smartspacer.plugin.notifications.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject

class K9Complication: BaseComplication() {

    companion object {
        const val PACKAGE_NAME = "com.fsck.k9"
    }

    private val databaseRepository by inject<DatabaseRepository>()

    override val packageName = PACKAGE_NAME

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val badgeCount = databaseRepository.getBadgeCount(packageName)
        if(badgeCount == 0) return emptyList()
        return listOf(
            ComplicationTemplate.Basic(
                id = "k9_$smartspacerId",
                icon = com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon(
                    Icon.createWithResource(
                        provideContext(), R.drawable.ic_complication_k9
                    )
                ),
                content = Text(badgeCount.toString()),
                onClick = getLaunchIntent()?.let { TapAction(intent = it) }
            ).create())
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(R.string.complication_k9_label),
            resources.getString(R.string.complication_k9_description),
            Icon.createWithResource(provideContext(), R.drawable.ic_complication_k9),
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.k9",
            compatibilityState = getCompatibilityState(R.string.complication_k9_incompatible),
        )
    }

}