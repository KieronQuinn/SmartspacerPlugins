package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories

import android.content.Context
import android.location.Location
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.R
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.complications.SunriseComplication
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.complications.SunsetComplication
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository.SunriseSunsetState
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.utils.extensions.getLastLocation
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.shredzone.commons.suncalc.SunTimes
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime

interface SunriseSunsetRepository {

    fun calculateSunrise()
    fun calculateSunset()
    fun getSunriseState(): SunriseSunsetState?
    fun getSunsetState(): SunriseSunsetState?
    fun setSunriseState(state: SunriseSunsetState)
    fun setSunsetState(state: SunriseSunsetState)

    data class SunriseSunsetState(
        @SerializedName("show_before")
        val showBefore: ShowFor = ShowFor.ONE_HOUR,
        @SerializedName("today")
        val timeToday: Long? = null,
        @SerializedName("tomorrow")
        val timeTomorrow: Long? = null,
        @SerializedName("show_after")
        val showAfter: ShowFor = ShowFor.ONE_HOUR
    )

    enum class ShowFor(private val minutes: Int, @StringRes val label: Int) {
        TEN_MINUTES(10, R.string.show_for_10),
        FIFTEEN_MINUTES(15, R.string.show_for_15),
        THIRTY_MINUTES(30, R.string.show_for_30),
        ONE_HOUR(30, R.string.show_for_60),
        TWO_HOURS(120, R.string.show_for_120),
        SIX_HOURS(360, R.string.show_for_360),
        TWELVE_HOURS(720, R.string.show_for_720);

        val millis = minutes * 60_000L
    }

}

class SunriseSunsetRepositoryImpl(
    private val context: Context,
    private val gson: Gson,
    private val alarmRepository: AlarmRepository
): SunriseSunsetRepository, KoinComponent {

    private val sunriseFile = File(context.filesDir, "sunrise.json").apply {
        parentFile?.mkdirs()
    }

    private val sunsetFile = File(context.filesDir, "sunset.json").apply {
        parentFile?.mkdirs()
    }

    private val scope = MainScope()

    override fun calculateSunrise() {
        scope.launch(Dispatchers.IO) {
            calculateSunriseSunset(::getSunriseState, ::setSunriseState) { rise }
        }
    }

    override fun calculateSunset() {
        scope.launch(Dispatchers.IO) {
            calculateSunriseSunset(::getSunsetState, ::setSunsetState) { set }
        }
    }

    private suspend fun calculateSunriseSunset(
        read: () -> SunriseSunsetState?,
        write: (SunriseSunsetState) -> Unit,
        get: SunTimes.() -> ZonedDateTime?
    ) {
        val lastLocation = context.getLastLocation()
        val current = read() ?: SunriseSunsetState()
        val today = lastLocation?.getNextTime(get) { on(LocalDate.now()) }
        val tomorrow = lastLocation?.getNextTime(get) { on(LocalDate.now().plusDays(1)) }
        val state = current.copy(timeToday = today, timeTomorrow = tomorrow)
        write(state)
    }

    override fun getSunriseState(): SunriseSunsetState? {
        return sunriseFile.getSunriseSunsetState()
    }

    override fun getSunsetState(): SunriseSunsetState? {
        return sunsetFile.getSunriseSunsetState()
    }

    override fun setSunriseState(state: SunriseSunsetState) {
        sunriseFile.writeText(gson.toJson(state))
        alarmRepository.scheduleNextSunriseAlarm()
        SmartspacerComplicationProvider.notifyChange(context, SunriseComplication::class.java)
    }

    override fun setSunsetState(state: SunriseSunsetState) {
        sunsetFile.writeText(gson.toJson(state))
        alarmRepository.scheduleNextSunsetAlarm()
        SmartspacerComplicationProvider.notifyChange(context, SunsetComplication::class.java)
    }

    private fun File.getSunriseSunsetState(): SunriseSunsetState? {
        return try {
            gson.fromJson(readText(), SunriseSunsetState::class.java)
        }catch (e: Exception){
            null
        }
    }

    private fun Location.getNextTime(
        get: SunTimes.() -> ZonedDateTime?,
        params: SunTimes.Parameters.() -> SunTimes.Parameters
    ): Long? {
        return get(getSunTimes(params))?.toInstant()?.toEpochMilli()
    }

    private fun Location.getSunTimes(
        params: SunTimes.Parameters.() -> SunTimes.Parameters
    ): SunTimes {
        return SunTimes.compute().at(latitude, longitude).params().execute()
    }

}