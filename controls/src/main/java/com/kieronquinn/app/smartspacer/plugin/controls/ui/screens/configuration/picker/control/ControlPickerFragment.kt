package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.control

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.control.ControlPickerViewModel.Control
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.control.ControlPickerViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import org.koin.androidx.viewmodel.ext.android.viewModel

class ControlPickerFragment: BaseSettingsFragment(), BackAvailable, ProvidesTitle {

    companion object {
        private const val KEY_CONTROL = "control"
        private const val KEY_RESULT = "result"

        fun Fragment.setupControlResultListener(callback: (result: Control) -> Unit) {
            setFragmentResultListener(KEY_CONTROL) { requestKey, bundle ->
                if(requestKey != KEY_CONTROL) return@setFragmentResultListener
                val result = bundle.getParcelableCompat(KEY_RESULT, Control::class.java)
                    ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }
    }

    override val adapter by lazy {
        Adapter()
    }

    private val viewModel by viewModel<ControlPickerViewModel>()
    private val args by navArgs<ControlPickerFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        viewModel.setup(args.app)
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
            is State.Controls -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
            }
            is State.Error -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(listOf(Card(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_error),
                    getString(R.string.configuration_control_picker_error, args.app.name)
                )))
            }
        }
    }

    private fun State.Controls.loadItems(): List<BaseSettingsItem> {
        return controls.map {
            Setting(
                it.label,
                it.subtitle,
                it.icon,
            ) {
                onControlClicked(it)
            }
        }.ifEmpty {
            listOf(Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_error),
                getString(R.string.configuration_control_picker_empty)
            ))
        }
    }

    private fun onControlClicked(control: Control) {
        setFragmentResult(KEY_CONTROL, bundleOf(KEY_RESULT to control))
        viewModel.dismiss()
    }

    override fun getTitle(): CharSequence {
        return args.app.name
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}