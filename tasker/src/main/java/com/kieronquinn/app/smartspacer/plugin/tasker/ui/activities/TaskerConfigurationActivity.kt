package com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos

abstract class TaskerConfigurationActivity<T : Any>: ConfigurationActivity(), TaskerPluginConfig<T> {

    private var staticInputs: T? = null
    private val dynamicInputs = TaskerInputInfos()

    override val context: Context
        get() = applicationContext

    override val inputForTasker: TaskerInput<T>
        get() = TaskerInput(staticInputs!!, dynamicInputs)

    var taskerInput: T? = null

    fun setStaticInputs(input: T) {
        staticInputs = input
    }

    fun setDynamicInputs(inputs: TaskerInputInfos) {
        dynamicInputs.clear()
        dynamicInputs.addAll(inputs)
    }

    override fun assignFromInput(input: TaskerInput<T>) {
        taskerInput = input.regular
    }

}