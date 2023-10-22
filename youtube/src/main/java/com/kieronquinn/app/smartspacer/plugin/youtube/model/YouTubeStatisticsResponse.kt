package com.kieronquinn.app.smartspacer.plugin.youtube.model

import com.google.gson.annotations.SerializedName

data class YouTubeStatisticsResponse(
    @SerializedName("items")
    val items: List<Item>
) {

    data class Item(
        @SerializedName("statistics")
        val statistics: Statistics,
        @SerializedName("snippet")
        val snippet: Snippet
    ) {

        data class Statistics(
            @SerializedName("subscriberCount")
            val subscriberCount: String,
            @SerializedName("hiddenSubscriberCount")
            val hiddenSubscriberCount: Boolean
        )

        data class Snippet(
            @SerializedName("title")
            val title: String
        )

    }

}
