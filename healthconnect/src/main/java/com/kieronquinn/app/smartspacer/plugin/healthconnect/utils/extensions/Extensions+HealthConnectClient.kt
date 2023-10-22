package com.kieronquinn.app.smartspacer.plugin.healthconnect.utils.extensions

import android.os.RemoteException
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlin.reflect.KClass

suspend fun HealthConnectClient.getGrantedPermissions(): Set<String>? {
    return try {
        permissionController.getGrantedPermissions()
    }catch (e: RemoteException) {
        //Seriously, THIS is getting rate limited???
        null
    }
}

/**
 *  Wrapper for [HealthConnectClient.readRecords] which will read all records over the last
 *  time period [timeRangeFilter] and a given [record] type. Automatically handles pages &
 *  remote crashes.
 */
suspend fun HealthConnectClient.readAllRecords(
    record: KClass<out Record>,
    timeRangeFilter: TimeRangeFilter
): List<Record> {
    var token: String? = null
    val records = ArrayList<Record>()
    do {
        val request = ReadRecordsRequest(
            record,
            timeRangeFilter =  timeRangeFilter,
            pageToken = token
        )
        try {
            val response = readRecords(request)
            token = response.pageToken
            records.addAll(response.records)
        }catch (e: Exception) {
            Log.e("HCR", "Error getting records", e)
            //Service has failed, stop here
            token = null
            throw e
        }
    } while (token != null)
    return records
}