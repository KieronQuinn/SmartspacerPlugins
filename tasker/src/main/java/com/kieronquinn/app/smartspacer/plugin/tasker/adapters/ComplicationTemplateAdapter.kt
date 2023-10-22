package com.kieronquinn.app.smartspacer.plugin.tasker.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.JsonDeserializer
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate.TemplateType
import java.lang.reflect.Type

object ComplicationTemplateAdapter: JsonDeserializer<ComplicationTemplate>() {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ComplicationTemplate {
        val obj = json.asJsonObject
        val rawType = obj.get(ComplicationTemplate.NAME_COMPLICATION_TYPE).asString
        val type = TemplateType.values().firstOrNull {
            it.name == rawType
        } ?: throw JsonParseException("Type was not specified or not recognised")
        return when(type) {
            TemplateType.BASIC -> context.deserialize<ComplicationTemplate.Basic>(json)
        }
    }

}