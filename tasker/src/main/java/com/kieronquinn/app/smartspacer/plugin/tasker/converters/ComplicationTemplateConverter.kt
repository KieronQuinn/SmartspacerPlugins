package com.kieronquinn.app.smartspacer.plugin.tasker.converters

import androidx.room.TypeConverter
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate

object ComplicationTemplateConverter: GsonConverter<ComplicationTemplate>(
    ComplicationTemplate::class.java
) {

    @TypeConverter
    override fun fromObject(obj: ComplicationTemplate): String {
        return super.fromObject(obj)
    }

    @TypeConverter
    override fun fromString(value: String): ComplicationTemplate {
        return super.fromString(value)
    }

}