package com.kieronquinn.app.smartspacer.plugin.tasker.actions

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Manipulative.Companion.replaceWithReplacements
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetUpdateTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess as TaskerPluginResultSuccess

class UpdateTargetAction(
    config: TaskerPluginConfig<SmartspacerTargetUpdateTaskerInput>
): TaskerPluginConfigHelperNoOutput<SmartspacerTargetUpdateTaskerInput, UpdateTargetActionRunner>(config) {

    override val inputClass = SmartspacerTargetUpdateTaskerInput::class.java
    override val runnerClass = UpdateTargetActionRunner::class.java

    override fun addToStringBlurb(
        input: TaskerInput<SmartspacerTargetUpdateTaskerInput>,
        blurbBuilder: StringBuilder
    ) {
        blurbBuilder.append(
            context.getString(R.string.action_target_update_blurb, input.regular.name)
        )
    }

}

class UpdateTargetActionRunner: TaskerPluginRunnerActionNoOutput<SmartspacerTargetUpdateTaskerInput>(), KoinComponent {

    private val databaseRepository by inject<DatabaseRepository>()

    override fun run(
        context: Context,
        input: TaskerInput<SmartspacerTargetUpdateTaskerInput>
    ): TaskerPluginResult<Unit> {
        val smartspacerId = input.regular.smartspacerId
            ?: return TaskerPluginResultError(NullPointerException("Target to update has not been set"))
        val template = input.regular.targetTemplate
            ?: return TaskerPluginResultError(NullPointerException("Target has not been configured"))
        val variables = template.getVariables()
        val dynamic = input.dynamic.bundle
        val replacements = dynamic.keySet().filter {
            variables.contains(it)
        }.associateWith {
            dynamic.getString(it) ?: it
        }
        val refreshPeriod = input.regular.refreshPeriod.replaceWithReplacements(replacements)
            .toIntOrNull() ?: 0
        val refreshIfNotVisible = input.regular.refreshIfNotVisible
        databaseRepository.updateActiveTarget(
            context,
            template,
            smartspacerId,
            replacements,
            refreshPeriod,
            refreshIfNotVisible
        )
        return TaskerPluginResultSuccess()
    }

}

