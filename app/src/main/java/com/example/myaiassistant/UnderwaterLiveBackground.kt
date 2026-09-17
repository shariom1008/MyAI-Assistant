package com.example.myaiassistant

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lightweight procedural underwater background for AURIX.
 *
 * Visual-only layer. It sits behind AurixOriginalUi and deliberately
 * consumes no touch input, so the existing buttons/icons remain usable.
 * No image/video assets are required.
 */
class UnderwaterLiveBackground(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fishPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val coralPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private val startTime = SystemClock.uptimeMillis()
    private var frame = 0L

    private data class Fish(
        val baseX: Float,
        val baseY: Float,
        val size: Float,
        val speed: Float,
        val phase: Float,
        val depth: Float,
        val flip: Boolean
    )

    private data class Bubble(
        val x: Float,
        val size: Float,
        val speed: Float,
        val phase: Float
    )

    private val fish = listOf(
        Fish(0.08f, 0.22f, 0.80f, 0.000030f, 0.4f, 0.40f, false),
        Fish(0.72f, 0.30f, 0.62f, 0.000024f, 2.1f, 0.28f, true),
        Fish(0.28f, 0.47f, 0.48f, 0.000020f, 4.0f, 0.18f, false),
        Fish(0.86f, 0.57f, 0.54f, 0.000026f, 5.2f, 0.32f, true),
        Fish(0.52f, 0.74f, 0.36f, 0.000018f, 1.7f, 0.14f, false)
    )

    private val bubbles = listOf(
        Bubble(0.10f, 4f, 0.000025f, 0.2f),
        Bubble(0.17f, 7f, 0.000019f, 1.5f),
        Bubble(0.34f, 3f, 0.000030f, 2.2f),
        Bubble(0.47f, 5f, 0.000022f, 3.1f),
        Bubble(0.63f, 4f, 0.000028f, 4.0f),
        Bubble(0.78f, 8f, 0.000017f, 5.0f),
        Bubble(0.91f, 3f, 0.000032f, 5.8f),
        Bubble(0.26f, 2.5f, 0.000035f, 6.4f)
    )

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val elapsed = (SystemClock.uptimeMillis() - startTime).toFloat()
        frame = elapsed.toLong()

        drawWater(canvas, w, h, elapsed)
        drawLightRays(canvas, w, h, elapsed)
        drawBubbles(canvas, w, h, elapsed)
        drawFish(canvas, w, h, elapsed)
        drawSeabed(canvas, w, h, elapsed)

        // Keep the background dark enough for the original AURIX UI.
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = 0x22000514
        canvas.drawRect(0f, 0f, w, h, paint)

        // Continuous animation; intentionally lightweight.
        postInvalidateOnAnimation()
    }

    private fun drawWater(canvas: Canvas, w: Float, h: Float, t: Float) {
        val top = 0xFF001B30.toInt()
        val middle = 0xFF003A52.toInt()
        val bottom = 0xFF00101F.toInt()

        val gradient = android.graphics.LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(top, middle, bottom),
            floatArrayOf(0f, 0.48f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        paint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        // Slow underwater glow near the upper-middle area.
        val glowX = w * (0.50f + 0.045f * sin(t * 0.00018f))
        val glowY = h * 0.17f
        paint.shader = RadialGradient(
            glowX,
            glowY,
            h * 0.42f,
            0x553AD9FF,
            0x00000B1A,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(glowX, glowY, h * 0.42f, paint)
        paint.shader = null
    }

    private fun drawLightRays(canvas: Canvas, w: Float, h: Float, t: Float) {
        rayPaint.style = Paint.Style.FILL
        rayPaint.color = 0x1434D8FF

        val sway = sin(t * 0.00022f) * w * 0.035f
        val topY = -h * 0.08f
        val bottomY = h * 0.72f

        drawRay(canvas, w * 0.18f + sway, topY, w * 0.38f, bottomY, w * 0.06f)
        drawRay(canvas, w * 0.42f - sway * 0.6f, topY, w * 0.55f, bottomY, w * 0.045f)
        drawRay(canvas, w * 0.69f + sway * 0.7f, topY, w * 0.76f, bottomY, w * 0.055f)
    }

    private fun drawRay(
        canvas: Canvas,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        width: Float
    ) {
        path.reset()
        path.moveTo(x1 - width, y1)
        path.lineTo(x1 + width, y1)
        path.lineTo(x2 + width * 2.2f, y2)
        path.lineTo(x2 - width * 2.2f, y2)
        path.close()
        canvas.drawPath(path, rayPaint)
    }

    private fun drawBubbles(canvas: Canvas, w: Float, h: Float, t: Float) {
        bubblePaint.style = Paint.Style.STROKE
        bubblePaint.strokeWidth = 1.2f

        bubbles.forEachIndexed { index, bubble ->
            val cycle = ((t * bubble.speed + bubble.phase) % 1f + 1f) % 1f
            val y = h * (1.06f - cycle * 1.20f)
            val drift = sin(t * 0.0012f + bubble.phase * 4f) * w * 0.012f
            val x = w * bubble.x + drift
            val alpha = (30 + 45 * sin(cycle * Math.PI).toFloat()).toInt().coerceIn(20, 80)
            bubblePaint.color = (alpha shl 24) or 0x9AE8FF
            canvas.drawCircle(x, y, bubble.size + sin(t * 0.001f + index).toFloat(), bubblePaint)
        }
    }

    private fun drawFish(canvas: Canvas, w: Float, h: Float, t: Float) {
        fish.forEach { fish ->
            val travel = ((t * fish.speed + fish.phase) % 1.25f)
            val x = ((fish.baseX + travel * 0.95f) % 1.35f) - 0.10f
            val bob = sin(t * 0.0011f + fish.phase * 2.2f) * h * 0.018f
            val y = h * fish.baseY + bob
            val scale = fish.size * (0.78f + fish.depth * 0.25f)
            drawFish(canvas, w * x, y, scale, fish.flip, fish.depth)
        }
    }

    private fun drawFish(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        flip: Boolean,
        depth: Float
    ) {
        val bodyAlpha = (55 + depth * 55).toInt().coerceIn(45, 120)
        fishPaint.style = Paint.Style.FILL
        fishPaint.color = (bodyAlpha shl 24) or 0x3FD8FF

        val direction = if (flip) -1f else 1f

        canvas.save()
        canvas.translate(x, y)
        canvas.scale(direction, 1f)

        // Body
        path.reset()
        path.moveTo(-size * 0.48f, 0f)
        path.cubicTo(
            -size * 0.24f, -size * 0.27f,
            size * 0.28f, -size * 0.25f,
            size * 0.50f, 0f
        )
        path.cubicTo(
            size * 0.28f, size * 0.25f,
            -size * 0.24f, size * 0.27f,
            -size * 0.48f, 0f
        )
        path.close()
        canvas.drawPath(path, fishPaint)

        // Tail
        path.reset()
        path.moveTo(-size * 0.43f, 0f)
        path.lineTo(-size * 0.72f, -size * 0.25f)
        path.lineTo(-size * 0.67f, 0f)
        path.lineTo(-size * 0.72f, size * 0.25f)
        path.close()
        canvas.drawPath(path, fishPaint)

        // Fin
        fishPaint.color = (bodyAlpha shl 24) or 0x7C9DFF
        path.reset()
        path.moveTo(size * 0.02f, -size * 0.15f)
        path.lineTo(size * 0.17f, -size * 0.42f)
        path.lineTo(size * 0.24f, -size * 0.08f)
        path.close()
        canvas.drawPath(path, fishPaint)

        // Eye
        fishPaint.color = 0xB8FFFFFF.toInt()
        canvas.drawCircle(size * 0.34f, -size * 0.055f, size * 0.035f, fishPaint)

        canvas.restore()
    }

    private fun drawSeabed(canvas: Canvas, w: Float, h: Float, t: Float) {
        val baseY = h * 0.91f
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = 0xCC00101A.toInt()
        canvas.drawRect(0f, baseY, w, h, paint)

        // Soft sand/grass silhouettes.
        coralPaint.style = Paint.Style.STROKE
        coralPaint.strokeCap = Paint.Cap.ROUND
        coralPaint.strokeWidth = 2.4f
        coralPaint.color = 0x7A1EA5A8

        for (i in 0 until 12) {
            val x = w * (i / 11f)
            val sway = sin(t * 0.001f + i) * w * 0.012f
            val height = h * (0.055f + (i % 4) * 0.012f)
            canvas.drawLine(x, baseY, x + sway, baseY - height, coralPaint)
        }

        // A few darker coral branches at the edges so the center stays clean.
        coralPaint.strokeWidth = 3f
        coralPaint.color = 0x8A1B7F8E.toInt()
        drawCoral(canvas, w * 0.06f, baseY, h * 0.13f, t, 1f)
        drawCoral(canvas, w * 0.94f, baseY, h * 0.16f, t, -1f)
    }

    private fun drawCoral(
        canvas: Canvas,
        x: Float,
        baseY: Float,
        height: Float,
        t: Float,
        direction: Float
    ) {
        val sway = sin(t * 0.0009f) * 6f
        canvas.drawLine(x, baseY, x + sway, baseY - height, coralPaint)
        canvas.drawLine(x + sway, baseY - height * 0.55f, x + direction * 16f + sway, baseY - height * 0.78f, coralPaint)
        canvas.drawLine(x + direction * 16f + sway, baseY - height * 0.78f, x + direction * 22f + sway, baseY - height * 0.98f, coralPaint)
        canvas.drawLine(x + sway, baseY - height * 0.40f, x - direction * 13f + sway, baseY - height * 0.62f, coralPaint)
    }
}
