package com.kieronquinn.app.smartspacer.plugin.shared.repositories

import android.content.Context
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.RequirementData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetData
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

interface DataRepository {

    fun <T> getTargetDataFlow(id: String, type: Class<T>): Flow<T?>
    fun <T> getTargetData(id: String, type: Class<T>): T?
    fun deleteTargetData(id: String)
    suspend fun addTargetData(targetData: TargetData)

    fun <T> getComplicationDataFlow(id: String, type: Class<T>): Flow<T?>
    fun <T> getComplicationData(id: String, type: Class<T>): T?
    fun deleteComplicationData(id: String)
    suspend fun addComplicationData(complicationData: ComplicationData)

    fun <T> getRequirementDataFlow(id: String, type: Class<T>): Flow<T?>
    fun <T> getRequirementData(id: String, type: Class<T>): T?
    fun deleteRequirementData(id: String)
    suspend fun addRequirementData(requirementData: RequirementData)

    fun <T> updateTargetData(
        id: String,
        type: Class<T>,
        dataType: String,
        onComplete: ((context: Context, smartspacerId: String) -> Unit)? = null,
        update: (T?) -> T
    )

    fun <T> updateComplicationData(
        id: String,
        type: Class<T>,
        dataType: String,
        onComplete: ((context: Context, smartspacerId: String) -> Unit)? = null,
        update: (T?) -> T
    )

    fun <T> updateRequirementData(
        id: String,
        type: Class<T>,
        dataType: String,
        onComplete: ((context: Context, smartspacerId: String) -> Unit)? = null,
        update: (T?) -> T
    )

}

class DataRepositoryImpl(
    private val context: Context,
    private val gson: Gson,
    private val databaseRepository: DatabaseRepository
): DataRepository {

    private val scope = MainScope()

    override fun <T> getTargetData(id: String, type: Class<T>): T? = runBlocking {
        databaseRepository.getTargetDataById(id)?.let {
            gson.fromJson(it.data, type)
        }
    }

    override fun <T> getTargetDataFlow(id: String, type: Class<T>): Flow<T?> {
        return databaseRepository.getTargetDataByIdAsFlow(id).map {
            gson.fromJson(it?.data ?: return@map null, type)
        }
    }

    override suspend fun addTargetData(targetData: TargetData) {
        databaseRepository.addTargetData(targetData)
    }

    override fun deleteTargetData(id: String) {
        scope.launch {
            databaseRepository.deleteTargetData(id)
        }
    }

    override fun <T> updateTargetData(
        id: String,
        type: Class<T>,
        dataType: String,
        onComplete: ((context: Context, smartspacerId: String) -> Unit)?,
        update: (T?) -> T
    ) {
        scope.launch {
            val targetData = getTargetData(id, type)
            val updated = update(targetData)
            val data = TargetData(id, dataType, gson.toJson(updated))
            addTargetData(data)
            delay(500L)
            onComplete?.invoke(context, id)
        }
    }

    override fun <T> getComplicationData(id: String, type: Class<T>): T? = runBlocking {
        databaseRepository.getComplicationDataById(id)?.let {
            gson.fromJson(it.data, type)
        }
    }

    override fun <T> getComplicationDataFlow(id: String, type: Class<T>): Flow<T?> {
        return databaseRepository.getComplicationDataByIdAsFlow(id).map {
            gson.fromJson(it?.data ?: return@map null, type)
        }
    }

    override suspend fun addComplicationData(complicationData: ComplicationData) {
        databaseRepository.addComplicationData(complicationData)
    }

    override fun deleteComplicationData(id: String) {
        scope.launch {
            databaseRepository.deleteComplicationData(id)
        }
    }

    override fun <T> updateComplicationData(
        id: String,
        type: Class<T>,
        dataType: String,
        onComplete: ((context: Context, smartspacerId: String) -> Unit)?,
        update: (T?) -> T
    ) {
        scope.launch {
            val complicationData = getComplicationData(id, type)
            val updated = update(complicationData)
            val data = ComplicationData(id, dataType, gson.toJson(updated))
            addComplicationData(data)
            delay(500L)
            onComplete?.invoke(context, id)
        }
    }



    override fun <T> getRequirementData(id: String, type: Class<T>): T? = runBlocking {
        databaseRepository.getRequirementDataById(id)?.let {
            gson.fromJson(it.data, type)
        }
    }

    override fun <T> getRequirementDataFlow(id: String, type: Class<T>): Flow<T?> {
        return databaseRepository.getRequirementDataByIdAsFlow(id).map {
            gson.fromJson(it?.data ?: return@map null, type)
        }
    }

    override suspend fun addRequirementData(requirementData: RequirementData) {
        databaseRepository.addRequirementData(requirementData)
    }

    override fun deleteRequirementData(id: String) {
        scope.launch {
            databaseRepository.deleteRequirementData(id)
        }
    }

    override fun <T> updateRequirementData(
        id: String,
        type: Class<T>,
        dataType: String,
        onComplete: ((context: Context, smartspacerId: String) -> Unit)?,
        update: (T?) -> T
    ) {
        scope.launch {
            val requirementData = getRequirementData(id, type)
            val updated = update(requirementData)
            val data = RequirementData(id, dataType, gson.toJson(updated))
            addRequirementData(data)
            delay(500L)
            onComplete?.invoke(context, id)
        }
    }

}