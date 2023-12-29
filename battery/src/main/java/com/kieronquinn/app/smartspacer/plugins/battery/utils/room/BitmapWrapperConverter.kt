package com.kieronquinn.app.smartspacer.plugins.battery.utils.room

import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.kieronquinn.app.smartspacer.plugins.battery.model.BitmapWrapper
import java.io.File

object BitmapWrapperConverter {

    @TypeConverter
    fun fromString(value: String): BitmapWrapper {
        val bitmap = if(File(value).exists()) {
            BitmapFactory.decodeFile(value)
        }else null
        return BitmapWrapper(value, bitmap)
    }

    @TypeConverter
    fun fromBitmapWrapper(wrapper: BitmapWrapper): String {
        return wrapper.path
    }

}