package com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class SearchRoutesRequest(
    @SerializedName("origin")
    val origin: String,
    @SerializedName("destination")
    val destination: String,
    @SerializedName("journeyType")
    val journeyType: String = "Single",
    @SerializedName("outwardJourney")
    val outwardJourney: OutwardJourney,
    @SerializedName("connections")
    val connections: List<Connection>,
    @SerializedName("transportModes")
    val transportModes: List<String> = listOf("Mixed"),
    @SerializedName("passengers")
    val passengers: List<Passenger> = listOf(Passenger()),
    @SerializedName("composition")
    val composition: List<String> = listOf("Through", "DirectSplit", "InterchangeSplit"),
    @SerializedName("additionalData")
    val additionalData: List<String> = listOf("realtime")
) {

    data class OutwardJourney(
        @SerializedName("type")
        val type: String = "LeaveAfter",
        @SerializedName("dateTime")
        val dateTime: String
    )

    data class Connection(
        @SerializedName("code")
        val data: String
    )

    data class Passenger(
        @SerializedName("id")
        val id: String = UUID.randomUUID().toString()
    )

}
