package com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories

import android.app.PendingIntent
import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.googlehealth.complications.GoogleHealthComplication
import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.HealthMetric
import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.database.HealthFocus
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.GoogleHealthRepository.HealthItem
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant

interface GoogleHealthRepository {

    fun setHealthItems(items: List<HealthItem>)
    fun getHealthItem(metric: HealthMetric): HealthItem?
    fun isHealthMetricAvailable(metric: HealthMetric): Flow<Boolean>

    data class HealthItem(
        val metric: HealthMetric,
        val value: String,
        val clickIntent: PendingIntent?,
        val refreshIntent: PendingIntent?,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        val instant: Instant = Instant.ofEpochMilli(timestamp)
        val isRefreshing
            // If the click intent is filled but there's no refresh intent, the widget is refreshing
            get() = clickIntent != null && refreshIntent == null
    }

}

class GoogleHealthRepositoryImpl(
    private val context: Context,
    private val databaseRepository: DatabaseRepository
): GoogleHealthRepository {

    private val scope = MainScope()
    private val clickIntentLock = Any()
    private val clickIntents = HashMap<HealthMetric, PendingIntent>()
    private val refreshIntents = HashMap<HealthMetric, PendingIntent>()

    private val healthFocuses = databaseRepository.healthItemCachedItems
        .stateIn(scope, SharingStarted.Eagerly, null)

    override fun setHealthItems(items: List<HealthItem>) {
        scope.launch {
            val healthFocusItems = items.map {
                it.toHealthFocus()
            }
            databaseRepository.setHealthFocusItems(healthFocusItems)
            synchronized(clickIntentLock) {
                clickIntents.clear()
                refreshIntents.clear()
                items.forEach {
                    clickIntents[it.metric] = it.clickIntent ?: return@forEach
                }
                items.forEach {
                    refreshIntents[it.metric] = it.refreshIntent ?: return@forEach
                }
            }
            SmartspacerComplicationProvider.notifyChange(
                context,
                GoogleHealthComplication::class.java
            )
        }
    }

    private suspend fun HealthItem.toHealthFocus(): HealthFocus {
        val current = getHealthFocus(metric)
        // Retain the timestamp if the value has not changed
        val timestamp = if (current != null && current.value == value) {
            current.timestamp
        } else timestamp
        return HealthFocus(
            metric = metric,
            value = value,
            timestamp = timestamp
        )
    }

    private suspend fun getHealthFocus(metric: HealthMetric): HealthFocus? {
        return healthFocuses.filterNotNull().first().firstOrNull { it.metric == metric }
    }

    override fun getHealthItem(metric: HealthMetric): HealthItem? {
        val focus = runBlocking { getHealthFocus(metric) } ?: return null
        val intents = synchronized(clickIntentLock) {
            clickIntents[metric] to refreshIntents[metric]
        }
        return HealthItem(
            metric,
            focus.value,
            intents.first,
            intents.second,
            focus.timestamp
        )
    }

    override fun isHealthMetricAvailable(metric: HealthMetric): Flow<Boolean> {
        return healthFocuses.filterNotNull().map {
            it.any { i -> i.metric == metric }
        }
    }

}