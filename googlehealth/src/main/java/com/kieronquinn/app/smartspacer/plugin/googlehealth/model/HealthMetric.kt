package com.kieronquinn.app.smartspacer.plugin.googlehealth.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.googlehealth.R

enum class HealthMetric(
    @StringRes val label: Int,
    val icon: String,
    @DrawableRes val iconFilled: Int,
    @DrawableRes val iconOutline: Int = iconFilled
) {
    STEPS(
        R.string.metric_steps,
        "gs_steps_fill1_vd_24",
        R.drawable.gs_steps_fill1_vd_24,
        R.drawable.gs_steps_vd_theme_24
    ),
    RUN_DISTANCE(
        R.string.metric_run_distance,
        "gs_fitbit_run_fill1_vd_24",
        R.drawable.gs_fitbit_run_fill1_vd_24
    ),
    DISTANCE(
        R.string.metric_distance,
        "gs_distance_fill1_vd_24",
        R.drawable.gs_distance_fill1_vd_24,
        R.drawable.gs_distance_vd_theme_24
    ),
    EXERCISE_DAYS(
        R.string.metric_exercise_days,
        "gs_exercise_fill1_vd_theme_24",
        R.drawable.gs_exercise_fill1_vd_theme_24,
        R.drawable.gs_exercise_vd_theme_24
    ),
    ACTIVE_ZONE_MINUTES(
        R.string.metric_active_zone_minutes,
        "gs_azm_fill1_vd_24",
        R.drawable.gs_azm_fill1_vd_24,
        R.drawable.gs_azm_vd_theme_24
    ),
    CALORIES_BURNED(
        R.string.metric_calories_burned,
        "gs_fitbit_calories_fill1_vd_theme_24",
        R.drawable.gs_fitbit_calories_fill1_vd_theme_24,
        R.drawable.gs_fitbit_calories_vd_theme_24
    ),
    FLOORS(
        R.string.metric_floors,
        "gs_floor_fill1_vd_24",
        R.drawable.gs_floor_fill1_vd_24
    ),
    READINESS(
        R.string.metric_readiness,
        "gs_fitbit_readiness_fill1_vd_theme_24",
        R.drawable.gs_fitbit_readiness_fill1_vd_theme_24
    ),
    HEART_RATE(
        R.string.metric_heart_rate,
        "gs_fitbit_heart_rate_fill1_vd_theme_24",
        R.drawable.gs_fitbit_heart_rate_fill1_vd_theme_24,
        R.drawable.gs_fitbit_heart_rate_vd_theme_24
    ),
    CARDIO_LOAD(
        R.string.metric_cardio_load,
        "gs_cardio_load_fill1_vd_theme_24",
        R.drawable.gs_cardio_load_fill1_vd_theme_24,
        R.drawable.gs_cardio_load_vd_theme_24
    ),
    SLEEP(
        R.string.metric_sleep,
        "gs_sleep_auto_fill1_vd_theme_24",
        R.drawable.gs_sleep_auto_fill1_vd_theme_24,
        R.drawable.gs_sleep_auto_vd_theme_24
    ),
    CALORIES(
        R.string.metric_calories,
        "gs_nutrition_fill1_vd_theme_24",
        R.drawable.gs_nutrition_fill1_vd_theme_24,
        R.drawable.gs_nutrition_vd_theme_24
    ),
    HYDRATION(
        R.string.metric_hydration,
        "gs_water_full_fill1_vd_theme_24",
        R.drawable.gs_water_full_fill1_vd_theme_24,
        R.drawable.gs_water_full_vd_theme_24
    ),
    WEIGHT(
        R.string.metric_weight,
        "gs_monitor_weight_fill1_vd_theme_24",
        R.drawable.gs_monitor_weight_fill1_vd_theme_24,
        R.drawable.gs_monitor_weight_vd_theme_24
    );

    companion object {
        fun forIcon(icon: String) = entries.firstOrNull { it.icon == icon }
    }
}