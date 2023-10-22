package com.kieronquinn.app.smartspacer.plugin.youtube.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeRepository.SubscriberCount
import java.lang.reflect.Type

object SubscriberCountAdapter: JsonDeserializer<SubscriberCount>() {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SubscriberCount {
        val obj = json.asJsonObject
        val rawType = obj.get(SubscriberCount.NAME_TYPE).asString
        val type = SubscriberCount.Type.values().firstOrNull {
            it.name == rawType
        } ?: throw JsonParseException("Type was not specified or not recognised")
        return when(type) {
            SubscriberCount.Type.COUNT -> context.deserialize<SubscriberCount.Count>(json)
            SubscriberCount.Type.ERROR -> context.deserialize<SubscriberCount.Error>(json)
            SubscriberCount.Type.INVALID_ID -> context.deserialize<SubscriberCount.InvalidId>(json)
            SubscriberCount.Type.INVALID_API_KEY -> context.deserialize<SubscriberCount.InvalidApiKey>(json)
            SubscriberCount.Type.HIDDEN -> context.deserialize<SubscriberCount.Hidden>(json)
        }
    }

}