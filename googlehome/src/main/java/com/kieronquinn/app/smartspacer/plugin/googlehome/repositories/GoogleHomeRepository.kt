package com.kieronquinn.app.smartspacer.plugin.googlehome.repositories

import android.app.PendingIntent
import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.kieronquinn.app.smartspacer.plugin.googlehome.complications.GoogleHomeComplication
import com.kieronquinn.app.smartspacer.plugin.googlehome.model.database.WidgetItem
import com.kieronquinn.app.smartspacer.plugin.googlehome.repositories.GoogleHomeRepository.Item
import com.kieronquinn.app.smartspacer.plugin.googlehome.repositories.GoogleHomeRepository.Item.Companion.toWidgetItem
import com.kieronquinn.app.smartspacer.plugin.googlehome.targets.GoogleHomeTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

interface GoogleHomeRepository {

    fun setItems(smartspacerId: String, items: List<Item>)
    fun getItems(smartspacerId: String): List<Item>
    fun deleteItems(smartspacerId: String)

    data class Item(
        @SerializedName("title")
        val title: String,
        @SerializedName("subtitle")
        val subtitle: String?,
        @SerializedName("icon")
        val icon: String,
        @SerializedName("on")
        val on: Boolean?,
        @Transient
        val click: PendingIntent?
    ) {

        companion object {
            private val list = object : TypeToken<List<Item>>() {}.type

            fun fromWidgetItem(gson: Gson, item: WidgetItem, click: List<PendingIntent?>): List<Item> {
                return gson.fromJson<List<Item>>(item.data, list)
                    .mapIndexed { index, item -> item.copy(click = click.getOrNull(index)) }
            }

            fun List<Item>.toWidgetItem(gson: Gson, smartspacerId: String) = WidgetItem(
                smartspacerId,
                gson.toJson(this, list)
            )
        }
    }

}

class GoogleHomeRepositoryImpl(
    private val context: Context,
    private val gson: Gson,
    private val databaseRepository: DatabaseRepository
): GoogleHomeRepository {

    private val scope = MainScope()

    private val clickLock = Any()
    private val clickIntents = HashMap<String, List<PendingIntent?>>()

    private val items = databaseRepository.items
        .stateIn(scope, SharingStarted.Eagerly, null)

    override fun setItems(smartspacerId: String, items: List<Item>) {
        synchronized(clickLock) {
            clickIntents[smartspacerId] = items.map { it.click }
        }
        scope.launch {
            databaseRepository.addItem(items.toWidgetItem(gson, smartspacerId))
            SmartspacerComplicationProvider.notifyChange(
                context,
                GoogleHomeComplication::class.java,
                smartspacerId
            )
            SmartspacerTargetProvider.notifyChange(
                context,
                GoogleHomeTarget::class.java,
                smartspacerId
            )
        }
    }

    override fun getItems(smartspacerId: String): List<Item> {
        val item = runBlocking {
            items.filterNotNull().first().firstOrNull { it.smartspacerId == smartspacerId }
        } ?: return emptyList()
        val click = synchronized(clickLock) {
            clickIntents[smartspacerId]
        } ?: emptyList()
        return Item.fromWidgetItem(gson, item, click)
    }

    override fun deleteItems(smartspacerId: String) {
        scope.launch {
            databaseRepository.deleteItem(smartspacerId)
        }
    }

}