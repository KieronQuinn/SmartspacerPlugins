package com.kieronquinn.app.smartspacer.plugin.amazon.repositories

import android.content.Context
import android.content.SharedPreferences
import com.kieronquinn.app.smartspacer.plugin.amazon.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.amazon.model.AmazonDomain
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepository.SmartspacerSetting
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl

abstract class AmazonSettingsRepository: BaseSettingsRepositoryImpl() {

    abstract val domain: SmartspacerSetting<AmazonDomain>
    abstract val showProductImage: SmartspacerSetting<Boolean>
    abstract val showAdvanced: SmartspacerSetting<Boolean>

}

class AmazonSettingsRepositoryImpl(context: Context): AmazonSettingsRepository() {

    companion object {
        private const val KEY_DOMAIN = "domain"
        private val DEFAULT_DOMAIN = AmazonDomain.UNKNOWN
        private const val KEY_SHOW_PRODUCT_IMAGE = "show_product_image"
        private const val DEFAULT_SHOW_PRODUCT_IMAGE = true
        private const val KEY_SHOW_ADVANCED = "show_advanced"
        private const val DEFAULT_SHOW_ADVANCED = false
    }

    override val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("${BuildConfig.APPLICATION_ID}_prefs", Context.MODE_PRIVATE)
    }

    override val domain = enum(KEY_DOMAIN, DEFAULT_DOMAIN)
    override val showProductImage = boolean(KEY_SHOW_PRODUCT_IMAGE, DEFAULT_SHOW_PRODUCT_IMAGE)
    override val showAdvanced = boolean(KEY_SHOW_ADVANCED, DEFAULT_SHOW_ADVANCED)

    override suspend fun getBackup(): Map<String, String> {
        return emptyMap()
    }

    override suspend fun restoreBackup(settings: Map<String, String>) {
        //No-op
    }

}