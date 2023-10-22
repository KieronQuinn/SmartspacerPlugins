package com.kieronquinn.app.smartspacer.plugins.datausage.utils.extensions

import java.time.Month

fun Month.getDayOrMax(day: Int): Int {
    return day.coerceAtMost(maxLength())
}