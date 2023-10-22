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
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTapActionEventInput

class TapActionEvent(
    config: TaskerPluginConfig<SmartspacerTapActionEventInput>
): TaskerPluginConfigHelper<SmartspacerTapActionEventInput, NoOutput, TapActionEventRunner>(config) {

    override val inputClass = SmartspacerTapActionEventInput::class.java
    override val runnerClass = TapActionEventRunner::class.java
    override val outputClass = NoOutput::class.java

    override fun addToStringBlurb(
        input: TaskerInput<SmartspacerTapActionEventInput>,
        blurbBuilder: StringBuilder
    ) {
        input.regular.id?.let {
            blurbBuilder.append(context.getString(R.string.tap_action_event_blurb, it))
        }
    }

}

class TapActionEventRunner: TaskerPluginRunnerConditionEvent<SmartspacerTapActionEventInput, NoOutput, SmartspacerTapActionEventInput>() {

    override fun getSatisfiedCondition(
        context: Context,
        input: TaskerInput<SmartspacerTapActionEventInput>,
        update: SmartspacerTapActionEventInput?
    ): TaskerPluginResultCondition<NoOutput> {
        val requiredId = input.regular.id
        val actualId = update?.id
        return if(requiredId != null && actualId != null && requiredId == actualId) {
            TaskerPluginResultConditionSatisfied(context)
        }else{
            TaskerPluginResultConditionUnsatisfied()
        }
    }

}