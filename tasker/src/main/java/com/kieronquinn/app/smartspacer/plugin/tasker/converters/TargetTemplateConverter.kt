package com.kieronquinn.app.smartspacer.plugin.tasker.converters

import androidx.room.TypeConverter
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate

object TargetTemplateConverter: GsonConverter<TargetTemplate>(
    TargetTemplate::class.java
) {

    @TypeConverter
    override fun fromObject(obj: TargetTemplate): String {
        return super.fromObject(obj)
    }

    @TypeConverter
    override fun fromString(value: String): TargetTemplate {
        return super.fromString(value)
    }

}