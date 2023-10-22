package com.kieronquinn.app.smartspacer.plugin.healthconnect.model

import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R

interface UnitType {
    val name: String?
    @get:StringRes
    val nameRes: Int
    @get:StringRes
    val description: Int?
}

enum class Energy(override val nameRes: Int, override val description: Int? = null): UnitType {
    CALORIES(R.string.unit_type_energy_calories_title),
    KILOCALORIES(R.string.unit_type_energy_kilocalories_title),
    JOULES(R.string.unit_type_energy_joules_title),
    KILOJOULES(R.string.unit_type_energy_kilojoules_title),
}

enum class Glucose(override val nameRes: Int, override val description: Int): UnitType {
    MMOL(
        R.string.unit_type_blood_glucose_mmol_title,
        R.string.unit_type_blood_glucose_mmol_description
    ),
    MGDL(
        R.string.unit_type_blood_glucose_mgdl_title,
        R.string.unit_type_blood_glucose_mgdl_description
    )
}

enum class Temperature(override val nameRes: Int, override val description: Int? = null): UnitType {
    CELSIUS(R.string.unit_type_blood_temperature_celsius),
    FAHRENHEIT(R.string.unit_type_blood_temperature_fahrenheit),
}

enum class Length(override val nameRes: Int, override val description: Int? = null): UnitType {
    METERS(R.string.unit_type_length_meters),
    KILOMETERS(R.string.unit_type_length_kilometers),
    MILES(R.string.unit_type_length_miles),
    INCHES(R.string.unit_type_length_inches),
    FEET(R.string.unit_type_length_feet)
}

enum class Volume(override val nameRes: Int, override val description: Int? = null): UnitType {
    LITERS(R.string.unit_type_volume_liters),
    MILLILITERS(R.string.unit_type_volume_milliliters),
    FLUID_OZ(R.string.unit_type_volume_fluid_oz)
}

enum class Power(override val nameRes: Int, override val description: Int? = null): UnitType {
    WATTS(R.string.unit_type_power_watts),
    KILOCALORIES(R.string.unit_type_power_kilocalories)
}

enum class Velocity(override val nameRes: Int, override val description: Int? = null): UnitType {
    METERS_PER_SECOND(R.string.unit_type_velocity_meters_per_second),
    KILOMETERS_PER_HOUR(R.string.unit_type_velocity_kilometers_per_hour),
    MILES_PER_HOUR(R.string.unit_type_velocity_miles_per_hour)
}