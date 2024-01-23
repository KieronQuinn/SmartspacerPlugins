package com.kieronquinn.app.smartspacer.plugin.amazon.model.database

import android.content.Context
import android.graphics.Bitmap
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.TrackingData
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.TrackingStatus
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
    @ColumnInfo(name = "order_id")
    val orderId: String,
    @ColumnInfo(name = "shipment_id")
    val shipmentId: String?,
    @ColumnInfo("index")
    val index: Int,
    @ColumnInfo(name = "name")
    val name: EncryptedValue,
    @ColumnInfo(name = "image_url")
    val imageUrl: EncryptedValue,
    @ColumnInfo("order_details_url")
    val orderDetailsUrl: EncryptedValue?,
    @ColumnInfo(name = "status")
    val status: EncryptedValue,
    @ColumnInfo(name = "message")
    val message: EncryptedValue,
    @ColumnInfo(name = "tracking_id")
    val trackingId: EncryptedValue?,
    @ColumnInfo(name = "customer_id")
    val customerId: EncryptedValue?,
    @ColumnInfo(name = "csrf_token")
    val csrfToken: EncryptedValue?,
    @ColumnInfo(name = "tracking_data")
    val trackingData: EncryptedValue?,
    @ColumnInfo(name = "tracking_status")
    val trackingStatus: EncryptedValue?,
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
        val orderId: String,
        val shipmentId: String?,
        val index: Int,
        val name: String,
        val imageUrl: String,
        val orderDetailsUrl: String?,
        val status: Status,
        val message: String,
        val trackingId: String?,
        val customerId: String?,
        var csrfToken: String?,
        val trackingData: TrackingData?,
        val trackingStatus: TrackingStatus?,
        val dismissedAtStatus: Status?,
        @IgnoredOnParcel
        val imageBitmap: Bitmap? = null,
        @IgnoredOnParcel
        val mapBitmap: Bitmap? = null
    ): KoinComponent, Parcelable {

        suspend fun encrypt(context: Context): AmazonDelivery {
            val gson by inject<Gson>()
            if (imageBitmap != null) {
                context.writeEncryptedBitmap(orderId, ImageType.IMAGE, imageBitmap)
            }
            if (mapBitmap != null) {
                context.writeEncryptedBitmap(orderId, ImageType.MAP, mapBitmap)
            }
            return AmazonDelivery(
                orderId,
                shipmentId,
                index,
                EncryptedValue(name.toByteArray()),
                EncryptedValue(imageUrl.toByteArray()),
                orderDetailsUrl?.let { EncryptedValue(it.toByteArray()) },
                EncryptedValue(status.name.toByteArray()),
                EncryptedValue(message.toByteArray()),
                trackingId?.let { EncryptedValue(it.toByteArray()) },
                customerId?.let { EncryptedValue(it.toByteArray()) },
                csrfToken?.let { EncryptedValue(it.toByteArray()) },
                trackingData?.let { EncryptedValue(gson.toJson(it).toByteArray()) },
                trackingStatus?.let { EncryptedValue(gson.toJson(it).toByteArray()) },
                dismissedAtStatus
            )
        }

        fun isDismissed(): Boolean {
            return dismissedAtStatus != null && dismissedAtStatus == status
        }

        fun getBestStatus(): Status {
            //Prefer live tracking data if it is available
            return trackingData?.packageLocationDetails?.status?.toStatus() ?: status
        }

        fun requiresLinkingDelivery(): Boolean {
            return status != Status.ORDERED && status != Status.DELIVERED && trackingId == null
        }

        fun canBeTracked(): Boolean {
            return trackingId != null && orderDetailsUrl != null && csrfToken != null
                    && status == Status.OUT_FOR_DELIVERY
        }

        fun isTracking(): Boolean {
            return status == Status.OUT_FOR_DELIVERY && mapBitmap != null
        }

    }

    suspend fun decrypt(context: Context): Delivery {
        val gson by inject<Gson>()
        val decryptedBitmap = context.readEncryptedBitmap(orderId, ImageType.IMAGE)
        val decryptedMap = context.readEncryptedBitmap(orderId, ImageType.MAP)
        return Delivery(
            orderId,
            shipmentId,
            index,
            String(name.bytes).unescape(),
            String(imageUrl.bytes),
            orderDetailsUrl?.bytes?.let { String(it) },
            Status.entries.first { it.name == String(status.bytes) },
            String(message.bytes).unescape(),
            trackingId?.bytes?.let { String(it) },
            customerId?.bytes?.let { String(it) },
            csrfToken?.bytes?.let { String(it) },
            trackingData?.bytes?.let { gson.fromJson(String(it), TrackingData::class.java) },
            trackingStatus?.bytes?.let { gson.fromJson(String(it), TrackingStatus::class.java) },
            dismissedAtStatus,
            decryptedBitmap,
            decryptedMap
        )
    }

}