package com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions

import android.content.res.Resources

fun Resources.dip(value: Int): Int = (value * displayMetrics.density).toInt()

val Int.dp
    get() = Resources.getSystem().dip(this)