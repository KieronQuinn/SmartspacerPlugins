package com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.configuration

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.countdown.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.countdown.complications.CountdownComplication
import com.kieronquinn.app.smartspacer.plugin.countdown.complications.CountdownComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.countdown.model.Icon
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.icon.IconPickerFragment
import com.kieronquinn.app.smartspacer.plugin.countdown.utils.extensions.hasAlarmPermission
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

abstract class ConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(smartspacerId: String)
    abstract fun onPermissionClicked()
    abstract fun onDateChanged(date: Long)
    abstract fun onCountUpChanged(enabled: Boolean)
    abstract fun onIconClicked(key: String)
    abstract fun onIconChanged(icon: Icon)

    sealed class State {
        object Loading: State()
        data class Loaded(val data: ComplicationData, val hasPermission: Boolean): State()
    }

}

class ConfigurationViewModelImpl(
    context: Context,
    private val navigation: ContainerNavigation,
    private val dataRepository: DataRepository
): ConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val complicationData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getComplicationDataFlow(it, ComplicationData::class.java)
    }

    override val state = combine(
        complicationData,
        context.hasAlarmPermission(viewModelScope)
    ) { data, permission ->
        State.Loaded(data ?: ComplicationData(), permission)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(smartspacerId: String) {
        viewModelScope.launch {
            this@ConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onPermissionClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            })
        }
    }

    override fun onDateChanged(date: Long) {
        val instant = Instant.ofEpochMilli(date).atOffset(ZoneOffset.UTC)
            .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        updateComplicationData {
            it.copy(endDate = instant)
        }
    }

    override fun onCountUpChanged(enabled: Boolean) {
        updateComplicationData {
            it.copy(allowCountUp = enabled)
        }
    }

    override fun onIconClicked(key: String) {
        viewModelScope.launch {
            val current = (state.value as? State.Loaded)?.data?.icon ?: return@launch
            navigation.navigate(
                ConfigurationFragmentDirections.actionConfigurationFragmentToIconPickerFragment(
                    IconPickerFragment.Config(key, current)
                )
            )
        }
    }

    override fun onIconChanged(icon: Icon) {
        updateComplicationData {
            it.copy(icon = icon)
        }
    }

    private fun updateComplicationData(block: (ComplicationData) -> ComplicationData) {
        val id = smartspacerId.value ?: return
        dataRepository.updateComplicationData(
            id,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onChanged
        ) {
            val data = it ?: ComplicationData()
            block(data)
        }
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        SmartspacerComplicationProvider.notifyChange(
            context,
            CountdownComplication::class.java,
            smartspacerId
        )
    }

}