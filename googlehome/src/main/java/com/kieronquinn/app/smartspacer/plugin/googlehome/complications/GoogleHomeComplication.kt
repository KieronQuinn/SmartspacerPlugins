package com.kieronquinn.app.smartspacer.plugin.googlehome.complications

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.googlehome.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.googlehome.GoogleHomePlugin
import com.kieronquinn.app.smartspacer.plugin.googlehome.GoogleHomePlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.googlehome.R
import com.kieronquinn.app.smartspacer.plugin.googlehome.model.HideMode
import com.kieronquinn.app.smartspacer.plugin.googlehome.repositories.GoogleHomeRepository
import com.kieronquinn.app.smartspacer.plugin.googlehome.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.googlehome.widgets.GoogleHomeWidget
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class GoogleHomeComplication: SmartspacerComplicationProvider() {

    private val dataRepository by inject<DataRepository>()
    private val googleHomeRepository by inject<GoogleHomeRepository>()
    private val gson by inject<Gson>()

    private val packageManager by lazy {
        provideContext().packageManager
    }

    private val defaultIcon by lazy {
        AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_home)
    }

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val googleHomeContext = GoogleHomePlugin.getGoogleHomeContext(provideContext())
            ?: return emptyList()
        val items = googleHomeRepository.getItems(smartspacerId)
        val config = getConfiguration(smartspacerId)
        return items.mapIndexedNotNull { index, item ->
            val shouldShow = when (config.hideMode) {
                HideMode.DISABLED -> true
                HideMode.WHEN_ON -> item.on != null && !item.on
                HideMode.WHEN_OFF -> item.on != null && item.on
            }
            if (!shouldShow) return@mapIndexedNotNull null
            val text = if (config.showSubtitle && item.subtitle != null) {
                resources.getString(
                    R.string.complication_google_home_bullet,
                    item.title,
                    item.subtitle
                )
            } else item.title
            val iconRes = googleHomeContext.resources.getIdentifier(
                item.icon, "drawable", googleHomeContext.packageName
            ).takeIf { it > 0 }
            val icon = if (iconRes != null) {
                AndroidIcon.createWithResource(PACKAGE_NAME, iconRes)
            } else defaultIcon
            val tapAction = if (item.click != null) {
                TapAction(pendingIntent = item.click)
            } else {
                TapAction(intent = packageManager.getLaunchIntentForPackage(PACKAGE_NAME))
            }
            ComplicationTemplate.Basic(
                id = "${smartspacerId}_${index}_${item.icon}",
                content = Text(text),
                icon = Icon(icon),
                onClick = tapAction,
            ).create()
        }
    }

    override fun getConfig(smartspacerId: String?): Config {
        val description = getDescription(smartspacerId)
        return Config(
            label = resources.getString(R.string.shared_google_home_title),
            description = description,
            icon = defaultIcon,
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.home",
            compatibilityState = getCompatibilityState(),
            configActivity = getConfigIntent(),
            allowAddingMoreThanOnce = true
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(GoogleHomeWidget.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(
                resources.getString(R.string.complication_google_home_incompatible)
            )
        }else CompatibilityState.Compatible
    }

    override fun createBackup(smartspacerId: String): Backup {
        val config = getConfiguration(smartspacerId)
        return Backup(gson.toJson(config))
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val config = try {
            gson.fromJson(backup.data, ComplicationData::class.java) ?: return false
        }catch (e: Exception) {
            return false
        }
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onChanged
        ) {
            ComplicationData(
                hideMode = config.hideMode,
                showSubtitle = config.showSubtitle
            )
        }
        return true
    }

    private fun getDescription(smartspacerId: String?): String {
        val itemCount = smartspacerId?.let { googleHomeRepository.getItems(it) }
            ?.size
            ?.takeIf { it > 0 }
            ?: return resources.getString(R.string.shared_google_home_description_unset)
        return resources.getQuantityString(
            R.plurals.shared_google_home_description,
            itemCount,
            itemCount
        )
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        notifyChange(context, this::class.java, smartspacerId)
    }

    private fun getConfiguration(smartspacerId: String): ComplicationData {
        return dataRepository.getComplicationData(smartspacerId, ComplicationData::class.java)
            ?: ComplicationData()
    }

    private fun getConfigIntent() = createIntent(provideContext(), NavGraphMapping.COMPLICATION)

    data class ComplicationData(
        @SerializedName("hide_mode")
        val hideMode: HideMode = HideMode.DISABLED,
        @SerializedName("show_subtitle")
        val showSubtitle: Boolean = false
    ) {

        companion object {
            const val TYPE = "home"
        }

    }

}