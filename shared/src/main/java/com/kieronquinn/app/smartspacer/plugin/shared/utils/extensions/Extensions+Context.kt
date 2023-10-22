package com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.AttrRes
import kotlin.math.max
import kotlin.math.min

val Context.isDarkMode: Boolean
    get() {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

fun Context.isLandscape(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun Context.getAttrColor(@AttrRes attr: Int): Int {
    val obtainStyledAttributes: TypedArray = obtainStyledAttributes(intArrayOf(attr))
    val color = obtainStyledAttributes.getColor(0, 0)
    obtainStyledAttributes.recycle()
    return color
}

fun Context.getDisplayPortraitWidth(): Int {
    return min(getDisplayWidth(), getDisplayHeight())
}

fun Context.getDisplayPortraitHeight(): Int {
    return max(getDisplayWidth(), getDisplayHeight())
}

@Suppress("DEPRECATION")
private fun Context.getDisplayWidth(): Int {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        windowManager.currentWindowMetrics.bounds.width()
    }else{
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

@Suppress("DEPRECATION")
private fun Context.getDisplayHeight(): Int {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        windowManager.currentWindowMetrics.bounds.height()
    }else{
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}