package com.abesam.mirabelli

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class GoldenOrbView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var phase = 0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2600L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()

        addUpdateListener {
            phase = it.animatedValue as Float
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
        val size = minOf(width, height).toFloat()
        val pulse = (sin(phase * 2.0 * PI).toFloat() + 1f) / 2f
        val radius = size * 0.25f

        // GOLDEN AURA
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx, cy, size * 0.49f,
            intArrayOf(
                Color.argb(130, 255, 225, 80),
                Color.argb(65, 255, 170, 0),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, size * 0.49f, paint)
        paint.shader = null

        // EXPANDING GOLD RINGS
        for (i in 0..2) {
            val progress = (phase + i / 3f) % 1f

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.2f
            paint.color = Color.argb(
                ((1f - progress) * 210).toInt(),
                255, 210, 35
            )

            canvas.drawCircle(
                cx, cy,
                radius + progress * size * 0.22f,
                paint
            )
        }

        // ROTATING ENERGY ORBITS
        for (i in 0..1) {
            canvas.save()
            canvas.rotate(
                (phase * 360f + i * 90f),
                cx,
                cy
            )

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.5f
            paint.color = Color.argb(210, 255, 205, 35)

            val ovalRadius = radius * 1.65f
            canvas.drawOval(
                cx - ovalRadius,
                cy - ovalRadius * 0.42f,
                cx + ovalRadius,
                cy + ovalRadius * 0.42f,
                paint
            )
            canvas.restore()
        }

        // MAIN GOLDEN ORB
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            cx - radius * 0.35f,
            cy - radius * 0.42f,
            radius * 1.8f,
            intArrayOf(
                Color.WHITE,
                Color.rgb(255, 245, 175),
                Color.rgb(255, 195, 25),
                Color.rgb(210, 110, 0),
                Color.rgb(65, 25, 0)
            ),
            null,
            Shader.TileMode.CLAMP
        )

        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null

        // MOVING LIGHT REFLECTION
        val shineX = cx + cos(phase * 2.0 * PI) * radius * 0.38
        val shineY = cy + sin(phase * 2.0 * PI) * radius * 0.38

        paint.color = Color.argb(190, 255, 255, 225)
        canvas.drawCircle(
            shineX.toFloat(),
            shineY.toFloat(),
            radius * 0.13f,
            paint
        )

        // ORBITING GOLDEN SPARKS
        for (i in 0..9) {
            val angle = phase * 2.0 * PI + i * 2.0 * PI / 10.0
            val orbit = radius * 1.95f

            val x = cx + cos(angle) * orbit
            val y = cy + sin(angle) * orbit

            paint.color = Color.argb(
                (100 + pulse * 155).toInt(),
                255, 225, 70
            )

            canvas.drawCircle(
                x.toFloat(),
                y.toFloat(),
                2f + pulse * 2f,
                paint
            )
        }

        // INNER GLOW
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.argb(
            (130 + pulse * 125).toInt(),
            255, 240, 150
        )
        canvas.drawCircle(cx, cy, radius * 1.08f, paint)
        paint.style = Paint.Style.FILL
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isStarted) animator.start()
    }
}
