package com.kieronquinn.app.smartspacer.plugin.healthconnect.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ElevationGainedRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.PowerRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WheelchairPushesRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

enum class DataType(
    @StringRes
    val label: Int,
    @StringRes
    val description: Int,
    @DrawableRes
    val icon: Int,
    val record: KClass<out Record>,
    val type: AggregateMetric<Any>?,
    val settingsType: SettingsType,
    val unitType: KClass<out UnitType>? = null
) {

    ACTIVE_CALORIES_BURNED(
        R.string.data_types_active_calories_burned_title,
        R.string.data_types_active_calories_burned_description,
        R.drawable.ic_data_type_calories,
        ActiveCaloriesBurnedRecord::class,
        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
        SettingsType.RESET_DAY,
        Energy::class
    ),

    BLOOD_GLUCOSE(
        R.string.data_types_blood_glucose_title,
        R.string.data_types_blood_glucose_description,
        R.drawable.ic_data_type_glucose,
        BloodGlucoseRecord::class,
        null,
        SettingsType.TIMEOUT,
        Glucose::class
    ),

    BLOOD_PRESSURE(
        R.string.data_types_blood_pressure_title,
        R.string.data_types_blood_pressure_description,
        R.drawable.ic_data_type_blood_pressure,
        BloodPressureRecord::class,
        null,
        SettingsType.TIMEOUT
    ),

    BODY_TEMPERATURE(
        R.string.data_types_body_temperature_title,
        R.string.data_types_body_temperature_description,
        R.drawable.ic_data_type_temperature,
        BodyTemperatureRecord::class,
        null,
        SettingsType.TIMEOUT,
        Temperature::class
    ),

    DISTANCE(
        R.string.data_types_distance_title,
        R.string.data_types_distance_description,
        R.drawable.ic_data_type_distance,
        DistanceRecord::class,
        DistanceRecord.DISTANCE_TOTAL,
        SettingsType.RESET_DAY,
        Length::class
    ),

    ELEVATION(
        R.string.data_types_elevation_gained_title,
        R.string.data_types_elevation_gained_description,
        R.drawable.ic_data_type_elevation,
        ElevationGainedRecord::class,
        ElevationGainedRecord.ELEVATION_GAINED_TOTAL,
        SettingsType.RESET_DAY,
        Length::class
    ),

    FLOORS_CLIMBED(
        R.string.data_types_floors_climbed_title,
        R.string.data_types_floors_climbed_description,
        R.drawable.ic_data_type_floors,
        FloorsClimbedRecord::class,
        FloorsClimbedRecord.FLOORS_CLIMBED_TOTAL,
        SettingsType.RESET_DAY,
        Length::class
    ),

    HEART_RATE(
        R.string.data_types_heart_rate_title,
        R.string.data_types_heart_rate_description,
        R.drawable.ic_data_type_heart,
        HeartRateRecord::class,
        HeartRateRecord.BPM_AVG,
        SettingsType.TIMEOUT
    ),

    HEART_RATE_VARIABILITY(
        R.string.data_types_heart_rate_variability_title,
        R.string.data_types_heart_rate_variability_description,
        R.drawable.ic_data_type_heart_rate_variability,
        HeartRateVariabilityRmssdRecord::class,
        null,
        SettingsType.TIMEOUT
    ),

    HYDRATION(
        R.string.data_types_hydration_record_title,
        R.string.data_types_hydration_record_description,
        R.drawable.ic_data_type_hydration,
        HydrationRecord::class,
        HydrationRecord.VOLUME_TOTAL,
        SettingsType.RESET_DAY,
        Volume::class
    ),

    OXYGEN_SATURATION(
        R.string.data_types_oxygen_saturation_title,
        R.string.data_types_oxygen_saturation_description,
        R.drawable.ic_data_type_blood_oxygen,
        OxygenSaturationRecord::class,
        null,
        SettingsType.TIMEOUT
    ),

    POWER(
        R.string.data_types_power_title,
        R.string.data_types_power_description,
        R.drawable.ic_data_type_power,
        PowerRecord::class,
        PowerRecord.POWER_AVG,
        SettingsType.TIMEOUT,
        Power::class
    ),

    RESPIRATORY_RATE(
        R.string.data_types_respiratory_rate_title,
        R.string.data_types_respiratory_rate_description,
        R.drawable.ic_data_type_respiratory,
        RespiratoryRateRecord::class,
        null,
        SettingsType.TIMEOUT
    ),

    RESTING_HEART_RATE(
        R.string.data_types_resting_heart_rate_title,
        R.string.data_types_resting_heart_rate_description,
        R.drawable.ic_data_type_resting_heart_rate,
        RestingHeartRateRecord::class,
        RestingHeartRateRecord.BPM_AVG,
        SettingsType.TIMEOUT
    ),

    SLEEP(
        R.string.data_types_sleep_title,
        R.string.data_types_sleep_description,
        R.drawable.ic_data_type_sleep,
        SleepSessionRecord::class,
        SleepSessionRecord.SLEEP_DURATION_TOTAL,
        SettingsType.RESET_NIGHT
    ),

    SPEED(
        R.string.data_types_speed_title,
        R.string.data_types_speed_description,
        R.drawable.ic_data_type_speed,
        SpeedRecord::class,
        null,
        SettingsType.TIMEOUT,
        Velocity::class
    ),

    STEPS(
        R.string.data_types_steps_title,
        R.string.data_types_steps_description,
        R.drawable.ic_data_type_steps,
        StepsRecord::class,
        StepsRecord.COUNT_TOTAL,
        SettingsType.RESET_DAY
    ),

    TOTAL_CALORIES(
        R.string.data_types_total_calories_title,
        R.string.data_types_total_calories_description,
        R.drawable.ic_data_type_calories,
        TotalCaloriesBurnedRecord::class,
        TotalCaloriesBurnedRecord.ENERGY_TOTAL,
        SettingsType.RESET_DAY,
        Energy::class
    ),

    VO2_MAX(
        R.string.data_types_vo2_max_title,
        R.string.data_types_vo2_max_description,
        R.drawable.ic_data_type_blood_oxygen,
        Vo2MaxRecord::class,
        null,
        SettingsType.TIMEOUT
    ),

    WHEELCHAIR_PUSHES(
        R.string.data_types_wheelchair_pushes_title,
        R.string.data_types_wheelchair_pushes_description,
        R.drawable.ic_data_type_wheelchair_pushes,
        WheelchairPushesRecord::class,
        WheelchairPushesRecord.COUNT_TOTAL,
        SettingsType.RESET_DAY
    );

    fun getDefaultResetTime(): String? {
        return when(settingsType) {
            SettingsType.RESET_DAY -> LocalTime.MIDNIGHT
            SettingsType.RESET_NIGHT -> LocalTime.NOON
            else -> null
        }?.format(DateTimeFormatter.ISO_LOCAL_TIME)
    }

    fun getDefaultTimeout(): TimeoutPeriod? {
        return when(settingsType) {
            SettingsType.TIMEOUT -> TimeoutPeriod.SIXTY_MINUTES
            else -> null
        }
    }

}