package com.kieronquinn.app.smartspacer.plugin.amazon.targets

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import com.kieronquinn.app.smartspacer.plugin.amazon.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Delivery
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Status
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.activities.ConfigurationActivity.NavGraphMapping.TARGET_AMAZON
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.PackagesFragment.Companion.setIsSetup
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.hasDisabledBatteryOptimisation
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.hasNotificationPermission
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.DoorbellState
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class AmazonTarget: SmartspacerTargetProvider() {

    companion object {
        private const val TARGET_ID_PREFIX = "amazon_"
    }

    private val amazonRepository by inject<AmazonRepository>()
    private val settings by inject<AmazonSettingsRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        if(!provideContext().hasDisabledBatteryOptimisation()
            || !provideContext().hasNotificationPermission()) {
            return listOf(getSetupRequiredTarget(smartspacerId))
        }
        return amazonRepository.getDeliveries().mapNotNull {
            it.loadTarget()
        }
    }

    private fun getSetupRequiredTarget(smartspacerId: String): SmartspaceTarget {
        return TargetTemplate.Basic(
            id = "amazon_setup_$smartspacerId",
            componentName = ComponentName(provideContext(), AmazonTarget::class.java),
            title = Text(resources.getString(R.string.target_amazon_setup_required_title)),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_amazon)),
            subtitle = Text(resources.getString(R.string.target_amazon_setup_required_subtitle)),
            onClick = TapAction(intent = createIntent(provideContext(), TARGET_AMAZON))
        ).create()
    }

    private fun Delivery.loadTarget(): SmartspaceTarget? {
        return when {
            isDismissed() -> null
            getBestStatus() != Status.DELIVERED && mapBitmap != null -> {
                createTargetWithMap(mapBitmap)
            }
            imageBitmap != null && settings.showProductImage.getSync() -> {
                createTargetWithImage(imageBitmap)
            }
            else -> createBasicTarget()
        }
    }

    private fun Delivery.getTapAction(): TapAction {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = amazonRepository.getClickUrl(this@getTapAction)
        }
        return TapAction(intent = intent)
    }

    private fun Delivery.createTargetWithImage(imageBitmap: Bitmap): SmartspaceTarget {
        return TargetTemplate.Doorbell(
            id = getId(),
            componentName = ComponentName(provideContext(), AmazonTarget::class.java),
            title = Text(name),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_amazon)),
            subtitle = Text(getSubtitle()),
            onClick = getTapAction(),
            doorbellState = DoorbellState.ImageBitmap(imageBitmap)
        ).create()
    }

    private fun Delivery.createTargetWithMap(mapBitmap: Bitmap): SmartspaceTarget {
        return TargetTemplate.Image(
            context = provideContext(),
            id = getId(),
            componentName = ComponentName(provideContext(), AmazonTarget::class.java),
            title = Text(name),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_amazon)),
            subtitle = Text(getSubtitle()),
            onClick = getTapAction(),
            image = Icon(AndroidIcon.createWithBitmap(mapBitmap))
        ).create()
    }

    private fun Delivery.createBasicTarget(): SmartspaceTarget {
        return TargetTemplate.Basic(
            id = getId(),
            componentName = ComponentName(provideContext(), AmazonTarget::class.java),
            title = Text(name),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_amazon)),
            subtitle = Text(getSubtitle()),
            onClick = getTapAction(),
        ).create()
    }

    private fun Delivery.getSubtitle(): String {
        val bestMessage = trackingStatus?.getBestMessage()
        return when {
            getBestStatus() == Status.DELIVERED -> {
                //We can't use the message in the target as it says "today" and will get stale
                resources.getString(R.string.target_amazon_status_delivered)
            }
            bestMessage != null -> bestMessage
            trackingData?.packageLocationDetails?.stopsRemaining == 0 -> {
                resources.getString(R.string.target_amazon_subtitle_stops_away_next)
            }
            trackingData?.packageLocationDetails?.stopsRemaining != null -> {
                resources.getQuantityString(
                    R.plurals.target_amazon_subtitle_stops_away,
                    trackingData.packageLocationDetails.stopsRemaining,
                    trackingData.packageLocationDetails.stopsRemaining
                )
            }
            else -> message
        }
    }

    private fun Delivery.getId(): String {
        return "${TARGET_ID_PREFIX}_at_${System.currentTimeMillis()}_$id"
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        amazonRepository.dismissDelivery(targetId.removePrefix())
        return true
    }

    private fun String.removePrefix(): String {
        if(!startsWith(TARGET_ID_PREFIX)) return this
        return substring(lastIndexOf("_") + 1)
    }

    override fun getConfig(smartspacerId: String?): Config {
        /**
         *  We only want to trigger the refresh intent more often if there's actually orders to
         *  refresh, otherwise the Target is effectively static, but we refresh it periodically
         *  just in case
         */
        val hasTrackable = amazonRepository.getDeliveries().any { it.canBeTracked() }
        val refreshPeriod = if(hasTrackable) 1 else 30
        return Config(
            label = resources.getString(R.string.target_label),
            description = resources.getString(R.string.target_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_amazon),
            setupActivity = createIntent(provideContext(), TARGET_AMAZON).setIsSetup(),
            configActivity = createIntent(provideContext(), TARGET_AMAZON),
            notificationProvider = "${BuildConfig.APPLICATION_ID}.notifications.amazon",
            refreshPeriodMinutes = refreshPeriod
        )
    }

}