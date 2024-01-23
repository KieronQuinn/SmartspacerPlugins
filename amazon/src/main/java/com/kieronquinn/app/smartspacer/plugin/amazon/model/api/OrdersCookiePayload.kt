package com.kieronquinn.app.smartspacer.plugin.amazon.model.api

import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Status

data class OrdersCookiePayload(
    @SerializedName("primaryStatus")
    val primaryStatus: String?,
    @SerializedName("shortStatus")
    val shortStatus: Status?
)
