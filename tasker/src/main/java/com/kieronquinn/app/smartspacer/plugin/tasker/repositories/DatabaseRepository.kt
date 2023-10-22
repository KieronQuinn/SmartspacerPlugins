package com.kieronquinn.app.smartspacer.plugin.tasker.repositories

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.firstNotNull
import com.kieronquinn.app.smartspacer.plugin.tasker.complications.TaskerComplication
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Complication
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Requirement
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Target
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.TaskerPluginDatabase
import com.kieronquinn.app.smartspacer.plugin.tasker.requirements.TaskerRequirement
import com.kieronquinn.app.smartspacer.plugin.tasker.targets.TaskerTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerRequirementProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface DatabaseRepository {

    fun getAllTargets(): Flow<List<Target>>
    suspend fun getTarget(smartspacerId: String): Target?
    fun getTargetSync(smartspacerId: String): Target?
    fun getTargetAsFlow(smartspacerId: String): Flow<Target?>
    suspend fun addTarget(target: Target)
    fun deleteTarget(smartspacerId: String)

    fun updateActiveTarget(
        context: Context,
        template: TargetTemplate,
        smartspacerId: String,
        replacements: Map<String, String>,
        refreshPeriod: Int,
        refreshIfNotVisible: Boolean
    )

    fun setTargetVisibility(
        context: Context,
        smartspacerId: String,
        visible: Boolean
    )

    fun getAllComplications(): Flow<List<Complication>>
    suspend fun getComplication(smartspacerId: String): Complication?
    fun getComplicationSync(smartspacerId: String): Complication?
    fun getComplicationAsFlow(smartspacerId: String): Flow<Complication?>
    suspend fun addComplication(complication: Complication)
    fun deleteComplication(smartspacerId: String)

    fun updateActiveComplication(
        context: Context,
        template: ComplicationTemplate,
        smartspacerId: String,
        replacements: Map<String, String>,
        refreshPeriod: Int,
        refreshIfNotVisible: Boolean
    )

    fun setComplicationVisibility(
        context: Context,
        smartspacerId: String,
        visible: Boolean
    )

    fun getAllRequirements(): Flow<List<Requirement>>
    suspend fun getRequirement(smartspacerId: String): Requirement?
    fun getRequirementSync(smartspacerId: String): Requirement?
    fun getRequirementAsFlow(smartspacerId: String): Flow<Requirement?>
    suspend fun addRequirement(requirement: Requirement)
    fun deleteRequirement(smartspacerId: String)

    fun setRequirementMet(
        context: Context,
        smartspacerId: String,
        isMet: Boolean
    )

}

class DatabaseRepositoryImpl(
    database: TaskerPluginDatabase
): DatabaseRepository {

    private val scope = MainScope()
    private val targetDao = database.targetDao()
    private val complicationDao = database.complicationDao()
    private val requirementDao = database.requirementDao()

    private val targets = targetDao.getAll()
        .mapLatest { it.associateBy { target -> target.smartspacerId } }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val complications = complicationDao.getAll()
        .mapLatest { it.associateBy { complication -> complication.smartspacerId } }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val requirements = requirementDao.getAll()
        .mapLatest { it.associateBy { requirement -> requirement.smartspacerId } }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, null)

    override fun getAllTargets(): Flow<List<Target>> {
        return targetDao.getAll()
            .flowOn(Dispatchers.IO)
    }

    override fun getAllComplications(): Flow<List<Complication>> {
        return complicationDao.getAll()
            .flowOn(Dispatchers.IO)
    }

    override fun getAllRequirements(): Flow<List<Requirement>> {
        return requirementDao.getAll()
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getTarget(smartspacerId: String): Target? {
        return targets.firstNotNull()[smartspacerId]
    }

    override suspend fun getComplication(smartspacerId: String): Complication? {
        return complications.firstNotNull()[smartspacerId]
    }

    override suspend fun getRequirement(smartspacerId: String): Requirement? {
        return requirements.firstNotNull()[smartspacerId]
    }

    override fun getTargetSync(smartspacerId: String): Target? {
        return runBlocking {
            getTarget(smartspacerId)
        }
    }

    override fun getComplicationSync(smartspacerId: String): Complication? {
        return runBlocking {
            getComplication(smartspacerId)
        }
    }

    override fun getRequirementSync(smartspacerId: String): Requirement? {
        return runBlocking {
            getRequirement(smartspacerId)
        }
    }

    override fun getTargetAsFlow(smartspacerId: String): Flow<Target?> {
        return targetDao.get(smartspacerId)
    }

    override fun getComplicationAsFlow(smartspacerId: String): Flow<Complication?> {
        return complicationDao.get(smartspacerId)
    }

    override fun getRequirementAsFlow(smartspacerId: String): Flow<Requirement?> {
        return requirementDao.get(smartspacerId)
    }

    override suspend fun addTarget(target: Target) {
        withContext(Dispatchers.IO){
            targetDao.insert(target)
        }
    }

    override suspend fun addComplication(complication: Complication) {
        withContext(Dispatchers.IO){
            complicationDao.insert(complication)
        }
    }

    override suspend fun addRequirement(requirement: Requirement) {
        withContext(Dispatchers.IO){
            requirementDao.insert(requirement)
        }
    }

    override fun deleteTarget(smartspacerId: String) {
        scope.launch(Dispatchers.IO) {
            targetDao.delete(smartspacerId)
        }
    }

    override fun deleteComplication(smartspacerId: String) {
        scope.launch(Dispatchers.IO) {
            complicationDao.delete(smartspacerId)
        }
    }

    override fun deleteRequirement(smartspacerId: String) {
        scope.launch(Dispatchers.IO) {
            requirementDao.delete(smartspacerId)
        }
    }

    override fun updateActiveTarget(
        context: Context,
        template: TargetTemplate,
        smartspacerId: String,
        replacements: Map<String, String>,
        refreshPeriod: Int,
        refreshIfNotVisible: Boolean
    ) {
        scope.launch(Dispatchers.IO) {
            val current = targets.firstNotNull()[smartspacerId] ?: return@launch
            val active = template.copyWithManipulations(context, replacements)
            val updated = current.copy(
                current = active,
                refreshPeriod = refreshPeriod,
                refreshIfNotVisible = refreshIfNotVisible,
                updatedAt = System.currentTimeMillis()
            )
            targetDao.insert(updated)
            delay(250L)
            SmartspacerTargetProvider.notifyChange(context, TaskerTarget::class.java, smartspacerId)
        }
    }

    override fun updateActiveComplication(
        context: Context,
        template: ComplicationTemplate,
        smartspacerId: String,
        replacements: Map<String, String>,
        refreshPeriod: Int,
        refreshIfNotVisible: Boolean
    ) {
        scope.launch(Dispatchers.IO) {
            val current = complications.firstNotNull()[smartspacerId] ?: return@launch
            val active = template.copyWithManipulations(context, replacements)
            val updated = current.copy(
                current = active,
                refreshPeriod = refreshPeriod,
                refreshIfNotVisible = refreshIfNotVisible,
                updatedAt = System.currentTimeMillis()
            )
            complicationDao.insert(updated)
            delay(250L)
            SmartspacerComplicationProvider.notifyChange(context, TaskerComplication::class.java, smartspacerId)
        }
    }

    override fun setTargetVisibility(context: Context, smartspacerId: String, visible: Boolean) {
        scope.launch(Dispatchers.IO) {
            val current = targets.firstNotNull()[smartspacerId] ?: return@launch
            val updated = current.copy(isVisible = visible)
            targetDao.insert(updated)
            delay(250L)
            SmartspacerTargetProvider.notifyChange(context, TaskerTarget::class.java, smartspacerId)
        }
    }

    override fun setComplicationVisibility(
        context: Context,
        smartspacerId: String,
        visible: Boolean
    ) {
        scope.launch(Dispatchers.IO) {
            val current = complications.firstNotNull()[smartspacerId] ?: return@launch
            val updated = current.copy(isVisible = visible)
            complicationDao.insert(updated)
            delay(250L)
            SmartspacerComplicationProvider.notifyChange(context, TaskerComplication::class.java, smartspacerId)
        }
    }

    override fun setRequirementMet(context: Context, smartspacerId: String, isMet: Boolean) {
        scope.launch(Dispatchers.IO) {
            val current = requirements.firstNotNull()[smartspacerId] ?: return@launch
            val updated = current.copy(isMet = isMet, updatedAt = System.currentTimeMillis())
            requirementDao.insert(updated)
            delay(250L)
            SmartspacerRequirementProvider.notifyChange(
                context, TaskerRequirement::class.java, smartspacerId
            )
        }
    }

}