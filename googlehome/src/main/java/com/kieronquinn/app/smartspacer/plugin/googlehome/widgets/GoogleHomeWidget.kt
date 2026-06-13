package com.kieronquinn.app.smartspacer.plugin.googlehome.widgets

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.view.children
import com.kieronquinn.app.smartspacer.plugin.googlehome.GoogleHomePlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.googlehome.repositories.GoogleHomeRepository
import com.kieronquinn.app.smartspacer.plugin.googlehome.repositories.GoogleHomeRepository.Item
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.dp
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewsByType
import com.kieronquinn.app.smartspacer.sdk.utils.getClickPendingIntent
import com.kieronquinn.app.smartspacer.sdk.utils.viewstructure.ViewGroup
import com.kieronquinn.app.smartspacer.sdk.utils.viewstructure.mapWidgetViewStructure
import org.koin.android.ext.android.inject

class GoogleHomeWidget: SmartspacerWidgetProvider() {

    companion object {
        private const val PROVIDER_CLASS =
            "com.google.android.apps.chromecast.app.appwidget.favorites.FavoritesWidgetReceiver"

        fun getProvider(context: Context): AppWidgetProviderInfo? {
            val manager = context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
            return manager.installedProviders.firstOrNull {
                it.provider.packageName == PACKAGE_NAME && it.provider.className == PROVIDER_CLASS
            }
        }

        private val WIDGET_WIDTH = 368.dp
        private val WIDGET_HEIGHT = 736.dp
        private const val ID_ITEM_CONTAINER = "item_container"

        private val STRUCTURE_WIDGET: ViewGroup.() -> Unit = {
            frameLayout {
                frameLayout {
                    frameLayout {
                        index = 1
                        linearLayout {
                            frameLayout {
                                index = 1
                                linearLayout {
                                    id = ID_ITEM_CONTAINER
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val googleHomeRepository by inject<GoogleHomeRepository>()

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext())
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val views = remoteViews?.load() ?: return
        val itemContainer = mapWidgetViewStructure(views, STRUCTURE_WIDGET)
            ?.findViewByStructureId<LinearLayout>(views, ID_ITEM_CONTAINER) ?: return
        val items = itemContainer.children.toList().filterIsInstance<LinearLayout>().flatMap {
            it.parseRow()
        }
        googleHomeRepository.setItems(smartspacerId, items)
    }

    private fun LinearLayout.parseRow(): List<Item> {
        return children.toList().filterIsInstance<FrameLayout>().mapNotNull {
            it.parseItem()
        }
    }

    private fun FrameLayout.parseItem(): Item? {
        val frameLayout = getChildAt(0) as? FrameLayout ?: return null
        val click = frameLayout.getClickPendingIntent() ?: return null
        val frameLayoutInner = frameLayout.getChildAt(0) as? FrameLayout ?: return null
        val linearLayout = frameLayoutInner.getChildAt(1) as? LinearLayout ?: return null
        val imageViews = linearLayout.findViewsByType(ImageView::class.java)
        val textViews = linearLayout.findViewsByType(TextView::class.java)
        val title = textViews.firstOrNull()?.text?.toString() ?: return null
        val subtitle = textViews.getOrNull(1)?.text?.toString()
        val icon = imageViews.firstOrNull()?.getResource()?.let {
            linearLayout.context.resources.getResourceEntryName(it)
        }?.takeUnless { it.startsWith("gs_keyboard_arrow_") } ?: return null
        // States seem to also show a subtitle, so use that to differentiate
        val on = icon.contains("_fill_").takeIf { subtitle != null }
        return Item(
            title,
            subtitle,
            icon,
            on,
            click
        )
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