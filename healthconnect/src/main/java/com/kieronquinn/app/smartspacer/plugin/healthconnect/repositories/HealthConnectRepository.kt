package com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories

import android.content.Context
import android.content.Intent
import android.health.connect.HealthConnectManager
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.Record
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.healthconnect.converters.DataTypeConverter
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.database.HealthData
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.HealthConnectRepository.HealthMetric
import com.kieronquinn.app.smartspacer.plugin.healthconnect.utils.extensions.getGrantedPermissions
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValue
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

interface HealthConnectRepository {

    fun isSdkAvailable(): Boolean

    suspend fun updateHealthMetric(smartspacerId: String, authority: String): Boolean
    fun getHealthData(smartspacerId: String): HealthMetric?
    fun getPermission(dataType: DataType): String
    fun removeData(smartspacerId: String)
    fun getOpenHealthConnectIntent(): Intent
    suspend fun hasPermission(permission: String): Boolean

    sealed class HealthMetric {
        object NoPermission: HealthMetric()
        data class Metric(val value: String?, val fromPackage: String?): HealthMetric()
    }

}

class HealthConnectRepositoryImpl(
    private val context: Context,
    private val databaseRepository: DatabaseRepository,
    private val dataRepository: DataRepository
): HealthConnectRepository {

    private val scope = MainScope()
    private val updateTimes = HashMap<String, Long>()
    private var grantedPermissions = emptySet<String>()

    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    override fun isSdkAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    override suspend fun updateHealthMetric(smartspacerId: String, authority: String): Boolean {
        if(!isSdkAvailable()) return false
        val config = dataRepository.getComplicationData(smartspacerId, ComplicationData::class.java)
            ?: return false
        if(!hasPermissionForRecord(config.dataType.record)) {
            writeHealthDataNoPermission(smartspacerId, authority)
            return false
        }
        val healthData = getHealthData(context, config)
        val wasRateLimited = healthData?.third == true
        writeHealthData(
            smartspacerId,
            authority,
            healthData?.first,
            wasRateLimited,
            healthData?.second,
        )
        return !wasRateLimited
    }

    override fun getHealthData(smartspacerId: String): HealthMetric? {
        val healthData = databaseRepository.getHealthData(smartspacerId) ?: return null
        return if(healthData.hasPermission){
            val metric = healthData.value?.let { String(it.bytes) } ?: return null
            val packageName = healthData.fromPackage?.let { String(it.bytes) } ?: return null
            HealthMetric.Metric(metric, packageName)
        }else HealthMetric.NoPermission
    }

    override fun getPermission(dataType: DataType): String {
        return HealthPermission.getReadPermission(dataType.record)
    }

    override suspend fun hasPermission(permission: String): Boolean {
        updatePermissions()
        return grantedPermissions.contains(permission)
    }

    override fun removeData(smartspacerId: String) {
        scope.launch {
            databaseRepository.removeHealthData(smartspacerId)
            dataRepository.deleteComplicationData(smartspacerId)
        }
    }

    override fun getOpenHealthConnectIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS)
                .putExtra(Intent.EXTRA_PACKAGE_NAME, BuildConfig.APPLICATION_ID)
        } else {
            Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
        }
    }

    private suspend fun getHealthData(
        context: Context,
        config: ComplicationData
    ): Triple<String?, String?, Boolean>? {
        val converter = DataTypeConverter.getConverterForRecord(config.dataType.record)
            ?: return null
        val record = converter.getData(healthConnectClient, config) ?: return null
        val formatted = record.first?.let {
            converter.formatToString(it, context, config) ?: return null
        }
        return Triple(formatted, record.second, record.third)
    }

    private suspend fun writeHealthDataNoPermission(smartspacerId: String, authority: String) {
        val healthData = HealthData(
            smartspacerId,
            authority,
            false,
            null,
            null,
            false
        )
        databaseRepository.setHealthData(healthData)
    }

    private suspend fun writeHealthData(
        smartspacerId: String,
        authority: String,
        value: String?,
        wasRateLimited: Boolean,
        fromPackage: String?
    ) {
        val encryptedValue = value?.let { EncryptedValue(it.toByteArray()) }
        val encryptedPackage = fromPackage?.let { EncryptedValue(it.toByteArray()) }
        val healthData = HealthData(
            smartspacerId,
            authority,
            true,
            encryptedValue,
            encryptedPackage,
            wasRateLimited
        )
        databaseRepository.setHealthData(healthData)
    }

    @Synchronized
    private fun onHealthDataChanged(new: List<HealthData>) = new.forEach {
        val existing = updateTimes[it.smartspacerId]
        if(existing == null || existing != it.updatedAt) {
            SmartspacerComplicationProvider.notifyChange(context, it.authority, it.smartspacerId)
        }
        updateTimes[it.smartspacerId] = it.updatedAt
    }

    private fun setupChangeListener() = scope.launch {
        databaseRepository.getAllHealthData().collect {
            onHealthDataChanged(it)
        }
    }

    private suspend fun updatePermissions() {
        healthConnectClient.getGrantedPermissions()?.let {
            grantedPermissions = it
        }
    }

    private suspend fun hasPermissionForRecord(record: KClass<out Record>): Boolean {
        val permission = HealthPermission.getReadPermission(record)
        return hasPermission(permission)
    }

    init {
        setupChangeListener()
    }

}