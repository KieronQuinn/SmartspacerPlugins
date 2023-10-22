package com.kieronquinn.app.smartspacer.plugin.tasker.actions

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetVisibilityTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess as TaskerPluginResultSuccess

class SetTargetVisibilityAction(
    config: TaskerPluginConfig<SmartspacerTargetVisibilityTaskerInput>
): TaskerPluginConfigHelperNoOutput<SmartspacerTargetVisibilityTaskerInput, SetTargetVisibilityActionRunner>(config) {

    override val inputClass = SmartspacerTargetVisibilityTaskerInput::class.java
    override val runnerClass = SetTargetVisibilityActionRunner::class.java

    override fun addToStringBlurb(
        input: TaskerInput<SmartspacerTargetVisibilityTaskerInput>,
        blurbBuilder: StringBuilder
    ) {
        if(input.regular.visibility == false){
            blurbBuilder.append(
                context.getString(
                    R.string.action_target_set_visibility_blurb_invisible, input.regular.name
                )
            )
        }else{
            blurbBuilder.append(
                context.getString(
                    R.string.action_target_set_visibility_blurb_visible, input.regular.name
                )
            )
        }
    }

}

class SetTargetVisibilityActionRunner: TaskerPluginRunnerActionNoOutput<SmartspacerTargetVisibilityTaskerInput>(), KoinComponent {

    private val databaseRepository by inject<DatabaseRepository>()

    override fun run(
        context: Context,
        input: TaskerInput<SmartspacerTargetVisibilityTaskerInput>
    ): TaskerPluginResult<Unit> {
        val smartspacerId = input.regular.smartspacerId
            ?: return TaskerPluginResultError(NullPointerException("Target to set visibility of has not been set"))
        val visibility = input.regular.visibility ?: true
        databaseRepository.setTargetVisibility(context, smartspacerId, visibility)
        return TaskerPluginResultSuccess()
    }

}

