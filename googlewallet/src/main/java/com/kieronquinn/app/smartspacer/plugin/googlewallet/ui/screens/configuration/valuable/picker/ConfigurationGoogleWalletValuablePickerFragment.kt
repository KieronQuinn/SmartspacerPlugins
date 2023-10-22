package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable.picker

import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
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
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable.picker.ConfigurationGoogleWalletValuablePickerViewModel.Companion.ALLOWED_VALUABLE_TYPES
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable.picker.ConfigurationGoogleWalletValuablePickerViewModel.State
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toBitmap
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onApplyInsets
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isDarkMode
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class ConfigurationGoogleWalletValuablePickerFragment: BoundFragment<FragmentConfigurationGoogleWalletValuableSignInBinding>(FragmentConfigurationGoogleWalletValuableSignInBinding::inflate), BackAvailable {

    private val viewModel by viewModel<ConfigurationGoogleWalletValuablePickerViewModel>()

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
            adapter = this@ConfigurationGoogleWalletValuablePickerFragment.adapter
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
                    state.loadItems(), binding.configurationGoogleSettings.settingsBaseRecyclerView
                )
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return valuables.groupBy {
            it::class.java
        }.flatMap {
            val title = ALLOWED_VALUABLE_TYPES[it.key] ?: return@flatMap emptyList()
            listOf(GenericSettingsItem.Header(getString(title))) + it.value.toSettings()
        }.ifEmpty {
            listOf(
                GenericSettingsItem.Card(
                    ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                    getString(R.string.target_wallet_valuable_empty)
                )
            )
        }
    }

    private fun List<GoogleWalletRepository.Valuable>.toSettings() = mapNotNull {
        val groupingInfo = it.getGroupingInfo() ?: return@mapNotNull null
        GenericSettingsItem.Setting(
            groupingInfo.groupingTitle,
            groupingInfo.groupingSubtitle,
            it.image?.toBitmap()?.let { bitmap -> BitmapDrawable(resources, bitmap) },
            tintIcon = false
        ){
            viewModel.onValuableClicked(it)
        }
    }.sortedBy { it.title.toString().lowercase() }

    inner class Adapter: BaseSettingsAdapter(
        binding.configurationGoogleSettings.settingsBaseRecyclerView,
        emptyList()
    )

}