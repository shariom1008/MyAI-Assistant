package com.example.myaiassistant

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.sin

/**
 * AURIX live underwater background.
 *
 * Visual-only: it never handles clicks and never changes the existing UI.
 * Everything is drawn procedurally, so no image/video asset is required.
 */
class UnderwaterLiveBackground(
    context: Context
) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fishPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private val fish = arrayOf(
        Fish(-0.20f, 0.18f, 0.95f, 1.00f, 0.00f),
        Fish(1.18f, 0.34f, 0.72f, 0.78f, 2.20f),
        Fish(-0.30f, 0.53f, 0.58f, 0.62f, 4.10f),
        Fish(1.22f, 0.68f, 0.82f, 0.86f, 1.10f),
        Fish(-0.25f, 0.80f, 0.48f, 0.52f, 3.20f)
    )

    private val bubbles = Array(18) { index ->
        Bubble(
            x = ((index * 37) % 100) / 100f,
            y = ((index * 61) % 100) / 100f,
            radius = 3f + (index % 5) * 1.7f,
            speed = 0.000018f + (index % 4) * 0.000006f,
            phase = index * 0.73f
        )
    }

    private data class Fish(
        var x: Float,
        val y: Float,
        val speed: Float,
        val scale: Float,
        val phase: Float
    )

    private data class Bubble(
        val x: Float,
        var y: Float,
        val radius: Float,
        val speed: Float,
        val phase: Float
    )

    init {
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val t = SystemClock.uptimeMillis()
        val time = t / 1000f

        // Deep-water base.
        paint.shader = LinearGradient(
            0f, 0f, 0f, h,
            Color.rgb(1, 24, 52),
            Color.rgb(0, 4, 22),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        // Large soft underwater glow behind the AURIX interface.
        paint.shader = RadialGradient(
            w * 0.50f,
            h * 0.36f,
            h * 0.70f,
            intArrayOf(
                Color.argb(110, 0, 145, 190),
                Color.argb(45, 0, 75, 130),
                Color.argb(0, 0, 0, 0)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        // Moving caustic/light rays.
        paint.strokeWidth = w * 0.018f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND

        for (i in 0 until 7) {
            val phase = time * 0.20f + i * 0.83f
            val x = w * (0.08f + i * 0.15f) +
                sin(phase) * w * 0.035f

            paint.color = Color.argb(
                24 + (i % 3) * 8,
                80,
                215,
                255
            )

            path.reset()
            path.moveTo(x, -20f)
            path.quadTo(
                x + sin(phase * 1.7f) * w * 0.08f,
                h * 0.32f,
                x + sin(phase) * w * 0.12f,
                h * 0.72f
            )
            canvas.drawPath(path, paint)
        }

        paint.style = Paint.Style.FILL

        // Tiny moving particles.
        for (i in 0 until 34) {
            val px = ((i * 79) % 100) / 100f * w
            val py = ((i * 43) % 100) / 100f * h
            val drift = sin(time * 0.45f + i) * 5f
            paint.color = Color.argb(30 + (i % 4) * 8, 120, 225, 255)
            canvas.drawCircle(px + drift, py, 1.2f + (i % 3), paint)
        }

        // Bubbles rise continuously.
        for (bubble in bubbles) {
            bubble.y -= bubble.speed * (t % 100000L)
            if (bubble.y < -0.08f) bubble.y = 1.08f

            val bx = bubble.x * w +
                sin(time * 0.7f + bubble.phase) * 18f
            val by = bubble.y * h

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            paint.color = Color.argb(90, 125, 230, 255)
            canvas.drawCircle(bx, by, bubble.radius, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.argb(22, 170, 245, 255)
            canvas.drawCircle(bx, by, bubble.radius * 0.72f, paint)
        }

        // Fish swimming in both directions.
        for (fish in fish) {
            val travel = (time * 0.055f * fish.speed) % 1.55f
            val rawX =
                if (fish.phase % 2f < 1f) {
                    fish.x + travel
                } else {
                    fish.x - travel
                }

            var fx = rawX
            if (fx > 1.25f) fx -= 1.55f
            if (fx < -0.25f) fx += 1.55f

            val fy =
                fish.y * h +
                    sin(time * 0.9f + fish.phase) * h * 0.018f

            drawFish(
                canvas,
                fx * w,
                fy,
                fish.scale,
                fish.phase % 2f < 1f
            )
        }

        // Sea floor.
        paint.color = Color.argb(185, 0, 7, 20)
        canvas.drawRect(0f, h * 0.88f, w, h, paint)

        // Grass/coral silhouettes.
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND

        for (i in 0 until 13) {
            val x = i * w / 12f
            val height = h * (0.035f + (i % 4) * 0.014f)
            val sway = sin(time * 0.9f + i) * 9f

            paint.strokeWidth = 3.5f + (i % 3)
            paint.color = Color.argb(150, 5, 75, 88)

            path.reset()
            path.moveTo(x, h * 0.91f)
            path.quadTo(
                x + sway,
                h * 0.91f - height * 0.5f,
                x + sway * 1.5f,
                h * 0.91f - height
            )
            canvas.drawPath(path, paint)
        }

        paint.style = Paint.Style.FILL

        // Gentle dark readability veil; intentionally translucent.
        paint.color = Color.argb(42, 0, 0, 10)
        canvas.drawRect(0f, 0f, w, h, paint)

        postInvalidateOnAnimation()
    }

    private fun drawFish(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        scale: Float,
        facingRight: Boolean
    ) {
        val s = 34f * scale

        canvas.save()
        canvas.translate(cx, cy)
        if (!facingRight) canvas.scale(-1f, 1f)

        // Body.
        fishPaint.style = Paint.Style.FILL
        fishPaint.color = Color.argb(170, 25, 185, 220)
        path.reset()
        path.moveTo(-s * 0.62f, 0f)
        path.cubicTo(
            -s * 0.45f, -s * 0.38f,
            s * 0.36f, -s * 0.38f,
            s * 0.58f, 0f
        )
        path.cubicTo(
            s * 0.36f, s * 0.38f,
            -s * 0.45f, s * 0.38f,
            -s * 0.62f, 0f
        )
        canvas.drawPath(path, fishPaint)

        // Tail.
        fishPaint.color = Color.argb(150, 55, 220, 245)
        path.reset()
        path.moveTo(-s * 0.58f, 0f)
        path.lineTo(-s * 0.95f, -s * 0.34f)
        path.lineTo(-s * 0.95f, s * 0.34f)
        path.close()
        canvas.drawPath(path, fishPaint)

        // Fin.
        fishPaint.color = Color.argb(125, 20, 135, 175)
        path.reset()
        path.moveTo(0f, -s * 0.28f)
        path.lineTo(s * 0.16f, -s * 0.58f)
        path.lineTo(s * 0.30f, -s * 0.12f)
        path.close()
        canvas.drawPath(path, fishPaint)

        // Eye.
        fishPaint.color = Color.WHITE
        canvas.drawCircle(s * 0.39f, -s * 0.07f, s * 0.065f, fishPaint)
        fishPaint.color = Color.rgb(2, 20, 35)
        canvas.drawCircle(s * 0.405f, -s * 0.07f, s * 0.032f, fishPaint)

        canvas.restore()
    }
}
