package com.kieronquinn.app.smartspacer.plugin.youtube.repositories

import android.content.Context
import android.content.SharedPreferences
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepository.SmartspacerSetting
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.youtube.BuildConfig

abstract class YouTubeSettingsRepository: BaseSettingsRepositoryImpl() {

    abstract val apiKey: SmartspacerSetting<String>

}

class YouTubeSettingsRepositoryImpl(context: Context): YouTubeSettingsRepository() {

    companion object {
        private const val SHARED_PREFS_NAME = "${BuildConfig.APPLICATION_ID}_prefs"

        private const val KEY_API_KEY = "api_key"
        private const val DEFAULT_API_KEY = ""
    }

    override val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    override val apiKey = string(KEY_API_KEY, DEFAULT_API_KEY)

    override suspend fun getBackup(): Map<String, String> {
        return emptyMap()
    }

    override suspend fun restoreBackup(settings: Map<String, String>) {
        //No-op
    }

}