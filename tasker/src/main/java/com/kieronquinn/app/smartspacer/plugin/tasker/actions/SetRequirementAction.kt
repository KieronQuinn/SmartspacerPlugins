package com.kieronquinn.app.smartspacer.plugin.tasker.actions

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerRequirementSetTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess as TaskerPluginResultSuccess

class SetRequirementAction(
    config: TaskerPluginConfig<SmartspacerRequirementSetTaskerInput>
): TaskerPluginConfigHelperNoOutput<SmartspacerRequirementSetTaskerInput, SetRequirementActionRunner>(config) {

    override val inputClass = SmartspacerRequirementSetTaskerInput::class.java
    override val runnerClass = SetRequirementActionRunner::class.java

    override fun addToStringBlurb(
        input: TaskerInput<SmartspacerRequirementSetTaskerInput>,
        blurbBuilder: StringBuilder
    ) {
        if(input.regular.isMet == false){
            blurbBuilder.append(
                context.getString(
                    R.string.action_requirement_set_blurb_unmet, input.regular.name
                )
            )
        }else{
            blurbBuilder.append(
                context.getString(
                    R.string.action_requirement_set_blurb_met, input.regular.name
                )
            )
        }
    }

}

class SetRequirementActionRunner: TaskerPluginRunnerActionNoOutput<SmartspacerRequirementSetTaskerInput>(), KoinComponent {

    private val databaseRepository by inject<DatabaseRepository>()

    override fun run(
        context: Context,
        input: TaskerInput<SmartspacerRequirementSetTaskerInput>
    ): TaskerPluginResult<Unit> {
        val smartspacerId = input.regular.smartspacerId
            ?: return TaskerPluginResultError(NullPointerException("Requirement to update has not been set"))
        val isMet = input.regular.isMet ?: true
        databaseRepository.setRequirementMet(context, smartspacerId, isMet)
        return TaskerPluginResultSuccess()
    }

}

