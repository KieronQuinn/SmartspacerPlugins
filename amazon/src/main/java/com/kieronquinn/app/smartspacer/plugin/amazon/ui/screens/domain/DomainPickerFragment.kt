package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.domain

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.AmazonDomain
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.domain.DomainPickerViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class DomainPickerFragment: BaseSettingsFragment(), BackAvailable {

    companion object {
        private const val REQUEST_KEY_DOMAIN = "domain"

        fun Fragment.setupDomainChangeListener(callback: () -> Unit) {
            setFragmentResultListener(REQUEST_KEY_DOMAIN) { _, _ ->
                callback.invoke()
            }
        }
    }

    private val viewModel by viewModel<DomainPickerViewModel>()
    private val args by navArgs<DomainPickerFragmentArgs>()

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) = with(binding) {
        when(state) {
            is State.Loading -> {
                settingsBaseLoading.isVisible = true
                settingsBaseRecyclerView.isVisible = false
            }
            is State.Loaded -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), binding.settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return listOfNotNull(recommendedDomain, *otherDomains.toTypedArray()).map {
            Setting(
                getString(it.nameRes),
                "",
                ContextCompat.getDrawable(requireContext(), it.iconRes),
                tintIcon = false
            ) {
                onDomainClicked(it)
            }
        }
    }

    private fun onDomainClicked(domain: AmazonDomain) {
        if(args.isSetup) {
            viewModel.onDomainClicked(domain, true)
            return
        }
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.target_configuration_settings_domain_dialog_title)
            setMessage(R.string.target_configuration_settings_sign_out_dialog_content)
            setPositiveButton(android.R.string.ok) { _, _ ->
                setFragmentResult(REQUEST_KEY_DOMAIN, Bundle.EMPTY)
                viewModel.onDomainClicked(domain, false)
            }
            setNegativeButton(android.R.string.cancel) { _, _ -> }
        }.show()
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}