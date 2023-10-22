package com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline

import com.google.gson.annotations.SerializedName

data class SearchRoutesResponse(
    @SerializedName("outwardJourneys")
    val outwardJourneys: List<OutwardJourney>
) {

    data class OutwardJourney(
        @SerializedName("localDepartAt")
        val departAtTimestamp: String,
        @SerializedName("localArriveAt")
        val arriveAtTimestamp: String,
        @SerializedName("legs")
        val legs: List<Leg>,
        @SerializedName("unsellableReason")
        val unsellableReason: UnsellableReason?
    )

    data class Station(
        @SerializedName("code")
        val code: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("timezone")
        val timezone: String
    )

    data class Leg(
        @SerializedName("departureStation")
        val departureStation: Station,
        @SerializedName("arrivalStation")
        val arrivalStation: Station,
        @SerializedName("localDepartAt")
        val localDepartAt: String,
        @SerializedName("localArriveAt")
        val localArriveAt: String,
        @SerializedName("realtime")
        val realtime: Realtime?,
        @SerializedName("transport")
        val transport: TransportMode,
        @SerializedName("timetableId")
        val timetableId: String?,
        @SerializedName("carrier")
        val carrier: Carrier
    ) {

        data class Carrier(
            @SerializedName("code")
            val code: String,
            @SerializedName("name")
            val name: String
        )

        data class TransportMode(
            @SerializedName("code")
            val code: String,
            @SerializedName("name")
            val name: String
        )

        data class Realtime(
            @SerializedName("isCancelled")
            val isCancelled: Boolean
        )

    }

    data class UnsellableReason(
        @SerializedName("code")
        val code: String
    )

}
