package com.kieronquinn.app.smartspacer.plugin.shared.repositories

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.RoomEncryptedSettingsRepository.RoomEncryptionFailedCallback
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.createEncryptedSharedPrefDestructively
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 *  Holds the encryption key and IV for the encrypted values in the Room database
 */
interface RoomEncryptedSettingsRepository {

    /**
     *  The encryption key used to encrypt sensitive data stored in the Room database.
     */
    fun getDatabaseEncryptionKey(): SecretKey

    /**
     *  The encryption IV used to encrypt sensitive data stored in the Room database.
     */
    fun getDatabaseEncryptionIV(): IvParameterSpec

    interface RoomEncryptionFailedCallback {
        fun onEncryptionFailed()
    }

}

class RoomEncryptedSettingsRepositoryImpl(
    context: Context,
    failedCallback: RoomEncryptionFailedCallback?
): BaseSettingsRepositoryImpl(), RoomEncryptedSettingsRepository {

    companion object {
        private const val SHARED_PREFS_NAME = "room_encrypted_shared_prefs"
        private const val KEY_ENCRYPTION_KEY = "encryption_key"
        private const val KEY_ENCRYPTION_IV = "encryption_iv"

        @Synchronized
        private fun getSharedPreferences(
            context: Context,
            failedCallback: RoomEncryptionFailedCallback?
        ): SharedPreferences {
            return context.createEncryptedSharedPrefDestructively(
                "${context.packageName}_$SHARED_PREFS_NAME"
            ) {
                failedCallback?.onEncryptionFailed()
            }
        }
    }

    override val sharedPreferences by lazy {
        getSharedPreferences(context, failedCallback)
    }

    private val encryptionKey = string(KEY_ENCRYPTION_KEY, "")
    private val encryptionIV = string(KEY_ENCRYPTION_IV, "")

    override suspend fun getBackup(): Map<String, String> {
        return emptyMap() //Not backed up
    }

    override suspend fun restoreBackup(settings: Map<String, String>) {
        //No-op, not backed up
    }

    @Synchronized
    override fun getDatabaseEncryptionKey(): SecretKey {
        return loadEncryptionKey() ?: saveEncryptionKey()
    }

    @Synchronized
    override fun getDatabaseEncryptionIV(): IvParameterSpec {
        return loadEncryptionIV() ?: saveEncryptionIV()
    }

    private fun loadEncryptionKey(): SecretKey? {
        val b64Key = encryptionKey.getSync()
        if(b64Key.isEmpty()) return null
        val key = Base64.decode(b64Key, Base64.DEFAULT)
        return SecretKeySpec(key, "AES")
    }

    private fun saveEncryptionKey(): SecretKey {
        return KeyGenerator.getInstance("AES").apply {
            init(256)
        }.generateKey().also {
            encryptionKey.setSync(Base64.encodeToString(it.encoded, Base64.DEFAULT))
        }
    }

    private fun loadEncryptionIV(): IvParameterSpec? {
        val b64IV = encryptionIV.getSync()
        if(b64IV.isEmpty()) return null
        val iv = Base64.decode(b64IV, Base64.DEFAULT)
        return IvParameterSpec(iv)
    }

    private fun saveEncryptionIV(): IvParameterSpec {
        val bytes = ByteArray(16)
        SecureRandom().apply {
            nextBytes(bytes)
        }
        return IvParameterSpec(bytes).also {
            encryptionIV.setSync(Base64.encodeToString(bytes, Base64.DEFAULT))
        }
    }

}