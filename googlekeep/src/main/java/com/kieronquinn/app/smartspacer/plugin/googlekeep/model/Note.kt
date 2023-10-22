package com.kieronquinn.app.smartspacer.plugin.googlekeep.model

import com.google.gson.annotations.SerializedName

sealed class Note(
    @Transient
    @SerializedName(NAME_TITLE)
    open val title: String,
    @Transient
    @SerializedName(NAME_APP_WIDGET_ID)
    open val appWidgetId: Int,
    @SerializedName(NAME_TYPE)
    val type: Type
) {

    companion object {
        const val NAME_TYPE = "type"
        private const val NAME_APP_WIDGET_ID = "app_widget_id"
        private const val NAME_TITLE = "title"
    }

    data class RegularNote(
        @SerializedName(NAME_TITLE)
        override val title: String,
        @SerializedName(NAME_APP_WIDGET_ID)
        override val appWidgetId: Int,
        @SerializedName("content")
        val content: String
    ): Note(title, appWidgetId, Type.REGULAR)

    data class ListNote(
        @SerializedName(NAME_TITLE)
        override val title: String,
        @SerializedName(NAME_APP_WIDGET_ID)
        override val appWidgetId: Int,
        @SerializedName("items")
        val items: List<Item>
    ): Note(title, appWidgetId, Type.LIST) {
        data class Item(
            @SerializedName("indented")
            val isIndented: Boolean,
            @SerializedName("content")
            val content: String
        )
    }

    enum class Type {
        REGULAR,
        LIST
    }

}
