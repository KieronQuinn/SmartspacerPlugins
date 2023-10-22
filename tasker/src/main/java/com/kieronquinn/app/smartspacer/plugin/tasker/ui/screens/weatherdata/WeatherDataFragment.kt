package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Switch
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate.WeatherData
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.Companion.setupStringResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.WeatherDataViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.WeatherDataViewModel.WeatherUnit
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.icon.WeatherDataIconFragment.Companion.setupWeatherDataIconResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.WeatherStateIcon_of
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.label
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel

class WeatherDataFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"
        const val REQUEST_KEY_WEATHER_STATE_ICON = "weather_state_icon"
        const val REQUEST_KEY_TEMPERATURE = "weather_temperature"

        fun Fragment.setupWeatherDataResultListener(
            key: String, callback: (result: WeatherData?) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableCompat(KEY_RESULT, WeatherData::class.java)
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<WeatherDataViewModel>()
    private val args by navArgs<WeatherDataFragmentArgs>()

    private val config by lazy {
        args.config as Config
    }

    override val adapter by lazy {
        Adapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupState()
        viewModel.setup(config.current)
    }

    override fun onBackPressed(): Boolean {
        val current = viewModel.state.value as? State.Loaded
        if(current != null) {
            setFragmentResult(config.key, bundleOf(
                KEY_RESULT to current.weatherData
            ))
        }
        viewModel.dismiss()
        return true
    }

    private fun setupListeners() {
        setupStringResultListener(REQUEST_KEY_TEMPERATURE) {
            viewModel.onTemperatureChanged(it)
        }
        setupWeatherDataIconResultListener(REQUEST_KEY_WEATHER_STATE_ICON) {
            viewModel.onWeatherStateIconChanged(it)
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
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        val options = if(weatherData != null) {
            val weatherUnit = WeatherUnit.fromBoolean(weatherData.useCelsius)
            listOf(
                Setting(
                    getString(R.string.weather_data_weather_state_icon_title),
                    weatherData._weatherStateIcon.getWeatherStateIconContent(),
                    icon = null,
                    onClick = viewModel::onWeatherStateIconClicked
                ),
                Dropdown(
                    getString(R.string.weather_data_temperature_unit_title),
                    getString(weatherUnit.label),
                    icon = null,
                    weatherUnit,
                    viewModel::onWeatherUnitChanged,
                    WeatherUnit.values().toList()
                ) {
                    it.label
                },
                Setting(
                    getString(R.string.weather_data_temperature_title),
                    weatherData.getTemperature(weatherUnit),
                    icon = null,
                    onClick = viewModel::onTemperatureClicked
                )
            )
        } else emptyList()
        return listOf(
            Switch(
                weatherData != null,
                getString(R.string.weather_data_switch),
                viewModel::onEnabledChanged
            )
        ) + options
    }

    private fun String.getWeatherStateIconContent(): String {
        return WeatherStateIcon_of(this)?.let { getString(it.label) } ?: this
    }

    private fun WeatherData.getTemperature(weatherUnit: WeatherUnit): String {
        val temperature = _temperature ?: temperature.toString()
        return "$temperature ${getString(weatherUnit.unit)}"
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: WeatherData?
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}