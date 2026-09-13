package com.example.myaiassistant

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlin.math.min

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var voiceButton: TextView
    private lateinit var voiceStatus: TextView
    private lateinit var conversationBox: LinearLayout

    companion object {
        private const val REQUEST_AUDIO = 1001
    }

    private var listening = false

    private val statusReceiver = object : BroadcastReceiver() {

        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {

            if (intent?.action != AurixService.ACTION_EVENT) {
                return
            }

            when (
                intent.getStringExtra(
                    AurixService.EXTRA_TYPE
                )
            ) {

                AurixService.TYPE_STATUS -> {

                    when (
                        intent.getStringExtra(
                            AurixService.EXTRA_TEXT
                        )
                    ) {

                        "LISTENING",
                        "HEY AURIX READY" -> {
                            setListeningState()
                        }

                        "THINKING" -> {
                            setThinkingState()
                        }

                        "READY" -> {
                            setReadyState()
                        }

                        else -> Unit
                    }
                }

                AurixService.TYPE_COMMAND -> {

                    val command =
                        intent.getStringExtra(
                            AurixService.EXTRA_TEXT
                        )
                            ?.trim()
                            .orEmpty()

                    if (command.isNotBlank()) {
                        addUserMessage(command)
                    }
                }

                AurixService.TYPE_SPEAK -> {

                    val response =
                        intent.getStringExtra(
                            AurixService.EXTRA_TEXT
                        )
                            ?.trim()
                            .orEmpty()

                    if (response.isNotBlank()) {
                        addAurixMessage(response)
                    }
                }
            }
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        buildInterface()

        ContextCompat.registerReceiver(
            this,
            statusReceiver,
            IntentFilter(
                AurixService.ACTION_EVENT
            ),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        checkMicrophonePermission()
    }

    override fun onDestroy() {

        try {
            unregisterReceiver(
                statusReceiver
            )
        } catch (_: Exception) {
        }

        super.onDestroy()
    }

    private fun buildInterface() {

        root = LinearLayout(this).apply {

            orientation =
                LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(14),
                dp(18),
                dp(10)
            )

            background =
                GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(
                        Color.rgb(
                            5,
                            8,
                            28
                        ),
                        Color.rgb(
                            9,
                            8,
                            35
                        ),
                        Color.rgb(
                            4,
                            12,
                            30
                        )
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

    private fun buildHeader() {

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        val menu =
            TextView(this).apply {

                text = "☰"

                textSize = 24f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.WHITE
                )

                setOnClickListener {
                    openMore()
                }
            }

        header.addView(
            menu,
            LinearLayout.LayoutParams(
                dp(42),
                dp(48)
            )
        )

        val brandBox =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        val brand =
            TextView(this).apply {

                text =
                    "A U R I X"

                textSize = 21f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.WHITE
                )
            }

        val tagline =
            TextView(this).apply {

                text =
                    "INTELLIGENCE CORE"

                textSize = 8f

                letterSpacing =
                    0.18f

                setTextColor(
                    Color.rgb(
                        120,
                        190,
                        255
                    )
                )
            }

        brandBox.addView(
            brand
        )

        brandBox.addView(
            tagline
        )

        header.addView(
            brandBox,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val online =
            TextView(this).apply {

                text =
                    "● ONLINE"

                textSize = 9f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.rgb(
                        80,
                        220,
                        255
                    )
                )

                gravity =
                    Gravity.CENTER
            }

        header.addView(
            online,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(40)
            )
        )

        voiceButton =
            TextView(this).apply {

                text = "◉"

                textSize = 22f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.WHITE
                )

                background =
                    createVoiceBackground(
                        false
                    )

                elevation =
                    dp(8).toFloat()

                setOnClickListener {
                    activateVoice()
                }
            }

        val micParams =
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )

        micParams.marginStart =
            dp(10)

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

    private fun buildTitle() {

        root.addView(
            TextView(this).apply {

                text =
                    "A U R I X  |  INTELLIGENCE CORE"

                textSize = 9f

                letterSpacing =
                    0.20f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        100,
                        190,
                        255
                    )
                )

                setPadding(
                    0,
                    dp(12),
                    0,
                    dp(3)
                )
            }
        )
    }

    private fun buildCore() {

        val container =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER
            }

        container.addView(
            AurixOrbView(this),
            LinearLayout.LayoutParams(
                dp(220),
                dp(220)
            )
        )

        voiceStatus =
            TextView(this).apply {

                text =
                    "VOICE  •  READY"

                textSize = 10f

                letterSpacing =
                    0.18f

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.rgb(
                        90,
                        210,
                        255
                    )
                )
            }

        container.addView(
            voiceStatus,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(34)
            )
        )

        statusText =
            TextView(this).apply {

                text =
                    "Tap the AURIX voice icon to speak"

                textSize = 10f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        145,
                        155,
                        190
                    )
                )
            }

        container.addView(
            statusText
        )

        root.addView(
            container,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(285)
            )
        )
    }

    private fun buildConversation() {

        conversationBox =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        addAurixMessage(
            "AURIX ready. Tap the voice icon to speak."
        )

        val scroll =
            ScrollView(this).apply {

                isVerticalScrollBarEnabled =
                    false

                addView(
                    conversationBox
                )
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
                Color.rgb(
                    125,
                    95,
                    255
                )
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
                Color.rgb(
                    60,
                    205,
                    255
                )
            )
        )
    }

    private fun messageCard(
        title: String,
        message: String,
        accent: Int
    ): LinearLayout {

        val box =
            LinearLayout(this).apply {

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
                                Color.red(
                                    accent
                                ),
                                Color.green(
                                    accent
                                ),
                                Color.blue(
                                    accent
                                )
                            )
                        )
                    }
            }

        box.addView(
            TextView(this).apply {

                text =
                    title

                textSize = 8f

                letterSpacing =
                    0.18f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    accent
                )
            }
        )

        box.addView(
            TextView(this).apply {

                text =
                    message

                textSize = 12f

                setTextColor(
                    Color.WHITE
                )

                setPadding(
                    0,
                    dp(4),
                    0,
                    0
                )
            }
        )

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(
            0,
            dp(4),
            0,
            dp(4)
        )

        box.layoutParams =
            params

        return box
    }

    private fun buildQuickActions() {

        root.addView(
            TextView(this).apply {

                text =
                    "QUICK ACTIONS"

                textSize = 9f

                letterSpacing =
                    0.18f

                setTextColor(
                    Color.rgb(
                        110,
                        170,
                        230
                    )
                )

                setPadding(
                    0,
                    dp(7),
                    0,
                    dp(6)
                )
            }
        )

        val row1 =
            LinearLayout(this)

        addAction(
            row1,
            "YouTube"
        ) {
            openYouTube()
        }

        addAction(
            row1,
            "Search"
        ) {
            openSearch()
        }

        addAction(
            row1,
            "Music"
        ) {
            openMusic()
        }

        addAction(
            row1,
            "Weather"
        ) {
            openWeather()
        }

        root.addView(
            row1
        )

        val row2 =
            LinearLayout(this)

        addAction(
            row2,
            "Call"
        ) {
            openDialer()
        }

        addAction(
            row2,
            "Messages"
        ) {
            openMessages()
        }

        addAction(
            row2,
            "Apps"
        ) {
            openApps()
        }

        addAction(
            row2,
            "More"
        ) {
            openMore()
        }

        root.addView(
            row2
        )
    }

    private fun addAction(
        row: LinearLayout,
        label: String,
        action: () -> Unit
    ) {

        val button =
            TextView(this).apply {

                text =
                    label

                textSize = 9f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.WHITE
                )

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

                setOnClickListener {
                    action()
                }
            }

        val params =
            LinearLayout.LayoutParams(
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

    private fun buildBottomNavigation() {

        val nav =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dp(8),
                    0,
                    0
                )
            }

        addNavItem(
            nav,
            "Home"
        ) {
            setReadyState()
        }

        addNavItem(
            nav,
            "History"
        ) {
            statusText.text =
                "Conversation history"
        }

        addNavItem(
            nav,
            "AURIX"
        ) {
            setReadyState()
        }

        addNavItem(
            nav,
            "Shortcuts"
        ) {
            statusText.text =
                "Quick actions are ready"
        }

        addNavItem(
            nav,
            "Settings"
        ) {
            openMore()
        }

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
        label: String,
        action: () -> Unit
    ) {

        nav.addView(
            TextView(this).apply {

                text =
                    label

                textSize = 8f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    if (
                        label == "AURIX"
                    ) {
                        Color.rgb(
                            90,
                            210,
                            255
                        )
                    } else {
                        Color.rgb(
                            130,
                            140,
                            175
                        )
                    }
                )

                setOnClickListener {
                    action()
                }
            },
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )
        )
    }

    private fun activateVoice() {

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

        voiceButton.text =
            "●"

        voiceButton.background =
            createVoiceBackground(
                true
            )

        voiceStatus.text =
            "VOICE  •  LISTENING"

        statusText.text =
            "Listening for your command..."

        val intent =
            Intent(
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
    }

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

        if (
            requestCode != REQUEST_AUDIO
        ) {
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

            voiceButton.text =
                "◉"

            voiceButton.background =
                createVoiceBackground(
                    false
                )
        }
    }

    private fun setListeningState() {

        listening = true

        voiceButton.text =
            "●"

        voiceButton.background =
            createVoiceBackground(
                true
            )

        voiceStatus.text =
            "VOICE  •  LISTENING"

        statusText.text =
            "Listening for your command..."
    }

    private fun setThinkingState() {

        listening = true

        voiceButton.text =
            "●"

        voiceButton.background =
            createVoiceBackground(
                true
            )

        voiceStatus.text =
            "VOICE  •  THINKING"

        statusText.text =
            "Processing your command..."
    }

    private fun setReadyState() {

        listening = false

        voiceButton.text =
            "◉"

        voiceButton.background =
            createVoiceBackground(
                false
            )

        voiceStatus.text =
            "VOICE  •  READY"

        statusText.text =
            "Tap the AURIX voice icon to speak"
    }

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

    private fun openYouTube() {

        try {

            startActivity(
                packageIntent(
                    "com.google.android.youtube"
                )
            )

        } catch (_: Exception) {

            openUrl(
                "https://www.youtube.com"
            )
        }
    }

    private fun openSearch() {

        openUrl(
            "https://www.google.com/search?q="
        )
    }

    private fun openMusic() {

        try {

            startActivity(
                Intent.makeMainSelectorActivity(
                    Intent.ACTION_MAIN,
                    Intent.CATEGORY_APP_MUSIC
                )
            )

        } catch (_: Exception) {

            openUrl(
                "https://music.youtube.com"
            )
        }
    }

    private fun openWeather() {

        openUrl(
            "https://www.google.com/search?q=weather"
        )
    }

    private fun openDialer() {

        startActivity(
            Intent(
                Intent.ACTION_DIAL
            )
        )
    }

    private fun openMessages() {

        startActivity(
            Intent(
                Intent.ACTION_SENDTO
            ).apply {

                data =
                    Uri.parse(
                        "smsto:"
                    )
            }
        )
    }

    private fun openApps() {

        startActivity(
            Intent(
                Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS
            )
        )
    }

    private fun openMore() {

        startActivity(
            Intent(
                Settings.ACTION_SETTINGS
            )
        )
    }

    private fun packageIntent(
        packageName: String
    ): Intent {

        return packageManager
            .getLaunchIntentForPackage(
                packageName
            )
            ?: Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://www.youtube.com"
                )
            )
    }

    private fun openUrl(
        url: String
    ) {

        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    }

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }

    class AurixOrbView(
        context: Context
    ) : View(context) {

        private val paint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            )

        private var rotation =
            0f

        private val animator =
            ValueAnimator.ofFloat(
                0f,
                360f
            ).apply {

                duration =
                    5000

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

            setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
            )

            animator.start()
        }

        override fun onDetachedFromWindow() {

            animator.cancel()

            super.onDetachedFromWindow()
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
                min(
                    width,
                    height
                ) * 0.30f

            // Transparent canvas:
            // no square panel behind orb.

            paint.shader =
                null

            paint.style =
                Paint.Style.FILL

            paint.color =
                Color.TRANSPARENT

            canvas.drawColor(
                Color.TRANSPARENT,
                PorterDuff.Mode.CLEAR
            )

            for (
                i in 5 downTo 1
            ) {

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

            paint.shader =
                null

            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth =
                3f

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
                cx -
                    radius * 1.18f,
                cy -
                    radius * 1.18f,
                cx +
                    radius * 1.18f,
                cy +
                    radius * 1.18f,
                20f,
                100f,
                false,
                paint
            )

            canvas.drawArc(
                cx -
                    radius * 1.32f,
                cy -
                    radius * 1.32f,
                cx +
                    radius * 1.32f,
                cy +
                    radius * 1.32f,
                190f,
                70f,
                false,
                paint
            )

            canvas.restore()

            paint.style =
                Paint.Style.FILL

            paint.shader =
                null

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
