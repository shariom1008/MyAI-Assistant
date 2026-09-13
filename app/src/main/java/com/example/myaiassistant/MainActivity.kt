package com.example.myaiassistant

import android.Manifest
import android.animation.ValueAnimator
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var voiceButton: TextView
    private lateinit var voiceStatus: TextView
    private lateinit var waveform: WaveformView

    private var listening = false

    companion object {
        private const val REQUEST_AUDIO = 1001
    }

    private val serviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {

                AurixService.ACTION_STATUS -> {
                    val status =
                        intent.getStringExtra("status") ?: "READY"

                    updateFromService(status)
                }

                AurixService.ACTION_RESPONSE -> {
                    val response =
                        intent.getStringExtra("response") ?: ""

                    if (response.isNotBlank()) {
                        addAurixMessage(response)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AurixService.isRunning) {
            AurixService.stop(this)
        }

        buildInterface()

        val filter = IntentFilter().apply {
            addAction(AurixService.ACTION_STATUS)
            addAction(AurixService.ACTION_RESPONSE)
        }

        ContextCompat.registerReceiver(
            this,
            serviceReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        checkMicrophonePermission()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(serviceReceiver)
        } catch (_: Exception) {
        }

        super.onDestroy()
    }

    // =========================================================
    // UI
    // =========================================================

    private fun buildInterface() {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
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

        val menu = TextView(this).apply {
            text = "☰"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        header.addView(
            menu,
            LinearLayout.LayoutParams(dp(42), dp(48))
        )

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
            setTextColor(Color.rgb(120, 190, 255))
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

        val online = TextView(this).apply {
            text = "● ONLINE"
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(80, 220, 255))
            gravity = Gravity.CENTER
        }

        header.addView(
            online,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(40)
            )
        )

        // -----------------------------------------------------
        // SMALL SMART VOICE BUTTON
        // -----------------------------------------------------

        voiceButton = TextView(this).apply {

            text = "◉"

            textSize = 23f

            gravity = Gravity.CENTER

            setTextColor(Color.WHITE)

            background = createVoiceBackground(false)

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
            text = "A U R I X  |  INTELLIGENCE CORE"
            textSize = 10f
            letterSpacing = 0.22f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(100, 190, 255))
            setPadding(0, dp(14), 0, dp(4))
        }

        root.addView(title)
    }

    // =========================================================
    // CORE
    // =========================================================

    private fun buildCore() {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val orb = AurixOrbView(this)

        container.addView(
            orb,
            LinearLayout.LayoutParams(
                dp(230),
                dp(230)
            )
        )

        voiceStatus = TextView(this).apply {
            text = "VOICE  •  READY"
            textSize = 10f
            letterSpacing = 0.20f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(90, 210, 255))
        }

        container.addView(
            voiceStatus,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(35)
            )
        )

        statusText = TextView(this).apply {
            text = "Tap the AURIX voice icon to speak"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(145, 155, 190))
        }

        container.addView(statusText)

        root.addView(
            container,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(300)
            )
        )
    }

    // =========================================================
    // CONVERSATION
    // =========================================================

    private lateinit var conversationBox: LinearLayout

    private fun buildConversation() {

        conversationBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        addUserMessage(
            "Hey Aurix, Man Bharrya song chalao."
        )

        addAurixMessage(
            "Ready. Tap the voice icon and give me a command."
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

    private fun addUserMessage(text: String) {

        val card = messageCard(
            "YOU",
            text,
            Color.rgb(120, 90, 255)
        )

        conversationBox.addView(card)
    }

    private fun addAurixMessage(text: String) {

        val card = messageCard(
            "AURIX",
            text,
            Color.rgb(60, 200, 255)
        )

        conversationBox.addView(card)
    }

    private fun messageCard(
        title: String,
        message: String,
        accent: Int
    ): LinearLayout {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
            )

            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.argb(80, 20, 28, 60))
                setStroke(dp(1), Color.argb(90, accent))
            }
        }

        val label = TextView(this).apply {
            text = title
            textSize = 8f
            letterSpacing = 0.18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(accent)
        }

        val text = TextView(this).apply {
            this.text = message
            textSize = 12f
            setTextColor(Color.WHITE)
            setPadding(0, dp(4), 0, 0)
        }

        box.addView(label)
        box.addView(text)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, dp(5), 0, dp(5))

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
            setTextColor(Color.rgb(110, 170, 230))
            setPadding(0, dp(8), 0, dp(8))
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
        text: String
    ) {

        val button = TextView(this).apply {

            this.text = text

            textSize = 9f

            gravity = Gravity.CENTER

            setTextColor(Color.WHITE)

            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                setColor(Color.argb(65, 35, 45, 85))
                setStroke(
                    dp(1),
                    Color.argb(70, 90, 160, 255)
                )
            }

            setOnClickListener {

                when (text) {

                    "YouTube" ->
                        sendCommand("YouTube kholo")

                    "Search" ->
                        sendCommand("search kholo")

                    "Music" ->
                        sendCommand("music kholo")

                    "Weather" ->
                        sendCommand("weather batao")

                    "Call" ->
                        sendCommand("phone kholo")

                    "Messages" ->
                        sendCommand("messages kholo")

                    "Apps" ->
                        sendCommand("apps kholo")

                    "More" ->
                        sendCommand("settings kholo")
                }
            }
        }

        val params = LinearLayout.LayoutParams(
            0,
            dp(42),
            1f
        )

        params.setMargins(
            dp(3),
            dp(3),
            dp(3),
            dp(3)
        )

        row.addView(button, params)
    }

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private fun buildBottomNavigation() {

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
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
                dp(55)
            )
        )
    }

    private fun addNavItem(
        nav: LinearLayout,
        text: String
    ) {

        val item = TextView(this).apply {
            this.text = text
            textSize = 8f
            gravity = Gravity.CENTER
            setTextColor(
                if (text == "AURIX")
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
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_AUDIO
            )
            return
        }

        listening = true

        voiceButton.background =
            createVoiceBackground(true)

        voiceButton.text = "●"

        voiceStatus.text =
            "VOICE  •  LISTENING"

        statusText.text =
            "Listening for your command..."

        startAurixListening()
    }

    private fun startAurixListening() {

        val intent = Intent(
            this,
            AurixService::class.java
        ).apply {
            action = AurixService.ACTION_LISTEN_ONCE
        }

        ContextCompat.startForegroundService(
            this,
            intent
        )
    }

    // =========================================================
    // SERVICE STATUS
    // =========================================================

    private fun updateFromService(status: String) {

        when (status.uppercase()) {

            "LISTENING" -> {

                listening = true

                voiceButton.text = "●"

                voiceButton.background =
                    createVoiceBackground(true)

                voiceStatus.text =
                    "VOICE  •  LISTENING"

                statusText.text =
                    "Listening..."
            }

            "PROCESSING" -> {

                voiceStatus.text =
                    "VOICE  •  PROCESSING"

                statusText.text =
                    "AURIX is processing..."
            }

            "READY",
            "IDLE",
            "STOPPED",
            "COMPLETED" -> {

                listening = false

                voiceButton.text = "◉"

                voiceButton.background =
                    createVoiceBackground(false)

                voiceStatus.text =
                    "VOICE  •  READY"

                statusText.text =
                    "Tap the AURIX voice icon to speak"
            }

            else -> {

                statusText.text = status

            }
        }
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
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_AUDIO
            )

        } else {

            // IMPORTANT:
            // Permission milne ke baad service start nahi hogi.
            // User ko manually small voice icon tap karna hoga.

            listening = false

            voiceStatus.text =
                "VOICE  •  READY"

            statusText.text =
                "Tap the AURIX voice icon to speak"
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

        if (requestCode == REQUEST_AUDIO) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                listening = false

                voiceButton.background =
                    createVoiceBackground(false)

                voiceStatus.text =
                    "VOICE  •  READY"

                statusText.text =
                    "Tap the AURIX voice icon to speak"

            } else {

                statusText.text =
                    "Microphone permission required"
            }
        }
    }

    // =========================================================
    // QUICK COMMAND
    // =========================================================

    private fun sendCommand(command: String) {

        addUserMessage(command)

        val intent = Intent(
            this,
            AurixService::class.java
        ).apply {
            action = AurixService.ACTION_COMMAND
            putExtra("command", command)
        }

        ContextCompat.startForegroundService(
            this,
            intent
        )
    }

    // =========================================================
    // VOICE BUTTON DESIGN
    // =========================================================

    private fun createVoiceBackground(
        active: Boolean
    ): GradientDrawable {

        return GradientDrawable().apply {

            shape = GradientDrawable.OVAL

            if (active) {

                setColor(
                    Color.rgb(45, 90, 145)
                )

                setStroke(
                    dp(2),
                    Color.rgb(80, 220, 255)
                )

            } else {

                setColor(
                    Color.rgb(25, 35, 70)
                )

                setStroke(
                    dp(1),
                    Color.rgb(80, 150, 240)
                )
            }
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private fun dp(value: Int): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    // =========================================================
    // WAVEFORM
    // =========================================================

    class WaveformView(
        context: Context
    ) : View(context) {

        private val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        private var phase = 0f

        private val animator =
            ValueAnimator.ofFloat(0f, 6.28f).apply {

                duration = 1200

                repeatCount =
                    ValueAnimator.INFINITE

                addUpdateListener {

                    phase =
                        it.animatedValue as Float

                    invalidate()
                }
            }

        init {

            paint.strokeWidth = 3f
            paint.style = Paint.Style.STROKE

            animator.start()
        }

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            val width = width.toFloat()
            val height = height.toFloat()

            val centerY =
                height / 2f

            val path = Path()

            for (x in 0..width.toInt()) {

                val normalized =
                    x / width

                val y =
                    centerY +
                        sin(
                            normalized * 14 +
                                phase
                        ) * 10

                if (x == 0) {
                    path.moveTo(
                        x.toFloat(),
                        y.toFloat()
                    )
                } else {
                    path.lineTo(
                        x.toFloat(),
                        y.toFloat()
                    )
                }
            }

            canvas.drawPath(
                path,
                paint
            )
        }
    }

    // =========================================================
    // AURIX ORB
    // =========================================================

    class AurixOrbView(
        context: Context
    ) : View(context) {

        private val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private var rotation = 0f

        private val animator =
            ValueAnimator.ofFloat(0f, 360f).apply {

                duration = 5000

                repeatCount =
                    ValueAnimator.INFINITE

                addUpdateListener {

                    rotation =
                        it.animatedValue as Float

                    invalidate()
                }
            }

        init {
            animator.start()
        }

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            val cx = width / 2f
            val cy = height / 2f

            val radius =
                minOf(width, height) * 0.30f

            // Outer glow
            paint.style = Paint.Style.FILL

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

            // Main orb
            paint.shader =
                RadialGradient(
                    cx - radius * 0.3f,
                    cy - radius * 0.3f,
                    radius * 1.4f,
                    intArrayOf(
                        Color.rgb(130, 230, 255),
                        Color.rgb(70, 90, 230),
                        Color.rgb(30, 20, 90)
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

            // Rotating ring
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.setColor(
                Color.rgb(90, 210, 255)
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

            // AURIX text
            paint.style = Paint.Style.FILL
            paint.shader = null
            paint.textAlign = Paint.Align.CENTER
            paint.typeface =
                Typeface.DEFAULT_BOLD
            paint.textSize = radius * 0.27f
            paint.setColor(Color.WHITE)

            canvas.drawText(
                "AURIX",
                cx,
                cy + radius * 0.08f,
                paint
            )

            paint.textSize =
                radius * 0.09f

            paint.setColor(
                Color.rgb(160, 220, 255)
            )

            canvas.drawText(
                "CORE",
                cx,
                cy + radius * 0.32f,
                paint
            )
        }
    }
}
