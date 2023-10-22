package com.kieronquinn.app.smartspacer.plugins.pokemongo.ui.screens.configuration

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin.Variant
import com.kieronquinn.app.smartspacer.plugins.pokemongo.complications.PokemonGoComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetType
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class ConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setupWithId(smartspacerId: String)

    abstract fun onUseStaticIconChanged(
        useStaticIcon: Boolean,
        widgetType: WidgetType,
        variant: Variant
    )

    sealed class State {
        object Loading: State()
        data class Loaded(val useStaticIcon: Boolean): State()
    }

}

class ConfigurationViewModelImpl(
    private val dataRepository: DataRepository,
    private val widgetRepository: WidgetRepository
): ConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val complicationData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getComplicationDataFlow(it, ComplicationData::class.java)
    }

    override val state = complicationData.map {
        val data = it ?: ComplicationData()
        State.Loaded(data.useStaticIcon)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setupWithId(smartspacerId: String) {
        viewModelScope.launch {
            this@ConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onUseStaticIconChanged(
        useStaticIcon: Boolean,
        widgetType: WidgetType,
        variant: Variant
    ) {
        val smartspacerId = smartspacerId.value ?: return
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            { context, id -> onDataChanged(context, id, widgetType, variant) }
        ) {
            ComplicationData(useStaticIcon)
        }
    }

    private fun onDataChanged(
        context: Context,
        smartspacerId: String,
        widgetType: WidgetType,
        variant: Variant
    ) {
        val complicationClass = widgetRepository.getComplicationClass(variant, widgetType)
        SmartspacerComplicationProvider.notifyChange(context, complicationClass, smartspacerId)
    }

}