package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.notifications.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.TelegramRepository
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.activities.TelegramWidgetReconfigureActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class TelegramComplication: BaseComplication() {

    private val databaseRepository by inject<DatabaseRepository>()
    private val telegramRepository by inject<TelegramRepository>()

    override val packageName = telegramRepository.getTelegramPackageName()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val badgeCount = databaseRepository.getBadgeCount(packageName)
        if(badgeCount == 0) return emptyList()
        return listOf(
            ComplicationTemplate.Basic(
            id = "telegram_$smartspacerId",
            icon = Icon(
                AndroidIcon.createWithResource(provideContext(), R.drawable.ic_complication_telegram)
            ),
            content = Text(badgeCount.toString()),
            onClick = getLaunchIntent()?.let { TapAction(intent = it) }
        ).create())
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(R.string.complication_telegram_label),
            resources.getString(R.string.complication_telegram_description),
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_complication_telegram),
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.telegram",
            compatibilityState = getCompatibilityState(R.string.complication_telegram_incompatible),
            configActivity = Intent(provideContext(), TelegramWidgetReconfigureActivity::class.java)
        )
    }

}