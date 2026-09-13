package com.example.myaiassistant

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlin.math.sin

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var voiceButton: TextView
    private lateinit var voiceStatus: TextView

    private var listening = false

    companion object {
        private const val REQUEST_AUDIO = 1001
    }

    // =========================================================
    // CREATE
    // =========================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildInterface()

        checkMicrophonePermission()
    }

    // =========================================================
    // MAIN UI
    // =========================================================

    private fun buildInterface() {

        root = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(14),
                dp(18),
                dp(10)
            )

            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(5, 8, 28),
                    Color.rgb(9, 8, 35),
                    Color.rgb(4, 12, 30)
                )
            )
        }

        setContentView(root)

        buildHeader()
        buildTitle()
        buildCore()
        buildConversation()
        buildQuickActions()
        buildBottomNavigation()
    }

    // =========================================================
    // HEADER
    // =========================================================

    private fun buildHeader() {

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // MENU
        val menu = TextView(this).apply {

            text = "☰"

            textSize = 24f

            gravity = Gravity.CENTER

            setTextColor(Color.WHITE)
        }

        header.addView(
            menu,
            LinearLayout.LayoutParams(
                dp(42),
                dp(48)
            )
        )

        // BRAND
        val brandBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val brand = TextView(this).apply {

            text = "A U R I X"

            textSize = 21f

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(Color.WHITE)
        }

        val tagline = TextView(this).apply {

            text = "INTELLIGENCE CORE"

            textSize = 8f

            letterSpacing = 0.18f

            setTextColor(
                Color.rgb(120, 190, 255)
            )
        }

        brandBox.addView(brand)
        brandBox.addView(tagline)

        header.addView(
            brandBox,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        // ONLINE
        val online = TextView(this).apply {

            text = "● ONLINE"

            textSize = 9f

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(
                Color.rgb(80, 220, 255)
            )

            gravity = Gravity.CENTER
        }

        header.addView(
            online,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(40)
            )
        )

        // =====================================================
        // SMALL SMART VOICE BUTTON
        // =====================================================

        voiceButton = TextView(this).apply {

            text = "◉"

            textSize = 22f

            gravity = Gravity.CENTER

            setTextColor(Color.WHITE)

            background =
                createVoiceBackground(false)

            elevation = dp(8).toFloat()

            setOnClickListener {
                activateVoice()
            }
        }

        val micParams = LinearLayout.LayoutParams(
            dp(48),
            dp(48)
        )

        micParams.marginStart = dp(10)

        header.addView(
            voiceButton,
            micParams
        )

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )
    }

    // =========================================================
    // TITLE
    // =========================================================

    private fun buildTitle() {

        val title = TextView(this).apply {

            text =
                "A U R I X  |  INTELLIGENCE CORE"

            textSize = 9f

            letterSpacing = 0.20f

            gravity = Gravity.CENTER

            setTextColor(
                Color.rgb(100, 190, 255)
            )

            setPadding(
                0,
                dp(12),
                0,
                dp(3)
            )
        }

        root.addView(title)
    }

    // =========================================================
    // CORE
    // =========================================================

    private fun buildCore() {

        val container = LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            gravity = Gravity.CENTER
        }

        val orb = AurixOrbView(this)

        container.addView(
            orb,
            LinearLayout.LayoutParams(
                dp(220),
                dp(220)
            )
        )

        voiceStatus = TextView(this).apply {

            text = "VOICE  •  READY"

            textSize = 10f

            letterSpacing = 0.18f

            gravity = Gravity.CENTER

            typeface =
                Typeface.DEFAULT_BOLD

            setTextColor(
                Color.rgb(90, 210, 255)
            )
        }

        container.addView(
            voiceStatus,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(34)
            )
        )

        statusText = TextView(this).apply {

            text =
                "Tap the AURIX voice icon to speak"

            textSize = 10f

            gravity = Gravity.CENTER

            setTextColor(
                Color.rgb(145, 155, 190)
            )
        }

        container.addView(statusText)

        root.addView(
            container,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(285)
            )
        )
    }

    // =========================================================
    // CONVERSATION
    // =========================================================

    private lateinit var conversationBox: LinearLayout

    private fun buildConversation() {

        conversationBox = LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL
        }

        addAurixMessage(
            "AURIX ready. Tap the voice icon to speak."
        )

        val scroll = ScrollView(this).apply {

            isVerticalScrollBarEnabled = false

            addView(conversationBox)
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
    }

    private fun addUserMessage(
        message: String
    ) {

        conversationBox.addView(
            messageCard(
                "YOU",
                message,
                Color.rgb(125, 95, 255)
            )
        )
    }

    private fun addAurixMessage(
        message: String
    ) {

        conversationBox.addView(
            messageCard(
                "AURIX",
                message,
                Color.rgb(60, 205, 255)
            )
        )
    }

    private fun messageCard(
        title: String,
        message: String,
        accent: Int
    ): LinearLayout {

        val box = LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
            )

            background =
                GradientDrawable().apply {

                    cornerRadius =
                        dp(16).toFloat()

                    setColor(
                        Color.argb(
                            75,
                            20,
                            28,
                            60
                        )
                    )

                    setStroke(
                        dp(1),
                        Color.argb(
                            100,
                            Color.red(accent),
                            Color.green(accent),
                            Color.blue(accent)
                        )
                    )
                }
        }

        val label = TextView(this).apply {

            text = title

            textSize = 8f

            letterSpacing = 0.18f

            typeface =
                Typeface.DEFAULT_BOLD

            setTextColor(accent)
        }

        val textView = TextView(this).apply {

            text = message

            textSize = 12f

            setTextColor(Color.WHITE)

            setPadding(
                0,
                dp(4),
                0,
                0
            )
        }

        box.addView(label)
        box.addView(textView)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(
            0,
            dp(4),
            0,
            dp(4)
        )

        box.layoutParams = params

        return box
    }

    // =========================================================
    // QUICK ACTIONS
    // =========================================================

    private fun buildQuickActions() {

        val title = TextView(this).apply {

            text = "QUICK ACTIONS"

            textSize = 9f

            letterSpacing = 0.18f

            setTextColor(
                Color.rgb(110, 170, 230)
            )

            setPadding(
                0,
                dp(7),
                0,
                dp(6)
            )
        }

        root.addView(title)

        val row1 = LinearLayout(this)

        addAction(row1, "YouTube")
        addAction(row1, "Search")
        addAction(row1, "Music")
        addAction(row1, "Weather")

        root.addView(row1)

        val row2 = LinearLayout(this)

        addAction(row2, "Call")
        addAction(row2, "Messages")
        addAction(row2, "Apps")
        addAction(row2, "More")

        root.addView(row2)
    }

    private fun addAction(
        row: LinearLayout,
        label: String
    ) {

        val button = TextView(this).apply {

            text = label

            textSize = 9f

            gravity = Gravity.CENTER

            setTextColor(Color.WHITE)

            background =
                GradientDrawable().apply {

                    cornerRadius =
                        dp(12).toFloat()

                    setColor(
                        Color.argb(
                            65,
                            35,
                            45,
                            85
                        )
                    )

                    setStroke(
                        dp(1),
                        Color.argb(
                            80,
                            90,
                            160,
                            255
                        )
                    )
                }

            /*
             * Quick action buttons abhi sirf UI hain.
             *
             * Voice command execution ka actual flow
             * AurixService ke existing implementation se chalega.
             */
        }

        val params = LinearLayout.LayoutParams(
            0,
            dp(40),
            1f
        )

        params.setMargins(
            dp(3),
            dp(3),
            dp(3),
            dp(3)
        )

        row.addView(
            button,
            params
        )
    }

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private fun buildBottomNavigation() {

        val nav = LinearLayout(this).apply {

            orientation =
                LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER

            setPadding(
                0,
                dp(8),
                0,
                0
            )
        }

        addNavItem(nav, "Home")
        addNavItem(nav, "History")
        addNavItem(nav, "AURIX")
        addNavItem(nav, "Shortcuts")
        addNavItem(nav, "Settings")

        root.addView(
            nav,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
            )
        )
    }

    private fun addNavItem(
        nav: LinearLayout,
        label: String
    ) {

        val item = TextView(this).apply {

            text = label

            textSize = 8f

            gravity = Gravity.CENTER

            setTextColor(
                if (label == "AURIX")
                    Color.rgb(90, 210, 255)
                else
                    Color.rgb(130, 140, 175)
            )
        }

        nav.addView(
            item,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )
    }

    // =========================================================
    // VOICE ACTIVATION
    // =========================================================

    private fun activateVoice() {

        if (listening) {
            return
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                REQUEST_AUDIO
            )

            return
        }

        listening = true

        voiceButton.text = "●"

        voiceButton.background =
            createVoiceBackground(true)

        voiceStatus.text =
            "VOICE  •  LISTENING"

        statusText.text =
            "Listening for your command..."

        val intent = Intent(
            this,
            AurixService::class.java
        ).apply {

            action =
                AurixService.ACTION_LISTEN_ONCE
        }

        ContextCompat.startForegroundService(
            this,
            intent
        )

        addUserMessage(
            "Voice command activated."
        )
    }

    // =========================================================
    // PERMISSION
    // =========================================================

    private fun checkMicrophonePermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                REQUEST_AUDIO
            )

        } else {

            // IMPORTANT:
            // Permission already hai to service START nahi hogi.
            // User ko top-right voice icon tap karna padega.

            setReadyState()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode != REQUEST_AUDIO) {
            return
        }

        if (
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {

            setReadyState()

        } else {

            listening = false

            voiceStatus.text =
                "VOICE  •  MIC REQUIRED"

            statusText.text =
                "Microphone permission is required"

            voiceButton.background =
                createVoiceBackground(false)
        }
    }

    // =========================================================
    // READY STATE
    // =========================================================

    private fun setReadyState() {

        listening = false

        voiceButton.text = "◉"

        voiceButton.background =
            createVoiceBackground(false)

        voiceStatus.text =
            "VOICE  •  READY"

        statusText.text =
            "Tap the AURIX voice icon to speak"
    }

    // =========================================================
    // VOICE BUTTON BACKGROUND
    // =========================================================

    private fun createVoiceBackground(
        active: Boolean
    ): GradientDrawable {

        return GradientDrawable().apply {

            shape =
                GradientDrawable.OVAL

            if (active) {

                setColor(
                    Color.rgb(
                        45,
                        90,
                        145
                    )
                )

                setStroke(
                    dp(2),
                    Color.rgb(
                        80,
                        220,
                        255
                    )
                )

            } else {

                setColor(
                    Color.rgb(
                        25,
                        35,
                        70
                    )
                )

                setStroke(
                    dp(1),
                    Color.rgb(
                        80,
                        150,
                        240
                    )
                )
            }
        }
    }

    // =========================================================
    // DP
    // =========================================================

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    // =========================================================
    // AURIX ORB
    // =========================================================

    class AurixOrbView(
        context: android.content.Context
    ) : View(context) {

        private val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private var rotation = 0f

        private val animator =
            ValueAnimator.ofFloat(
                0f,
                360f
            ).apply {

                duration = 5000

                repeatCount =
                    ValueAnimator.INFINITE

                addUpdateListener {

                    rotation =
                        it.animatedValue
                            as Float

                    invalidate()
                }
            }

        init {
            animator.start()
        }

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            val cx =
                width / 2f

            val cy =
                height / 2f

            val radius =
                minOf(
                    width,
                    height
                ) * 0.30f

            // -------------------------------------------------
            // OUTER GLOW
            // -------------------------------------------------

            paint.style =
                Paint.Style.FILL

            for (i in 5 downTo 1) {

                paint.shader =
                    RadialGradient(
                        cx,
                        cy,
                        radius * i,
                        intArrayOf(
                            Color.argb(
                                35,
                                70,
                                180,
                                255
                            ),
                            Color.TRANSPARENT
                        ),
                        null,
                        Shader.TileMode.CLAMP
                    )

                canvas.drawCircle(
                    cx,
                    cy,
                    radius * i,
                    paint
                )
            }

            // -------------------------------------------------
            // MAIN ORB
            // -------------------------------------------------

            paint.shader =
                RadialGradient(
                    cx -
                        radius * 0.3f,
                    cy -
                        radius * 0.3f,
                    radius * 1.4f,
                    intArrayOf(
                        Color.rgb(
                            130,
                            230,
                            255
                        ),
                        Color.rgb(
                            70,
                            90,
                            230
                        ),
                        Color.rgb(
                            30,
                            20,
                            90
                        )
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )

            canvas.drawCircle(
                cx,
                cy,
                radius,
                paint
            )

            // -------------------------------------------------
            // ROTATING RINGS
            // -------------------------------------------------

            paint.shader = null

            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth = 3f

            paint.color =
                Color.rgb(
                    90,
                    210,
                    255
                )

            canvas.save()

            canvas.rotate(
                rotation,
                cx,
                cy
            )

            canvas.drawArc(
                cx - radius * 1.18f,
                cy - radius * 1.18f,
                cx + radius * 1.18f,
                cy + radius * 1.18f,
                20f,
                100f,
                false,
                paint
            )

            canvas.drawArc(
                cx - radius * 1.32f,
                cy - radius * 1.32f,
                cx + radius * 1.32f,
                cy + radius * 1.32f,
                190f,
                70f,
                false,
                paint
            )

            canvas.restore()

            // -------------------------------------------------
            // AURIX TEXT
            // -------------------------------------------------

            paint.style =
                Paint.Style.FILL

            paint.shader = null

            paint.textAlign =
                Paint.Align.CENTER

            paint.typeface =
                Typeface.DEFAULT_BOLD

            paint.textSize =
                radius * 0.27f

            paint.color =
                Color.WHITE

            canvas.drawText(
                "AURIX",
                cx,
                cy +
                    radius * 0.08f,
                paint
            )

            paint.textSize =
                radius * 0.09f

            paint.color =
                Color.rgb(
                    160,
                    220,
                    255
                )

            canvas.drawText(
                "CORE",
                cx,
                cy +
                    radius * 0.32f,
                paint
            )
        }
    }
}
