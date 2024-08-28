package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.dynamic

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.smartspacer.plugin.googlewallet.R
import com.kieronquinn.app.smartspacer.plugin.googlewallet.databinding.FragmentConfigurationGoogleWalletValuableSignInBinding
import com.kieronquinn.app.smartspacer.plugin.googlewallet.targets.GoogleWalletDynamicTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.dynamic.ConfigurationGoogleWalletDynamicViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isDarkMode
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onApplyInsets
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.shared.utils.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class ConfigurationGoogleWalletDynamicFragment: BoundFragment<FragmentConfigurationGoogleWalletValuableSignInBinding>(FragmentConfigurationGoogleWalletValuableSignInBinding::inflate), BackAvailable {

    private val viewModel by viewModel<ConfigurationGoogleWalletDynamicViewModel>()

    private val adapter by lazy {
        Adapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSignIn()
        setupSettings()
        setupLoading()
        setupState()
        val id = requireActivity().intent.getStringExtra(SmartspacerConstants.EXTRA_SMARTSPACER_ID)
        viewModel.setupWithId(id ?: return)
    }

    private fun setupSignIn() = with(binding.configurationGoogleSignIn) {
        configurationGoogleWalletValuableSignInCard.run {
            val background = monet.getPrimaryColor(context, !context.isDarkMode)
            backgroundTintList = ColorStateList.valueOf(background)
        }
        configurationGoogleWalletValuableSignInButton.run {
            whenResumed {
                onClicked().collect {
                    viewModel.onSignInClicked()
                }
            }
        }
    }

    private fun setupSettings() = with(binding.configurationGoogleSettings) {
        settingsBaseRecyclerView.run {
            updatePadding(top = resources.getDimensionPixelSize(SharedR.dimen.margin_8))
            layoutManager = LinearLayoutManager(context)
            adapter = this@ConfigurationGoogleWalletDynamicFragment.adapter
            onApplyInsets { view, insets ->
                val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                view.updatePadding(bottom = bottomInset)
            }
        }
    }

    private fun setupLoading() = with(binding.configurationGoogleSettings) {
        settingsBaseLoadingProgress.applyMonet()
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
                binding.configurationGoogleSettings.settingsBaseLoading.isVisible = true
                binding.configurationGoogleSignIn.root.isVisible = false
                binding.configurationGoogleSettings.settingsBaseRecyclerView.isVisible = false
            }
            is State.Error -> {
                Toast.makeText(
                    requireContext(), R.string.sign_in_with_google_error, Toast.LENGTH_LONG
                ).show()
                requireActivity().finish()
            }
            is State.SignInRequired -> {
                binding.configurationGoogleSettings.settingsBaseLoading.isVisible = false
                binding.configurationGoogleSignIn.root.isVisible = true
                binding.configurationGoogleSettings.settingsBaseRecyclerView.isVisible = false
            }
            is State.Loaded -> {
                binding.configurationGoogleSettings.settingsBaseLoading.isVisible = false
                binding.configurationGoogleSignIn.root.isVisible = false
                binding.configurationGoogleSettings.settingsBaseRecyclerView.isVisible = true
                adapter.update(
                    state.loadItems(state.settings),
                    binding.configurationGoogleSettings.settingsBaseRecyclerView
                )
                //Only OK adding the Target if the user has logged in successfully
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }
    }

    private fun State.Loaded.loadItems(settings: TargetData): List<BaseSettingsItem> {
        return listOf(
            GenericSettingsItem.SwitchSetting(
                allowOnMetered,
                getString(R.string.target_wallet_dynamic_settings_allow_metered_title),
                getString(R.string.target_wallet_dynamic_settings_allow_metered_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_metered_wifi),
                onChanged = viewModel::onAllowMeteredConnectionsChanged
            ),
            GenericSettingsItem.Setting(
                getString(R.string.target_wallet_dynamic_settings_refresh_now_title),
                if(isReloading){
                   getString(R.string.target_wallet_dynamic_settings_refresh_now_content_reloading)
                }else{
                    getString(R.string.target_wallet_dynamic_settings_refresh_now_content)
                },
                if(isReloading) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.avd_refreshing)
                }else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_reload)
                },
                isEnabled = !isReloading,
                onClick = viewModel::onReloadClicked
            ),
            GenericSettingsItem.SwitchSetting(
                settings.popUnder,
                getString(R.string.target_wallet_dynamic_settings_pop_under_title),
                getString(R.string.target_wallet_dynamic_settings_pop_under_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_google_wallet),
                onChanged = viewModel::onPopUnderChanged
            )
        )
    }

    inner class Adapter: BaseSettingsAdapter(
        binding.configurationGoogleSettings.settingsBaseRecyclerView,
        emptyList()
    )

}