package com.kieronquinn.app.smartspacer.plugin.tasker.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.JsonDeserializer
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Doorbell.DoorbellState
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Doorbell.DoorbellState.DoorbellStateType
import java.lang.reflect.Type

object DoorbellStateAdapter: JsonDeserializer<DoorbellState>() {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): DoorbellState {
        val obj = json.asJsonObject
        val rawType = obj.get(DoorbellState.NAME_TYPE).asString
        val type = DoorbellStateType.values().firstOrNull {
            it.name == rawType
        } ?: throw JsonParseException("Type was not specified or not recognised")
        return when(type) {
            DoorbellStateType.IMAGE_URI -> context.deserialize<DoorbellState.ImageUri>(json)
            DoorbellStateType.VIDEOCAM_OFF -> context.deserialize<DoorbellState.VideocamOff>(json)
            DoorbellStateType.IMAGE_BITMAP -> context.deserialize<DoorbellState.ImageBitmap>(json)
            DoorbellStateType.VIDEOCAM -> context.deserialize<DoorbellState.Videocam>(json)
            DoorbellStateType.LOADING -> context.deserialize<DoorbellState.Loading>(json)
            DoorbellStateType.LOADING_INDETERMINATE ->
                context.deserialize<DoorbellState.LoadingIndeterminate>(json)
        }
    }

}