package com.kieronquinn.app.smartspacer.plugin.controls.model

import android.service.controls.templates.TemperatureControlTemplate
import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.controls.R

enum class ControlMode(@StringRes val label: Int, val mode: Int, val modeFlag: Int) {
    MODE_OFF(R.string.control_template_state_off, TemperatureControlTemplate.MODE_OFF, TemperatureControlTemplate.FLAG_MODE_OFF),
    MODE_HEAT(R.string.control_template_state_heat, TemperatureControlTemplate.MODE_HEAT, TemperatureControlTemplate.FLAG_MODE_HEAT),
    MODE_COOL(R.string.control_template_state_cool, TemperatureControlTemplate.MODE_COOL, TemperatureControlTemplate.FLAG_MODE_COOL),
    MODE_HEAT_COOL(R.string.control_template_state_heat_cool, TemperatureControlTemplate.MODE_HEAT_COOL, TemperatureControlTemplate.FLAG_MODE_HEAT_COOL),
    MODE_ECO(R.string.control_template_state_eco, TemperatureControlTemplate.MODE_ECO, TemperatureControlTemplate.FLAG_MODE_ECO);

    companion object {
        fun fromMode(mode: Int): ControlMode? {
            return entries.firstOrNull { it.mode == mode }
        }
    }
}