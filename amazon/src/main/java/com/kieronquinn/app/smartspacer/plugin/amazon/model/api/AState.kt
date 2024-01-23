package com.kieronquinn.app.smartspacer.plugin.amazon.model.api

import com.google.gson.annotations.SerializedName

data class AState(
    @SerializedName("orderId")
    val orderId: String,
    @SerializedName("trackingId")
    val trackingId: String,
    @SerializedName("customerId")
    val customerId: String
)
