package com.kieronquinn.app.smartspacer.plugin.shared.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationDataDao
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.RequirementData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.RequirementDataDao
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetDataDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface DatabaseRepository {

    suspend fun getTargetDataById(id: String): TargetData?
    suspend fun addTargetData(targetData: TargetData)
    suspend fun deleteTargetData(id: String)

    suspend fun getComplicationDataById(id: String): ComplicationData?
    suspend fun addComplicationData(complicationData: ComplicationData)
    suspend fun deleteComplicationData(id: String)

    suspend fun getRequirementDataById(id: String): RequirementData?
    suspend fun addRequirementData(requirementData: RequirementData)
    suspend fun deleteRequirementData(id: String)

    fun getTargetDataByIdAsFlow(id: String): Flow<TargetData?>
    fun getComplicationDataByIdAsFlow(id: String): Flow<ComplicationData?>
    fun getRequirementDataByIdAsFlow(id: String): Flow<RequirementData?>

}

open class DatabaseRepositoryImpl(
    private val _targetData: TargetDataDao? = null,
    private val _complicationData: ComplicationDataDao? = null,
    private val _requirementData: RequirementDataDao? = null
): DatabaseRepository {

    private val targetData
        get() = _targetData ?: throw RuntimeException("TargetDataDao is not set")

    private val complicationData
        get() = _complicationData ?: throw RuntimeException("ComplicationDataDao is not set")

    private val requirementData
        get() = _requirementData ?: throw RuntimeException("RequirementDataDao is not set")

    override suspend fun getTargetDataById(id: String): TargetData? = withContext(Dispatchers.IO) {
        targetData.getById(id)
    }

    override fun getTargetDataByIdAsFlow(id: String): Flow<TargetData?> {
        return targetData.getByIdAsFlow(id)
    }

    override suspend fun addTargetData(targetData: TargetData) = withContext(Dispatchers.IO) {
        this@DatabaseRepositoryImpl.targetData.insert(targetData)
    }

    override suspend fun deleteTargetData(id: String) = withContext(Dispatchers.IO) {
        targetData.delete(id)
    }

    override suspend fun getComplicationDataById(id: String): ComplicationData? {
        return withContext(Dispatchers.IO) {
            complicationData.getById(id)
        }
    }

    override fun getComplicationDataByIdAsFlow(id: String): Flow<ComplicationData?> {
        return complicationData.getByIdAsFlow(id)
    }

    override suspend fun addComplicationData(complicationData: ComplicationData) {
        withContext(Dispatchers.IO) {
            this@DatabaseRepositoryImpl.complicationData.insert(complicationData)
        }
    }

    override suspend fun deleteComplicationData(id: String) = withContext(Dispatchers.IO) {
        complicationData.delete(id)
    }

    override suspend fun getRequirementDataById(id: String): RequirementData? {
        return withContext(Dispatchers.IO) {
            requirementData.getById(id)
        }
    }

    override fun getRequirementDataByIdAsFlow(id: String): Flow<RequirementData?> {
        return requirementData.getByIdAsFlow(id)
    }

    override suspend fun addRequirementData(requirementData: RequirementData) {
        withContext(Dispatchers.IO) {
            this@DatabaseRepositoryImpl.requirementData.insert(requirementData)
        }
    }

    override suspend fun deleteRequirementData(id: String) = withContext(Dispatchers.IO) {
        requirementData.delete(id)
    }

}