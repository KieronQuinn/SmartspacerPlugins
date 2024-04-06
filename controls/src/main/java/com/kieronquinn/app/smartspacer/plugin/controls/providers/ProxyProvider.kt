package com.kieronquinn.app.smartspacer.plugin.controls.providers

import com.kieronquinn.app.smartspacer.plugin.controls.BuildConfig
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerProxyProvider

class ProxyProvider: SmartspacerProxyProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider.proxy"
    }

}