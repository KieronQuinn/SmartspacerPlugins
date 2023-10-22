package com.kieronquinn.app.smartspacer.plugin.tasker.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.JsonDeserializer
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TemplateType
import java.lang.reflect.Type

object TargetTemplateAdapter: JsonDeserializer<TargetTemplate>() {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TargetTemplate {
        val obj = json.asJsonObject
        val rawType = obj.get(TargetTemplate.NAME_TARGET_TYPE).asString
        val type = TemplateType.values().firstOrNull {
            it.name == rawType
        } ?: throw JsonParseException("Type was not specified or not recognised")
        return when(type) {
            TemplateType.BASIC -> context.deserialize<TargetTemplate.Basic>(json)
            TemplateType.HEAD_TO_HEAD -> context.deserialize<TargetTemplate.HeadToHead>(json)
            TemplateType.BUTTON -> context.deserialize<TargetTemplate.Button>(json)
            TemplateType.LIST_ITEMS -> context.deserialize<TargetTemplate.ListItems>(json)
            TemplateType.LOYALTY_CARD -> context.deserialize<TargetTemplate.LoyaltyCard>(json)
            TemplateType.IMAGE -> context.deserialize<TargetTemplate.Image>(json)
            TemplateType.DOORBELL -> context.deserialize<TargetTemplate.Doorbell>(json)
            TemplateType.IMAGES -> context.deserialize<TargetTemplate.Images>(json)
            TemplateType.CAROUSEL -> context.deserialize<TargetTemplate.Carousel>(json)
        }
    }

}