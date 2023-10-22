package com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.base

import kotlinx.coroutines.flow.StateFlow

interface BaseSearchViewModel {

    val showSearchClear: StateFlow<Boolean>

    fun setSearchTerm(term: String)
    fun getSearchTerm(): String

}