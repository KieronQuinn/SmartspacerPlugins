package com.kieronquinn.app.smartspacer.plugin.tasker.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.JsonDeserializer
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction.TapActionType
import java.lang.reflect.Type

object TapActionAdapter: JsonDeserializer<TapAction>() {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TapAction {
        val obj = json.asJsonObject
        val rawType = obj.get(TapAction.NAME_TYPE).asString
        val type = TapActionType.values().firstOrNull {
            it.name == rawType
        } ?: throw JsonParseException("Type was not specified or not recognised")
        return when(type) {
            TapActionType.URL -> context.deserialize<TapAction.Url>(json)
            TapActionType.LAUNCH_APP -> context.deserialize<TapAction.LaunchApp>(json)
            TapActionType.TASKER_EVENT -> context.deserialize<TapAction.TaskerEvent>(json)
        }
    }

}