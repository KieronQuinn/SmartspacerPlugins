package com.kieronquinn.app.smartspacer.plugin.tasker.model

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
data class SmartspacerRequirementSetTaskerInput @JvmOverloads constructor(
    @field:TaskerInputField("smartspacer_id", ignoreInStringBlurb = true)
    val smartspacerId: String? = null,
    @field:TaskerInputField("name", ignoreInStringBlurb = true)
    val name: String? = null,
    @field:TaskerInputField("is_met", ignoreInStringBlurb = true)
    val isMet: Boolean? = null
)