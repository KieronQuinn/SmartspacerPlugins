package com.kieronquinn.app.smartspacer.plugin.shared.utils.appbar

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

class DragOptionalAppBarLayoutBehaviour: AppBarLayout.Behavior {

    companion object {
        fun AppBarLayout.setDraggable(canDrag: Boolean) {
            val layoutParams = layoutParams as CoordinatorLayout.LayoutParams
            (layoutParams.behavior as DragOptionalAppBarLayoutBehaviour).canDrag = canDrag
        }

        fun AppBarLayout.isDraggable(): Boolean {
            val layoutParams = layoutParams as CoordinatorLayout.LayoutParams
            return (layoutParams.behavior as DragOptionalAppBarLayoutBehaviour).canDrag
        }
    }

    private var canDrag = false

    constructor(): super()
    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs)

    init {
        setDragCallback(object: DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return canDrag
            }
        })
    }

}