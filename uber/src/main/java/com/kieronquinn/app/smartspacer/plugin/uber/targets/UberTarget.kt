package com.kieronquinn.app.smartspacer.plugin.uber.targets

import android.content.ComponentName
import android.content.Context
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugin.uber.R
import com.kieronquinn.app.smartspacer.plugin.uber.UberPlugin.Companion.isAppInstalled
import com.kieronquinn.app.smartspacer.plugin.uber.model.TargetData
import com.kieronquinn.app.smartspacer.plugin.uber.notifications.UberNotificationProvider
import com.kieronquinn.app.smartspacer.plugin.uber.repositories.NotificationRepository
import com.kieronquinn.app.smartspacer.plugin.uber.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class UberTarget: SmartspacerTargetProvider() {

    private val notificationRepository by inject<NotificationRepository>()
    private val dataRepository by inject<DataRepository>()
    private val gson by inject<Gson>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val config = getConfig(smartspacerId)
        return notificationRepository.getTargetData().map {
            it.toTarget(smartspacerId, config)
        }
    }

    override fun createBackup(smartspacerId: String): Backup {
        val config = getConfig(smartspacerId)
        return Backup(
            gson.toJson(config),
            resources.getString(R.string.target_description)
        )
    }

    override fun onProviderRemoved(smartspacerId: String) {
        super.onProviderRemoved(smartspacerId)
        dataRepository.deleteTargetData(smartspacerId)
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val config = try {
            gson.fromJson(backup.data, UberTargetData::class.java)
        }catch (e: Exception) {
            null
        } ?: return false
        dataRepository.updateTargetData(
            smartspacerId,
            UberTargetData::class.java,
            UberTargetData.TYPE_UBER,
            ::onRestoreComplete
        ) {
            config
        }
        return true
    }

    private fun onRestoreComplete(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    private fun TargetData.toTarget(
        smartspacerId: String,
        config: UberTargetData
    ): SmartspaceTarget {
        return if(config.showExpandedInfo) {
            toTargetWithImage(smartspacerId)
        } else{
            toTargetBasic(smartspacerId)
        }.apply {
            if(config.hideNotification) {
                sourceNotificationKey = notification.key
            }
        }
    }

    private fun TargetData.toTargetWithImage(smartspacerId: String): SmartspaceTarget {
        return TargetTemplate.Image(
            provideContext(),
            "${smartspacerId}_${notification.id}",
            ComponentName(provideContext(), UberTarget::class.java),
            SmartspaceTarget.FEATURE_COMMUTE_TIME,
            Text(title),
            Text(subtitle),
            Icon(icon),
            Icon(AndroidIcon.createWithBitmap(image)),
            TapAction(pendingIntent = notification.notification.contentIntent)
        ).create()
    }

    private fun TargetData.toTargetBasic(smartspacerId: String): SmartspaceTarget {
        return TargetTemplate.Basic(
            "${smartspacerId}_${notification.id}",
            ComponentName(provideContext(), UberTarget::class.java),
            SmartspaceTarget.FEATURE_UNDEFINED,
            Text(title),
            Text(subtitle),
            Icon(icon),
            TapAction(pendingIntent = notification.notification.contentIntent)
        ).create()
    }

    private fun getConfig(smartspacerId: String): UberTargetData {
        return dataRepository.getTargetData(smartspacerId, UberTargetData::class.java)
            ?: UberTargetData()
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(R.string.target_label),
            resources.getString(R.string.target_description),
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_uber),
            notificationProvider = UberNotificationProvider.AUTHORITY,
            configActivity = createIntent(provideContext(), NavGraphMapping.TARGET_UBER),
            compatibilityState = getCompatibilityState()
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(!isAppInstalled(provideContext())) {
            CompatibilityState.Incompatible(resources.getString(R.string.target_incompatible))
        }else CompatibilityState.Compatible
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        val id = targetId.removePrefix("${smartspacerId}_")
        return notificationRepository.dismissNotification(id)
    }

    data class UberTargetData(
        val showExpandedInfo: Boolean = true,
        val hideNotification: Boolean = true
    ) {

        companion object {
            const val TYPE_UBER = "uber"
        }

    }

}