package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate.WeatherData
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.InputValidation
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.icon.WeatherDataIconFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class WeatherDataViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: WeatherData?)
    abstract fun dismiss()
    abstract fun onEnabledChanged(enabled: Boolean)
    abstract fun onWeatherStateIconClicked()
    abstract fun onWeatherStateIconChanged(icon: String)
    abstract fun onWeatherUnitChanged(unit: WeatherUnit)
    abstract fun onTemperatureClicked()
    abstract fun onTemperatureChanged(temperature: String)

    enum class WeatherUnit(val label: Int, val unit: Int) {
        CELSIUS(
            R.string.weather_data_temperature_unit_celsius_label,
            R.string.weather_data_temperature_unit_celsius,
        ),
        FAHRENHEIT(
            R.string.weather_data_temperature_unit_fahrenheit_label,
            R.string.weather_data_temperature_unit_fahrenheit,
        );

        companion object {
            fun fromBoolean(useCelsius: Boolean): WeatherUnit {
                return if(useCelsius) CELSIUS else FAHRENHEIT
            }
        }
    }

    sealed class State {
        object Loading: State()
        data class Loaded(val weatherData: WeatherData?): State()
    }

}

class WeatherDataViewModelImpl(
    private val navigation: ContainerNavigation
): WeatherDataViewModel() {

    private val weatherData = MutableStateFlow<WeatherDataWrapper?>(null)

    override val state = weatherData.filterNotNull().mapLatest {
        State.Loaded(it.weatherData)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: WeatherData?) {
        if(weatherData.value != null) return
        viewModelScope.launch {
            weatherData.emit(WeatherDataWrapper(current))
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            if(enabled) {
                weatherData.emit(WeatherDataWrapper(null))
            }else{
                weatherData.emit(WeatherDataWrapper(WeatherData()))
            }
        }
    }

    override fun onWeatherStateIconClicked() {
        val current = weatherData.value?.weatherData?._weatherStateIcon ?: return
        viewModelScope.launch {
            navigation.navigate(WeatherDataFragmentDirections.actionWeatherDataFragmentToWeatherDataIconFragment(
                WeatherDataIconFragment.Config(
                    WeatherDataFragment.REQUEST_KEY_WEATHER_STATE_ICON,
                    current
                )
            ))
        }
    }

    override fun onWeatherStateIconChanged(icon: String) {
        updateWeatherData {
            copy(_weatherStateIcon = icon)
        }
    }

    override fun onTemperatureClicked() {
        val current = weatherData.value?.weatherData ?: return
        val weatherUnit = WeatherUnit.fromBoolean(current.useCelsius)
        viewModelScope.launch {
            navigation.navigate(WeatherDataFragmentDirections.actionGlobalNavGraphIncludeString3(
                StringInputFragment.Config(
                    current._temperature ?: "",
                    WeatherDataFragment.REQUEST_KEY_TEMPERATURE,
                    R.string.weather_data_temperature_title,
                    R.string.weather_data_temperature_content,
                    R.string.weather_data_temperature_title,
                    suffix = weatherUnit.unit,
                    inputValidation = InputValidation.TEMPERATURE
                )
            ))
        }
    }

    override fun onTemperatureChanged(temperature: String) {
        updateWeatherData {
            copy(_temperature = temperature)
        }
    }

    override fun onWeatherUnitChanged(unit: WeatherUnit) {
        updateWeatherData {
            copy(useCelsius = unit == WeatherUnit.CELSIUS)
        }
    }

    private fun updateWeatherData(block: WeatherData.() -> WeatherData) {
        viewModelScope.launch {
            weatherData.emit(
                WeatherDataWrapper(block(weatherData.value?.weatherData ?: return@launch))
            )
        }
    }

    data class WeatherDataWrapper(val weatherData: WeatherData?)

}