package com.example.myaiassistant

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.SystemClock
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

class UnderwaterLiveBackground(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val random = Random(42)
    private val particles = Array(90) {
        Triple(random.nextFloat(), random.nextFloat(), 0.5f + random.nextFloat() * 1.8f)
    }
    private val bubbles = Array(12) {
        Triple(random.nextFloat(), random.nextFloat(), 3f + random.nextFloat() * 7f)
    }
    private var start = SystemClock.uptimeMillis()

    init {
        setWillNotDraw(false)
        isClickable = false
        isFocusable = false
        alpha = 1f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) {
            postInvalidateOnAnimation()
            return
        }

        val t = (SystemClock.uptimeMillis() - start) / 1000f

        paint.shader = LinearGradient(
            0f, 0f, 0f, h,
            intArrayOf(0xFF020C1C.toInt(), 0xFF031B2C.toInt(), 0xFF061226.toInt()),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null

        paint.style = Paint.Style.FILL
        paint.color = 0x2238DFFF
        for (i in 0 until 6) {
            val x = w * (i / 5f) + sin(t * 0.35f + i) * 45f
            paint.strokeWidth = 70f
            canvas.drawLine(x, 0f, x - 120f, h, paint)
        }

        paint.color = 0x5547C8FF
        particles.forEachIndexed { i, p ->
            val x = ((p.first * w) + sin(t * 0.28f + i) * 10f).mod(w)
            val y = ((p.second * h) - t * (5f + i % 4)).mod(h)
            canvas.drawCircle(x, y, p.third, paint)
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = 0x6698E9FF
        bubbles.forEachIndexed { i, b ->
            val x = b.first * w + sin(t * 0.5f + i) * 12f
            val y = (b.second * h - t * (9f + i % 5) * 2f).mod(h)
            canvas.drawCircle(x, y, b.third, paint)
        }

        paint.style = Paint.Style.FILL
        paint.color = 0x5568BFFF
        repeat(4) { i ->
            val fx = ((w * (0.12f + i * 0.27f)) + sin(t * 0.22f + i * 2f) * 80f)
            val fy = h * (0.28f + i * 0.14f) + sin(t * 0.7f + i) * 16f
            drawFish(canvas, fx, fy, 0.8f + i * 0.08f, i % 2 == 0)
        }

        paint.color = 0x3344FFB0
        canvas.drawRect(0f, h * 0.84f, w, h, paint)

        postInvalidateOnAnimation()
    }

    private fun drawFish(canvas: Canvas, x: Float, y: Float, s: Float, flip: Boolean) {
        canvas.save()
        canvas.translate(x, y)
        if (flip) canvas.scale(-1f, 1f)
        paint.style = Paint.Style.FILL
        canvas.drawOval(-24f*s, -10f*s, 20f*s, 10f*s, paint)
        val path = android.graphics.Path().apply {
            moveTo(-18f*s, 0f)
            lineTo(-38f*s, -15f*s)
            lineTo(-38f*s, 15f*s)
            close()
        }
        canvas.drawPath(path, paint)
        paint.color = 0x99FFFFFF.toInt()
        canvas.drawCircle(10f*s, -3f*s, 2f*s, paint)
        paint.color = 0x5568BFFF
        canvas.restore()
    }
}