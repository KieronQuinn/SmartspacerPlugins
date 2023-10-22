package com.kieronquinn.app.smartspacer.plugin.countdown.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.kieronquinn.app.smartspacer.plugin.countdown.model.Icon
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.JsonDeserializer
import java.lang.reflect.Type

object IconAdapter: JsonDeserializer<Icon>() {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Icon {
        val obj = json.asJsonObject
        val rawType = obj.get(Icon.NAME_TYPE).asString
        val type = Icon.Type.values().firstOrNull {
            it.name == rawType
        } ?: throw JsonParseException("Type was not specified or not recognised")
        return when(type) {
            Icon.Type.FONT -> context.deserialize<Icon.Font>(json)
            Icon.Type.FILE -> context.deserialize<Icon.File>(json)
        }
    }

}