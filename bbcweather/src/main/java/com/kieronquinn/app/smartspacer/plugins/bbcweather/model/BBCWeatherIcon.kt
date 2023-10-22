package com.kieronquinn.app.smartspacer.plugins.bbcweather.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugins.bbcweather.BBCWeatherPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugins.bbcweather.BuildConfig
import com.kieronquinn.app.smartspacer.sdk.model.weather.WeatherData.WeatherStateIcon
import com.kieronquinn.app.smartspacer.sdk.utils.getResourceForIdentifier

/**
 *  Mapping for BBC Weather icons to their icons and [WeatherStateIcon]s.
 */
enum class BBCWeatherIcon(
    private val iconIndex: Int,
    val state: WeatherStateIcon
) {

    CLEAR_SKY(0, WeatherStateIcon.CLEAR_NIGHT),
    SUNNY(1, WeatherStateIcon.SUNNY),
    SUNNY_INTERVALS(3, WeatherStateIcon.PARTLY_CLOUDY),
    SANDSTORM(4, WeatherStateIcon.HAZE_FOG_DUST_SMOKE),
    MIST(5, WeatherStateIcon.HAZE_FOG_DUST_SMOKE),
    FOG(6, WeatherStateIcon.HAZE_FOG_DUST_SMOKE),
    LIGHT_CLOUD(7, WeatherStateIcon.MOSTLY_CLOUDY_DAY),
    LIGHT_CLOUD_NIGHT(2, WeatherStateIcon.MOSTLY_CLOUDY_NIGHT),
    THICK_CLOUD(8, WeatherStateIcon.CLOUDY),
    LIGHT_RAIN_SHOWER(10, WeatherStateIcon.SHOWERS_RAIN),
    LIGHT_RAIN_SHOWER_NIGHT(9, WeatherStateIcon.SHOWERS_RAIN),
    DRIZZLE(11, WeatherStateIcon.DRIZZLE),
    LIGHT_RAIN(12, WeatherStateIcon.SHOWERS_RAIN),
    HEAVY_RAIN_SHOWER(14, WeatherStateIcon.SHOWERS_RAIN),
    HEAVY_RAIN_SHOWER_NIGHT(13, WeatherStateIcon.SHOWERS_RAIN),
    HEAVY_RAIN(15, WeatherStateIcon.HEAVY_RAIN),
    SLEET_SHOWER(17, WeatherStateIcon.WINTRY_MIX_RAIN_SNOW),
    SLEET_SHOWER_NIGHT(16, WeatherStateIcon.WINTRY_MIX_RAIN_SNOW),
    SLEET(18, WeatherStateIcon.SLEET_HAIL),
    HAIL_SHOWER(20, WeatherStateIcon.SLEET_HAIL),
    HAIL_SHOWER_NIGHT(19, WeatherStateIcon.SLEET_HAIL),
    HAIL(21, WeatherStateIcon.SLEET_HAIL),
    LIGHT_SNOW_SHOWER(23, WeatherStateIcon.SNOW_SHOWERS_SNOW),
    LIGHT_SNOW_SHOWER_NIGHT(22, WeatherStateIcon.SNOW_SHOWERS_SNOW),
    LIGHT_SNOW(24, WeatherStateIcon.SNOW_SHOWERS_SNOW),
    HEAVY_SNOW_SHOWER(26, WeatherStateIcon.SNOW_SHOWERS_SNOW),
    HEAVY_SNOW_SHOWER_NIGHT(25, WeatherStateIcon.SNOW_SHOWERS_SNOW),
    HEAVY_SNOW(27, WeatherStateIcon.HEAVY_SNOW),
    THUNDERY_SHOWER(29, WeatherStateIcon.ISOLATED_SCATTERED_TSTORMS_DAY),
    THUNDERY_SHOWER_NIGHT(28, WeatherStateIcon.ISOLATED_SCATTERED_TSTORMS_NIGHT),
    THUNDERSTORM(30, WeatherStateIcon.STRONG_TSTORMS),
    TROPICAL_STORM(31, WeatherStateIcon.TROPICAL_STORM_HURRICANE),
    HAZY(32, WeatherStateIcon.HAZE_FOG_DUST_SMOKE);

    private val iconMedium
        get() = "$ICON_PREFIX_MEDIUM$iconIndex"

    private val iconSmall
        get() = "$ICON_PREFIX_SMALL$iconIndex"

    private val iconRegular
        get() = "$ICON_PREFIX_ICON_REGULAR$iconIndex"

    private val iconAod
        get() = "$ICON_PREFIX_ICON_AOD$iconIndex"

    companion object {
        private const val IDENTIFIER_TEMPLATE_BBC = "$PACKAGE_NAME:drawable/"
        private const val IDENTIFIER_TEMPLATE_ICON = "${BuildConfig.APPLICATION_ID}:drawable/"
        private const val ICON_PREFIX_MEDIUM = "weathertype_medium_"
        private const val ICON_PREFIX_SMALL = "weathertype_small_"
        private const val ICON_PREFIX_ICON_REGULAR = "ic_weather_regular_"
        private const val ICON_PREFIX_ICON_AOD = "ic_weather_aod_"

        fun getWeatherIconBasedOnMedium(context: Context, icon: Bitmap): BBCWeatherIcon? {
            return values().firstNotNullOfOrNull {
                val expected = it.getMediumIcon(context) ?: return@firstNotNullOfOrNull null
                it.takeIf { icon.sameAs(expected) }
            }
        }

        fun getWeatherIconBasedOnSmall(context: Context, icon: Bitmap): BBCWeatherIcon? {
            return values().firstNotNullOfOrNull {
                val expected = it.getSmallIcon(context) ?: return@firstNotNullOfOrNull null
                it.takeIf { icon.sameAs(expected) }
            }
        }
    }

    fun getRegularIcon(context: Context): Bitmap? {
        val identifier = context
            .getResourceForIdentifier(IDENTIFIER_TEMPLATE_ICON + iconRegular)
            ?: return null
        return ContextCompat.getDrawable(context, identifier)?.toBitmap()
    }

    fun getAodIcon(context: Context): Bitmap? {
        val identifier = context
            .getResourceForIdentifier(IDENTIFIER_TEMPLATE_ICON + iconAod)
            ?: return null
        return ContextCompat.getDrawable(context, identifier)?.toBitmap()
    }

    fun getMediumIcon(context: Context): Bitmap? {
        val identifier = context
            .getResourceForIdentifier(IDENTIFIER_TEMPLATE_BBC + iconMedium)
            ?: return null
        return BitmapFactory.decodeResource(context.resources, identifier)
    }

    fun getSmallIcon(context: Context): Bitmap? {
        val identifier = context
            .getResourceForIdentifier(IDENTIFIER_TEMPLATE_BBC + iconSmall)
            ?: return null
        return BitmapFactory.decodeResource(context.resources, identifier)
    }

}