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
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerComplicationUpdateTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess as TaskerPluginResultSuccess

class UpdateComplicationAction(
    config: TaskerPluginConfig<SmartspacerComplicationUpdateTaskerInput>
): TaskerPluginConfigHelperNoOutput<SmartspacerComplicationUpdateTaskerInput, UpdateComplicationActionRunner>(config) {

    override val inputClass = SmartspacerComplicationUpdateTaskerInput::class.java
    override val runnerClass = UpdateComplicationActionRunner::class.java

    override fun addToStringBlurb(
        input: TaskerInput<SmartspacerComplicationUpdateTaskerInput>,
        blurbBuilder: StringBuilder
    ) {
        blurbBuilder.append(
            context.getString(R.string.action_complication_update_blurb, input.regular.name)
        )
    }

}

class UpdateComplicationActionRunner: TaskerPluginRunnerActionNoOutput<SmartspacerComplicationUpdateTaskerInput>(), KoinComponent {

    private val databaseRepository by inject<DatabaseRepository>()

    override fun run(
        context: Context,
        input: TaskerInput<SmartspacerComplicationUpdateTaskerInput>
    ): TaskerPluginResult<Unit> {
        val smartspacerId = input.regular.smartspacerId
            ?: return TaskerPluginResultError(NullPointerException("Complication to update has not been set"))
        val template = input.regular.complicationTemplate
            ?: return TaskerPluginResultError(NullPointerException("Complication has not been configured"))
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
        databaseRepository.updateActiveComplication(
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

