package com.kieronquinn.app.smartspacer.plugins.bbcweather.repositories

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.dp
import com.kieronquinn.app.smartspacer.plugins.bbcweather.complications.BBCWeatherComplication
import com.kieronquinn.app.smartspacer.plugins.bbcweather.model.BBCWeatherIcon
import com.kieronquinn.app.smartspacer.plugins.bbcweather.model.ComplicationState
import com.kieronquinn.app.smartspacer.plugins.bbcweather.model.TargetState
import com.kieronquinn.app.smartspacer.plugins.bbcweather.targets.BBCWeatherTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

interface BBCWeatherRepository {

    fun setComplicationState(
        bbcWeatherContext: Context,
        temperature: String,
        icon: Bitmap,
        contentDescription: String
    )

    fun getComplicationState(): ComplicationState?

    fun setTargetState(bbcWeatherContext: Context, targetState: TargetState)
    fun getTargetState(): TargetState?

}

class BBCWeatherRepositoryImpl(
    private val gson: Gson,
    private val context: Context
): BBCWeatherRepository {

    companion object {
        private val SIZE_CAROUSEL_ICON = 24.dp
    }

    private val complicationStateFile = File(context.filesDir, "complication.json").apply {
        parentFile?.mkdirs()
    }

    private val targetStateFile = File(context.filesDir, "target.json").apply {
        parentFile?.mkdirs()
    }

    private val scope = MainScope()

    override fun setComplicationState(
        bbcWeatherContext: Context,
        temperature: String,
        icon: Bitmap,
        contentDescription: String
    ) {
        scope.launch(Dispatchers.IO) {
            val bbcWeatherIcon = BBCWeatherIcon.getWeatherIconBasedOnMedium(bbcWeatherContext, icon)
                ?: return@launch
            val notificationIcon = bbcWeatherIcon.getAodIcon(context) ?: return@launch
            val state = ComplicationState(
                notificationIcon, temperature, contentDescription, bbcWeatherIcon.state.name
            )
            complicationStateFile.writeText(gson.toJson(state))
            SmartspacerComplicationProvider.notifyChange(context, BBCWeatherComplication::class.java)
        }
    }

    override fun getComplicationState(): ComplicationState? {
        return try {
            gson.fromJson(complicationStateFile.readText(), ComplicationState::class.java)
        }catch (e: Exception){
            null
        }
    }

    override fun setTargetState(bbcWeatherContext: Context, targetState: TargetState) {
        scope.launch {
            val converted = targetState.convertIcons(bbcWeatherContext) ?: return@launch
            targetStateFile.writeText(gson.toJson(converted))
            SmartspacerTargetProvider.notifyChange(context, BBCWeatherTarget::class.java)
        }
    }

    override fun getTargetState(): TargetState? {
        return try {
            gson.fromJson(targetStateFile.readText(), TargetState::class.java)
        }catch (e: Exception){
            null
        }
    }

    private fun TargetState.convertIcons(bbcWeatherContext: Context): TargetState? {
        return copy(
            icon = BBCWeatherIcon.getWeatherIconBasedOnMedium(bbcWeatherContext, icon)
                ?.getAodIcon(context) ?: return null,
            items = items.mapNotNull { it.convertIcons(bbcWeatherContext) }
        )
    }

    private fun TargetState.Item.convertIcons(bbcWeatherContext: Context): TargetState.Item? {
        return copy(
            icon = BBCWeatherIcon.getWeatherIconBasedOnSmall(bbcWeatherContext, icon)
                ?.getRegularIcon(context)?.scale(SIZE_CAROUSEL_ICON, SIZE_CAROUSEL_ICON)
                ?: return null
        )
    }

}