package com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories

import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.database.WalletDatabase
import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.database.WalletValuable
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.RoomEncryptedSettingsRepository.RoomEncryptionFailedCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository as SharedDatabaseRepository

interface DatabaseRepository: SharedDatabaseRepository {

    fun getWalletValuables(): Flow<List<WalletValuable>>
    fun getWalletValuableById(id: String): Flow<WalletValuable?>

    suspend fun addWalletValuable(walletValuable: WalletValuable)
    suspend fun deleteWalletValuable(id: String)

}

class DatabaseRepositoryImpl(
    database: WalletDatabase
): DatabaseRepository, DatabaseRepositoryImpl(database.targetDataDao()), RoomEncryptionFailedCallback {

    private val walletValuable = database.walletValuableDao()
    private val databaseLock = Mutex()

    override fun getWalletValuables() = walletValuable.getAll()

    override fun getWalletValuableById(id: String): Flow<WalletValuable?> {
        return walletValuable.getValuableById(id)
    }

    override suspend fun addWalletValuable(
        walletValuable: WalletValuable
    ) = withContext(Dispatchers.IO) {
        databaseLock.withLock {
            this@DatabaseRepositoryImpl.walletValuable.insert(walletValuable)
        }
    }

    override suspend fun deleteWalletValuable(id: String) = withContext(Dispatchers.IO) {
        databaseLock.withLock {
            this@DatabaseRepositoryImpl.walletValuable.delete(id)
        }
    }

    override fun onEncryptionFailed() {
        walletValuable.clear()
    }

}