package com.kieronquinn.app.smartspacer.plugin.notifications.repositories

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.FacebookComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.GoogleVoiceComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.InstagramComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.K9Complication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.OutlookComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.TelegramComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.ThreadsComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.TwitterComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.WhatsAppComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.WhatsAppLegacyComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.model.database.Badge
import com.kieronquinn.app.smartspacer.plugin.notifications.model.database.BadgeDatabase
import com.kieronquinn.app.smartspacer.plugin.notifications.providers.WhatsAppWidgetProvider
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.firstNotNull
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface DatabaseRepository {

    fun getBadgeCount(packageName: String): Int
    fun setBadgeCount(packageName: String, count: Int)

}

class DatabaseRepositoryImpl(private val context: Context): DatabaseRepository {

    companion object: KoinComponent {
        private val telegramRepository by inject<TelegramRepository>()

        private val COMPLICATIONS = mapOf(
            FacebookComplication.PACKAGE_NAME to FacebookComplication::class.java,
            TwitterComplication.PACKAGE_NAME to TwitterComplication::class.java,
            WhatsAppLegacyComplication.PACKAGE_NAME to WhatsAppLegacyComplication::class.java,
            WhatsAppWidgetProvider.PACKAGE_NAME_V2 to WhatsAppComplication::class.java,
            telegramRepository.getTelegramPackageName() to TelegramComplication::class.java,
            ThreadsComplication.PACKAGE_NAME to ThreadsComplication::class.java,
            InstagramComplication.PACKAGE_NAME to InstagramComplication::class.java,
            GoogleVoiceComplication.PACKAGE_NAME to GoogleVoiceComplication::class.java,
            OutlookComplication.PACKAGE_NAME to OutlookComplication::class.java,
            K9Complication.PACKAGE_NAME to K9Complication::class.java,
        )
    }

    private val database = BadgeDatabase.getDatabase(context)
    private val badgeDao = database.badgeDao()
    private val scope = MainScope()
    private var pendingChanges = HashSet<String>()

    private val badges = badgeDao.getBadges()
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, null)

    override fun getBadgeCount(packageName: String): Int {
        return runBlocking {
            badges.firstNotNull().firstOrNull { it.packageName == packageName }?.badgeCount ?: 0
        }
    }

    override fun setBadgeCount(packageName: String, count: Int) {
        scope.launch(Dispatchers.IO) {
            pendingChanges.add(packageName)
            badgeDao.updateBadge(Badge(packageName, count))
        }
    }

    private fun setupChanges() = scope.launch {
        badgeDao.getBadges().debounce(250L).collect {
            notifyChanges()
        }
    }

    private fun notifyChanges() {
        pendingChanges.forEach {
            val provider = COMPLICATIONS[it] ?: return@forEach
            SmartspacerComplicationProvider.notifyChange(context, provider)
        }
        pendingChanges.clear()
    }

    init {
        setupChanges()
    }

}