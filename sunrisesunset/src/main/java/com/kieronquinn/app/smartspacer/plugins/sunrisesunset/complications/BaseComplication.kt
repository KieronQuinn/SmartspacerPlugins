package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.complications

import android.content.Intent
import android.text.format.DateFormat
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.R
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.AlarmRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository.SunriseSunsetState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import java.time.Instant
import java.util.Date
import android.graphics.drawable.Icon as AndroidIcon
import java.text.DateFormat as JavaDateFormat

abstract class BaseComplication: SmartspacerComplicationProvider() {

    private val sunriseSunsetRepository by inject<SunriseSunsetRepository>()
    private val alarmRepository by inject<AlarmRepository>()

    abstract val type: String
    abstract val label: Int
    abstract val description: Int
    abstract val icon: Int
    abstract val settingsIntent: Intent

    private val timeFormat by lazy {
        DateFormat.getTimeFormat(provideContext())
    }

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        if(!alarmRepository.hasAlarmPermission()) {
            //This is fatal, we cannot schedule updates without the permission
            return listOf(getErrorAction(smartspacerId))
        }
        val state = sunriseSunsetRepository.getState()
        if(state == null) {
            sunriseSunsetRepository.loadState()
            return emptyList()
        }
        val time = state.timeToday?.takeIfAfterNow() ?: state.timeTomorrow?.takeIfAfterNow()
            ?: return emptyList()
        if(!state.shouldShow(time)) return emptyList()
        return listOfNotNull(state.createAction(smartspacerId))
    }

    private fun SunriseSunsetState.createAction(smartspacerId: String): SmartspaceAction? {
        if(timeToday == null) return null
        val formattedTime = timeFormat.formatDate(timeToday)
        return ComplicationTemplate.Basic(
            "${smartspacerId}_${type}_at_${System.currentTimeMillis()}",
            Icon(AndroidIcon.createWithResource(provideContext(), icon)),
            Text(formattedTime),
            TapAction()
        ).create()
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(label),
            resources.getString(description),
            AndroidIcon.createWithResource(provideContext(), icon),
            configActivity = settingsIntent
        )
    }

    private fun SunriseSunsetState.shouldShow(time: Long): Boolean {
        val now = System.currentTimeMillis()
        val show = time - showBefore.millis
        val hide = time + showAfter.millis
        return now in show..hide
    }

    private fun Long.takeIfAfterNow(): Long? {
        return takeIf { it > System.currentTimeMillis() }
    }

    private fun getErrorAction(smartspacerId: String): SmartspaceAction {
        return ComplicationTemplate.Basic(
            "${smartspacerId}_${type}_error",
            Icon(AndroidIcon.createWithResource(provideContext(), icon)),
            Text(resources.getString(R.string.complication_error)),
            TapAction(intent = settingsIntent)
        ).create()
    }

    abstract fun SunriseSunsetRepository.getState(): SunriseSunsetState?
    abstract fun SunriseSunsetRepository.loadState()

    private fun JavaDateFormat.formatDate(time: Long): String {
        return format(Date.from(Instant.ofEpochMilli(time)))
    }

}