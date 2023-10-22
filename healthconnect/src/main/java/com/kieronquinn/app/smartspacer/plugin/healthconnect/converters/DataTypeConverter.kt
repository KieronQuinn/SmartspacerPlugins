package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import android.os.RemoteException
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ElevationGainedRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.PowerRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WheelchairPushesRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType
import com.kieronquinn.app.smartspacer.plugin.healthconnect.utils.extensions.readAllRecords
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.reflect.KClass

abstract class DataTypeConverter<T: Record, A : Any> {

    companion object {
        fun getConverterForRecord(record: KClass<out Record>): DataTypeConverter<out Record, out Any>? {
            return when(record) {
                ActiveCaloriesBurnedRecord::class -> ActiveCaloriesBurnedRecordConverter
                BloodGlucoseRecord::class -> BloodGlucoseRecordConverter
                BloodPressureRecord::class -> BloodPressureRecordConverter
                BodyTemperatureRecord::class -> BodyTemperatureRecordConverter
                DistanceRecord::class -> DistanceRecordConverter
                ElevationGainedRecord::class -> ElevationGainedRecordConverter
                FloorsClimbedRecord::class -> FloorsClimbedRecordConverter
                HeartRateRecord::class -> HeartRateRecordConverter
                HeartRateVariabilityRmssdRecord::class -> HeartRateVariabilityRmssdRecordConverter
                HydrationRecord::class -> HydrationRecordConverter
                OxygenSaturationRecord::class -> OxygenSaturationRecordConverter
                PowerRecord::class -> PowerRecordConverter
                RespiratoryRateRecord::class -> RespiratoryRateRecordConverter
                RestingHeartRateRecord::class -> RestingHeartRateRecordConverter
                SleepSessionRecord::class -> SleepSessionRecordConverter
                SpeedRecord::class -> SpeedRecordConverter
                StepsRecord::class -> StepsRecordConverter
                TotalCaloriesBurnedRecord::class -> TotalCaloriesBurnedRecordConverter
                Vo2MaxRecord::class -> Vo2MaxRecordConverter
                WheelchairPushesRecord::class -> WheelchairPushesRecordConverter
                else -> null
            }
        }
    }

    abstract fun A.format(context: Context, config: ComplicationData): String?

    fun formatToString(data: Any, context: Context, config: ComplicationData): String? {
        data as A
        return data.format(context, config)
    }
    
    open suspend fun getData(
        healthConnectClient: HealthConnectClient,
        config: ComplicationData
    ): Triple<A?, String?, Boolean>? {
        val dataType = config.dataType
        val resetTime = config.resetTime
        val timeout = config.timeout
        val startTime = when {
            resetTime != null -> {
                if(resetTime.isBefore(LocalTime.now())){
                    resetTime.atDate(LocalDate.now())
                }else{
                    resetTime.atDate(LocalDate.now().minusDays(1))
                }.atZone(ZoneOffset.systemDefault())
            }
            timeout != null -> {
                LocalDateTime.now().minus(timeout.duration).atZone(ZoneOffset.systemDefault())
            }
            else -> return null
        }
        return try {
            if(dataType.type != null) {
                healthConnectClient.getAggregateData(startTime, dataType)
            }else{
                healthConnectClient.getLatestData(startTime, dataType)
            }.let { Triple(it?.first, it?.second, false) }
        }catch (e: RemoteException) {
            //Rate limited :(
            Triple(null, null, true)
        }
    }

    private suspend fun HealthConnectClient.getLatestData(
        startTime: ZonedDateTime,
        dataType: DataType
    ): Pair<A, String>? {
        val timeRangeFilter = TimeRangeFilter.between(
            startTime.toLocalDateTime(),
            LocalDateTime.now()
        )
        val records = readAllRecords(dataType.record, timeRangeFilter) as List<T>
        val data = records.maxByOrNull { it.getTime() } ?: return null
        val fromPackage = data.metadata.dataOrigin.packageName
        return Pair(data as? A ?: return null, fromPackage)
    }

    private suspend fun HealthConnectClient.getAggregateData(
        startTime: ZonedDateTime,
        dataType: DataType
    ): Pair<A, String>? {
        val metric = dataType.type!!
        val timeRangeFilter = TimeRangeFilter.between(
            startTime.toLocalDateTime(),
            LocalDateTime.now()
        )
        val request = AggregateRequest(
            metrics = setOf(metric),
            timeRangeFilter = timeRangeFilter
        )
        val result = aggregate(request)
        val fromPackage = result.dataOrigins.firstOrNull()?.packageName ?: return null
        val data = result[metric] as? A ?: return null
        return Pair(data, fromPackage)
    }

    abstract fun T.getTime(): Instant

    fun Double.format(decimalPlaces: Int = 2): String {
        return String.format("%.${decimalPlaces}f", this)
    }

}