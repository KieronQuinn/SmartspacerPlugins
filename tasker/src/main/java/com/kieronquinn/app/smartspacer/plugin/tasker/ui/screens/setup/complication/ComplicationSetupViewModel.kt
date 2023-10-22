package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.setup.complication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Complication
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.setup.complication.ComplicationSetupFragment.Companion.CONFIG_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class ComplicationSetupViewModel: ViewModel() {

    abstract val errorBus: Flow<Int>
    abstract val state: StateFlow<State>

    abstract fun setup(smartspacerId: String)
    abstract fun onNameClicked()
    abstract fun onNameResult(result: String)
    abstract fun onSaveClicked(context: Context): Boolean

    sealed class State {
        object Loading: State()
        data class Loaded(
            val smartspacerId: String,
            val name: String?
        ): State()
    }

}

class ComplicationSetupViewModelImpl(
    private val databaseRepository: DatabaseRepository,
    private val navigation: ContainerNavigation
): ComplicationSetupViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)
    private val customName = MutableStateFlow<String?>(null)

    private val complication = smartspacerId.filterNotNull().mapLatest {
        databaseRepository.getComplication(it)
    }

    override val errorBus = MutableSharedFlow<Int>()

    override val state = combine(
        smartspacerId.filterNotNull(),
        complication,
        customName
    ) { i, t, n ->
        State.Loaded(i, n ?: t?.name)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(smartspacerId: String) {
        viewModelScope.launch {
            this@ComplicationSetupViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onNameClicked() {
        viewModelScope.launch {
            val name = (state.value as? State.Loaded)?.name ?: ""
            navigation.navigate(
                ComplicationSetupFragmentDirections.actionComplicationSetupFragmentToNavGraphIncludeString(
                    CONFIG_NAME.copy(initialContent = name)
                )
            )
        }
    }

    override fun onNameResult(result: String) {
        viewModelScope.launch {
            customName.emit(result)
        }
    }

    override fun onSaveClicked(context: Context): Boolean {
        val state = state.value as? State.Loaded ?: return true
        val isError = state.name.isNullOrEmpty()
        viewModelScope.launch {
            if(isError) {
                errorBus.emit(R.string.complication_setup_name_error)
                return@launch
            }
            val complication = Complication.createNewComplication(
                state.smartspacerId, state.name ?: ""
            )
            databaseRepository.addComplication(complication)
        }
        return !isError
    }

}