package com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class SetupRequest(
    @SerializedName("clientId")
    val clientId: String = UUID.randomUUID().toString(),
    @SerializedName("wasabiUserId")
    val wasabiUserId: String = UUID.randomUUID().toString()
)
