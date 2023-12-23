package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import android.content.Intent
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.notifications.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.activities.WidgetReconfigureActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class OutlookComplication: BaseComplication() {

    companion object {
        const val PACKAGE_NAME = "com.microsoft.office.outlook"
    }

    private val databaseRepository by inject<DatabaseRepository>()

    override val packageName = PACKAGE_NAME

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val badgeCount = databaseRepository.getBadgeCount(packageName)
        if(badgeCount == 0) return emptyList()
        return listOf(
            ComplicationTemplate.Basic(
                id = "outlook_$smartspacerId",
                icon = Icon(
                    AndroidIcon.createWithResource(
                        provideContext(), R.drawable.ic_complication_outlook
                    )
                ),
                content = Text(badgeCount.toString()),
                onClick = getLaunchIntent()?.let { TapAction(intent = it) }
            ).create())
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(R.string.complication_outlook_label),
            resources.getString(R.string.complication_outlook_description),
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_complication_outlook),
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.outlook",
            compatibilityState = getCompatibilityState(R.string.complication_outlook_incompatible),
            configActivity = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Intent(provideContext(), WidgetReconfigureActivity::class.java)
            }else null
        )
    }

}