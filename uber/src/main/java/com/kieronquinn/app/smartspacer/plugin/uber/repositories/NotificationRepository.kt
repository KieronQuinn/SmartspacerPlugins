package com.kieronquinn.app.smartspacer.plugin.uber.repositories

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.uber.drawing.drawNotificationData
import com.kieronquinn.app.smartspacer.plugin.uber.model.NotificationData
import com.kieronquinn.app.smartspacer.plugin.uber.model.TargetData
import com.kieronquinn.app.smartspacer.plugin.uber.targets.UberTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerNotificationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface NotificationRepository {

    fun setNotifications(notifications: List<NotificationData>)
    fun dismissNotification(id: String): Boolean
    fun getTargetData(): List<TargetData>

}

class NotificationRepositoryImpl(private val context: Context): NotificationRepository {

    private val scope = MainScope()
    private var targetData = emptyList<TargetData>()
    private val updateLock = Mutex()

    override fun setNotifications(notifications: List<NotificationData>) {
        scope.launch(Dispatchers.IO) {
            updateLock.withLock {
                updateTargetsLocked(notifications)
            }
        }
    }

    override fun dismissNotification(id: String): Boolean {
        val targetData = targetData.firstOrNull { it.notification.id.toString() == id }
            ?: return false
        SmartspacerNotificationProvider.dismissNotification(context, targetData.notification)
        return true
    }

    private fun updateTargetsLocked(notifications: List<NotificationData>) {
        targetData = notifications.map {
            it.toTargetData()
        }
        SmartspacerTargetProvider.notifyChange(context, UberTarget::class.java)
    }

    private fun NotificationData.toTargetData(): TargetData {
        return TargetData(
            notification, icon, title, subtitle, context.drawNotificationData(this)
        )
    }

    override fun getTargetData(): List<TargetData> {
        return targetData
    }

}