package com.kieronquinn.app.smartspacer.plugin.countdown.providers

import com.kieronquinn.app.smartspacer.plugin.countdown.BuildConfig
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerProxyProvider

class ProxyProvider: SmartspacerProxyProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider.proxy"
    }

}