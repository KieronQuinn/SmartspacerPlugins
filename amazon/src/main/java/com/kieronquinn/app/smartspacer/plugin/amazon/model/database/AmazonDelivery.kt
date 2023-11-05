package com.kieronquinn.app.smartspacer.plugin.amazon.model.database

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.ImageType
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.readEncryptedBitmap
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.unescape
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.writeEncryptedBitmap
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValue
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Entity
@Parcelize
data class AmazonDelivery(
    @PrimaryKey
    @ColumnInfo(name = "shipment_id")
    val shipmentId: String,
    @ColumnInfo(name = "name")
    val name: EncryptedValue,
    @ColumnInfo(name = "image_url")
    val imageUrl: EncryptedValue,
    @ColumnInfo("line_item_id")
    val lineItemId: EncryptedValue,
    @ColumnInfo("order_id")
    val orderId: EncryptedValue,
    @ColumnInfo(name = "status")
    val status: EncryptedValue,
    @ColumnInfo(name = "message")
    val message: EncryptedValue,
    @ColumnInfo(name = "tracking_id")
    val trackingId: EncryptedValue?,
    @ColumnInfo(name = "tracking_data")
    val trackingData: EncryptedValue?,
    @ColumnInfo(name = "dismissed_at_status")
    val dismissedAtStatus: Status? = null
): KoinComponent, Parcelable {

    enum class Status(@StringRes val content: Int) {
        ORDERED(R.string.target_amazon_status_ordered),
        SHIPPED(R.string.target_amazon_status_shipped),
        IN_TRANSIT(R.string.target_amazon_status_in_transit),
        OUT_FOR_DELIVERY(R.string.target_amazon_status_out_for_delivery),
        DELIVERED(R.string.target_amazon_status_delivered)
    }

    @Parcelize
    data class Delivery(
        val shipmentId: String,
        val name: String,
        val imageUrl: String,
        val lineItemId: String,
        val orderId: String,
        val status: Status,
        val message: String,
        val trackingId: String?,
        val trackingData: TrackingData?,
        val dismissedAtStatus: Status?,
        @IgnoredOnParcel
        val imageBitmap: Bitmap? = null,
        @IgnoredOnParcel
        val mapBitmap: Bitmap? = null
    ): KoinComponent, Parcelable {

        suspend fun encrypt(context: Context): AmazonDelivery {
            val gson by inject<Gson>()
            if (imageBitmap != null) {
                context.writeEncryptedBitmap(shipmentId, ImageType.IMAGE, imageBitmap)
            }
            if (mapBitmap != null) {
                context.writeEncryptedBitmap(shipmentId, ImageType.MAP, mapBitmap)
            }
            return AmazonDelivery(
                shipmentId,
                EncryptedValue(name.toByteArray()),
                EncryptedValue(imageUrl.toByteArray()),
                EncryptedValue(lineItemId.toByteArray()),
                EncryptedValue(orderId.toByteArray()),
                EncryptedValue(status.name.toByteArray()),
                EncryptedValue(message.toByteArray()),
                trackingId?.let { EncryptedValue(it.toByteArray()) },
                trackingData?.let { EncryptedValue(gson.toJson(it).toByteArray()) },
                dismissedAtStatus
            )
        }

        fun getBestStatus(): Status {
            //Prefer live tracking data if it is available
            return trackingData?.status ?: status
        }

    }

    suspend fun decrypt(context: Context): Delivery {
        val gson by inject<Gson>()
        val decryptedBitmap = context.readEncryptedBitmap(shipmentId, ImageType.IMAGE)
        val decryptedMap = context.readEncryptedBitmap(shipmentId, ImageType.MAP)
        return Delivery(
            shipmentId,
            String(name.bytes).unescape(),
            String(imageUrl.bytes),
            String(lineItemId.bytes),
            String(orderId.bytes),
            Status.values().first { it.name == String(status.bytes) },
            String(message.bytes).unescape(),
            trackingId?.bytes?.let { String(it) },
            trackingData?.bytes?.let { gson.fromJson(String(it), TrackingData::class.java) },
            dismissedAtStatus,
            decryptedBitmap,
            decryptedMap
        )
    }

    @Parcelize
    data class TrackingData(
        @SerializedName("destinationAddress")
        val destinationAddress: Location,
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
        ): Parcelable {

            @Parcelize
            data class GeoLocation(
                @SerializedName("latitude")
                val latitude: Double,
                @SerializedName("longitude")
                val longitude: Double
            ): Parcelable

        }

    }

}