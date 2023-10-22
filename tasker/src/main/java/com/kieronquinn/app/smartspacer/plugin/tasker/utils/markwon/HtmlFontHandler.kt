package com.kieronquinn.app.smartspacer.plugin.tasker.utils.markwon

import android.content.Context
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.toColorOrNull
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.html.HtmlTag
import io.noties.markwon.html.MarkwonHtmlRenderer
import io.noties.markwon.html.TagHandler
import java.util.Collections

class HtmlFontHandler(private val context: Context): TagHandler() {

    override fun handle(visitor: MarkwonVisitor, renderer: MarkwonHtmlRenderer, tag: HtmlTag) {
        val size = tag.attributes()["size"]?.toFloatOrNull()
            ?.coerceAtMost(1f)
            ?.coerceAtLeast(0f)
        if(size != null) {
            visitor.builder().setSpan(
                RelativeSizeSpan(size),
                tag.start(),
                tag.end()
            )
        }
        val colour = tag.attributes()["color"]?.toColorOrNull(context)
        if(colour != null){
            visitor.builder().setSpan(
                ForegroundColorSpan(colour),
                tag.start(),
                tag.end()
            )
        }
    }

    override fun supportedTags(): MutableCollection<String> {
        return Collections.singleton("font")
    }

}