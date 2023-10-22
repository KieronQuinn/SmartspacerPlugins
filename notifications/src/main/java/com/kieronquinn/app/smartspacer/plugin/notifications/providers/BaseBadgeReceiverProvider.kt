package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import android.content.Intent
import android.content.IntentFilter
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerBroadcastProvider
import org.koin.android.ext.android.inject

abstract class BaseBadgeReceiverProvider: SmartspacerBroadcastProvider() {

    companion object {
        private const val ACTION_BADGE_UPDATE = "android.intent.action.BADGE_COUNT_UPDATE"
        private const val ACTION_BADGE_UPDATE_SEC = "com.sec.intent.action.BADGE_COUNT_UPDATE"
        private const val EXTRA_BADGE_COUNT = "badge_count"
        private const val EXTRA_PACKAGE_NAME = "badge_count_package_name"
        private const val EXTRA_ACTIVITY_NAME = "badge_count_class_name"
    }

    abstract val packageName: String
    open val activityName: String? = null

    private val databaseRepository by inject<DatabaseRepository>()

    override fun getConfig(smartspacerId: String): Config {
        val filter = IntentFilter().apply {
            addAction(ACTION_BADGE_UPDATE)
            addAction(ACTION_BADGE_UPDATE_SEC)
        }
        return Config(listOf(filter))
    }

    override fun onReceive(intent: Intent) {
        val badgeCount = intent.getIntExtra(EXTRA_BADGE_COUNT, 0)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        val activityName = intent.getStringExtra(EXTRA_ACTIVITY_NAME)
        if(packageName != this.packageName) return
        if(this.activityName != null && activityName != this.activityName) return
        databaseRepository.setBadgeCount(packageName, badgeCount)
    }

}