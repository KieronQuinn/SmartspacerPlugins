package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions

import android.text.TextUtils.TruncateAt
import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.tasker.R

@StringRes
fun TruncateAt.label(): Int? {
    return when(this){
        TruncateAt.START -> R.string.truncate_at_start
        TruncateAt.MIDDLE -> R.string.truncate_at_middle
        TruncateAt.END -> R.string.truncate_at_end
        TruncateAt.MARQUEE -> R.string.truncate_at_marquee
        else -> null
    }
}