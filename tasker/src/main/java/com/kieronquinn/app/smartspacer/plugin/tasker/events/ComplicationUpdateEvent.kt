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
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerComplicationUpdateInput

class ComplicationUpdateEvent(
    config: TaskerPluginConfig<SmartspacerComplicationUpdateInput>
): TaskerPluginConfigHelper<SmartspacerComplicationUpdateInput, NoOutput, ComplicationUpdateEventRunner>(config) {

    override val inputClass = SmartspacerComplicationUpdateInput::class.java
    override val runnerClass = ComplicationUpdateEventRunner::class.java
    override val outputClass = NoOutput::class.java

    override fun addToStringBlurb(
        input: TaskerInput<SmartspacerComplicationUpdateInput>,
        blurbBuilder: StringBuilder
    ) {
        input.regular.name?.let {
            blurbBuilder.append(context.getString(R.string.update_complication_event_complication_blurb, it))
        }
    }

}

class ComplicationUpdateEventRunner: TaskerPluginRunnerConditionEvent<SmartspacerComplicationUpdateInput, NoOutput, SmartspacerComplicationUpdateInput>() {

    override fun getSatisfiedCondition(
        context: Context,
        input: TaskerInput<SmartspacerComplicationUpdateInput>,
        update: SmartspacerComplicationUpdateInput?
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