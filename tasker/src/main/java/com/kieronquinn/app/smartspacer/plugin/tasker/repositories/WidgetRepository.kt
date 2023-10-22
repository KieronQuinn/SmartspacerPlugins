package com.kieronquinn.app.smartspacer.plugin.tasker.repositories

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getPackageLabel
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.WidgetRepository.Widget
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.WidgetRepository.WidgetApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

interface WidgetRepository {

    fun getWidgets(): Flow<List<WidgetApp>>

    data class WidgetApp(val packageName: String, val label: String, val widgets: List<Widget>)
    data class Widget(val componentName: String, val label: String, val subtitle: String?)

}

class WidgetRepositoryImpl(context: Context): WidgetRepository {

    private val appWidgetManager = AppWidgetManager.getInstance(context)
    private val packageManager = context.packageManager

    override fun getWidgets() = flow {
        appWidgetManager.installedProviders.groupBy { it.provider.packageName }.mapValues {
            it.value.toWidgets()
        }.mapKeys {
            it.key.toWidgetApp()
        }.map {
            WidgetApp(it.key.first, it.key.second, it.value.sortedBy { w -> w.label.lowercase() })
        }.sortedBy {
            it.label.lowercase()
        }.let {
            emit(it)
        }
    }.flowOn(Dispatchers.IO)

    private fun String.toWidgetApp(): Pair<String, String> {
        val label = packageManager.getPackageLabel(this)
        return Pair(this, label?.toString() ?: this)
    }

    private fun List<AppWidgetProviderInfo>.toWidgets(): List<Widget> = map {
        val label = it.loadLabel(packageManager)
        Widget(it.provider.flattenToString(), label, it.provider.getSubtitle())
    }

    private fun ComponentName.getSubtitle(): String? {
        return shortClassName?.split(".")?.last()
    }


}