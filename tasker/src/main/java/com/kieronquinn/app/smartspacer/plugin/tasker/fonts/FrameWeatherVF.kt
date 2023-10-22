package com.kieronquinn.app.smartspacer.plugin.tasker.fonts

import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.char
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.ITypeface
import java.util.LinkedList

/**
 *  Weather font used by SystemUI on the lockscreen/AoD on Pixel Android 14 with the "Weather" lock
 *  screen style set.
 */
object FrameWeatherVF: ITypeface {

    override val author: String
        get() = "Alex Tomlinson"

    override val description: String
        get() = "Weather icons used by SystemUI"

    override val fontName: String
        get() = "Frame Weather"

    override val fontRes: Int
        get() = R.font.frame_weather_vf

    override val iconCount: Int
        get() = characters.size

    override val icons: List<String>
        get() = characters.keys.toCollection(LinkedList())

    override val license: String
        get() = "Apache 2.0"

    override val licenseUrl: String
        get() = "https://www.apache.org/licenses/LICENSE-2.0"

    override val mappingPrefix: String
        get() = "frw"

    override val url: String
        get() = "https://alex.gd"

    override val version: String
        get() = "1.0"

    override val characters: Map<String, Char> by lazy {
        Icon.values().associate { it.name to it.character }
    }

    override fun getIcon(key: String): IIcon = Icon.valueOf(key)

    enum class Icon constructor(
        override val character: Char,
        val label: Int
    ) : IIcon {
        SUNNY("a".char(), R.string.weather_state_icon_sunny),
        PARTLY_CLOUDY("b".char(), R.string.weather_state_icon_partly_cloudy),
        RAIN("c".char(), R.string.weather_state_icon_rain),
        HAZE("d".char(), R.string.weather_state_icon_haze),
        CLOUDY("e".char(), R.string.weather_state_icon_cloudy),
        CLEAR("f".char(), R.string.weather_state_icon_clear),
        SNOW("g".char(), R.string.weather_state_icon_snow),
        SNOW_SHOWERS("h".char(), R.string.weather_state_icon_snow_showers),
        THUNDER("i".char(), R.string.weather_state_icon_thunderstorms),
        HEAVY_SNOW("j".char(), R.string.weather_state_icon_heavy_snow),
        WINDY("k".char(), R.string.weather_state_icon_windy),
        TORNADO("l".char(), R.string.weather_state_icon_tornado),
        HURRICANE("m".char(), R.string.weather_state_icon_hurricane),
        MOSTLY_CLEAR("n".char(), R.string.weather_state_icon_mostly_clear);

        override val typeface: ITypeface by lazy { FrameWeatherVF }
    }

}