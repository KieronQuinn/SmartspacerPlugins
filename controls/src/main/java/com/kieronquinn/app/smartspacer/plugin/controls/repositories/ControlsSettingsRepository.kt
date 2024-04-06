package com.kieronquinn.app.smartspacer.plugin.controls.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.controls.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepository.SmartspacerSetting
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl
import java.time.Duration

abstract class ControlsSettingsRepository: BaseSettingsRepositoryImpl() {

    abstract val refreshPeriod: SmartspacerSetting<RefreshPeriod>
    abstract val refreshOnScreenStateChanged: SmartspacerSetting<Boolean>

    enum class RefreshPeriod(
        @StringRes
        val labelRes: Int,
        val duration: Duration?
    ) {
        FIFTEEN_SECONDS(R.string.refresh_period_15_seconds, Duration.ofSeconds(15)),
        THIRTY_SECONDS(R.string.refresh_period_30_seconds, Duration.ofSeconds(30)),
        ONE_MINUTE(R.string.refresh_period_1_minute, Duration.ofMinutes(1)),
        FIVE_MINUTES(R.string.refresh_period_5_minutes, Duration.ofMinutes(5)),
        FIFTEEN_MINUTES(R.string.refresh_period_15_minutes, Duration.ofMinutes(15)),
        THIRTY_MINUTES(R.string.refresh_period_30_minutes, Duration.ofMinutes(30)),
        ONE_HOUR(R.string.refresh_period_1_hour, Duration.ofHours(1)),
        INFINITE(R.string.refresh_period_infinite, null)
    }

}

class ControlsSettingsRepositoryImpl(context: Context): ControlsSettingsRepository() {

    companion object {
        private const val SHARED_PREFS_NAME = "${BuildConfig.APPLICATION_ID}_prefs"

        private const val KEY_REFRESH_PERIOD = "refresh_period"
        private val DEFAULT_REFRESH_PERIOD = RefreshPeriod.THIRTY_SECONDS

        private const val KEY_REFRESH_ON_SCREEN_STATE_CHANGED = "refresh_on_state_changed"
        private const val DEFAULT_REFRESH_ON_SCREEN_STATE_CHANGED = true
    }

    override val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    }

    override val refreshPeriod = enum(KEY_REFRESH_PERIOD, DEFAULT_REFRESH_PERIOD)
    override val refreshOnScreenStateChanged = boolean(
        KEY_REFRESH_ON_SCREEN_STATE_CHANGED, DEFAULT_REFRESH_ON_SCREEN_STATE_CHANGED
    )

    override suspend fun getBackup(): Map<String, String> {
        return emptyMap()
    }

    override suspend fun restoreBackup(settings: Map<String, String>) {
        //No-op
    }

}