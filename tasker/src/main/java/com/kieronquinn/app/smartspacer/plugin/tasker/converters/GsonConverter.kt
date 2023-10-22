package com.kieronquinn.app.smartspacer.plugin.tasker.converters

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class GsonConverter<T>(clazz: Class<T>): KoinComponent {

    private val gson by inject<Gson>()
    private val setType = TypeToken.getParameterized(clazz).type

    open fun fromString(value: String): T {
        return gson.fromJson(value, setType)
    }

    open fun fromObject(obj: T): String {
        return gson.toJson(obj)
    }

}