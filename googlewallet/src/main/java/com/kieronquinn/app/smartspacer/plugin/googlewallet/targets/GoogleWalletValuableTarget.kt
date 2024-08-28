package com.kieronquinn.app.smartspacer.plugin.googlewallet.targets

import android.content.ComponentName
import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.googlewallet.R
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleApiRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.activities.ConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.activities.ConfigurationActivity.NavGraphMapping.TARGET_WALLET_STATIC
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.activities.WalletLaunchProxyActivity
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.popup.PopupWalletDialogFragment
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toBitmap
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class GoogleWalletValuableTarget: SmartspacerTargetProvider() {

    private val dataRepository by inject<DataRepository>()
    private val walletRepository by inject<GoogleWalletRepository>()
    private val googleApiRepository by inject<GoogleApiRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        getSignInTarget(smartspacerId)?.let {
            return listOf(it)
        }
        val settings = getSettings(smartspacerId) ?: return emptyList()
        //If the valuables haven't yet loaded, don't show the empty message
        val target = if(walletRepository.hasLoadedValuables()) {
            getValuable(smartspacerId)?.toTarget(settings) ?: getEmptyTarget(smartspacerId)
        }else null
        return listOfNotNull(target)
    }

    private fun getSignInTarget(smartspacerId: String): SmartspaceTarget? {
        if(googleApiRepository.isSignedIn()) return null
        val onClick = TapAction(
            intent = BaseConfigurationActivity.createIntent(
                provideContext(), ConfigurationActivity.NavGraphMapping.TARGET_WALLET_STATIC
            )
        )
        return TargetTemplate.Basic(
            "${smartspacerId}_sign_in",
            ComponentName(provideContext(), GoogleWalletDynamicTarget::class.java),
            SmartspaceTarget.FEATURE_UNDEFINED,
            Text(provideContext().getString(R.string.target_sign_in_again_title)),
            Text(provideContext().getString(R.string.target_sign_in_again_subtitle)),
            Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet)),
            onClick
        ).create()
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false //Can't be dismissed
    }

    private fun getSettings(smartspacerId: String): TargetData? {
        return dataRepository.getTargetData(smartspacerId, TargetData::class.java)
    }

    override fun onProviderRemoved(smartspacerId: String) {
        super.onProviderRemoved(smartspacerId)
        dataRepository.deleteTargetData(smartspacerId)
    }

    private fun getValuable(smartspacerId: String): GoogleWalletRepository.Valuable? {
        val id = getSettings(smartspacerId)?.valuableId ?: return null
        return walletRepository.getValuableById(id)
    }

    private fun GoogleWalletRepository.Valuable.toTarget(settings: TargetData): SmartspaceTarget? {
        val image = cardImage?.toBitmap()
        return if(image != null && settings.showCardImage){
            toTargetWithImage(image, settings)
        }else{
            toTargetWithoutImage(settings)
        }
    }

    private fun GoogleWalletRepository.Valuable.toTargetWithImage(
        image: Bitmap,
        settings: TargetData
    ): SmartspaceTarget? {
        val groupingInfo = getGroupingInfo() ?: return null
        return TargetTemplate.Image(
            provideContext(),
            id,
            ComponentName(provideContext(), GoogleWalletValuableTarget::class.java),
            title = Text(groupingInfo.groupingTitle),
            subtitle = Text(groupingInfo.groupingSubtitle),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet)),
            image = Icon(AndroidIcon.createWithBitmap(image)),
            onClick = getCardClickAction(settings.showAsPopup, settings.lockOrientation, settings.popUnder)
        ).create()
    }

    private fun GoogleWalletRepository.Valuable.toTargetWithoutImage(
        settings: TargetData
    ): SmartspaceTarget? {
        val groupingInfo = getGroupingInfo() ?: return null
        return TargetTemplate.Basic(
            id,
            ComponentName(provideContext(), GoogleWalletValuableTarget::class.java),
            title = Text(groupingInfo.groupingTitle),
            subtitle = Text(groupingInfo.groupingSubtitle),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet)),
            onClick = getCardClickAction(settings.showAsPopup, settings.lockOrientation, settings.popUnder)
        ).create()
    }

    private fun getEmptyTarget(id: String): SmartspaceTarget {
        val onClick = TapAction(
            intent = BaseConfigurationActivity.createIntent(
                provideContext(), ConfigurationActivity.NavGraphMapping.TARGET_WALLET_STATIC
            )
        )
        return TargetTemplate.Basic(
            id,
            ComponentName(provideContext(), GoogleWalletValuableTarget::class.java),
            title = Text(resources.getString(R.string.target_wallet_valuable_not_found_title)),
            subtitle = Text(resources.getString(R.string.target_wallet_valuable_not_found_subtitle)),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet)),
            onClick = onClick
        ).create()
    }

    private fun GoogleWalletRepository.Valuable.getCardClickAction(
        showAsPopup: Boolean,
        lockOrientation: Boolean,
        popUnder: Boolean
    ): TapAction {
        val intent = if(showAsPopup){
            PopupWalletDialogFragment.createLaunchIntent(provideContext(), id, lockOrientation, popUnder)
        }else{
            WalletLaunchProxyActivity.createIntent(provideContext(), id, popUnder)
        }
        return TapAction(
            intent = intent,
            shouldShowOnLockScreen = true
        )
    }

    override fun getConfig(smartspacerId: String?): Config {
        val valuableName = smartspacerId?.let { getSettings(it) }?.valuableName
        val description = if(valuableName != null){
            resources.getString(R.string.target_wallet_valuable_description_with_name, valuableName)
        }else{
            resources.getString(R.string.target_wallet_valuable_description)
        }
        return Config(
            resources.getString(R.string.target_wallet_valuable_title),
            description,
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_google_wallet),
            allowAddingMoreThanOnce = true,
            configActivity = BaseConfigurationActivity.createIntent(
                provideContext(), TARGET_WALLET_STATIC
            )
        )
    }

    data class TargetData(
        @SerializedName("valuable_id")
        val valuableId: String? = null,
        @SerializedName("valuable_name")
        val valuableName: String? = null,
        @SerializedName("show_card_image")
        val showCardImage: Boolean = true,
        @SerializedName("show_as_popup")
        val showAsPopup: Boolean = false,
        @SerializedName("lock_orientation")
        val lockOrientation: Boolean = true,
        @SerializedName("pop_under")
        val popUnder: Boolean = false
    ) {

        companion object {
            const val TYPE = "wallet_valuable"
        }

    }

}