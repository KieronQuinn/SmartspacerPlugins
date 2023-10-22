package com.kieronquinn.app.smartspacer.plugin.amazon.repositories

import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDatabase
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface DatabaseRepository {

    fun getAmazonDeliveries(): Flow<List<AmazonDelivery>>
    suspend fun addAmazonDelivery(delivery: AmazonDelivery)
    suspend fun deleteAmazonDelivery(shipmentId: String)
    suspend fun clearAll()

}

class DatabaseRepositoryImpl(
    database: AmazonDatabase
): DatabaseRepository {

    private val amazonDelivery = database.amazonDeliveryDao()

    override fun getAmazonDeliveries() = amazonDelivery.getAll()

    override suspend fun addAmazonDelivery(delivery: AmazonDelivery) = withContext(Dispatchers.IO) {
        this@DatabaseRepositoryImpl.amazonDelivery.insert(delivery)
    }

    override suspend fun deleteAmazonDelivery(shipmentId: String) = withContext(Dispatchers.IO) {
        this@DatabaseRepositoryImpl.amazonDelivery.delete(shipmentId)
    }

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        amazonDelivery.clear()
    }

}