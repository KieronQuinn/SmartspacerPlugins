package com.kieronquinn.app.smartspacer.plugin.googlehome.ui.screens.complication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlehome.complications.GoogleHomeComplication
import com.kieronquinn.app.smartspacer.plugin.googlehome.complications.GoogleHomeComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.googlehome.model.HideMode
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class GoogleHomeComplicationConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract fun setupWithId(id: String)
    abstract fun onHideModeChanged(hideMode: HideMode)
    abstract fun onShowSubtitleChanged(showSubtitle: Boolean)

    sealed class State {
        data object Loading: State()
        data class Loaded(val hideMode: HideMode, val showSubtitle: Boolean): State()
    }

}

class GoogleHomeComplicationConfigurationViewModelImpl(
    private val dataRepository: DataRepository
): GoogleHomeComplicationConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val complicationData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getComplicationDataFlow(it, ComplicationData::class.java).map { data ->
            data ?: ComplicationData()
        }
    }

    override val state = complicationData.mapLatest {
        State.Loaded(it.hideMode, it.showSubtitle)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setupWithId(id: String) {
        viewModelScope.launch {
            smartspacerId.emit(id)
        }
    }

    override fun onHideModeChanged(hideMode: HideMode) {
        val smartspacerId = smartspacerId.value ?: return
        updateConfiguration(smartspacerId, hideMode)
    }

    override fun onShowSubtitleChanged(showSubtitle: Boolean) {
        val smartspacerId = smartspacerId.value ?: return
        updateConfiguration(smartspacerId, showSubtitle = showSubtitle)
    }

    private fun updateConfiguration(
        smartspacerId: String,
        hideMode: HideMode? = null,
        showSubtitle: Boolean? = null
    ) {
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onChanged
        ) {
            ComplicationData(
                hideMode = hideMode ?: it?.hideMode ?: HideMode.DISABLED,
                showSubtitle = showSubtitle ?: it?.showSubtitle ?: false
            )
        }
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        SmartspacerComplicationProvider.notifyChange(
            context, GoogleHomeComplication::class.java, smartspacerId
        )
    }

}