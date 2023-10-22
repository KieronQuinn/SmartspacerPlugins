package com.kieronquinn.app.smartspacer.plugin.aftership.targets

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.aftership.AftershipPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.aftership.R
import com.kieronquinn.app.smartspacer.plugin.aftership.model.database.Package
import com.kieronquinn.app.smartspacer.plugin.aftership.model.database.Package.Status
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.AftershipRepository
import com.kieronquinn.app.smartspacer.plugin.aftership.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.aftership.widgets.AftershipWidgetProvider
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.DoorbellState
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class AftershipTarget: SmartspacerTargetProvider() {

    companion object {
        private const val TARGET_PREFIX = "aftership_"
    }

    private val aftershipRepository by inject<AftershipRepository>()
    private val dataRepository by inject<DataRepository>()
    private val gson by inject<Gson>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val packages = aftershipRepository.getActivePackages()
        val config = getTargetData(smartspacerId)
        return packages.map {
            it.toTarget(config)
        }
    }

    override fun onProviderRemoved(smartspacerId: String) {
        super.onProviderRemoved(smartspacerId)
        dataRepository.deleteTargetData(smartspacerId)
    }

    override fun createBackup(smartspacerId: String): Backup {
        val targetData = getConfig(smartspacerId)
        return Backup(gson.toJson(targetData), resources.getString(R.string.target_description))
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val targetData = try {
            gson.fromJson(backup.data, TargetData::class.java)
        }catch (e: Exception){
            null
        } ?: return false
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE_AFTERSHIP,
            ::onRestoreComplete
        ) {
            targetData
        }
        return true
    }

    private fun onRestoreComplete(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    private fun Package.toTarget(config: TargetData): SmartspaceTarget {
        return when {
            status == Status.DELIVERED && image?.bitmap != null && config.showImage -> {
                toTargetWithImage(image.bitmap)
            }
            status == Status.DELIVERED -> {
                toTargetWithNoExtra()
            }
            map?.bitmap != null && config.showMap -> {
                toTargetWithMap(map.bitmap)
            }
            image?.bitmap != null && config.showImage -> {
                toTargetWithImage(image.bitmap)
            }
            else -> {
                toTargetWithNoExtra()
            }
        }
    }

    private fun Package.toTargetWithMap(map: Bitmap): SmartspaceTarget {
        return TargetTemplate.Image(
            provideContext(),
            getId(),
            ComponentName(provideContext(), this::class.java),
            SmartspaceTarget.FEATURE_COMMUTE_TIME,
            Text(title),
            getSubtitle(),
            getIcon(),
            Icon(AndroidIcon.createWithBitmap(map)),
            getTapAction()
        ).create()
    }

    private fun Package.toTargetWithImage(image: Bitmap): SmartspaceTarget {
        return TargetTemplate.Doorbell(
            getId(),
            ComponentName(provideContext(), this::class.java),
            SmartspaceTarget.FEATURE_DOORBELL,
            Text(title),
            getSubtitle(),
            getIcon(),
            DoorbellState.ImageBitmap(image),
            getTapAction()
        ).create()
    }

    private fun Package.toTargetWithNoExtra(): SmartspaceTarget {
        return TargetTemplate.Basic(
            getId(),
            ComponentName(provideContext(), this::class.java),
            SmartspaceTarget.FEATURE_UNDEFINED,
            Text(title),
            getSubtitle(),
            getIcon(),
            getTapAction()
        ).create()
    }

    private fun Package.getTapAction(): TapAction {
        val intent = aftershipRepository.getTrackingUrl(this)?.let {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(it)
                `package` = PACKAGE_NAME
            }
        } ?: run {
            provideContext().packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
        }
        return TapAction(intent = intent)
    }

    private fun Package.getSubtitle(): Text {
        return if(status == Status.DELIVERED) {
            //Usually when delivered the widget status is better & includes the time
            Text(state)
        }else{
            //Otherwise, use carrier tracking state if available
            Text(tracking?.title ?: state)
        }
    }

    private fun Package.getId(): String {
        return "$TARGET_PREFIX$id"
    }

    private fun Package.getIcon(): Icon {
        return Icon(AndroidIcon.createWithResource(provideContext(), status.icon))
    }

    override fun getConfig(smartspacerId: String?): Config {
        val targetData = smartspacerId?.let {
            getTargetData(it)
        } ?: TargetData()
        return Config(
            resources.getString(R.string.target_label),
            resources.getString(R.string.target_description),
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_aftership),
            widgetProvider = AftershipWidgetProvider.AUTHORITY,
            compatibilityState = getCompatibilityState(),
            configActivity = BaseConfigurationActivity.createIntent(
                provideContext(), NavGraphMapping.TARGET_AFTERSHIP
            ),
            refreshPeriodMinutes = if(targetData.enableUpdates) 60 else 0,
            refreshIfNotVisible = targetData.enableUpdates
        )
    }

    private fun getTargetData(smartspacerId: String): TargetData {
        return dataRepository.getTargetData(smartspacerId, TargetData::class.java) ?: TargetData()
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(AftershipWidgetProvider.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(resources.getString(R.string.target_incompatible))
        }else CompatibilityState.Compatible
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        val id = targetId.removePrefix(TARGET_PREFIX)
        aftershipRepository.dismissPackage(id)
        return true
    }

    data class TargetData(
        @SerializedName("show_image")
        val showImage: Boolean = true,
        @SerializedName("show_map")
        val showMap: Boolean = true,
        @SerializedName("enable_updates")
        val enableUpdates: Boolean = true
    ) {
        companion object {
            const val TYPE_AFTERSHIP = "aftership"
        }
    }

}