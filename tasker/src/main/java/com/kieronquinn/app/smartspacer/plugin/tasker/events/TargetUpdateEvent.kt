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
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetUpdateInput

class TargetUpdateEvent(
    config: TaskerPluginConfig<SmartspacerTargetUpdateInput>
): TaskerPluginConfigHelper<SmartspacerTargetUpdateInput, NoOutput, TargetUpdateEventRunner>(config) {

    override val inputClass = SmartspacerTargetUpdateInput::class.java
    override val runnerClass = TargetUpdateEventRunner::class.java
    override val outputClass = NoOutput::class.java

    override fun addToStringBlurb(
        input: TaskerInput<SmartspacerTargetUpdateInput>,
        blurbBuilder: StringBuilder
    ) {
        input.regular.name?.let {
            blurbBuilder.append(context.getString(R.string.update_target_event_target_blurb, it))
        }
    }

}

class TargetUpdateEventRunner: TaskerPluginRunnerConditionEvent<SmartspacerTargetUpdateInput, NoOutput, SmartspacerTargetUpdateInput>() {

    override fun getSatisfiedCondition(
        context: Context,
        input: TaskerInput<SmartspacerTargetUpdateInput>,
        update: SmartspacerTargetUpdateInput?
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