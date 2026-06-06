package com.kieronquinn.app.smartspacer.plugin.googlehealth.widgets

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.view.children
import com.kieronquinn.app.smartspacer.plugin.googlehealth.GoogleHealthPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.HealthMetric
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.GoogleHealthRepository
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.GoogleHealthRepository.HealthItem
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.dp
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewsByType
import com.kieronquinn.app.smartspacer.sdk.utils.getClickPendingIntent
import com.kieronquinn.app.smartspacer.sdk.utils.viewstructure.ViewGroup
import com.kieronquinn.app.smartspacer.sdk.utils.viewstructure.mapWidgetViewStructure
import org.koin.android.ext.android.inject

class GoogleHealthWidget: SmartspacerWidgetProvider() {

    companion object {
        private const val PROVIDER_CLASS =
            "com.google.android.apps.fitbit.app.widgets.focusmetricswidget.impl.FocusMetricsWidgetReceiver"

        fun getProvider(context: Context): AppWidgetProviderInfo? {
            val manager = context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
            return manager.installedProviders.firstOrNull {
                it.provider.packageName == PACKAGE_NAME && it.provider.className == PROVIDER_CLASS
            }
        }

        private val WIDGET_WIDTH = 368.dp
        private val WIDGET_HEIGHT = 146.dp
        private const val ID_REFRESH = "refresh"
        private const val ID_ITEMS_CONTAINER = "items_container"

        private val STRUCTURE_WIDGET: ViewGroup.() -> Unit = {
            frameLayout {
                linearLayout {
                    linearLayout {
                        id = ID_REFRESH
                    }
                    frameLayout {
                        linearLayout {
                            id = ID_ITEMS_CONTAINER
                        }
                    }
                }
            }
        }
    }

    private val googleHealthRepository by inject<GoogleHealthRepository>()

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext())
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val views = remoteViews?.load() ?: return
        val structure = mapWidgetViewStructure(views, STRUCTURE_WIDGET) ?: return
        val container = structure.findViewByStructureId<LinearLayout>(
            views,
            ID_ITEMS_CONTAINER
        ) ?: return
        val refreshContainer = structure.findViewByStructureId<LinearLayout>(
            views,
            ID_REFRESH
        ) ?: return
        val refresh = refreshContainer.children.lastOrNull()
        val refreshIntent = refresh?.getClickPendingIntent()
        googleHealthRepository.setHealthItems(container.loadItems(refreshIntent))
    }

    private fun LinearLayout.loadItems(refresh: PendingIntent?): List<HealthItem> {
        val columns = children.filterIsInstance<LinearLayout>().toList()
        return columns.flatMap {
            it.loadColumn(refresh)
        }
    }

    private fun LinearLayout.loadColumn(refresh: PendingIntent?): List<HealthItem> {
        return children.filterIsInstance<FrameLayout>().toList().mapNotNull {
            it.loadRow(refresh)
        }
    }

    private fun FrameLayout.loadRow(refresh: PendingIntent?): HealthItem? {
        val click = getClickPendingIntent() ?: return null
        val imageViews = findViewsByType(ImageView::class.java)
        val textViews = findViewsByType(TextView::class.java)
        val metric = imageViews.reversed().getOrNull(1)?.let {
            val resourceId = it.getResource() ?: return@let null
            val resourceName = context.resources.getResourceEntryName(resourceId)
            HealthMetric.forIcon(resourceName)
        } ?: return null
        val title = textViews.getOrNull(0)?.text?.toString() ?: return null
        val content = textViews.getOrNull(1)?.text?.toString() ?: return null
        val value = when (metric) {
            HealthMetric.CARDIO_LOAD -> title
            else -> content
        }
        return HealthItem(metric, value, click, refresh)
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(
            width = WIDGET_WIDTH,
            height = WIDGET_HEIGHT
        )
    }

    @SuppressLint("DiscouragedPrivateApi")
    private fun ImageView.getResource(): Int? {
        return ImageView::class.java.getDeclaredField("mResource").apply {
            isAccessible = true
        }.getInt(this).takeIf { it > 0 }
    }

}