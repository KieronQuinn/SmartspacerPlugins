package com.kieronquinn.app.smartspacer.plugin.tasker.model

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
data class SmartspacerSmartspaceVisibilityTaskerInput @JvmOverloads constructor(
    @field:TaskerInputField("visibility", ignoreInStringBlurb = true)
    val visibility: Boolean? = null
)