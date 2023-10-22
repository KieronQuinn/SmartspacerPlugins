package com.kieronquinn.app.smartspacer.plugin.healthconnect.model

import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import java.time.Duration

enum class TimeoutPeriod(
    @StringRes
    val title: Int,
    val duration: Duration
) {

    FIFTEEN_MINUTES(R.string.duration_15_minutes, Duration.ofMinutes(15)),
    THIRTY_MINUTES(R.string.duration_30_minutes, Duration.ofMinutes(30)),
    SIXTY_MINUTES(R.string.duration_60_minutes, Duration.ofMinutes(60)),
    ONE_HUNDRED_TWENTY_MINUTES(R.string.duration_120_minutes, Duration.ofMinutes(120)),

}