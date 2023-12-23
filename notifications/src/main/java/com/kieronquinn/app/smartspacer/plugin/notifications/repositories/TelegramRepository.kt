package com.kieronquinn.app.smartspacer.plugin.notifications.repositories

import android.appwidget.AppWidgetManager
import android.content.Context

interface TelegramRepository {

    fun getTelegramPackageName(): String

}

class TelegramRepositoryImpl(context: Context): TelegramRepository {

    companion object {
        private const val DEFAULT_TELEGRAM_PACKAGE_NAME = "org.telegram.messenger"

        /**
         *  We assume any package with both a ChatsWidgetProvider and a ContactsWidgetProvider
         *  is Telegram
         */
        private fun List<String>.isTelegram(): Boolean {
            if(none { it.endsWith(".ChatsWidgetProvider") }) return false
            return any { it.endsWith(".ContactsWidgetProvider") }
        }

        /**
         *  Looks through all the installed widgets and returns the first package matching our
         *  predicate for Telegram. If multiple Telegram apps are installed, the first will be used.
         */
        private fun findTelegramPackageName(context: Context): String {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val providers = appWidgetManager.installedProviders
                .groupBy { it.provider.packageName }
                .mapValues { it.value.map { p -> p.provider.className } }
            return providers.entries.firstOrNull {
                it.value.isTelegram()
            }?.key ?: DEFAULT_TELEGRAM_PACKAGE_NAME
        }
    }

    private val currentTelegramPackageName by lazy {
        findTelegramPackageName(context)
    }

    override fun getTelegramPackageName(): String {
        return currentTelegramPackageName
    }

}