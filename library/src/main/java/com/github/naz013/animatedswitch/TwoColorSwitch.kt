package com.github.naz013.animatedswitch

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

    private val paint = Paint()
    private val thumb = Thumb()
    private val track = Track()
    private var isChecked = false

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

        super.setOnClickListener { toggle() }
    }

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
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, height)
        calculateObjects(width, height)
    }

    private fun calculateObjects(width: Int, height: Int) {

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
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

        private val anchor = PointF()
        var bounds: RectF = RectF()
        var leftAnchor = 0.0f
            set(value) {
                field = value
                anchor.x = value
            }
        var rightAnchor = 0.0f

        @ColorInt
        var color: Int = 0

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

        }

        fun animateOn() {
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
    }

    private inner class Track {

        var bounds: RectF = RectF()

        @ColorInt
        var colorOn: Int = 0

        @ColorInt
        var colorOff: Int = 0

        @ColorInt
        var color: Int = 0

        private val colorPropertyAnim = object : FloatPropertyCompat<Track>("bg_color") {
            override fun getValue(track: Track?): Float {
                return track?.color?.toFloat() ?: colorOn.toFloat()
            }

            override fun setValue(track: Track?, value: Float) {
                track?.color = value.toInt()
                invalidate()
            }
        }

        fun draw(canvas: Canvas) {

        }

        fun animateOn() {
            SpringAnimation(this, colorPropertyAnim, colorOn.toFloat()).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
        }

        fun animateOff() {
            SpringAnimation(this, colorPropertyAnim, colorOff.toFloat()).apply {
                spring.stiffness = SpringForce.STIFFNESS_LOW
                spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                start()
            }
        }
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