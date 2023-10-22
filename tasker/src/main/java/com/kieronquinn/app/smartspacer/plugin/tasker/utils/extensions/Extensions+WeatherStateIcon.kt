package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions

import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.fonts.FrameWeatherVF.Icon
import com.kieronquinn.app.smartspacer.sdk.model.weather.WeatherData.WeatherStateIcon

fun WeatherStateIcon_validItems(): List<WeatherStateIcon> {
    return WeatherStateIcon.values().filterNot { it == WeatherStateIcon.UNKNOWN_ICON }
}

fun WeatherStateIcon_of(id: String): WeatherStateIcon? {
    val idToFind = id.toIntOrNull() ?: return null
    return WeatherStateIcon.values().firstOrNull { it.id == idToFind }
}

val WeatherStateIcon.glyph: Icon?
    get() {
        return when(this) {
            WeatherStateIcon.SUNNY -> Icon.SUNNY
            WeatherStateIcon.MOSTLY_SUNNY,
            WeatherStateIcon.PARTLY_CLOUDY -> Icon.PARTLY_CLOUDY
            WeatherStateIcon.DRIZZLE,
            WeatherStateIcon.HEAVY_RAIN,
            WeatherStateIcon.SHOWERS_RAIN,
            WeatherStateIcon.SCATTERED_SHOWERS_DAY,
            WeatherStateIcon.SCATTERED_SHOWERS_NIGHT -> Icon.RAIN
            WeatherStateIcon.HAZE_FOG_DUST_SMOKE -> Icon.HAZE
            WeatherStateIcon.MOSTLY_CLOUDY_DAY,
            WeatherStateIcon.MOSTLY_CLOUDY_NIGHT,
            WeatherStateIcon.CLOUDY -> Icon.CLOUDY
            WeatherStateIcon.CLEAR_NIGHT -> Icon.CLEAR
            WeatherStateIcon.SNOW_SHOWERS_SNOW -> Icon.SNOW
            WeatherStateIcon.FLURRIES,
            WeatherStateIcon.SCATTERED_SNOW_SHOWERS_DAY,
            WeatherStateIcon.SCATTERED_SNOW_SHOWERS_NIGHT,
            WeatherStateIcon.MIXED_RAIN_HAIL_RAIN_SLEET,
            WeatherStateIcon.SLEET_HAIL,
            WeatherStateIcon.WINTRY_MIX_RAIN_SNOW -> Icon.SNOW_SHOWERS
            WeatherStateIcon.ISOLATED_SCATTERED_TSTORMS_DAY,
            WeatherStateIcon.ISOLATED_SCATTERED_TSTORMS_NIGHT,
            WeatherStateIcon.STRONG_TSTORMS -> Icon.THUNDER
            WeatherStateIcon.BLIZZARD,
            WeatherStateIcon.BLOWING_SNOW,
            WeatherStateIcon.HEAVY_SNOW -> Icon.HEAVY_SNOW
            WeatherStateIcon.WINDY_BREEZY -> Icon.WINDY
            WeatherStateIcon.TORNADO -> Icon.TORNADO
            WeatherStateIcon.TROPICAL_STORM_HURRICANE -> Icon.HURRICANE
            WeatherStateIcon.MOSTLY_CLEAR_NIGHT,
            WeatherStateIcon.PARTLY_CLOUDY_NIGHT -> Icon.MOSTLY_CLEAR
            WeatherStateIcon.UNKNOWN_ICON -> null
        }
    }

val WeatherStateIcon.label: Int
    get() {
        return when(this) {
            WeatherStateIcon.SUNNY -> R.string.weather_state_icon_sunny
            WeatherStateIcon.MOSTLY_SUNNY -> R.string.weather_state_icon_mostly_sunny
            WeatherStateIcon.PARTLY_CLOUDY -> R.string.weather_state_icon_partly_cloudy
            WeatherStateIcon.DRIZZLE -> R.string.weather_state_icon_drizzle
            WeatherStateIcon.HEAVY_RAIN -> R.string.weather_state_icon_heavy_rain
            WeatherStateIcon.SHOWERS_RAIN -> R.string.weather_state_icon_showers_rain
            WeatherStateIcon.SCATTERED_SHOWERS_DAY -> R.string.weather_state_icon_scatted_showers_day
            WeatherStateIcon.SCATTERED_SHOWERS_NIGHT -> R.string.weather_state_icon_scatted_showers_night
            WeatherStateIcon.HAZE_FOG_DUST_SMOKE -> R.string.weather_state_icon_haze
            WeatherStateIcon.MOSTLY_CLOUDY_DAY -> R.string.weather_state_icon_mostly_cloudy_day
            WeatherStateIcon.MOSTLY_CLOUDY_NIGHT -> R.string.weather_state_icon_mostly_cloudy_night
            WeatherStateIcon.CLOUDY -> R.string.weather_state_icon_cloudy
            WeatherStateIcon.CLEAR_NIGHT -> R.string.weather_state_icon_clear
            WeatherStateIcon.SNOW_SHOWERS_SNOW -> R.string.weather_state_icon_snow
            WeatherStateIcon.FLURRIES -> R.string.weather_state_icon_flurries
            WeatherStateIcon.SCATTERED_SNOW_SHOWERS_DAY -> R.string.weather_state_icon_snow_showers_day
            WeatherStateIcon.SCATTERED_SNOW_SHOWERS_NIGHT -> R.string.weather_state_icon_snow_showers_night
            WeatherStateIcon.MIXED_RAIN_HAIL_RAIN_SLEET -> R.string.weather_state_icon_mixed_sleet_hail_rain
            WeatherStateIcon.SLEET_HAIL -> R.string.weather_state_icon_sleet
            WeatherStateIcon.WINTRY_MIX_RAIN_SNOW -> R.string.weather_state_icon_wintry
            WeatherStateIcon.ISOLATED_SCATTERED_TSTORMS_DAY -> R.string.weather_state_icon_scattered_thunderstorms_day
            WeatherStateIcon.ISOLATED_SCATTERED_TSTORMS_NIGHT -> R.string.weather_state_icon_scattered_thunderstorms_night
            WeatherStateIcon.STRONG_TSTORMS -> R.string.weather_state_icon_thunderstorms
            WeatherStateIcon.BLIZZARD -> R.string.weather_state_icon_blizzard
            WeatherStateIcon.BLOWING_SNOW -> R.string.weather_state_icon_blowing_snow
            WeatherStateIcon.HEAVY_SNOW -> R.string.weather_state_icon_heavy_snow
            WeatherStateIcon.WINDY_BREEZY -> R.string.weather_state_icon_breezy
            WeatherStateIcon.TORNADO -> R.string.weather_state_icon_tornado
            WeatherStateIcon.TROPICAL_STORM_HURRICANE -> R.string.weather_state_icon_hurricane
            WeatherStateIcon.MOSTLY_CLEAR_NIGHT -> R.string.weather_state_icon_mostly_clear
            WeatherStateIcon.PARTLY_CLOUDY_NIGHT -> R.string.weather_state_icon_partly_cloudy_night
            WeatherStateIcon.UNKNOWN_ICON -> R.string.weather_state_icon_unset
        }
    }