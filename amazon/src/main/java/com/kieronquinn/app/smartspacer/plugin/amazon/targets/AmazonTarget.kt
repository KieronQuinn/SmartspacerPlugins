package com.kieronquinn.app.smartspacer.plugin.amazon.targets

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.kieronquinn.app.smartspacer.plugin.amazon.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Delivery
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Status
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.activities.ConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.activities.ConfigurationActivity.NavGraphMapping.TARGET_AMAZON
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
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.target.amazon"
        private const val TARGET_ID_PREFIX = "amazon_"
    }

    private val amazonRepository by inject<AmazonRepository>()
    private val settings by inject<AmazonSettingsRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val isSignedIn = amazonRepository.isSignedIn()
        if(!isSignedIn) return listOf(createSignInTarget(smartspacerId))
        return amazonRepository.getDeliveries().mapNotNull {
            it.loadTarget()
        }
    }

    private fun Delivery.loadTarget(): SmartspaceTarget? {
        if(dismissedAtStatus == status) return null //Item has been dismissed
        return when {
            getBestStatus() != Status.DELIVERED && mapBitmap != null -> {
                createTargetWithMap(mapBitmap)
            }
            imageBitmap != null && settings.showProductImage.getSync() -> {
                createTargetWithImage(imageBitmap)
            }
            else -> createBasicTarget()
        }
    }

    private fun Delivery.getTapAction(): TapAction? {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = amazonRepository.getClickUrl(this@getTapAction)?.let {
                Uri.parse(it)
            } ?: return null
        }
        return TapAction(intent = intent)
    }

    private fun createSignInTarget(smartspacerId: String): SmartspaceTarget {
        val configIntent = createIntent(provideContext(), TARGET_AMAZON).also {
            ConfigurationActivity.setIsSettings(it)
        }
        return TargetTemplate.Basic(
            id = "${smartspacerId}_amazon_sign_in",
            componentName = ComponentName(provideContext(), AmazonTarget::class.java),
            title = Text(resources.getString(R.string.target_amazon_title_signed_out)),
            subtitle = Text(resources.getString(R.string.target_amazon_subtitle_signed_out)),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_amazon)),
            onClick = TapAction(intent = configIntent)
        ).create()
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
        return when {
            getBestStatus() == Status.DELIVERED -> {
                message.ifBlank {
                    resources.getString(R.string.target_amazon_status_delivered)
                }
            }
            trackingData?.stopsRemaining == 0 -> {
                resources.getString(R.string.target_amazon_subtitle_stops_away_next)
            }
            trackingData?.stopsRemaining != null -> {
                resources.getQuantityString(
                    R.plurals.target_amazon_subtitle_stops_away,
                    trackingData.stopsRemaining,
                    trackingData.stopsRemaining
                )
            }
            else -> resources.getString(status.content)
        }
    }

    private fun Delivery.getId(): String {
        return "${TARGET_ID_PREFIX}_at_${System.currentTimeMillis()}_$shipmentId"
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        amazonRepository.dismissDelivery(targetId.removePrefix(), smartspacerId)
        return true
    }

    private fun String.removePrefix(): String {
        if(!startsWith(TARGET_ID_PREFIX)) return this
        return substring(lastIndexOf("_") + 1)
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = resources.getString(R.string.target_amazon_title),
            description = resources.getString(R.string.target_amazon_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_amazon),
            setupActivity = createIntent(provideContext(), TARGET_AMAZON),
            configActivity = createIntent(provideContext(), TARGET_AMAZON).also {
                ConfigurationActivity.setIsSettings(it)
            },
            notificationProvider = "${BuildConfig.APPLICATION_ID}.notifications.amazon",
            refreshPeriodMinutes = 1
        )
    }

}