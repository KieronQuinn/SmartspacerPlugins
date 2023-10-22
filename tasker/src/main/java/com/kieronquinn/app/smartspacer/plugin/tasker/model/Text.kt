package com.kieronquinn.app.smartspacer.plugin.tasker.model

import android.content.Context
import android.os.Parcelable
import android.text.TextUtils.TruncateAt
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.parseMarkdown
import kotlinx.parcelize.Parcelize
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text as SmartspacerText

@Parcelize
data class Text(
    @SerializedName("text")
    val text: String,
    @SerializedName("truncate_at_type")
    val truncateAtType: TruncateAt = TruncateAt.END,
    @SerializedName("max_lines")
    val maxLines: Int = 1
): Manipulative<Text>, Parcelable {

    fun toText(): SmartspacerText {
        return SmartspacerText(
            text = text.parseMarkdown(),
            truncateAtType = truncateAtType,
            maxLines = maxLines
        )
    }

    fun describe() = text

    override fun getVariables(): Array<String> {
        return arrayOf(*text.getVariables())
    }

    override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Text {
        return copy(text = text.replace(replacements))
    }

}
