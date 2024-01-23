package com.kieronquinn.app.smartspacer.plugin.amazon.model.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrackingData(
    @SerializedName("packageLocationDetails")
    val packageLocationDetails: PackageLocationDetails?,
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("responseCode")
    val responseCode: String
) : Parcelable {

    companion object {
        private const val RESPONSE_CODE_INVALID_TOKEN = "INVALID_TOKEN"
    }

    fun isTokenInvalid(): Boolean {
        return !success
    }

    @Parcelize
    data class PackageLocationDetails(
        @SerializedName("destinationAddress")
        val destinationAddress: Location?,
        @SerializedName("stopsRemaining")
        val stopsRemaining: Int?,
        @SerializedName("trackingObjectState")
        val status: Status?,
        @SerializedName("transporterDetails")
        val transporterDetails: Location?
    ): Parcelable {

        @Parcelize
        data class Location(
            @SerializedName("geoLocation")
            val geoLocation: GeoLocation?
        ) : Parcelable {

            @Parcelize
            data class GeoLocation(
                @SerializedName("latitude")
                val latitude: Double,
                @SerializedName("longitude")
                val longitude: Double
            ) : Parcelable

        }

        enum class Status {
            STARTUP,
            RENDERED,
            OUT_FOR_DELIVERY,
            PICKED_UP,
            YOU_ARE_NEXT,
            DELIVERED,
            ERROR,
            FATAL,
            ADDRESS_NOT_FOUND,
            SKIPPED_STOP,
            DELIVERY_FAILED,
            DELIVERY_ATTEMPTED,
            PARTIALLY_DELIVERED,
            REJECTED,
            NOT_DELIVERED;

            fun toStatus(): AmazonDelivery.Status {
                return when(this) {
                    STARTUP, RENDERED -> AmazonDelivery.Status.IN_TRANSIT
                    DELIVERED, PARTIALLY_DELIVERED -> AmazonDelivery.Status.DELIVERED
                    else -> AmazonDelivery.Status.OUT_FOR_DELIVERY
                }
            }
        }

    }

}