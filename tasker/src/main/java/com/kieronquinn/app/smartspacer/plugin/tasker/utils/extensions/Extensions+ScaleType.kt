package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions

import android.widget.ImageView.ScaleType
import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.tasker.R

@StringRes
fun ScaleType.label(): Int {
    return when(this){
        ScaleType.MATRIX -> R.string.scale_type_matrix
        ScaleType.FIT_XY -> R.string.scale_type_fit_xy
        ScaleType.FIT_START -> R.string.scale_type_fit_start
        ScaleType.FIT_CENTER -> R.string.scale_type_fit_center
        ScaleType.FIT_END -> R.string.scale_type_fit_end
        ScaleType.CENTER -> R.string.scale_type_center
        ScaleType.CENTER_CROP -> R.string.scale_type_center_crop
        ScaleType.CENTER_INSIDE -> R.string.scale_type_center_inside
    }
}