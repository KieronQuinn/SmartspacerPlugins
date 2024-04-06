package com.kieronquinn.app.smartspacer.plugin.controls.model

import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.controls.R

enum class ControlTapAction(@StringRes val label: Int) {
    SHOW_CONTROL(R.string.control_tap_action_show_control),
    OPEN_PANEL(R.string.control_tap_action_open_panel),
    OPEN_APP(R.string.control_tap_action_open_app),
    //Stateless Template
    COMMAND(R.string.control_tap_action_command),
    //Toggle Template
    BOOLEAN(R.string.control_tap_action_boolean),
    //Temperature Control Template
    MODE_SET(R.string.control_tap_action_mode_set),
    //Range Template
    FLOAT(R.string.control_tap_action_float)
}