package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import com.kieronquinn.app.smartspacer.plugin.notifications.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class GoogleVoiceComplication: BaseComplication() {

    companion object {
        const val PACKAGE_NAME = "com.google.android.apps.googlevoice"
    }

    private val databaseRepository by inject<DatabaseRepository>()

    override val packageName = PACKAGE_NAME

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val badgeCount = databaseRepository.getBadgeCount(packageName)
        if(badgeCount == 0) return emptyList()
        return listOf(
            ComplicationTemplate.Basic(
                id = "google_voice_$smartspacerId",
                icon = Icon(
                    AndroidIcon.createWithResource(provideContext(), R.drawable.ic_complication_google_voice)
                ),
                content = Text(badgeCount.toString()),
                onClick = getLaunchIntent()?.let { TapAction(intent = it) }
            ).create())
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(R.string.complication_google_voice_label),
            resources.getString(R.string.complication_google_voice_description),
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_complication_google_voice),
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.googlevoice",
            compatibilityState = getCompatibilityState(R.string.complication_google_voice_incompatible)
        )
    }

}