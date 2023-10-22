package com.kieronquinn.app.smartspacer.plugin.tasker.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.JsonDeserializer
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon.IconType
import java.lang.reflect.Type

object IconAdapter: JsonDeserializer<Icon>() {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Icon {
        val obj = json.asJsonObject
        val rawType = obj.get(Icon.NAME_TYPE).asString
        val type = IconType.values().firstOrNull {
            it.name == rawType
        } ?: throw JsonParseException("Type was not specified or not recognised")
        return when(type) {
            IconType.URL -> context.deserialize<Icon.Url>(json)
            IconType.BITMAP -> context.deserialize<Icon.Bitmap>(json)
            IconType.FONT -> context.deserialize<Icon.Font>(json)
        }
    }

}