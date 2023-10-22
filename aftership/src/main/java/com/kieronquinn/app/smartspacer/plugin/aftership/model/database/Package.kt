package com.kieronquinn.app.smartspacer.plugin.aftership.model.database

import androidx.annotation.DrawableRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.aftership.R
import com.kieronquinn.app.smartspacer.plugin.aftership.model.BitmapWrapper
import org.koin.core.component.KoinComponent

@Entity
data class Package(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo("changed_at")
    val changedAt: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "courier")
    val courier: String,
    @ColumnInfo(name = "state")
    val state: String,
    @ColumnInfo("icon")
    val icon: BitmapWrapper,
    @ColumnInfo("image")
    val image: BitmapWrapper?,
    @ColumnInfo("map")
    val map: BitmapWrapper?,
    @ColumnInfo(name = "status")
    val status: Status,
    @ColumnInfo(name = "tracking_url")
    val trackingUrl: String?,
    @ColumnInfo(name = "tracking")
    val tracking: Tracking?,
    @ColumnInfo("dismissed_at")
    val dismissedAt: Int? = null
): KoinComponent {

    data class Tracking(
        @SerializedName("title")
        val title: String,
        @SerializedName("date")
        val date: String,
        @SerializedName("time")
        val time: String,
        @SerializedName("latitude")
        val latitude: Double?,
        @SerializedName("longitude")
        val longitude: Double?
    )

    fun getNormalisedHashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + courier.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + icon.path.hashCode()
        result = 31 * result + status.name.hashCode()
        result = 31 * result + trackingUrl.hashCode()
        result = 31 * result + tracking.hashCode()
        return result
    }

    enum class Status(@DrawableRes val icon: Int) {
        CURRENT(R.drawable.ic_status_current),
        PAST(R.drawable.ic_status_past),
        SHIPPING(R.drawable.ic_status_shipping),
        READY_TO_SHIP(R.drawable.ic_status_ready_to_ship),
        DELIVERED(R.drawable.ic_status_delivered)
    }

}