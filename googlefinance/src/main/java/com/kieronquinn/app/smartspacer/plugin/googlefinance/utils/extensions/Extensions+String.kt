package com.kieronquinn.app.smartspacer.plugin.googlefinance.utils.extensions

import java.text.NumberFormat
import java.util.Locale

fun String.parseTrend(): Double? {
    val format = NumberFormat.getInstance(Locale.getDefault())
    //Remove percentage from start and plus, but keep minus
    val normalised = replace("%", "")
        .replace("+", "")
    return try {
        format.parse(normalised)?.toDouble()
    }catch (e: NumberFormatException){
        null
    }
}