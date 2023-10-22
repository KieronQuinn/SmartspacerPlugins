package com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline

import com.google.gson.annotations.SerializedName

data class TravelServiceInformationResponse(
    @SerializedName("customerOriginIndex")
    val customerOriginIndex: Int,
    @SerializedName("customerDestinationIndex")
    val customerDestinationIndex: Int,
    @SerializedName("transportMode")
    val transportMode: TransportMode,
    @SerializedName("realTime")
    val realTime: Realtime,
    @SerializedName("callingPoints")
    val callingPoints: List<CallingPoint>
) {

    data class TransportMode(
        @SerializedName("code")
        val code: String,
        @SerializedName("name")
        val name: String
    )

    data class Realtime(
        @SerializedName("isCancelled")
        val isCancelled: Boolean,
        @SerializedName("delayReasons")
        val delayReasons: List<String>,
        @SerializedName("cancellationReasons")
        val cancellationReasons: List<String>
    )

    data class CallingPoint(
        @SerializedName("location")
        val location: Location,
        @SerializedName("callingType")
        val callingType: String,
        @SerializedName("arrival")
        val arrival: Time?,
        @SerializedName("departure")
        val departure: Time?,
        @SerializedName("numberOfCarriages")
        val numberOfCarriages: Int,
        @SerializedName("index")
        val index: Int
    ) {

        data class Location(
            @SerializedName("genericCode")
            val genericCode: String,
            @SerializedName("inventoryCode")
            val inventoryCode: String,
            @SerializedName("name")
            val name: String,
            @SerializedName("shortName")
            val shortName: String
        )

        data class Time(
            @SerializedName("schedule")
            val schedule: Scheduled,
            @SerializedName("realTime")
            val realTime: RealTime?
        ) {

            data class Scheduled(
                @SerializedName("time")
                val time: String,
                @SerializedName("platform")
                val platform: String?
            )

            data class RealTime(
                @SerializedName("hasDeparted")
                val hasDeparted: Boolean,
                @SerializedName("status")
                val status: Status,
                @SerializedName("type")
                val type: String,
                @SerializedName("time")
                val time: String,
                @SerializedName("platform")
                val platform: String
            ) {

                enum class Status {
                    @SerializedName("cancelled")
                    CANCELLED,
                    @SerializedName("delayed")
                    DELAYED,
                    @SerializedName("late")
                    LATE,
                    @SerializedName("onTime")
                    ON_TIME,
                    @SerializedName("unknown")
                    UNKNOWN
                }

            }

        }

    }

}
