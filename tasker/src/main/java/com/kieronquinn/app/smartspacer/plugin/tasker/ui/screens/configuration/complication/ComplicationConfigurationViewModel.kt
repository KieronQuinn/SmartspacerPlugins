package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate.ComplicationExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate.WeatherData
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerComplicationUpdateTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.limittosurfaces.LimitToSurfacesFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.InputValidation
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.NeutralAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.weatherdata.WeatherDataFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.takeIfNotBlank
import com.kieronquinn.app.smartspacer.sdk.model.UiSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Complication as DatabaseComplication

abstract class ComplicationConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setInitialSmartspacerId(smartspacerId: String?)
    abstract fun setSmartspacerId(smartspacerId: String?)
    abstract fun setup(input: SmartspacerComplicationUpdateTaskerInput?, blank: ComplicationTemplate)
    abstract fun setComplicationTemplate(complicationTemplate: ComplicationTemplate)

    abstract fun onSelectComplicationClicked()

    abstract fun onComplicationIconClicked()
    abstract fun onComplicationIconChanged(icon: Icon?)
    abstract fun onComplicationContentClicked()
    abstract fun onComplicationContentChanged(content: Text)
    abstract fun onComplicationTapActionClicked()
    abstract fun onComplicationTapActionChanged(tapAction: TapAction)
    abstract fun onComplicationLimitToSurfacesClicked()
    abstract fun onComplicationLimitToSurfacesChanged(surfaces: Set<UiSurface>)
    abstract fun onComplicationWeatherDataClicked()
    abstract fun onComplicationWeatherDataChanged(weatherData: WeatherData?)
    abstract fun onComplicationRefreshPeriodClicked()
    abstract fun onComplicationRefreshPeriodChanged(refreshPeriod: String)
    abstract fun onComplicationRefreshWhenNotVisibleChanged(enabled: Boolean)
    abstract fun onComplicationDisableTrimChanged(enabled: Boolean)

    sealed class State {
        object Loading: State()
        object SelectComplication: State()
        data class Complication(
            val complication: DatabaseComplication?,
            val refreshPeriod: String = "0",
            val refreshIfNotVisible: Boolean = false,
            val template: ComplicationTemplate,
            val preview: ComplicationTemplate
        ): State()
    }

}

class ComplicationConfigurationViewModelImpl(
    private val navigation: ContainerNavigation,
    databaseRepository: DatabaseRepository
): ComplicationConfigurationViewModel(), KoinComponent {

    private val passedSmartspacerId = MutableStateFlow<String?>(null)
    private val setSmartspacerId = MutableStateFlow<String?>(null)

    private val smartspacerId = combine(
        passedSmartspacerId,
        setSmartspacerId
    ) { passed, set ->
        set ?: passed
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val complicationTemplate = MutableStateFlow<ComplicationTemplate?>(null)
    private val refreshPeriod = MutableStateFlow<String?>(null)
    private val refreshIfNotVisible = MutableStateFlow<Boolean?>(null)
    private val applicationContext by inject<Context>()

    private val basicComplicationProvider =
        ComplicationOptionsProvider.getProvider<ComplicationTemplate.Basic>()

    private val previewTemplate = complicationTemplate.filterNotNull().map {
        it.copyWithManipulations(applicationContext, emptyMap())
    }

    private val complication = smartspacerId.flatMapLatest {
        if(it == null) return@flatMapLatest flowOf(null)
        databaseRepository.getComplicationAsFlow(it)
    }.flowOn(Dispatchers.IO)

    private val template = combine(
        complication,
        complicationTemplate.filterNotNull(),
        previewTemplate.filterNotNull()
    ) { database, complication, preview ->
        Triple(database, complication, preview)
    }

    override val state = combine(
        smartspacerId,
        template,
        refreshPeriod.filterNotNull(),
        refreshIfNotVisible.filterNotNull(),
    ) { id, complication, period, notVisible ->
        if(id == null || complication.first == null){
            State.SelectComplication
        }else{
            State.Complication(complication.first, period, notVisible, complication.second, complication.third)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setInitialSmartspacerId(smartspacerId: String?) {
        viewModelScope.launch {
            this@ComplicationConfigurationViewModelImpl.passedSmartspacerId.emit(smartspacerId)
        }
    }

    override fun setSmartspacerId(smartspacerId: String?) {
        viewModelScope.launch {
            this@ComplicationConfigurationViewModelImpl.setSmartspacerId.emit(smartspacerId)
        }
    }

    override fun setup(input: SmartspacerComplicationUpdateTaskerInput?, blank: ComplicationTemplate) {
        viewModelScope.launch {
            if(this@ComplicationConfigurationViewModelImpl.complicationTemplate.value != null) {
                return@launch
            }
            complicationTemplate.emit(input?.complicationTemplate ?: blank)
            refreshPeriod.emit(input?.refreshPeriod ?: "0")
            refreshIfNotVisible.emit(input?.refreshIfNotVisible ?: false)
        }
    }

    override fun setComplicationTemplate(complicationTemplate: ComplicationTemplate) {
        viewModelScope.launch {
            this@ComplicationConfigurationViewModelImpl.complicationTemplate.emit(complicationTemplate)
        }
    }

    override fun onSelectComplicationClicked() {
        viewModelScope.launch {
            navigation.navigate(ComplicationConfigurationFragmentDirections.actionComplicationConfigurationFragmentToComplicationPickerFragment())
        }
    }

    override fun onComplicationIconClicked() {
        withCurrent<ComplicationTemplate> {
            navigation.navigate(ComplicationConfigurationFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_complication_icon_title,
                    ComplicationConfigurationFragment.REQUEST_KEY_COMPLICATION_ICON,
                    icon,
                    showNone = true
                )
            ))
        }
    }

    override fun onComplicationIconChanged(icon: Icon?) {
        updateComplication<ComplicationTemplate> {
            copyCompat(icon)
        }
    }

    override fun onComplicationContentClicked() {
        withCurrent<ComplicationTemplate> {
            navigation.navigate(ComplicationConfigurationFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_complication_content_title,
                    ComplicationConfigurationFragment.REQUEST_KEY_COMPLICATION_CONTENT,
                    content
                )
            ))
        }
    }

    override fun onComplicationContentChanged(content: Text) {
        updateComplication<ComplicationTemplate> {
            copyCompat(content = content)
        }
    }

    override fun onComplicationTapActionClicked() {
        withCurrent<ComplicationTemplate> {
            navigation.navigate(ComplicationConfigurationFragmentDirections.actionGlobalNavGraphIncludeTapAction(
                TapActionFragment.Config(
                    R.string.configuration_complication_tap_action_title,
                    ComplicationConfigurationFragment.REQUEST_KEY_COMPLICATION_TAP_ACTION,
                    onClick
                )
            ))
        }
    }

    override fun onComplicationTapActionChanged(tapAction: TapAction) {
        updateComplication<ComplicationTemplate> {
            copyCompat(onClick = tapAction)
        }
    }

    override fun onComplicationLimitToSurfacesClicked() {
        withCurrentExtras {
            navigation.navigate(ComplicationConfigurationFragmentDirections.actionGlobalNavGraphIncludeSurface(
                LimitToSurfacesFragment.Config(
                    ComplicationConfigurationFragment.REQUEST_KEY_COMPLICATION_LIMIT_TO_SURFACES,
                    limitToSurfaces.toList()
                )
            ))
        }
    }

    override fun onComplicationLimitToSurfacesChanged(surfaces: Set<UiSurface>) {
        updateComplicationExtras {
            copy(limitToSurfaces = surfaces)
        }
    }

    override fun onComplicationWeatherDataClicked() {
        withCurrentExtras {
            navigation.navigate(ComplicationConfigurationFragmentDirections.actionGlobalNavGraphIncludeWeatherData(
                WeatherDataFragment.Config(
                    ComplicationConfigurationFragment.REQUEST_KEY_COMPLICATION_WEATHER_DATA,
                    weatherData
                )
            ))
        }
    }

    override fun onComplicationWeatherDataChanged(weatherData: WeatherData?) {
        updateComplicationExtras {
            copy(weatherData = weatherData)
        }
    }

    override fun onComplicationRefreshPeriodClicked() {
        viewModelScope.launch {
            navigation.navigate(ComplicationConfigurationFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    refreshPeriod.value.takeIf { it != "0" } ?: "",
                    ComplicationConfigurationFragment.REQUEST_KEY_REFRESH_PERIOD,
                    R.string.configuration_complication_refresh_period_title,
                    R.string.configuration_complication_refresh_period_content,
                    R.string.configuration_complication_refresh_period_title,
                    inputValidation = InputValidation.REFRESH_PERIOD,
                    neutralAction = NeutralAction.REFRESH_PERIOD
                )
            ))
        }
    }

    override fun onComplicationRefreshPeriodChanged(refreshPeriod: String) {
        viewModelScope.launch {
            this@ComplicationConfigurationViewModelImpl.refreshPeriod.emit(
                refreshPeriod.takeIfNotBlank() ?: "0"
            )
        }
    }

    override fun onComplicationRefreshWhenNotVisibleChanged(enabled: Boolean) {
        viewModelScope.launch {
            refreshIfNotVisible.emit(enabled)
        }
    }

    override fun onComplicationDisableTrimChanged(enabled: Boolean) {
        viewModelScope.launch {
            updateComplication<ComplicationTemplate> {
                copyCompat(disableTrim = enabled)
            }
        }
    }

    private fun <T: ComplicationTemplate> withCurrent(block: suspend T.() -> Unit) {
        viewModelScope.launch {
            val current = (state.value as? State.Complication)?.template as? T ?: return@launch
            block(current)
        }
    }

    private fun withCurrentExtras(block: suspend ComplicationExtras.() -> Unit) {
        withCurrent<ComplicationTemplate> {
            block(complicationExtras)
        }
    }

    private fun <T: ComplicationTemplate> updateComplication(block: T.() -> T) {
        withCurrent<T> {
            complicationTemplate.emit(block(this))
        }
    }

    private fun updateComplicationExtras(block: ComplicationExtras.() -> ComplicationExtras) {
        updateComplication<ComplicationTemplate> {
            copyCompat(complicationExtras = block(complicationExtras))
        }
    }

}