package com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.configuration

import android.app.Activity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.datepicker.MaterialDatePicker
import com.kieronquinn.app.smartspacer.plugin.countdown.R
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.configuration.ConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.icon.IconPickerFragment.Companion.setupIconResultListener
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import com.kieronquinn.app.shared.R as SharedR

class ConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    companion object {
        private const val KEY_ICON = "icon"
    }

    private val viewModel by viewModel<ConfigurationViewModel>()

    private val dateFormat by lazy {
        DateFormat.getDateFormat(requireContext())
    }

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupState()
        viewModel.setup(requireActivity().intent.getStringExtra(EXTRA_SMARTSPACER_ID)!!)
    }

    private fun setupListener() {
        setupIconResultListener(KEY_ICON) {
            viewModel.onIconChanged(it)
        }
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
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return listOfNotNull(
            Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                getString(R.string.configuration_permission_content),
                viewModel::onPermissionClicked
            ).takeIf { !hasPermission },
            Setting(
                getString(R.string.configuration_date_title),
                data.endLocalDate?.formatTime() ?: getString(R.string.configuration_date_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_countdown)
            ) {
                onDateClicked(data.endTimestamp ?: getDefaultTime())
            },
            Setting(
                getString(R.string.configuration_icon_title),
                data.icon.describe(requireContext()),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_icon_built_in)
            ) {
                viewModel.onIconClicked(KEY_ICON)
            }
        )
    }

    private fun getDefaultTime(): Long {
        return LocalDate.now()
            .atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun onDateClicked(current: Long) {
        showDatePicker(current) {
            viewModel.onDateChanged(it)
        }
    }

    private fun LocalDate.formatTime(): String {
        val instant = atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        val date = Date.from(instant)
        return dateFormat.format(date)
    }

    private fun showDatePicker(current: Long, action: (Long) -> Unit) {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.configuration_date_title))
            .setSelection(current)
            .setPositiveButtonText(android.R.string.ok)
            .setNegativeButtonText(android.R.string.cancel)
            .build().apply {
                addOnPositiveButtonClickListener { action(it) }
            }.show(childFragmentManager, "date_picker")
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}