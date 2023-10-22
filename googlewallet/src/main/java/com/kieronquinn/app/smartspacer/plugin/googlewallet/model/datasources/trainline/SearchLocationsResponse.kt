package com.kieronquinn.app.smartspacer.plugin.googlewallet.model.datasources.trainline

import com.google.gson.annotations.SerializedName

data class SearchLocationsResponse(
    @SerializedName("searchLocations")
    val searchLocations: List<SearchLocation>
) {

    data class SearchLocation(
        @SerializedName("name")
        val name: String,
        @SerializedName("code")
        val code: String,
        @SerializedName("timezone")
        val timezone: String
    )

}
