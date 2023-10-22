package com.kieronquinn.app.smartspacer.plugin.uber.notifications

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Icon
import android.os.Build
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugin.uber.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.uber.R
import com.kieronquinn.app.smartspacer.plugin.uber.UberPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.uber.model.NotificationData
import com.kieronquinn.app.smartspacer.plugin.uber.repositories.NotificationRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerNotificationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import com.kieronquinn.app.smartspacer.sdk.utils.getResourceForIdentifier
import org.koin.android.ext.android.inject

class UberNotificationProvider: SmartspacerNotificationProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.notifications.uber"

        private const val IDENTIFIER_LAYOUT = "$PACKAGE_NAME:layout/ub__rich_notification_custom_big"
        private const val IDENTIFIER_TITLE = "$PACKAGE_NAME:id/ub__rich_notification_title"
        private const val IDENTIFIER_SUBTITLE = "$PACKAGE_NAME:id/ub__rich_notification_subtitle"
        private const val IDENTIFIER_PROGRESS_CAR = "$PACKAGE_NAME:id/progress_bar_indicator"
        private const val IDENTIFIER_PROGRESS = "$PACKAGE_NAME:id/progress_bar_filled"
        private const val IDENTIFIER_DRIVER = "$PACKAGE_NAME:id/ub__rich_notification_big_left_image"
        private const val IDENTIFIER_CAR = "$PACKAGE_NAME:id/ub__rich_notification_big_right_image"
        private const val IDENTIFIER_EXPANDED_TITLE =
            "$PACKAGE_NAME:id/ub__rich_notification_expanded_title"
        private const val IDENTIFIER_EXPANDED_SUBTITLE =
            "$PACKAGE_NAME:id/ub__rich_notification_expanded_subtitle"
    }

    private val notificationRepository by inject<NotificationRepository>()

    override fun getConfig(smartspacerId: String): Config {
        return Config(setOf(PACKAGE_NAME))
    }

    @Suppress("DEPRECATION")
    override fun onNotificationsChanged(
        smartspacerId: String,
        isListenerEnabled: Boolean,
        notifications: List<StatusBarNotification>
    ) {
        val data = notifications.mapNotNull {
            it.notification.bigContentView?.processRemoteViews(it, it.notification.smallIcon)
        }
        notificationRepository.setNotifications(data)
    }

    private fun RemoteViews.processRemoteViews(
        notification: StatusBarNotification,
        icon: Icon
    ): NotificationData? {
        val uberContext = createContext() ?: return null
        val layoutId = uberContext.getResourceForIdentifier(IDENTIFIER_LAYOUT) ?: return null
        if(this.layoutId != layoutId) return null
        val view = load() ?: return null
        val title = view.findViewByIdentifier<TextView>(IDENTIFIER_TITLE)?.text ?: return null
        val subtitle = view.findViewByIdentifier<TextView>(IDENTIFIER_SUBTITLE)?.text ?: return null
        val progressCar = view.findViewByIdentifier<ImageView>(IDENTIFIER_PROGRESS_CAR)
            ?.drawable?.toBitmap() ?: return null
        val progress = view.findViewByIdentifier<FrameLayout>(IDENTIFIER_PROGRESS)?.let {
            calculateProgress(it.paddingLeft)
        } ?: return null
        val driver = view.findViewByIdentifier<ImageView>(IDENTIFIER_DRIVER)?.drawable?.toBitmap()
            ?: return null
        val car = view.findViewByIdentifier<ImageView>(IDENTIFIER_CAR)?.drawable?.toBitmap()
            ?: return null
        val expandedTitle = view.findViewByIdentifier<TextView>(IDENTIFIER_EXPANDED_TITLE)?.text
            ?: return null
        val expandedSubtitle = view.findViewByIdentifier<TextView>(IDENTIFIER_EXPANDED_SUBTITLE)
            ?.text ?: return null
        return NotificationData(
            notification,
            icon,
            title.toString(),
            subtitle.toString(),
            progress,
            progressCar,
            driver,
            car,
            expandedTitle.toString(),
            expandedSubtitle.toString()
        )
    }

    private fun RemoteViews.load(): View? {
        return try {
            apply(provideContext(), null)
        }catch (e: Exception) {
            Log.d(this::class.java.simpleName, "Error loading RemoteViews", e)
            null
        }
    }

    private fun createContext(): Context? {
        return try {
            provideContext().createPackageContext(PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY)
        }catch (e: Exception){
            null
        }
    }

    private fun calculateProgress(leftPadding: Int): Float {
        return leftPadding / getUsableWidth().toFloat()
    }

    private fun getUsableWidth(): Int {
        val displayWidth = Resources.getSystem().displayMetrics.widthPixels
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            displayWidth - this.edgeDistancePreS()
        } else {
            displayWidth - this.edgeDistanceS()
        }
    }

    private fun edgeDistanceS(): Int {
        return provideContext().resources.getDimensionPixelSize(
            R.dimen.progress_bar_expanded_screen_edges_distance_s)
    }

    private fun edgeDistancePreS(): Int {
        return provideContext().resources.getDimensionPixelSize(
            R.dimen.progress_bar_expanded_screen_edges_distance_before_s
        )
    }

}