package com.kieronquinn.app.smartspacer.plugin.controls.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ControlExtraData(
    val requiresUnlock: Boolean = false,
    val modeSetMode: ControlMode? = null,
    val floatSetFloat: Float? = null,
    val shouldHideDetails: Boolean = false,
    val passcode: String? = null
): Parcelable