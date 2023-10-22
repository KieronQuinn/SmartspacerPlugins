package com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories

import android.content.Context
import android.content.SharedPreferences
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepository.SmartspacerSetting
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl

abstract class SamsungHealthSettingsRepository: BaseSettingsRepositoryImpl() {

    abstract val sleepTime: SmartspacerSetting<String>
    abstract val sleepTimestamp: SmartspacerSetting<Long>
    abstract val steps: SmartspacerSetting<String>

}

class SamsungHealthSettingsRepositoryImpl(context: Context): SamsungHealthSettingsRepository() {

    companion object {
        private const val SHARED_PREFS_NAME = "${BuildConfig.APPLICATION_ID}_prefs"

        private const val KEY_SLEEP_TIME = "sleep_time"
        private const val DEFAULT_SLEEP_TIME = ""

        private const val KEY_SLEEP_TIMESTAMP = "sleep_timestamp"
        private const val DEFAULT_SLEEP_TIMESTAMP = 0L

        private const val KEY_STEPS = "steps"
        private const val DEFAULT_STEPS = ""
    }

    override val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    override val sleepTime = string(KEY_SLEEP_TIME, DEFAULT_SLEEP_TIME)
    override val sleepTimestamp = long(KEY_SLEEP_TIMESTAMP, DEFAULT_SLEEP_TIMESTAMP)
    override val steps = string(KEY_STEPS, DEFAULT_STEPS)

    override suspend fun getBackup(): Map<String, String> {
        return emptyMap()
    }

    override suspend fun restoreBackup(settings: Map<String, String>) {
        //No-op
    }

}