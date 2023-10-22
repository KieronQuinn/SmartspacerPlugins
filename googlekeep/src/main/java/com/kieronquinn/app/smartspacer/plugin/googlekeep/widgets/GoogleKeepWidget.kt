package com.kieronquinn.app.smartspacer.plugin.googlekeep.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.kieronquinn.app.smartspacer.plugin.googlekeep.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.googlekeep.GoogleKeepPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.googlekeep.model.Note
import com.kieronquinn.app.smartspacer.plugin.googlekeep.repositories.KeepRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.RemoteAdapter
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import com.kieronquinn.app.smartspacer.sdk.utils.getResourceForIdentifier
import org.koin.android.ext.android.inject

class GoogleKeepWidget: SmartspacerWidgetProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.widgets.keep"

        const val IDENTIFIER_OPEN_NOTE = "$PACKAGE_NAME:id/open_note_button"
        private const val IDENTIFIER_MAIN_LAYOUT = "$PACKAGE_NAME:id/main_layout"
        private const val IDENTIFIER_TITLE = "$PACKAGE_NAME:id/title"
        private const val IDENTIFIER_DESCRIPTION = "$PACKAGE_NAME:id/description"
        private const val IDENTIFIER_WRAPPER = "$PACKAGE_NAME:id/list_item_wrapper"
        private const val IDENTIFIER_THEME = "$PACKAGE_NAME:style/WidgetGM3NoteColorThemeOverlay"

        private val COMPONENT_PROVIDER = ComponentName(
            PACKAGE_NAME,
            "com.google.android.apps.keep.ui.widgets.singlenote.SingleNoteWidgetProvider"
        )

        fun getProvider(context: Context): AppWidgetProviderInfo? {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return appWidgetManager.installedProviders.firstOrNull {
                it.provider == COMPONENT_PROVIDER
            }
        }
    }

    private val materialThemedContext by lazy {
        val context = provideContext().createPackageContext(
            PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY
        )
        val themeResource = context.getResourceForIdentifier(IDENTIFIER_THEME)!!
        ContextThemeWrapper(context, themeResource)
    }

    private val keepRepository by inject<KeepRepository>()

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext())
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        if(remoteViews != null) {
            getAdapter(smartspacerId, IDENTIFIER_MAIN_LAYOUT)
        }
    }

    override fun onAdapterConnected(smartspacerId: String, adapter: RemoteAdapter) {
        super.onAdapterConnected(smartspacerId, adapter)
        val items = ArrayList<Item>()
        val fakeParent = FrameLayout(materialThemedContext)
        val appWidgetId = getAppWidgetId(provideContext(), smartspacerId) ?: return
        for(i in 0 until adapter.getCount()) {
            val item = adapter.getViewAt(i)?.remoteViews?.getItem(fakeParent) ?: break
            items.add(item)
        }
        if(items.isEmpty()) return //Either been deleted or not loaded, assume latter and don't fail
        val title = items.getOrNull(0) as? Item.Title ?: return
        val note = when(items.getOrNull(1)) {
            is Item.ListItem -> {
                val listItems = items.subList(1, items.size).filterIsInstance<Item.ListItem>().map {
                    Note.ListNote.Item(it.isIndented, it.content)
                }
                Note.ListNote(title.title, appWidgetId, listItems)
            }
            is Item.Content -> {
                Note.RegularNote(title.title, appWidgetId, (items[1] as Item.Content).content)
            }
            else -> {
                //Assume list item with no items
                Note.ListNote(title.title, appWidgetId, emptyList())
            }
        }
        keepRepository.setNote(smartspacerId, note)
    }

    private fun RemoteViews.getItem(parent: ViewGroup): Item? {
        val view = apply(materialThemedContext, parent) ?: return null
        view.findViewByIdentifier<TextView>(IDENTIFIER_TITLE)?.let {
            return Item.Title(it.text.toString())
        }
        val description = view.findViewByIdentifier<TextView>(IDENTIFIER_DESCRIPTION)
            ?: return null
        view.findViewByIdentifier<LinearLayout>(IDENTIFIER_WRAPPER)?.let {
            return Item.ListItem(it.isIndented(), description.text.toString())
        }
        return Item.Content(description.text.toString())
    }

    private fun View.isIndented(): Boolean {
        return paddingLeft != 0 && paddingRight != 0
    }

    private sealed class Item {
        data class Title(val title: String): Item()
        data class Content(val content: String): Item()
        data class ListItem(val isIndented: Boolean, val content: String): Item()
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

}