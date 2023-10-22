package com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories

import android.content.Context
import android.content.SharedPreferences
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepository.SmartspacerSetting
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.BaseSettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.createEncryptedSharedPrefDestructively

interface EncryptedSettingsRepository {

    /**
     *  The Master (AAS) token used to log in to private Google APIs and get auth tokens. Also
     *  required to refresh tokens, so it must be stored.
     */
    val aasToken: SmartspacerSetting<String>

    /**
     *  The current Wallet token used for the Wallet API. Can be refreshed using the [aasToken].
     */
    val walletToken: SmartspacerSetting<String>

    /**
     *  Whether to run loading on a metered connection for Dynamic targets
     */
    val reloadOnMeteredConnection: SmartspacerSetting<Boolean>

}

class EncryptedSettingsRepositoryImpl(
    context: Context
): BaseSettingsRepositoryImpl(), EncryptedSettingsRepository {

    companion object {
        private const val SHARED_PREFS_NAME = "encrypted_shared_prefs"
        private const val KEY_AAS_TOKEN = "aas_token"
        private const val KEY_WALLET_TOKEN = "wallet_token"
        private const val KEY_RELOAD_ON_METERED_CONNECTION = "reload_on_metered_connection"
        private const val DEFAULT_RELOAD_ON_METERED_CONNECTION = false

        @Synchronized
        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.createEncryptedSharedPrefDestructively(
                "${context.packageName}_$SHARED_PREFS_NAME"
            )
        }
    }

    override val aasToken = string(KEY_AAS_TOKEN, "")
    override val walletToken = string(KEY_WALLET_TOKEN, "")

    override val reloadOnMeteredConnection = boolean(
        KEY_RELOAD_ON_METERED_CONNECTION, DEFAULT_RELOAD_ON_METERED_CONNECTION
    )

    override val sharedPreferences by lazy {
        getSharedPreferences(context)
    }

    override suspend fun getBackup(): Map<String, String> {
        return emptyMap() //Not backed up
    }

    override suspend fun restoreBackup(settings: Map<String, String>) {
        //No-op, not backed up
    }

}