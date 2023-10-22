package com.kieronquinn.app.smartspacer.plugin.tasker.providers

import com.kieronquinn.app.smartspacer.plugin.tasker.BuildConfig
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerProxyProvider

class TaskerProxyProvider: SmartspacerProxyProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider.proxy"
    }

}