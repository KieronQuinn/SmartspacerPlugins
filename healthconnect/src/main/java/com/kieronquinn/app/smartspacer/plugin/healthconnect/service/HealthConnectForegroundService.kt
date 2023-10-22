package com.kieronquinn.app.smartspacer.plugin.healthconnect.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.notifications.NotificationChannel
import com.kieronquinn.app.smartspacer.plugin.healthconnect.notifications.NotificationId
import com.kieronquinn.app.smartspacer.plugin.healthconnect.notifications.createNotification
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.HealthConnectRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getParcelableArrayListCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenCreated
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.parcelize.Parcelize
import org.koin.android.ext.android.inject
import java.util.LinkedList

class HealthConnectForegroundService: LifecycleService() {

    companion object {
        private const val KEY_UPDATE_ITEMS = "update_items"

        fun createIntent(context: Context, updateItems: List<UpdateItem>): Intent {
            return Intent(context, HealthConnectForegroundService::class.java).apply {
                putExtra(KEY_UPDATE_ITEMS, ArrayList(updateItems))
            }
        }
    }

    private val updateQueue = LinkedList<UpdateItem>()
    private val updateLock = Mutex()
    private val healthConnectRepository by inject<HealthConnectRepository>()

    override fun onCreate() {
        super.onCreate()
        Log.e("HCFS", "onCreate")
        try {
            startForeground(NotificationId.BACKGROUND_SERVICE.ordinal, createNotification())
        }catch (e: Exception) {
            Log.e("HCFS", "Error", e)
            stopSelf()
        }
    }

    private fun createNotification(): Notification {
        return createNotification(NotificationChannel.BACKGROUND_SERVICE) {
            val notificationIntent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannel.BACKGROUND_SERVICE.id)
            }
            it.setSmallIcon(R.drawable.ic_health_connect_tinted)
            it.setContentTitle(getString(R.string.notification_background_service_title))
            it.setContentText(getString(R.string.notification_background_service_content))
            it.setContentIntent(
                PendingIntent.getActivity(
                this,
                NotificationId.BACKGROUND_SERVICE.ordinal,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            ))
            it.setAutoCancel(true)
            it.priority = NotificationCompat.PRIORITY_HIGH
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getUpdateItems()?.let {
            updateQueue.addAll(it)
        }
        runUpdates()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun runUpdates() = whenCreated {
        updateLock.withLock {
            while (updateQueue.isNotEmpty()) {
                val item = updateQueue.pop()
                healthConnectRepository.updateHealthMetric(item.smartspacerId, item.authority)
            }
            stopSelf()
        }
    }

    private fun Intent.getUpdateItems(): ArrayList<UpdateItem>? {
        return getParcelableArrayListCompat(KEY_UPDATE_ITEMS, UpdateItem::class.java)
    }

    @Parcelize
    data class UpdateItem(val smartspacerId: String, val authority: String) : Parcelable

}