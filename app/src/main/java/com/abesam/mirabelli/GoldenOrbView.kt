package com.abesam.mirabelli

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator

class GoldenOrbView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var pulse = 0.7f

    private val animator = ValueAnimator.ofFloat(0.55f, 1f).apply {
        duration = 1400
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            pulse = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(width, height) * 0.29f

        paint.shader = RadialGradient(
            cx, cy, radius * 2.1f,
            intArrayOf(0x99FFD700.toInt(), 0x44D99A00, 0x00D99A00),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius * 2.1f * pulse, paint)

        paint.shader = RadialGradient(
            cx - radius * 0.25f, cy - radius * 0.3f, radius * 1.5f,
            intArrayOf(
                0xFFFFF2B0.toInt(),
                0xFFFFC000.toInt(),
                0xFFB87500.toInt(),
                0xFF493000.toInt()
            ),
            floatArrayOf(0f, 0.35f, 0.72f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null

        paint.color = 0x99FFF1B8.toInt()
        canvas.drawCircle(cx - radius * 0.3f, cy - radius * 0.38f,
            radius * 0.12f, paint)
    }
}
