package com.abesam.mirabelli

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A hardware-accelerated, self-contained animated orb suitable for an overlay window. */
class GoldenOrbView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    enum class State { IDLE, LISTENING, SPEAKING }

    var state: State = State.IDLE
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private var phase = 0f
    private val density = resources.displayMetrics.density
    private val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private val reflectionPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val orbBounds = RectF()
    private var auraShader: RadialGradient? = null
    private var orbShader: RadialGradient? = null
    private var reflectionShader: RadialGradient? = null

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 7_200L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            phase = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        // Keep the view on the normal hardware-accelerated canvas; software layers are
        // particularly costly for continuously animated gradients on Samsung devices.
        setWillNotDraw(false)
        contentDescription = "Mirabelli golden AI orb"
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        val cx = w / 2f
        val cy = h / 2f
        val size = minOf(w, h).toFloat()
        val radius = size * 0.235f
        orbBounds.set(cx - radius, cy - radius, cx + radius, cy + radius)
        auraShader = RadialGradient(
            cx, cy, size * 0.49f,
            intArrayOf(
                Color.argb(105, 255, 232, 120),
                Color.argb(55, 255, 174, 18),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.52f, 1f),
            Shader.TileMode.CLAMP
        )
        orbShader = RadialGradient(
            cx - radius * 0.36f,
            cy - radius * 0.42f,
            radius * 1.8f,
            intArrayOf(
                Color.rgb(255, 255, 231),
                Color.rgb(255, 239, 154),
                Color.rgb(255, 197, 38),
                Color.rgb(205, 105, 4),
                Color.rgb(47, 21, 4)
            ),
            floatArrayOf(0f, 0.19f, 0.48f, 0.78f, 1f),
            Shader.TileMode.CLAMP
        )
        reflectionShader = RadialGradient(
            0f, 0f, radius * 0.58f,
            intArrayOf(Color.argb(185, 255, 255, 237), Color.argb(55, 255, 246, 198), Color.TRANSPARENT),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val cx = width / 2f
        val cy = height / 2f
        val size = minOf(width, height).toFloat()
        val time = phase * 2.0 * PI
        val breathe = (sin(time).toFloat() + 1f) * 0.5f
        val intensity = when (state) {
            State.IDLE -> 0.72f + breathe * 0.28f
            State.LISTENING -> 1f
            State.SPEAKING -> 0.78f + (0.5f + 0.5f * sin(time * 3.2)).toFloat() * 0.22f
        }
        val radius = size * 0.235f

        auraPaint.shader = auraShader
        auraPaint.alpha = (150 * intensity).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, size * 0.49f, auraPaint)
        auraPaint.shader = null

        // Three staggered, continuously expanding and fading energy waves.
        val waveCount = if (state == State.IDLE) 3 else 4
        for (i in 0 until waveCount) {
            val progress = (phase + i.toFloat() / waveCount) % 1f
            val waveAlpha = ((1f - progress) * 190f * intensity).toInt().coerceIn(0, 255)
            ringPaint.color = Color.argb(waveAlpha, 255, 207, 54)
            ringPaint.strokeWidth = (1.25f + density * 0.85f) * (1f - progress * 0.32f)
            canvas.drawCircle(cx, cy, radius * 1.1f + progress * size * 0.23f, ringPaint)
        }

        // Tilted elliptical energy paths, with a bright moving point on each orbit.
        for (i in 0..1) {
            val rotation = phase * 360f * (if (i == 0) 1f else -0.72f) + i * 76f
            canvas.save()
            canvas.rotate(rotation, cx, cy)
            ringPaint.color = Color.argb((155 * intensity).toInt(), 255, 211, 93)
            ringPaint.strokeWidth = 1.35f * density
            val orbit = radius * 1.58f
            canvas.drawOval(cx - orbit, cy - orbit * 0.43f, cx + orbit, cy + orbit * 0.43f, ringPaint)
            val sparkAngle = time * (if (i == 0) 1.0 else -0.72) + i * PI
            sparkPaint.color = Color.argb(245, 255, 250, 206)
            canvas.drawCircle(
                cx + (cos(sparkAngle) * orbit).toFloat(),
                cy + (sin(sparkAngle) * orbit * 0.43).toFloat(),
                2.3f * density,
                sparkPaint
            )
            canvas.restore()
        }

        // Deep, metallic gold surface with a stationary, cached radial gradient.
        orbPaint.shader = orbShader
        canvas.drawCircle(cx, cy, radius * (0.985f + breathe * 0.015f), orbPaint)
        orbPaint.shader = null

        // A soft specular reflection travels over the orb without allocating shaders per frame.
        val reflectionX = cx + (cos(time) * radius * 0.34).toFloat()
        val reflectionY = cy + (sin(time) * radius * 0.31).toFloat()
        reflectionPaint.shader = reflectionShader
        canvas.save()
        canvas.clipPath(android.graphics.Path().apply { addCircle(cx, cy, radius * 0.98f, android.graphics.Path.Direction.CW) })
        canvas.drawCircle(reflectionX, reflectionY, radius * 0.58f, reflectionPaint)
        canvas.restore()
        reflectionPaint.shader = null

        // Orbiting micro-sparks; fixed count keeps frame cost predictable.
        for (i in 0 until 10) {
            val angle = time + i * 2.0 * PI / 10.0
            val orbit = radius * (1.92f + 0.06f * sin(time * 2.0 + i).toFloat())
            val twinkle = (0.55f + 0.45f * sin(time * 2.4 + i * 1.7)).toFloat()
            sparkPaint.color = Color.argb((95 + 160 * twinkle * intensity).toInt().coerceIn(0, 255), 255, 224, 104)
            canvas.drawCircle(
                cx + (cos(angle) * orbit).toFloat(),
                cy + (sin(angle) * orbit).toFloat(),
                (1.1f + 1.4f * twinkle) * density,
                sparkPaint
            )
        }

        highlightPaint.style = Paint.Style.STROKE
        highlightPaint.strokeWidth = 1.5f * density
        highlightPaint.color = Color.argb((125 + 115 * intensity).toInt().coerceIn(0, 255), 255, 246, 178)
        canvas.drawCircle(cx, cy, radius * 1.025f, highlightPaint)
        highlightPaint.style = Paint.Style.FILL
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }
}
