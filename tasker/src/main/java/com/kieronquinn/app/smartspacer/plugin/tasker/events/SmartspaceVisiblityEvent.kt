package com.kieronquinn.app.smartspacer.plugin.tasker.events

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginRunnerConditionEvent
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionSatisfied
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionUnsatisfied
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.NoOutput
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerSmartspaceVisibilityTaskerInput

class SmartspaceVisiblityEvent(
    config: TaskerPluginConfig<SmartspacerSmartspaceVisibilityTaskerInput>
): TaskerPluginConfigHelper<SmartspacerSmartspaceVisibilityTaskerInput, NoOutput, SmartspaceVisiblityEventRunner>(config) {

    override val inputClass = SmartspacerSmartspaceVisibilityTaskerInput::class.java
    override val runnerClass = SmartspaceVisiblityEventRunner::class.java
    override val outputClass = NoOutput::class.java

    override fun addToStringBlurb(
        input: TaskerInput<SmartspacerSmartspaceVisibilityTaskerInput>,
        blurbBuilder: StringBuilder
    ) {
        val content = if(input.regular.visibility ?: return) {
            context.getString(R.string.smartspace_visibility_event_blurb_visible)
        }else{
            context.getString(R.string.smartspace_visibility_event_blurb_invisible)
        }
        blurbBuilder.append(content)
    }

}

class SmartspaceVisiblityEventRunner: TaskerPluginRunnerConditionEvent<SmartspacerSmartspaceVisibilityTaskerInput, NoOutput, SmartspacerSmartspaceVisibilityTaskerInput>() {

    override fun getSatisfiedCondition(
        context: Context,
        input: TaskerInput<SmartspacerSmartspaceVisibilityTaskerInput>,
        update: SmartspacerSmartspaceVisibilityTaskerInput?
    ): TaskerPluginResultCondition<NoOutput> {
        val requiredVisibility = input.regular.visibility
        val actualVisibility = update?.visibility
        return if(requiredVisibility != null && actualVisibility != null && requiredVisibility == actualVisibility) {
            TaskerPluginResultConditionSatisfied(context)
        }else{
            TaskerPluginResultConditionUnsatisfied()
        }
    }

}