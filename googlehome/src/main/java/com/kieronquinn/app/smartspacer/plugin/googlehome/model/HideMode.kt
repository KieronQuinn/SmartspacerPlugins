package com.kieronquinn.app.smartspacer.plugin.googlehome.model

import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.googlehome.R

enum class HideMode(@StringRes val label: Int) {
    DISABLED(R.string.hide_mode_disabled),
    WHEN_ON(R.string.hide_mode_on),
    WHEN_OFF(R.string.hide_mode_off)
}