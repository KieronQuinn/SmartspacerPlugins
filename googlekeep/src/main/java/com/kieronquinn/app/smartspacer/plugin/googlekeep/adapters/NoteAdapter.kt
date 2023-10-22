package com.kieronquinn.app.smartspacer.plugin.googlekeep.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.kieronquinn.app.smartspacer.plugin.googlekeep.model.Note
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.JsonDeserializer
import java.lang.reflect.Type

object NoteAdapter: JsonDeserializer<Note>() {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Note {
        val obj = json.asJsonObject
        val rawType = obj.get(Note.NAME_TYPE).asString
        val type = Note.Type.values().firstOrNull {
            it.name == rawType
        } ?: throw JsonParseException("Type was not specified or not recognised")
        return when(type) {
            Note.Type.REGULAR -> context.deserialize<Note.RegularNote>(json)
            Note.Type.LIST -> context.deserialize<Note.ListNote>(json)
        }
    }

}