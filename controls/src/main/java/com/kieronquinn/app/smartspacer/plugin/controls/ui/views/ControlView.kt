package com.kieronquinn.app.smartspacer.plugin.controls.ui.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.os.Build
import android.service.controls.templates.RangeTemplate
import android.service.controls.templates.ThumbnailTemplate
import android.service.controls.templates.ToggleRangeTemplate
import android.service.controls.templates.ToggleTemplate
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates.NoTemplate.getIcon
import com.kieronquinn.app.smartspacer.plugin.controls.model.RenderInfo
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlState
import com.kieronquinn.app.smartspacer.plugin.controls.utils.MathUtils
import java.util.IllegalFormatException
import kotlin.math.abs


class ControlView: FrameLayout {

    companion object {
        private const val MIN_LEVEL = 0
        private const val MAX_LEVEL = 10000
        private const val STATE_ANIMATION_DURATION = 700L
        private const val DEFAULT_FORMAT = "%.1f"
        private val CONTROL_STATE = PathInterpolator(
            0.4f, 0f, 0.2f,
            1.0f
        )
    }

    constructor(context: Context, attributeSet: AttributeSet? = null, defStyleRes: Int):
            super(context, attributeSet, defStyleRes)
    constructor(context: Context, attributeSet: AttributeSet?):
            this(context, attributeSet, 0)
    constructor(context: Context):
            this(context, null, 0)

    private val defaultIcon by lazy {
        Icon.createWithResource(context, R.drawable.ic_controls)
    }

    private val controlThumbnail by lazy {
        findViewById<ImageView>(R.id.control_thumbnail)
    }

    private val controlIcon by lazy {
        findViewById<ImageView>(R.id.control_icon)
    }

    private val controlStatus by lazy {
        findViewById<TextView>(R.id.control_status)
    }

    private val controlTitle by lazy {
        findViewById<TextView>(R.id.control_title)
    }

    private val controlSubtitle by lazy {
        findViewById<TextView>(R.id.control_subtitle)
    }

    private val paint = Paint()
    private var controlState: ControlState? = null
    private var rangeAnimator: ValueAnimator? = null
    private var level = 0
    private var isChecked: Boolean = false
    private var currentRangeValue: String = ""
    private var currentStatusText: CharSequence = ""
    private var customColour: Int? = null
    private var listener: ControlListener? = null
    private var scaleAnimation: ViewPropertyAnimator? = null

    @SuppressLint("ClickableViewAccessibility")
    private fun setupGestureListener() {
        val gestureListener = GestureListener()
        val gestureDetector = GestureDetector(context, gestureListener)
        setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_DOWN) {
                scaleAnimation?.cancel()
                scaleAnimation = animateParent().scaleX(0.95f).scaleY(0.95f).also {
                    it.start()
                }
            }
            if(event.action == MotionEvent.ACTION_UP) {
                scaleAnimation?.cancel()
                scaleAnimation = animateParent().scaleX(1f).scaleY(1f).also {
                    it.start()
                }
            }
            if (event.action == MotionEvent.ACTION_UP && gestureListener.isDragging) {
                parent.requestDisallowInterceptTouchEvent(false)
                gestureListener.isDragging = false
                endUpdateRange()
                return@setOnTouchListener false
            }
            gestureDetector.onTouchEvent(event)
        }
    }

    init {
        setupGestureListener()
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val progressWidth = (level / MAX_LEVEL.toFloat()) * width
        val colour = customColour
            ?: ContextCompat.getColor(context, getRenderInfo()?.enabledBackground ?: return)
        paint.color = colour
        canvas.drawRect(0f, 0f, progressWidth, height.toFloat(), paint)
    }

    fun setControlState(state: ControlState) {
        controlState = state
        isChecked = isChecked()
        when(state) {
            is ControlState.Loading -> setControlState(state)
            is ControlState.Hidden -> setControlState(state)
            is ControlState.Control -> setControlState(state)
            is ControlState.Sending -> setControlState(state)
            is ControlState.Error -> setControlState(state)
        }
    }

    fun setListener(listener: ControlListener?) {
        this.listener = listener
    }

    private fun setControlState(state: ControlState.Loading) {
        val icon = state.cachedIcon ?: defaultIcon
        controlIcon.setImageIcon(icon)
        val status = context.getString(R.string.control_loading)
        controlStatus.text = status
        controlTitle.text = state.cachedTitle ?: ""
        controlSubtitle.text = ""
    }

    private fun setControlState(state: ControlState.Hidden) {
        val icon = state.cachedIcon ?: defaultIcon
        controlIcon.setImageIcon(icon)
        //Treated as loading since the UI is visible
        val status = context.getString(R.string.control_loading)
        controlStatus.text = status
        controlTitle.text = state.cachedTitle ?: ""
        controlSubtitle.text = ""
    }

    private fun setControlState(state: ControlState.Control) {
        val icon = state.control.getIcon(context, state.componentName)
        val range = getRangeTemplate()
        currentStatusText = state.control.statusText
        currentRangeValue = range?.let {
            it.format(it.formatString.toString(), DEFAULT_FORMAT, it.currentValue)
        } ?: ""
        controlThumbnail.setImageIcon(getThumbnail())
        controlIcon.setImageIcon(icon)
        controlStatus.text = state.control.statusText
        controlTitle.text = state.control.title
        controlSubtitle.text = state.control.subtitle
        isChecked = isChecked()
        if(range != null) {
            updateRange(range.rangeToLevelValue(range.currentValue), isChecked,false)
        }else{
            val level = if(isChecked()) MAX_LEVEL else MIN_LEVEL
            updateRange(level, isChecked,false)
        }
        invalidate()
    }

    private fun setControlState(state: ControlState.Sending) {
        val icon = state.cachedIcon ?: defaultIcon
        controlIcon.setImageIcon(icon)
        val status = context.getString(R.string.control_sending)
        controlStatus.text = status
        controlTitle.text = state.cachedTitle ?: ""
        controlSubtitle.text = ""
    }

    private fun setControlState(state: ControlState.Error) {
        val icon = state.cachedIcon ?: defaultIcon
        controlIcon.setImageIcon(icon)
        val status = context.getString(R.string.control_error)
        controlStatus.text = status
        controlTitle.text = state.cachedTitle ?: ""
        controlSubtitle.text = ""
    }

    private fun isToggleable(): Boolean {
        val control = (controlState as? ControlState.Control)?.control ?: return false
        return when(control.controlTemplate) {
            is ToggleTemplate -> true
            is ToggleRangeTemplate -> true
            else -> false
        }
    }

    private fun isScrollable(): Boolean {
        val control = (controlState as? ControlState.Control)?.control ?: return false
        return when(control.controlTemplate) {
            is RangeTemplate -> true
            is ToggleRangeTemplate -> true
            else -> false
        }
    }

    private fun getRangeTemplate(): RangeTemplate? {
        val control = (controlState as? ControlState.Control)?.control ?: return null
        return when(val template = control.controlTemplate) {
            is RangeTemplate -> template
            is ToggleRangeTemplate -> template.range
            else -> null
        }
    }

    private fun getThumbnail(): Icon? {
        val control = (controlState as? ControlState.Control)?.control ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when(val template = control.controlTemplate) {
                is ThumbnailTemplate -> template.thumbnail
                else -> null
            }
        } else null
    }

    private fun isChecked(): Boolean {
        val control = (controlState as? ControlState.Control)?.control ?: return false
        return when(val template = control.controlTemplate) {
            is ToggleTemplate -> template.isChecked
            is ToggleRangeTemplate -> template.isChecked
            is RangeTemplate -> template.currentValue != template.minValue
            else -> false
        }
    }

    private fun getRenderInfo(): RenderInfo? {
        val controlState = (controlState as? ControlState.Control) ?: return null
        return RenderInfo.lookup(
            context,
            controlState.componentName,
            controlState.control.deviceType
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateRange(level: Int, checked: Boolean, isDragging: Boolean) {
        rangeAnimator?.cancel()
        val rangeTemplate = getRangeTemplate() ?: return
        val newLevel = MIN_LEVEL.coerceAtLeast(MAX_LEVEL.coerceAtMost(level))

        if (isDragging) {
            if (this.level != newLevel) {
                this.level = newLevel
                invalidate()
            }
        } else if (newLevel != this.level) {
            rangeAnimator = ValueAnimator.ofInt(this.level, newLevel).apply {
                addUpdateListener {
                    this@ControlView.level = it.animatedValue as Int
                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        rangeAnimator = null
                    }
                })
                duration = STATE_ANIMATION_DURATION
                interpolator = CONTROL_STATE
                start()
            }
        }

         if (checked) {
            val newValue = rangeTemplate.levelToRangeValue(newLevel)
            currentRangeValue = rangeTemplate.format(rangeTemplate.formatString.toString(),
                DEFAULT_FORMAT, newValue)
            if (isDragging || currentStatusText.isBlank()) {
                controlStatus.text = currentRangeValue
            } else {
                controlStatus.text = "$currentStatusText • $currentRangeValue"
            }
        } else {
             controlStatus.text = currentStatusText
        }
    }

    private fun RangeTemplate.levelToRangeValue(i: Int): Float {
        return MathUtils.constrainedMap(
            minValue, maxValue, MIN_LEVEL.toFloat(), MAX_LEVEL.toFloat(), i.toFloat()
        )
    }

    private fun RangeTemplate.rangeToLevelValue(i: Float): Int {
        return MathUtils.constrainedMap(
            MIN_LEVEL.toFloat(), MAX_LEVEL.toFloat(), minValue, maxValue, i
        ).toInt()
    }

    private fun endUpdateRange() {
        val template = getRangeTemplate() ?: return
        val endValue = template.findNearestStep(template.levelToRangeValue(level))
        listener?.onSetValue(endValue)
    }

    private fun animateParent(): ViewPropertyAnimator {
        return (parent as View).animate()
    }

    private inner class GestureListener: SimpleOnGestureListener() {

        var isDragging: Boolean = false

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            if (isDragging) {
                return
            }
            listener?.onLongPress()
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            xDiff: Float,
            yDiff: Float
        ): Boolean {
            if(!isScrollable()) return false

            if (!isDragging) {
                parent.requestDisallowInterceptTouchEvent(true)
                isDragging = true
            }

            val ratioDiff = -xDiff / width
            val changeAmount = ((MAX_LEVEL - MIN_LEVEL) * ratioDiff).toInt()
            updateRange(level + changeAmount, checked = true, isDragging = true)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (!isToggleable()) return false
            listener?.onToggle()
            return true
        }

    }

    private fun RangeTemplate.format(primaryFormat: String, backupFormat: String, value: Float): String {
        return try {
            String.format(primaryFormat, findNearestStep(value))
        } catch (e: IllegalFormatException) {
            if (backupFormat == "") {
                ""
            } else {
                format(backupFormat, "", value)
            }
        }
    }

    private fun RangeTemplate.findNearestStep(value: Float): Float {
        var minDiff = Float.MAX_VALUE

        var f = minValue
        while (f <= maxValue) {
            val currentDiff = abs(value - f)
            if (currentDiff < minDiff) {
                minDiff = currentDiff
            } else {
                return f - stepValue
            }

            f += stepValue
        }

        return maxValue
    }

    interface ControlListener {
        fun onSetValue(newValue: Float)
        fun onToggle()
        fun onLongPress()
    }

}