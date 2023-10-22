package com.kieronquinn.app.smartspacer.plugin.tasker.model

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
data class SmartspacerTargetVisibilityTaskerInput @JvmOverloads constructor(
    @field:TaskerInputField("smartspacer_id", ignoreInStringBlurb = true)
    val smartspacerId: String? = null,
    @field:TaskerInputField("name", ignoreInStringBlurb = true)
    val name: String? = null,
    @field:TaskerInputField("visibility", ignoreInStringBlurb = true)
    val visibility: Boolean? = null
)