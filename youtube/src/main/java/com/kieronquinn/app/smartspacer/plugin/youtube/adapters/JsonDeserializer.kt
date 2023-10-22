package com.kieronquinn.app.smartspacer.plugin.youtube.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

abstract class JsonDeserializer<T>: JsonDeserializer<T> {

    protected inline fun <reified T> JsonDeserializationContext.deserialize(json: JsonElement): T {
        val type = TypeToken.getParameterized(T::class.java).type
        return deserialize(json, type)
    }

}