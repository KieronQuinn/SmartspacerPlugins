package com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.configuration

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.RefreshPeriod
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.SettingsType
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.TimeoutPeriod
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.UnitType
import com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.configuration.ConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Footer
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getSerializableExtraCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import com.kieronquinn.app.shared.R as SharedR

class ConfigurationFragment: BaseSettingsFragment(), BackAvailable, ProvidesTitle {

    companion object {
        private const val KEY_DATA_TYPE = "data_type"

        fun applyConfig(intent: Intent, dataType: DataType): Intent {
            return intent.putExtra(KEY_DATA_TYPE, dataType)
        }
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val viewModel by viewModel<ConfigurationViewModel>()

    private val timeFormatter by lazy {
        DateFormat.getTimeFormat(requireContext())
    }

    private val smartspacerId by lazy {
        requireActivity().intent.getStringExtra(SmartspacerConstants.EXTRA_SMARTSPACER_ID)!!
    }

    private val authority by lazy {
        requireActivity().intent.getStringExtra(SmartspacerConstants.EXTRA_AUTHORITY)!!
    }

    private val dataType by lazy {
        requireActivity().intent.getSerializableExtraCompat(KEY_DATA_TYPE, DataType::class.java)!!
    }

    override val adapter by lazy {
        Adapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        setupRefreshToast()
        viewModel.setup(smartspacerId, authority)
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
    }

    override fun getTitle(): CharSequence {
        return getString(dataType.label)
    }

    private fun setupRefreshToast() = whenResumed {
        viewModel.refreshCompleteBus.collect {
            val text = if(it) {
                getString(R.string.configuration_refresh_success)
            }else{
                getString(R.string.configuration_refresh_failed)
            }
            Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
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

    private fun handleState(state: State) {
        when(state) {
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
        val unit = config.getUnitOrNull<UnitType>()
        val dataType = config.dataType
        return listOfNotNull(
            GenericSettingsItem.Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                getString(R.string.configuration_rate_limited)
            ).takeIf { healthData?.wasRateLimited == true },
            when(dataType.settingsType) {
                SettingsType.TIMEOUT -> {
                    val timeoutPeriod = config.timeout ?: dataType.getDefaultTimeout()!!
                    Dropdown(
                        getString(R.string.configuration_timeout_title),
                        getString(
                            R.string.configuration_timeout_content, getString(timeoutPeriod.title)
                        ),
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_timeout),
                        timeoutPeriod,
                        viewModel::onTimeoutChanged,
                        TimeoutPeriod.values().toList()
                    ) {
                        it.title
                    }
                }
                SettingsType.RESET_NIGHT,
                SettingsType.RESET_DAY -> {
                    val resetTime = config.resetTime ?: LocalTime.parse(
                        dataType.getDefaultResetTime()!!,
                        DateTimeFormatter.ISO_LOCAL_TIME
                    )
                    Setting(
                        getString(R.string.configuration_reset_title),
                        getString(R.string.configuration_reset_content, resetTime.format()),
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_reset_time),
                        onClick = {
                            showTimePickerDialog(resetTime)
                        }
                    )
                }
            },
            if(permissionsRequired.isNotEmpty()) {
                val labels = permissionsRequired.joinToString(", ") { getString(it) }
                Setting(
                    getString(R.string.setup_request_permission_title),
                    getString(R.string.setup_request_permission_content, labels),
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_health_connect_tinted),
                    onClick = viewModel::onPermissionClicked
                )
            }else null,
            if(unit != null) {
                Dropdown(
                    getString(R.string.configuration_unit_title),
                    getString(R.string.configuration_unit_content, getString(unit.nameRes)),
                    ContextCompat.getDrawable(requireContext(), dataType.icon),
                    unit,
                    viewModel::onUnitTypeChanged,
                    dataType.unitType!!.java.enumConstants.toList()
                ) {
                    it.nameRes
                }
            }else null,
            Dropdown(
                getString(R.string.configuration_refresh_period_title),
                getString(
                    R.string.configuration_refresh_period_content,
                    getString(config.refreshPeriod.title)
                ),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_refresh_rate),
                config.refreshPeriod,
                viewModel::onRefreshPeriodChanged,
                RefreshPeriod.values().toList()
            ) {
                it.title
            },
            Setting(
                getString(R.string.configuration_refresh_title),
                getString(R.string.configuration_refresh_content),
                if(isRefreshing) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.avd_refreshing)
                }else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_refresh)
                },
                onClick = viewModel::onRefreshClicked,
                isEnabled = !isRefreshing
            ),
            Footer(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_info),
                getText(R.string.configuration_footer)
            )
        )
    }

    private fun showTimePickerDialog(resetTime: LocalTime) {
        val picker = MaterialTimePicker.Builder()
            .setHour(resetTime.hour)
            .setMinute(resetTime.minute)
            .setTimeFormat(getTimeFormat())
            .setPositiveButtonText(android.R.string.ok)
            .setNegativeButtonText(android.R.string.cancel)
            .build()
        picker.addOnPositiveButtonClickListener {
            val time = LocalTime.of(picker.hour, picker.minute)
            viewModel.onResetTimeChanged(time)
        }
        picker.show(childFragmentManager, "time_picker")
    }

    private fun getTimeFormat(): Int {
        return if(DateFormat.is24HourFormat(requireContext())) {
            TimeFormat.CLOCK_24H
        }else{
            TimeFormat.CLOCK_12H
        }
    }

    private fun LocalTime.format(): String {
        val instant = atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant()
        val date = Date.from(instant)
        return timeFormatter.format(date)
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}