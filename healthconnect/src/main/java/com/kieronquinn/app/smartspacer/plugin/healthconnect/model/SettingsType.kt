package com.kieronquinn.app.smartspacer.plugin.healthconnect.model

enum class SettingsType {
    /**
     *  The time at which to consider a 'reset', of the day, defaults to midnight
     */
    RESET_DAY,

    /**
     *  The time at which to consider a 'reset' of the night, defaults to midday
     */
    RESET_NIGHT,

    /**
     *  How long to show the 'current' measurement for, defaults to 30 minutes
     */
    TIMEOUT
}