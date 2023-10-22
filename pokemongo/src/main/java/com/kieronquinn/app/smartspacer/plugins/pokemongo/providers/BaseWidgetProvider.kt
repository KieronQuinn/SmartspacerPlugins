package com.kieronquinn.app.smartspacer.plugins.pokemongo.providers

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin.Variant
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier

abstract class BaseWidgetProvider: SmartspacerWidgetProvider() {

    abstract val variant: Variant

    protected fun ImageView.getImageAsBitmap(): Bitmap? {
        return (drawable as? BitmapDrawable)?.bitmap
    }

    protected fun LinearLayout.getProgressText(): String {
        return children.filterIsInstance<TextView>().joinToString(" ") {
            it.text
        }
    }

    protected fun LinearLayout.child(): LinearLayout? {
        return children.firstOrNull { it is LinearLayout } as? LinearLayout
    }

    protected fun getIdentifier(identifier: String): String {
        return "${variant.packageName}$identifier"
    }

    protected fun View.isVisible(identifier: String): Boolean {
        return findViewByIdentifier<View>(getIdentifier(identifier))?.isVisible ?: false
    }

}