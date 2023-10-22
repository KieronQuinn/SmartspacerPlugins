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
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetDismissInput

class TargetDismissEvent(
    config: TaskerPluginConfig<SmartspacerTargetDismissInput>
): TaskerPluginConfigHelper<SmartspacerTargetDismissInput, NoOutput, TargetDismissEventRunner>(config) {

    override val inputClass = SmartspacerTargetDismissInput::class.java
    override val runnerClass = TargetDismissEventRunner::class.java
    override val outputClass = NoOutput::class.java

    override fun addToStringBlurb(
        input: TaskerInput<SmartspacerTargetDismissInput>,
        blurbBuilder: StringBuilder
    ) {
        input.regular.name?.let {
            blurbBuilder.append(context.getString(R.string.dismiss_target_event_target_blurb, it))
        }
    }

}

class TargetDismissEventRunner: TaskerPluginRunnerConditionEvent<SmartspacerTargetDismissInput, NoOutput, SmartspacerTargetDismissInput>() {

    override fun getSatisfiedCondition(
        context: Context,
        input: TaskerInput<SmartspacerTargetDismissInput>,
        update: SmartspacerTargetDismissInput?
    ): TaskerPluginResultCondition<NoOutput> {
        val requiredId = input.regular.smartspacerId
        val actualId = update?.smartspacerId
        return if(requiredId != null && actualId != null && requiredId == actualId) {
            TaskerPluginResultConditionSatisfied(context)
        }else{
            TaskerPluginResultConditionUnsatisfied()
        }
    }

}