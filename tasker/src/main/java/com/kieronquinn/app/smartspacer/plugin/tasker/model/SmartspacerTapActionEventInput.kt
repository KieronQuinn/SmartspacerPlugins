package com.kieronquinn.app.smartspacer.plugin.tasker.model

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
data class SmartspacerTapActionEventInput(
    @field:TaskerInputField("id", ignoreInStringBlurb = true)
    val id: String? = null
)
