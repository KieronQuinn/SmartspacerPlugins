package com.kieronquinn.app.smartspacer.plugin.tasker.model

import com.google.gson.Gson
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@TaskerInputRoot
data class SmartspacerComplicationUpdateTaskerInput @JvmOverloads constructor(
    @field:TaskerInputField("smartspacer_id", ignoreInStringBlurb = true)
    val smartspacerId: String? = null,
    @field:TaskerInputField("name", ignoreInStringBlurb = true)
    val name: String? = null,
    @field:TaskerInputField("target_template", ignoreInStringBlurb = true)
    val _complicationTemplate: String? = null,
    @field:TaskerInputField("refresh_period", ignoreInStringBlurb = true)
    val _refreshPeriod: String? = null,
    @field:TaskerInputField("refresh_if_not_visible", ignoreInStringBlurb = true)
    val _refreshIfNotVisible: Boolean? = null
): KoinComponent {

    private val gson by inject<Gson>()

    val complicationTemplate
        get() = _complicationTemplate?.let {
            gson.fromJson(it, ComplicationTemplate::class.java)
        }

    val refreshPeriod
        get() = _refreshPeriod ?: "0"

    val refreshIfNotVisible
        get() = _refreshIfNotVisible ?: false

}