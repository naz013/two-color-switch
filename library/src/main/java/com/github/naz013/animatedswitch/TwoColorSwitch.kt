package com.github.naz013.animatedswitch

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.Checkable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

class TwoColorSwitch : View, Checkable {

    private val thumb = Thumb()
    private val track = Track()
    private var isChecked = false
    var onStateChangeListener: OnStateChangeListener? = null

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context, attrs)
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        printLog("initView: ")
        var checked = false
        attrs?.let {
            val a =
                context.theme.obtainStyledAttributes(attrs, R.styleable.TwoColorSwitch, 0, 0)
            try {
                thumb.color = a.getColor(R.styleable.TwoColorSwitch_tcs_thumbColor, thumb.color)
                track.colorOn =
                    a.getColor(R.styleable.TwoColorSwitch_tcs_trackColorOn, track.colorOn)
                track.colorOff =
                    a.getColor(R.styleable.TwoColorSwitch_tcs_trackColorOff, track.colorOff)
                checked = a.getBoolean(R.styleable.TwoColorSwitch_tcs_isChecked, isChecked)
            } catch (e: Exception) {
                printLog("initView: " + e.localizedMessage)
            } finally {
                a.recycle()
            }
        }
        super.setOnClickListener { toggle() }

        setChecked(checked)
        track.updateColor()
    }

    fun setThumbColor(@ColorInt color: Int) {
        thumb.color = color
        invalidate()
    }

    @ColorInt
    fun getThumbColor(): Int = thumb.color

    fun setTrackColors(@ColorInt colorOn: Int, @ColorInt colorOff: Int) {
        track.colorOn = colorOn
        track.colorOff = colorOff
        invalidate()
    }

    @ColorInt
    fun getTrackColorOn(): Int = track.colorOn

    @ColorInt
    fun getTrackColorOff(): Int = track.colorOff

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            track.draw(canvas)
            thumb.draw(canvas)
        }
    }

    override fun isChecked(): Boolean = isChecked

    override fun toggle() {
        setChecked(!isChecked)
        onStateChangeListener?.onStateChanged(isChecked)
    }

    override fun setChecked(checked: Boolean) {
        this.isChecked = checked
        if (checked) {
            startOnAnimation()
        } else {
            startOffAnimation()
        }
    }

    private fun startOnAnimation() {
        thumb.animateOn()
        track.animateOn()
    }

    private fun startOffAnimation() {
        thumb.animateOff()
        track.animateOff()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = dp2px(42)
        val width = dp2px(72)
        setMeasuredDimension(width, height)
        calculateObjects(width, height)
    }

    private fun calculateObjects(width: Int, height: Int) {
        printLog("calculateObjects: $width, $height")

        val padding = dp2px(4).toFloat()

        track.bounds.left = padding
        track.bounds.top = padding
        track.bounds.right = width.toFloat() - padding
        track.bounds.bottom = height.toFloat() - padding

        val thumbPadding = padding * 2f
        val thumbHeight = track.bounds.height() - (thumbPadding * 2f)

        thumb.bounds.left = track.bounds.left + thumbPadding
        thumb.bounds.top = track.bounds.top + thumbPadding
        thumb.bounds.right = thumb.bounds.left + thumbHeight
        thumb.bounds.bottom = thumb.bounds.top + thumbHeight

        thumb.leftAnchor = 0f
        thumb.rightAnchor = track.bounds.right - thumbHeight - thumbPadding / 2f - thumbHeight
        thumb.updateAnchor()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            isChecked = state.getBoolean(STATE_KEY, isChecked)
            track.colorOn = state.getInt(COLOR_ON_KEY, track.colorOn)
            track.colorOff = state.getInt(COLOR_OFF_KEY, track.colorOff)
            thumb.color = state.getInt(COLOR_THUMB_KEY, thumb.color)
            super.onRestoreInstanceState(state.getParcelable(SUPER_KEY))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedInstance = Bundle()
        savedInstance.putParcelable(SUPER_KEY, super.onSaveInstanceState())
        savedInstance.putBoolean(STATE_KEY, isChecked)
        savedInstance.putInt(COLOR_ON_KEY, track.colorOn)
        savedInstance.putInt(COLOR_OFF_KEY, track.colorOff)
        savedInstance.putInt(COLOR_THUMB_KEY, thumb.color)
        return savedInstance
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
    }

    override fun setOnClickListener(l: OnClickListener?) {
    }

    override fun setBackgroundColor(color: Int) {
    }

    override fun setBackgroundTintBlendMode(blendMode: BlendMode?) {
    }

    override fun setBackgroundDrawable(background: Drawable?) {
    }

    override fun setBackground(background: Drawable?) {
    }

    override fun setBackgroundResource(resid: Int) {
    }

    override fun setBackgroundTintList(tint: ColorStateList?) {
    }

    override fun setBackgroundTintMode(tintMode: PorterDuff.Mode?) {
    }

    override fun getBackground(): Drawable? = null

    @Px
    private fun dp2px(dp: Int): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        var display: Display? = null
        if (wm != null) display = wm.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display?.getMetrics(displayMetrics)
        return (dp * displayMetrics.density + 0.5f).toInt()
    }

    private fun printLog(message: String) {
        if (SHOW_LOGS) Log.d("TwoColorSwitch", message)
    }

    private inner class Thumb {

        var bounds: RectF = RectF()
        var leftAnchor = 0.0f
            set(value) {
                field = value
                anchor.x = value
            }
        var rightAnchor = 0.0f

        @ColorInt
        var color: Int = 0
            set(value) {
                field = value
                paint.color = value
            }

        private val paint = Paint()
        private val anchor = PointF()
        private val translatePropertyAnimX = object : FloatPropertyCompat<PointF>("thumb_x") {
            override fun setValue(point: PointF?, value: Float) {
                point?.x = value
                invalidate()
            }

            override fun getValue(point: PointF?): Float {
                return point?.x ?: bounds.left
            }
        }

        fun draw(canvas: Canvas) {
            canvas.drawCircle(
                bounds.centerX() + anchor.x,
                bounds.centerY(),
                bounds.height() / 2f,
                paint
            )
        }

        fun animateOn() {
            if (rightAnchor == 0.0f) return
            SpringAnimation(anchor, translatePropertyAnimX, rightAnchor).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
        }

        fun animateOff() {
            SpringAnimation(anchor, translatePropertyAnimX, leftAnchor).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
        }

        fun updateAnchor() {
            anchor.x = if (isChecked) rightAnchor else leftAnchor
        }
    }

    private inner class Track {

        var bounds: RectF = RectF()

        @ColorInt
        var colorOn: Int = 0

        @ColorInt
        var colorOff: Int = 0

        @ColorInt
        var color: Int = 0

        private val paint = Paint()
        private val cornerRadius = dp2px(50).toFloat()
        private var animator: ValueAnimator? = null

        fun draw(canvas: Canvas) {
            paint.color = color
            canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint)
        }

        fun animateOn() {
            animator?.cancel()

            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), color, colorOn)
            colorAnimation.duration = 250
            colorAnimation.addUpdateListener { animator ->
                color = animator.animatedValue as Int
                invalidate()
            }
            colorAnimation.start()
            animator = colorAnimation
        }

        fun animateOff() {
            animator?.cancel()

            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), color, colorOff)
            colorAnimation.duration = 250
            colorAnimation.addUpdateListener { animator ->
                color = animator.animatedValue as Int
                invalidate()
            }
            colorAnimation.start()
            animator = colorAnimation
        }

        fun updateColor() {
            color = if (isChecked) colorOn else colorOff
        }
    }

    interface OnStateChangeListener {
        fun onStateChanged(isChecked: Boolean)
    }

    private companion object {
        private const val SUPER_KEY = "super"
        private const val STATE_KEY = "state"
        private const val COLOR_ON_KEY = "color_on"
        private const val COLOR_OFF_KEY = "color_off"
        private const val COLOR_THUMB_KEY = "color_thumb"
        private const val SHOW_LOGS = true
    }
}