package com.kieronquinn.app.smartspacer.plugin.tasker.utils

import android.content.Context
import android.text.format.DateFormat
import java.time.Instant
import java.util.Date

class DateTimeFormatter(context: Context) {

    companion object {
        fun Instant.format(dateTimeFormatter: DateTimeFormatter): String {
            return dateTimeFormatter.format(this)
        }
    }

    private val dateFormat = DateFormat.getDateFormat(context)
    private val timeFormat = DateFormat.getTimeFormat(context)

    fun format(instant: Instant): String {
        val date = Date.from(instant)
        return "${dateFormat.format(date)}, ${timeFormat.format(date)}"
    }

}