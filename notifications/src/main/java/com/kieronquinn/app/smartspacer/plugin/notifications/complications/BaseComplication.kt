package com.kieronquinn.app.smartspacer.plugin.notifications.complications

import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isPackageInstalled
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider

abstract class BaseComplication: SmartspacerComplicationProvider() {

    abstract val packageName: String

    protected fun getLaunchIntent(): Intent? {
        return provideContext().packageManager.getLaunchIntentForPackage(packageName)
    }

    protected fun getCompatibilityState(incompatibleRes: Int): CompatibilityState {
        return if(!provideContext().packageManager.isPackageInstalled(packageName)){
            CompatibilityState.Incompatible(resources.getString(incompatibleRes))
        }else CompatibilityState.Compatible
    }

}