package com.kieronquinn.app.smartspacer.plugin.amazon.model.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrackingStatus(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("responseCode")
    val responseCode: String,
    @SerializedName("value")
    val value: Value?
): Parcelable {

    @Parcelize
    data class Value(
        @SerializedName("primaryStatus")
        val primaryStatus: String?,
        @SerializedName("secondaryStatus")
        val secondaryStatus: String?,
        @SerializedName("calloutMessage")
        val calloutMessage: String?
    ): Parcelable

    fun getBestMessage(): String? {
        return value?.calloutMessage ?: value?.primaryStatus ?: value?.secondaryStatus
    }

}
