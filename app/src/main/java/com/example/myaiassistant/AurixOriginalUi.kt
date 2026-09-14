package com.example.myaiassistant

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

/**
 * AURIX original-style UI layer.
 *
 * MainActivity remains responsible for permissions, service, navigation logic,
 * and callbacks. This class owns only the visual hierarchy.
 */
class AurixOriginalUi(
    private val context: Context,
    private val callbacks: Callbacks
) {
    interface Callbacks {
        fun onMenu()
        fun onVoice()
        fun onYouTube()
        fun onSearch()
        fun onMusic()
        fun onWeather()
        fun onCall()
        fun onMessages()
        fun onApps()
        fun onMore()
        fun onHome()
        fun onHistory()
        fun onAurix()
        fun onShortcuts()
        fun onSettings()
    }

    lateinit var voiceButton: TextView
        private set
    lateinit var voiceStatus: TextView
        private set
    lateinit var statusText: TextView
        private set
    lateinit var conversationBox: LinearLayout
        private set

    fun build(): View {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(14), dp(18), dp(10))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(5, 8, 28),
                    Color.rgb(9, 8, 35),
                    Color.rgb(4, 12, 30)
                )
            )
        }

        buildHeader(root)
        buildTitle(root)
        buildCore(root)
        buildConversation(root)
        buildQuickActions(root)
        buildBottomNavigation(root)

        return root
    }

    private fun buildHeader(root: LinearLayout) {
        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(TextView(context).apply {
            text = "☰"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setOnClickListener { callbacks.onMenu() }
        }, LinearLayout.LayoutParams(dp(42), dp(48)))

        val brand = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        brand.addView(TextView(context).apply {
            text = "A U R I X"
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        brand.addView(TextView(context).apply {
            text = "INTELLIGENCE CORE"
            textSize = 8f
            letterSpacing = 0.18f
            setTextColor(Color.rgb(120, 190, 255))
        })

        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))

        header.addView(TextView(context).apply {
            text = "● ONLINE"
            textSize = 9f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(80, 220, 150))
        }, LinearLayout.LayoutParams(-2, dp(40)))

        voiceButton = TextView(context).apply {
            text = "◉"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = voiceBackground(false)
            elevation = dp(8).toFloat()
            setOnClickListener { callbacks.onVoice() }
        }

        header.addView(
            voiceButton,
            LinearLayout.LayoutParams(dp(48), dp(48)).apply {
                marginStart = dp(10)
            }
        )

        root.addView(header, LinearLayout.LayoutParams(-1, dp(54)))
    }

    private fun buildTitle(root: LinearLayout) {
        root.addView(TextView(context).apply {
            text = "A U R I X   •   INTELLIGENCE CORE"
            textSize = 9f
            letterSpacing = 0.18f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(100, 175, 225))
            setPadding(0, dp(8), 0, dp(3))
        }, LinearLayout.LayoutParams(-1, dp(40)))
    }

    private fun buildCore(root: LinearLayout) {
        val core = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        core.addView(
            AurixOrbView(context),
            LinearLayout.LayoutParams(dp(220), dp(220))
        )

        voiceStatus = TextView(context).apply {
            text = "VOICE  •  READY"
            textSize = 10f
            letterSpacing = 0.16f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(90, 210, 255))
        }
        statusText = voiceStatus

        core.addView(
            voiceStatus,
            LinearLayout.LayoutParams(-1, dp(30))
        )

        root.addView(core, LinearLayout.LayoutParams(-1, dp(255)))
    }

    private fun buildConversation(root: LinearLayout) {
        root.addView(TextView(context).apply {
            text = "SAVED CONVERSATION"
            textSize = 9f
            letterSpacing = 0.18f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.rgb(105, 170, 225))
        }, LinearLayout.LayoutParams(-1, dp(28)))

        conversationBox = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        addAurixMessage("Good night boss, abhi kya karna hai?")

        val scroll = ScrollView(context).apply {
            isVerticalScrollBarEnabled = false
            addView(conversationBox)
        }

        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    fun addUserMessage(message: String) {
        conversationBox.addView(messageCard("YOU", message, Color.rgb(145, 105, 255)))
    }

    fun addAurixMessage(message: String) {
        conversationBox.addView(messageCard("AURIX", message, Color.rgb(60, 205, 255)))
    }

    private fun messageCard(title: String, message: String, accent: Int): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(Color.argb(70, 18, 27, 60))
                setStroke(
                    dp(1),
                    Color.argb(90, Color.red(accent), Color.green(accent), Color.blue(accent))
                )
            }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, dp(3), 0, dp(3))
            }

            addView(TextView(context).apply {
                text = title
                textSize = 8f
                letterSpacing = 0.16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(accent)
            })

            addView(TextView(context).apply {
                text = message
                textSize = 11f
                setTextColor(Color.WHITE)
                setPadding(0, dp(3), 0, 0)
            })
        }
    }

    private fun buildQuickActions(root: LinearLayout) {
        root.addView(TextView(context).apply {
            text = "QUICK ACTIONS"
            textSize = 9f
            letterSpacing = 0.18f
            setTextColor(Color.rgb(110, 170, 230))
            setPadding(0, dp(4), 0, dp(3))
        })

        val row1 = LinearLayout(context)
        addAction(row1, "YouTube", callbacks::onYouTube)
        addAction(row1, "Search", callbacks::onSearch)
        addAction(row1, "Music", callbacks::onMusic)
        addAction(row1, "Weather", callbacks::onWeather)
        root.addView(row1, LinearLayout.LayoutParams(-1, dp(66)))

        val row2 = LinearLayout(context)
        addAction(row2, "Call", callbacks::onCall)
        addAction(row2, "Messages", callbacks::onMessages)
        addAction(row2, "Apps", callbacks::onApps)
        addAction(row2, "More", callbacks::onMore)
        root.addView(row2, LinearLayout.LayoutParams(-1, dp(66)))
    }

    private fun addAction(
        row: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val tile = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(Color.rgb(14, 22, 43))
                setStroke(dp(1), Color.rgb(38, 58, 90))
            }
            setOnClickListener { action() }
        }

        tile.addView(
            QuickActionIcon(context, label),
            LinearLayout.LayoutParams(dp(25), dp(25))
        )

        tile.addView(TextView(context).apply {
            text = label
            textSize = 9f
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(Color.rgb(225, 232, 245))
        }, LinearLayout.LayoutParams(-1, dp(16)))

        row.addView(
            tile,
            LinearLayout.LayoutParams(0, dp(58), 1f).apply {
                setMargins(dp(3), dp(4), dp(3), dp(4))
            }
        )
    }

    private fun buildBottomNavigation(root: LinearLayout) {
        val nav = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                cornerRadius = dp(15).toFloat()
                setColor(Color.argb(45, 10, 35, 70))
                setStroke(dp(1), Color.rgb(35, 75, 120))
            }
        }

        addNavItem(nav, "Home", callbacks::onHome)
        addNavItem(nav, "History", callbacks::onHistory)
        addNavItem(nav, "AURIX", callbacks::onAurix)
        addNavItem(nav, "Shortcuts", callbacks::onShortcuts)
        addNavItem(nav, "Settings", callbacks::onSettings)

        root.addView(
            nav,
            LinearLayout.LayoutParams(-1, dp(46)).apply {
                topMargin = dp(5)
            }
        )
    }

    private fun addNavItem(
        nav: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        nav.addView(TextView(context).apply {
            text = label
            textSize = 8f
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(
                if (label == "AURIX")
                    Color.rgb(85, 210, 255)
                else
                    Color.rgb(135, 145, 175)
            )
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(0, -1, 1f))
    }

    fun setReadyState() {
        voiceStatus.text = "VOICE  •  READY"
        voiceButton.background = voiceBackground(false)
    }

    fun setListeningState() {
        voiceStatus.text = "VOICE  •  LISTENING"
        voiceButton.background = voiceBackground(true)
    }

    fun setThinkingState() {
        voiceStatus.text = "VOICE  •  THINKING"
        voiceButton.background = voiceBackground(true)
    }

    private fun voiceBackground(active: Boolean) =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(
                if (active) Color.rgb(35, 80, 135)
                else Color.rgb(22, 34, 68)
            )
            setStroke(
                dp(1),
                Color.rgb(75, 175, 240)
            )
        }

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    private class AurixOrbView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var rotation = 0f

        init {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            post(object : Runnable {
                override fun run() {
                    rotation = (rotation + 0.7f) % 360f
                    invalidate()
                    postDelayed(this, 24L)
                }
            })
        }

        override fun onDraw(canvas: Canvas) {
            val cx = width / 2f
            val cy = height / 2f
            val r = kotlin.math.min(width, height) * 0.31f

            paint.style = Paint.Style.FILL
            paint.shader = RadialGradient(
                cx, cy, r * 1.8f,
                intArrayOf(
                    Color.argb(85, 40, 150, 255),
                    Color.argb(25, 80, 70, 220),
                    Color.TRANSPARENT
                ),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, r * 1.8f, paint)

            paint.shader = RadialGradient(
                cx - r * .25f, cy - r * .30f, r * 1.25f,
                intArrayOf(
                    Color.rgb(70, 190, 255),
                    Color.rgb(70, 75, 220),
                    Color.rgb(35, 20, 95)
                ),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, r, paint)

            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(2)
            paint.color = Color.rgb(95, 215, 255)

            canvas.save()
            canvas.rotate(rotation, cx, cy)

            canvas.drawOval(
                RectF(cx - r * 1.22f, cy - r * .40f,
                    cx + r * 1.22f, cy + r * .40f),
                paint
            )

            canvas.drawOval(
                RectF(cx - r * .48f, cy - r * 1.22f,
                    cx + r * .48f, cy + r * 1.22f),
                paint
            )

            canvas.restore()

            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textSize = r * .30f
            paint.color = Color.WHITE
            canvas.drawText("AURIX", cx, cy + r * .08f, paint)

            paint.textSize = r * .11f
            paint.color = Color.rgb(175, 225, 255)
            canvas.drawText("CORE", cx, cy + r * .34f, paint)
        }

        private fun dp(value: Int): Float =
            value * resources.displayMetrics.density
    }

    private class QuickActionIcon(
        context: Context,
        private val label: String
    ) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val path = Path()

        private fun d(v: Float) = v * resources.displayMetrics.density

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()

            paint.style = Paint.Style.FILL

            when (label.lowercase(Locale.ROOT)) {
                "youtube" -> {
                    paint.color = Color.rgb(255, 0, 40)
                    canvas.drawRoundRect(
                        RectF(.08f*w, .18f*h, .92f*w, .82f*h),
                        d(5f), d(5f), paint
                    )
                    paint.color = Color.WHITE
                    path.reset()
                    path.moveTo(.43f*w, .33f*h)
                    path.lineTo(.43f*w, .67f*h)
                    path.lineTo(.70f*w, .50f*h)
                    path.close()
                    canvas.drawPath(path, paint)
                }

                "search" -> {
                    paint.color = Color.rgb(40, 210, 235)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = d(2.4f)
                    canvas.drawCircle(.42f*w, .42f*h, .23f*w, paint)
                    canvas.drawLine(.59f*w, .59f*h, .82f*w, .82f*h, paint)
                }

                "music" -> {
                    paint.color = Color.rgb(205, 70, 245)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = d(2.8f)
                    canvas.drawLine(.62f*w, .20f*h, .62f*w, .68f*h, paint)
                    canvas.drawLine(.62f*w, .20f*h, .84f*w, .14f*h, paint)
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(.45f*w, .70f*h, d(4f), paint)
                    canvas.drawCircle(.73f*w, .64f*h, d(4f), paint)
                }

                "weather" -> {
                    paint.color = Color.rgb(250, 205, 35)
                    canvas.drawCircle(.37f*w, .36f*h, d(6f), paint)
                    paint.color = Color.rgb(55, 185, 245)
                    canvas.drawCircle(.44f*w, .63f*h, d(7f), paint)
                    canvas.drawCircle(.63f*w, .57f*h, d(6f), paint)
                    canvas.drawRoundRect(
                        RectF(.28f*w, .57f*h, .78f*w, .78f*h),
                        d(7f), d(7f), paint
                    )
                }

                "call" -> {
                    paint.color = Color.rgb(35, 205, 105)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = d(3.2f)
                    path.reset()
                    path.moveTo(.30f*w, .25f*h)
                    path.cubicTo(.20f*w, .45f*h, .46f*w, .74f*h, .70f*w, .72f*h)
                    path.lineTo(.82f*w, .58f*h)
                    canvas.drawPath(path, paint)
                }

                "messages" -> {
                    paint.color = Color.rgb(55, 125, 245)
                    canvas.drawRoundRect(
                        RectF(.10f*w, .20f*h, .90f*w, .70f*h),
                        d(6f), d(6f), paint
                    )
                    paint.color = Color.WHITE
                    canvas.drawCircle(.36f*w, .45f*h, d(2f), paint)
                    canvas.drawCircle(.50f*w, .45f*h, d(2f), paint)
                    canvas.drawCircle(.64f*w, .45f*h, d(2f), paint)
                }

                "apps" -> {
                    val colors = intArrayOf(
                        Color.rgb(40, 210, 235),
                        Color.rgb(170, 80, 245),
                        Color.rgb(245, 65, 110),
                        Color.rgb(250, 205, 35)
                    )
                    val xy = arrayOf(
                        floatArrayOf(.30f,.30f),
                        floatArrayOf(.70f,.30f),
                        floatArrayOf(.30f,.70f),
                        floatArrayOf(.70f,.70f)
                    )
                    for (i in 0..3) {
                        paint.color = colors[i]
                        val x = xy[i][0] * w
                        val y = xy[i][1] * h
                        canvas.drawRoundRect(
                            RectF(x-.13f*w, y-.13f*h, x+.13f*w, y+.13f*h),
                            d(3f), d(3f), paint
                        )
                    }
                }

                else -> {
                    paint.color = Color.rgb(150, 85, 245)
                    canvas.drawCircle(w/2f, h/2f, d(8f), paint)
                    paint.color = Color.WHITE
                    canvas.drawCircle(w/2f, h/2f, d(3f), paint)
                }
            }
        }
    }
}
