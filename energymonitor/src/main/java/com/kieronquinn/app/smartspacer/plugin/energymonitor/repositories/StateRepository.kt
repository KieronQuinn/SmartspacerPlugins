package com.kieronquinn.app.smartspacer.plugin.energymonitor.repositories

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.energymonitor.complications.EnergyMonitorComplication
import com.kieronquinn.app.smartspacer.plugin.energymonitor.repositories.StateRepository.State
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.BitmapTypeAdapter
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

interface StateRepository {

    fun getState(smartspacerId: String): State?
    fun setState(smartspacerId: String, state: State)
    fun deleteState(smartspacerId: String)

    data class State(
        val batteryLevel: String,
        val deviceName: String,
        val isCharging: Boolean,
        val icon: Bitmap
    )

}

class StateRepositoryImpl(private val context: Context): StateRepository {

    private val gson = GsonBuilder()
        .registerTypeAdapter(Bitmap::class.java, BitmapTypeAdapter())
        .create()

    private val stateDir = context.filesDir.also {
        it.mkdirs()
    }

    private val scope = MainScope()

    override fun getState(smartspacerId: String): State? {
        val file = getStateFile(smartspacerId)
        if(!file.exists()) return null
        return try {
            gson.fromJson(file.readText(), State::class.java)
        }catch (e: Exception) {
            null
        }
    }

    override fun setState(smartspacerId: String, state: State) {
        scope.launch(Dispatchers.IO) {
            val file = getStateFile(smartspacerId)
            val serialised = gson.toJson(state)
            file.writeText(serialised)
            SmartspacerComplicationProvider.notifyChange(
                context, EnergyMonitorComplication::class.java, smartspacerId
            )
        }
    }

    override fun deleteState(smartspacerId: String) {
        scope.launch(Dispatchers.IO) {
            val file = getStateFile(smartspacerId)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun getStateFile(smartspacerId: String): File {
        return File(stateDir, "state_$smartspacerId.json")
    }

}