package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.googlewallet.R
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable.ConfigurationGoogleWalletValuableViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class ConfigurationGoogleWalletValuableFragment: BaseSettingsFragment(), BackAvailable {

    private val viewModel by viewModel<ConfigurationGoogleWalletValuableViewModel>()

    override val adapter by lazy {
        Adapter()
    }

    override val backIcon = SharedR.drawable.ic_close

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        val id = requireActivity().intent.getStringExtra(SmartspacerConstants.EXTRA_SMARTSPACER_ID)
        viewModel.setupWithId(id ?: return)
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        when(state){
            is State.Loading -> {
                binding.settingsBaseLoading.isVisible = true
                binding.settingsBaseRecyclerView.isVisible = false
            }
            is State.Loaded -> {
                binding.settingsBaseLoading.isVisible = false
                binding.settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), binding.settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        val picker = listOf(
            GenericSettingsItem.Setting(
                getString(R.string.target_wallet_valuable_settings_select_card_title),
                cardName ?: getString(R.string.target_wallet_valuable_settings_select_card_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_google_wallet),
                onClick = viewModel::onSelectCardClicked
            )
        )
        val settings = if (settings != null) {
            val cardDescription = if (cardHasImage) {
                getString(
                    R.string.target_wallet_valuable_settings_show_card_image_content
                )
            } else {
                getString(
                    R.string.target_wallet_valuable_settings_show_card_image_content_unavailable
                )
            }
            listOfNotNull(
                GenericSettingsItem.Header(
                    getString(R.string.target_wallet_valuable_settings_title)
                ),
                GenericSettingsItem.SwitchSetting(
                    settings.showCardImage && cardHasImage,
                    getString(R.string.target_wallet_valuable_settings_show_card_image_title),
                    cardDescription,
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_target_google_wallet_show_card_image
                    ),
                    enabled = cardHasImage,
                    onChanged = viewModel::onShowImageChanged
                ),
                GenericSettingsItem.SwitchSetting(
                    settings.showAsPopup,
                    getString(R.string.target_wallet_valuable_settings_show_as_popup_title),
                    getString(R.string.target_wallet_valuable_settings_show_as_popup_content),
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_target_google_wallet_valuable_show_as_popup
                    ),
                    onChanged = viewModel::onShowPopupChanged
                ),
                GenericSettingsItem.SwitchSetting(
                    settings.lockOrientation,
                    getString(R.string.target_wallet_valuable_settings_popup_portrait_title),
                    getString(R.string.target_wallet_valuable_settings_popup_portrait_content),
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_settings_lock_rotation
                    ),
                    onChanged = viewModel::onLockRotationChanged
                ).takeIf { settings.showAsPopup },
                GenericSettingsItem.SwitchSetting(
                    settings.popUnder,
                    getString(R.string.target_wallet_valuable_settings_pop_under_title),
                    getString(R.string.target_wallet_valuable_settings_pop_under_content),
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_google_wallet
                    ),
                    onChanged = viewModel::onPopUnderChanged
                )
            )
        } else emptyList()
        return picker + settings
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}