package com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration

import androidx.lifecycle.ViewModel
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository

abstract class ConfigurationViewModel: ViewModel() {

    abstract fun onClearClicked()

}

class ConfigurationViewModelImpl(
    private val databaseRepository: DatabaseRepository,
    private val packageName: String
): ConfigurationViewModel() {

    override fun onClearClicked() {
        databaseRepository.setBadgeCount(packageName, 0)
    }

}