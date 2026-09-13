package com.example.myaiassistant

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var statusText: TextView
    private lateinit var coreText: TextView
    private lateinit var systemText: TextView
    private lateinit var waveform: WaveformView
    private lateinit var voiceButton: TextView

    private var active = false

    private lateinit var auth: FirebaseAuth

    // =========================================================
    // COLORS
    // =========================================================

    private val bgTop = Color.rgb(2, 12, 28)
    private val bgBottom = Color.rgb(9, 2, 25)

    private val cyan = Color.rgb(80, 220, 255)
    private val blue = Color.rgb(45, 120, 255)
    private val purple = Color.rgb(145, 55, 240)

    private val white = Color.WHITE
    private val muted = Color.rgb(105, 155, 190)

    // =========================================================
    // AURIX EVENT RECEIVER
    // =========================================================

    private val aurixReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                if (
                    intent?.action !=
                    AurixService.ACTION_EVENT
                ) {
                    return
                }

                val type =
                    intent.getStringExtra(
                        AurixService.EXTRA_TYPE
                    )

                val text =
                    intent.getStringExtra(
                        AurixService.EXTRA_TEXT
                    ) ?: ""

                when (type) {

                    AurixService.TYPE_STATUS -> {
                        updateStatus(text)
                    }

                    AurixService.TYPE_COMMAND -> {
                        updateStatus("THINKING")
                    }

                    AurixService.TYPE_SPEAK -> {
                        updateStatus("RESPONDING")
                    }
                }
            }
        }

    // =========================================================
    // GO HOME RECEIVER
    // =========================================================

    private val homeReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {

                if (
                    intent?.action !=
                    "com.example.myaiassistant.GO_HOME"
                ) {
                    return
                }

                goToHomeScreen()
            }
        }

    // =========================================================
    // CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        auth =
            FirebaseAuth.getInstance()

        if (
            auth.currentUser == null
        ) {

            startActivity(
                Intent(
                    this,
                    AuthActivity::class.java
                )
            )

            finish()

            return
        }

        window.statusBarColor =
            Color.TRANSPARENT

        window.navigationBarColor =
            Color.BLACK

        createInterface()

        registerAurixReceiver()

        registerReceiver(
            homeReceiver,
            IntentFilter(
                "com.example.myaiassistant.GO_HOME"
            ),
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {
                Context.RECEIVER_NOT_EXPORTED
            } else {
                0
            }
        )

        requestMicrophonePermission()

        updateInterface()
    }

    // =========================================================
    // MICROPHONE PERMISSION
    // =========================================================

    private fun requestMicrophonePermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                500
            )

        } else {

            // IMPORTANT:
            // Permission milne ke baad AURIX
            // automatically listening start nahi karega.

            active = false

            updateStatus("READY")

            updateInterface()
        }
    }

    // =========================================================
    // PERMISSION RESULT
    // =========================================================

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
            requestCode == 500
        ) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                active = false

                updateStatus("READY")

                updateInterface()

            } else {

                active = false

                updateStatus(
                    "MIC PERMISSION REQUIRED"
                )

                updateInterface()
            }
        }
    }

    // =========================================================
    // MAIN UI
    // =========================================================

    private fun createInterface() {

        root =
            FrameLayout(this)

        root.background =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    bgTop,
                    Color.rgb(1, 5, 16),
                    bgBottom
                )
            )

        setContentView(root)

        // =====================================================
        // SCROLL
        // =====================================================

        val scroll =
            ScrollView(this)

        scroll.isFillViewport =
            true

        scroll.overScrollMode =
            View.OVER_SCROLL_NEVER

        root.addView(
            scroll,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val content =
            LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        content.gravity =
            Gravity.CENTER_HORIZONTAL

        content.setPadding(
            dp(18),
            dp(24),
            dp(18),
            dp(28)
        )

        scroll.addView(
            content,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // =====================================================
        // HEADER
        // =====================================================

        val header =
            LinearLayout(this)

        header.orientation =
            LinearLayout.HORIZONTAL

        header.gravity =
            Gravity.CENTER_VERTICAL

        content.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        val menu =
            TextView(this)

        menu.text =
            "☰"

        menu.textSize =
            25f

        menu.setTextColor(
            white
        )

        menu.gravity =
            Gravity.CENTER

        header.addView(
            menu,
            LinearLayout.LayoutParams(
                dp(45),
                dp(50)
            )
        )

        val brand =
            LinearLayout(this)

        brand.orientation =
            LinearLayout.VERTICAL

        header.addView(
            brand,
            LinearLayout.LayoutParams(
                0,
                dp(54),
                1f
            )
        )

        val title =
            TextView(this)

        title.text =
            "AURIX"

        title.textSize =
            25f

        title.setTextColor(
            white
        )

        title.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        title.letterSpacing =
            0.16f

        brand.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(31)
            )
        )

        val tagline =
            TextView(this)

        tagline.text =
            "YOUR VOICE  |  YOUR AI"

        tagline.textSize =
            8f

        tagline.setTextColor(
            muted
        )

        tagline.letterSpacing =
            0.16f

        brand.addView(
            tagline,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(20)
            )
        )

        val online =
            TextView(this)

        online.text =
            "● ONLINE"

        online.textSize =
            10f

        online.setTextColor(
            Color.rgb(
                90,
                235,
                145
            )
        )

        online.gravity =
            Gravity.CENTER

        header.addView(
            online,
            LinearLayout.LayoutParams(
                dp(82),
                dp(40)
            )
        )

        // =====================================================
        // CORE LABEL
        // =====================================================

        val coreLabel =
            TextView(this)

        coreLabel.text =
            "A U R I X   |   INTELLIGENCE CORE"

        coreLabel.textSize =
            9f

        coreLabel.setTextColor(
            Color.rgb(
                100,
                175,
                225
            )
        )

        coreLabel.gravity =
            Gravity.CENTER

        coreLabel.letterSpacing =
            0.18f

        val labelParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )

        labelParams.topMargin =
            dp(5)

        content.addView(
            coreLabel,
            labelParams
        )

        // =====================================================
        // CORE AREA
        // =====================================================

        val coreArea =
            FrameLayout(this)

        content.addView(
            coreArea,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(300)
            )
        )

        // =====================================================
        // OUTER GLOW
        // =====================================================

        val glow =
            View(this)

        glow.background =
            ovalGradient(
                intArrayOf(
                    Color.rgb(
                        15,
                        65,
                        170
                    ),
                    Color.rgb(
                        125,
                        35,
                        215
                    ),
                    Color.rgb(
                        20,
                        150,
                        215
                    )
                )
            )

        val glowSize =
            dp(235)

        val glowLayout =
            FrameLayout.LayoutParams(
                glowSize,
                glowSize
            )

        glowLayout.gravity =
            Gravity.CENTER

        coreArea.addView(
            glow,
            glowLayout
        )

        // =====================================================
        // DARK RING
        // =====================================================

        val darkRing =
            View(this)

        darkRing.background =
            ovalGradient(
                intArrayOf(
                    Color.rgb(
                        3,
                        18,
                        48
                    ),
                    Color.rgb(
                        13,
                        5,
                        38
                    ),
                    Color.rgb(
                        3,
                        34,
                        52
                    )
                )
            )

        val darkSize =
            dp(210)

        val darkLayout =
            FrameLayout.LayoutParams(
                darkSize,
                darkSize
            )

        darkLayout.gravity =
            Gravity.CENTER

        coreArea.addView(
            darkRing,
            darkLayout
        )

        // =====================================================
        // CORE ORB
        // =====================================================

        val orb =
            View(this)

        orb.background =
            createOrb()

        val orbSize =
            dp(168)

        val orbLayout =
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            )

        orbLayout.gravity =
            Gravity.CENTER

        coreArea.addView(
            orb,
            orbLayout
        )

        // =====================================================
        // CORE TEXT
        // =====================================================

        coreText =
            TextView(this)

        coreText.text =
            "AURIX"

        coreText.textSize =
            23f

        coreText.setTextColor(
            white
        )

        coreText.gravity =
            Gravity.CENTER

        coreText.typeface =
            Typeface.DEFAULT_BOLD

        coreText.letterSpacing =
            0.18f

        val coreTextLayout =
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            )

        coreTextLayout.gravity =
            Gravity.CENTER

        coreArea.addView(
            coreText,
            coreTextLayout
        )

        // =====================================================
        // CORE ANIMATION
        // =====================================================

        glow.animate()
            .rotationBy(360f)
            .setDuration(9000)
            .withEndAction {
                rotateCore(glow)
            }
            .start()

        pulseCore(orb)

        // =====================================================
        // VOICE READY BADGE
        // =====================================================

        val listening =
            TextView(this)

        listening.text =
            "  ●  VOICE  |  READY  "

        listening.textSize =
            11f

        listening.setTextColor(
            cyan
        )

        listening.gravity =
            Gravity.CENTER

        listening.letterSpacing =
            0.08f

        listening.background =
            roundedBackground(
                Color.argb(
                    45,
                    60,
                    190,
                    255
                ),
                cyan
            )

        val listeningParams =
            LinearLayout.LayoutParams(
                dp(210),
                dp(38)
            )

        listeningParams.topMargin =
            dp(-12)

        content.addView(
            listening,
            listeningParams
        )

        // =====================================================
        // SYSTEM
        // =====================================================

        systemText =
            TextView(this)

        systemText.text =
            "SYSTEM ONLINE"

        systemText.textSize =
            9f

        systemText.setTextColor(
            muted
        )

        systemText.gravity =
            Gravity.CENTER

        systemText.letterSpacing =
            0.20f

        content.addView(
            systemText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(26)
            )
        )

        // =====================================================
        // STATUS
        // =====================================================

        statusText =
            TextView(this)

        statusText.text =
            "READY"

        statusText.textSize =
            14f

        statusText.setTextColor(
            cyan
        )

        statusText.gravity =
            Gravity.CENTER

        statusText.typeface =
            Typeface.DEFAULT_BOLD

        statusText.letterSpacing =
            0.20f

        content.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(34)
            )
        )

        // =====================================================
        // WAVEFORM
        // =====================================================

        waveform =
            WaveformView(this)

        val waveformParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            )

        waveformParams.topMargin =
            dp(2)

        content.addView(
            waveform,
            waveformParams
        )

        // =====================================================
        // CONVERSATION
        // =====================================================

        addSectionTitle(
            content,
            "CONVERSATION"
        )

        val emptyConversation =
            createMessageCard(
                "AURIX",
                "Ready when you are. Tap the voice bar to speak.",
                true
            )

        content.addView(
            emptyConversation,
            cardParams()
        )

        // =====================================================
        // QUICK ACTIONS
        // =====================================================

        addSectionTitle(
            content,
            "QUICK ACTIONS"
        )

        val actions =
            LinearLayout(this)

        actions.orientation =
            LinearLayout.VERTICAL

        content.addView(
            actions,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // -----------------------------------------------------
        // ROW 1
        // -----------------------------------------------------

        val row1 =
            LinearLayout(this)

        row1.orientation =
            LinearLayout.HORIZONTAL

        actions.addView(
            row1,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(76)
            )
        )

        addAction(
            row1,
            "▶",
            "YouTube"
        ) {

            openUrl(
                "https://www.youtube.com"
            )
        }

        addAction(
            row1,
            "⌕",
            "Search"
        ) {

            openUrl(
                "https://www.google.com"
            )
        }

        addAction(
            row1,
            "♪",
            "Music"
        ) {

            openUrl(
                "https://music.youtube.com"
            )
        }

        addAction(
            row1,
            "☁",
            "Weather"
        ) {

            openUrl(
                "https://www.google.com/search?q=weather"
            )
        }

        // -----------------------------------------------------
        // ROW 2
        // -----------------------------------------------------

        val row2 =
            LinearLayout(this)

        row2.orientation =
            LinearLayout.HORIZONTAL

        actions.addView(
            row2,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(76)
            )
        )

        addAction(
            row2,
            "☎",
            "Call"
        ) {

            startActivity(
                Intent(
                    Intent.ACTION_DIAL
                )
            )
        }

        addAction(
            row2,
            "✉",
            "Messages"
        ) {

            val intent =
                Intent(
                    Intent.ACTION_SENDTO
                )

            intent.data =
                Uri.parse(
                    "smsto:"
                )

            startActivity(intent)
        }

        addAction(
            row2,
            "▦",
            "Apps"
        ) {

            try {

                startActivity(
                    Intent(
                        android.provider.Settings
                            .ACTION_SETTINGS
                    )
                )

            } catch (_: Exception) {
            }
        }

        addAction(
            row2,
            "•••",
            "More"
        ) {

            updateStatus(
                "READY"
            )
        }

        // =====================================================
        // VOICE INPUT
        // =====================================================

        voiceButton =
            TextView(this)

        voiceButton.tag =
            "AURIX_VOICE_BUTTON"

        voiceButton.text =
            "  🎙   Tap to activate AURIX"

        voiceButton.textSize =
            13f

        voiceButton.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        voiceButton.setTextColor(
            white
        )

        voiceButton.gravity =
            Gravity.CENTER

        voiceButton.background =
            roundedBackground(
                Color.argb(
                    35,
                    70,
                    190,
                    255
                ),
                Color.rgb(
                    55,
                    130,
                    210
                )
            )

        voiceButton.setOnClickListener {

            activateVoice()
        }

        val inputParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )

        inputParams.topMargin =
            dp(18)

        content.addView(
            voiceButton,
            inputParams
        )

        // =====================================================
        // BOTTOM NAV
        // =====================================================

        val nav =
            LinearLayout(this)

        nav.orientation =
            LinearLayout.HORIZONTAL

        nav.gravity =
            Gravity.CENTER

        nav.background =
            roundedBackground(
                Color.argb(
                    35,
                    10,
                    40,
                    75
                ),
                Color.rgb(
                    35,
                    80,
                    125
                )
            )

        val navParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(68)
            )

        navParams.topMargin =
            dp(18)

        content.addView(
            nav,
            navParams
        )

        addNavItem(
            nav,
            "⌂",
            "Home"
        )

        addNavItem(
            nav,
            "↶",
            "History"
        )

        addNavItem(
            nav,
            "A",
            "AURIX"
        )

        addNavItem(
            nav,
            "★",
            "Shortcuts"
        )

        addNavItem(
            nav,
            "⚙",
            "Settings"
        )
    }

    // =========================================================
    // ACTIVATE VOICE
    // =========================================================

    private fun activateVoice() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            requestMicrophonePermission()

            return
        }

        val intent =
            Intent(
                this,
                AurixService::class.java
            ).apply {

                action =
                    AurixService.ACTION_LISTEN_ONCE
            }

        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                ContextCompat.startForegroundService(
                    this,
                    intent
                )

            } else {

                startService(intent)
            }

            active = true

            updateStatus(
                "LISTENING"
            )

        } catch (_: Exception) {

            active = false

            updateStatus(
                "START FAILED"
            )
        }
    }

    // =========================================================
    // MESSAGE CARD
    // =========================================================

    private fun createMessageCard(
        name: String,
        message: String,
        aurix: Boolean
    ): View {

        val box =
            LinearLayout(this)

        box.orientation =
            LinearLayout.VERTICAL

        box.setPadding(
            dp(16),
            dp(12),
            dp(16),
            dp(12)
        )

        box.background =
            roundedBackground(
                if (aurix)
                    Color.argb(
                        35,
                        75,
                        150,
                        255
                    )
                else
                    Color.argb(
                        25,
                        150,
                        70,
                        240
                    ),
                if (aurix)
                    Color.rgb(
                        45,
                        145,
                        230
                    )
                else
                    Color.rgb(
                        110,
                        70,
                        200
                    )
            )

        val label =
            TextView(this)

        label.text =
            name

        label.textSize =
            9f

        label.setTextColor(
            if (aurix)
                cyan
            else
                purple
        )

        label.typeface =
            Typeface.DEFAULT_BOLD

        label.letterSpacing =
            0.18f

        box.addView(
            label,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(22)
            )
        )

        val text =
            TextView(this)

        text.text =
            message

        text.textSize =
            14f

        text.setTextColor(
            white
        )

        box.addView(
            text,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        return box
    }

    // =========================================================
    // ACTION TILE
    // =========================================================

    private fun addAction(
        row: LinearLayout,
        icon: String,
        label: String,
        action: () -> Unit
    ) {

        val tile =
            LinearLayout(this)

        tile.orientation =
            LinearLayout.VERTICAL

        tile.gravity =
            Gravity.CENTER

        tile.background =
            roundedBackground(
                Color.argb(
                    28,
                    60,
                    110,
                    170
                ),
                Color.rgb(
                    35,
                    85,
                    125
                )
            )

        tile.setOnClickListener {
            action()
        }

        val params =
            LinearLayout.LayoutParams(
                0,
                dp(66),
                1f
            )

        params.setMargins(
            dp(3),
            dp(4),
            dp(3),
            dp(4)
        )

        row.addView(
            tile,
            params
        )

        val iconText =
            TextView(this)

        iconText.text =
            icon

        iconText.textSize =
            19f

        iconText.setTextColor(
            cyan
        )

        iconText.gravity =
            Gravity.CENTER

        tile.addView(
            iconText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )

        val labelText =
            TextView(this)

        labelText.text =
            label

        labelText.textSize =
            8f

        labelText.setTextColor(
            white
        )

        labelText.gravity =
            Gravity.CENTER

        tile.addView(
            labelText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(25)
            )
        )
    }

    // =========================================================
    // NAV ITEM
    // =========================================================

    private fun addNavItem(
        nav: LinearLayout,
        icon: String,
        label: String
    ) {

        val item =
            LinearLayout(this)

        item.orientation =
            LinearLayout.VERTICAL

        item.gravity =
            Gravity.CENTER

        val params =
            LinearLayout.LayoutParams(
                0,
                dp(64),
                1f
            )

        nav.addView(
            item,
            params
        )

        val iconText =
            TextView(this)

        iconText.text =
            icon

        iconText.textSize =
            if (
                label == "AURIX"
            )
                20f
            else
                17f

        iconText.setTextColor(
            if (
                label == "AURIX"
            )
                cyan
            else
                muted
        )

        iconText.gravity =
            Gravity.CENTER

        item.addView(
            iconText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(34)
            )
        )

        val labelText =
            TextView(this)

        labelText.text =
            label

        labelText.textSize =
            7f

        labelText.setTextColor(
            if (
                label == "AURIX"
            )
                cyan
            else
                muted
        )

        labelText.gravity =
            Gravity.CENTER

        item.addView(
            labelText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(20)
            )
        )
    }

    // =========================================================
    // SECTION TITLE
    // =========================================================

    private fun addSectionTitle(
        parent: LinearLayout,
        text: String
    ) {

        val title =
            TextView(this)

        title.text =
            text

        title.textSize =
            9f

        title.setTextColor(
            muted
        )

        title.typeface =
            Typeface.DEFAULT_BOLD

        title.letterSpacing =
            0.18f

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(30)
            )

        params.topMargin =
            dp(15)

        parent.addView(
            title,
            params
        )
    }

    // =========================================================
    // CARD PARAMS
    // =========================================================

    private fun cardParams():
        LinearLayout.LayoutParams {

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

        return params
    }

    // =========================================================
    // OVAL GRADIENT
    // =========================================================

    private fun ovalGradient(
        colors: IntArray
    ): GradientDrawable {

        val drawable =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                colors
            )

        drawable.shape =
            GradientDrawable.OVAL

        return drawable
    }

    // =========================================================
    // ORB
    // =========================================================

    private fun createOrb():
        GradientDrawable {

        val drawable =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(
                        25,
                        95,
                        220
                    ),
                    Color.rgb(
                        100,
                        35,
                        190
                    ),
                    Color.rgb(
                        10,
                        170,
                        215
                    )
                )
            )

        drawable.shape =
            GradientDrawable.OVAL

        drawable.setStroke(
            dp(2),
            Color.rgb(
                170,
                235,
                255
            )
        )

        return drawable
    }

    // =========================================================
    // ROUNDED BACKGROUND
    // =========================================================

    private fun roundedBackground(
        fill: Int,
        stroke: Int
    ): GradientDrawable {

        val drawable =
            GradientDrawable()

        drawable.cornerRadius =
            dp(18).toFloat()

        drawable.setColor(
            fill
        )

        drawable.setStroke(
            dp(1),
            stroke
        )

        return drawable
    }

    // =========================================================
    // ROTATE CORE
    // =========================================================

    private fun rotateCore(
        view: View
    ) {

        if (
            isFinishing
        ) {
            return
        }

        view.animate()
            .rotationBy(360f)
            .setDuration(9000)
            .withEndAction {
                rotateCore(view)
            }
            .start()
    }

    // =========================================================
    // PULSE CORE
    // =========================================================

    private fun pulseCore(
        view: View
    ) {

        view.animate()
            .scaleX(1.045f)
            .scaleY(1.045f)
            .setDuration(1500)
            .withEndAction {

                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1500)
                    .withEndAction {

                        if (
                            !isFinishing
                        ) {

                            pulseCore(
                                view
                            )
                        }
                    }
                    .start()
            }
            .start()
    }

    // =========================================================
    // STATUS
    // =========================================================

    private fun updateStatus(
        status: String
    ) {

        runOnUiThread {

            val clean =
                status.uppercase(
                    Locale.getDefault()
                )

            when {

                clean.contains(
                    "LISTEN"
                ) -> {

                    active = true

                    statusText.text =
                        "LISTENING"

                    coreText.text =
                        "LISTEN"

                    statusText.setTextColor(
                        cyan
                    )

                    waveform.running =
                        true

                    updateVoiceButton(
                        "  🎙   Listening... Speak now",
                        true
                    )
                }

                clean.contains(
                    "THINK"
                ) ||
                        clean.contains(
                            "PROCESS"
                        ) -> {

                    statusText.text =
                        "THINKING"

                    coreText.text =
                        "THINK"

                    statusText.setTextColor(
                        Color.rgb(
                            210,
                            145,
                            255
                        )
                    )

                    waveform.running =
                        true

                    updateVoiceButton(
                        "  ✦   Processing...",
                        true
                    )
                }

                clean.contains(
                    "EXECUT"
                ) -> {

                    statusText.text =
                        "EXECUTING"

                    coreText.text =
                        "EXEC"

                    statusText.setTextColor(
                        Color.rgb(
                            110,
                            175,
                            255
                        )
                    )

                    waveform.running =
                        true

                    updateVoiceButton(
                        "  ✦   Executing...",
                        true
                    )
                }

                clean.contains(
                    "RESPOND"
                ) ||
                        clean.contains(
                            "SPEAK"
                        ) -> {

                    statusText.text =
                        "RESPONDING"

                    coreText.text =
                        "VOICE"

                    statusText.setTextColor(
                        Color.rgb(
                            180,
                            110,
                            255
                        )
                    )

                    waveform.running =
                        true

                    updateVoiceButton(
                        "  ◉   AURIX is responding...",
                        true
                    )
                }

                else -> {

                    active = false

                    statusText.text =
                        "READY"

                    coreText.text =
                        "AURIX"

                    statusText.setTextColor(
                        cyan
                    )

                    waveform.running =
                        false

                    updateVoiceButton(
                        "  🎙   Tap to activate AURIX",
                        false
                    )
                }
            }

            updateSystemState()
        }
    }

    // =========================================================
    // VOICE BUTTON
    // =========================================================

    private fun updateVoiceButton(
        text: String,
        activeState: Boolean
    ) {

        if (
            !::voiceButton.isInitialized
        ) {
            return
        }

        voiceButton.text =
            text

        voiceButton.setTextColor(
            if (activeState)
                cyan
            else
                white
        )

        voiceButton.background =
            roundedBackground(
                if (activeState)
                    Color.argb(
                        65,
                        40,
                        150,
                        255
                    )
                else
                    Color.argb(
                        35,
                        70,
                        190,
                        255
                    ),
                if (activeState)
                    cyan
                else
                    Color.rgb(
                        55,
                        130,
                        210
                    )
            )
    }

    // =========================================================
    // INTERFACE STATE
    // =========================================================

    private fun updateInterface() {

        if (
            active
        ) {

            systemText.text =
                "SYSTEM ONLINE  |  ACTIVE"

        } else {

            systemText.text =
                "SYSTEM ONLINE"
        }

        statusText.text =
            "READY"

        coreText.text =
            "AURIX"

        waveform.running =
            false

        updateVoiceButton(
            "  🎙   Tap to activate AURIX",
            false
        )
    }

    // =========================================================
    // SYSTEM STATE
    // =========================================================

    private fun updateSystemState() {

        if (
            !::systemText.isInitialized
        ) {
            return
        }

        systemText.text =
            if (active)
                "SYSTEM ONLINE  |  ACTIVE"
            else
                "SYSTEM ONLINE"
    }

    // =========================================================
    // OPEN URL
    // =========================================================

    private fun openUrl(
        url: String
    ) {

        try {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )

        } catch (_: Exception) {
        }
    }

    // =========================================================
    // HOME
    // =========================================================

    fun goToHomeScreen() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_MAIN
                ).apply {

                    addCategory(
                        Intent.CATEGORY_HOME
                    )

                    addCategory(
                        Intent.CATEGORY_DEFAULT
                    )
                }

            startActivity(
                intent
            )

        } catch (_: Exception) {
        }
    }

    // =========================================================
    // REGISTER RECEIVER
    // =========================================================

    private fun registerAurixReceiver() {

        val filter =
            IntentFilter(
                AurixService.ACTION_EVENT
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            registerReceiver(
                aurixReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )

        } else {

            @Suppress("DEPRECATION")
            registerReceiver(
                aurixReceiver,
                filter
            )
        }
    }

    // =========================================================
    // RESUME
    // =========================================================

    override fun onResume() {

        super.onResume()

        if (
            ::statusText.isInitialized
        ) {

            if (
                !AurixService.isRunning
            ) {

                active = false

                updateStatus(
                    "READY"
                )
            }
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        try {

            unregisterReceiver(
                aurixReceiver
            )

        } catch (_: Exception) {
        }

        try {

            unregisterReceiver(
                homeReceiver
            )

        } catch (_: Exception) {
        }

        super.onDestroy()
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
    // WAVEFORM
    // =========================================================

    private class WaveformView(
        context: Context
    ) : View(context) {

        private val paint =
            android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG
            )

        private var phase =
            0f

        var running =
            false
            set(value) {

                field =
                    value

                invalidate()
            }

        private val handler =
            android.os.Handler(
                android.os.Looper.getMainLooper()
            )

        private val animator =
            object : Runnable {

                override fun run() {

                    if (
                        running
                    ) {

                        phase +=
                            0.22f

                        invalidate()
                    }

                    handler.postDelayed(
                        this,
                        40
                    )
                }
            }

        init {

            paint.strokeWidth =
                3f

            paint.strokeCap =
                android.graphics.Paint.Cap.ROUND

            handler.post(
                animator
            )
        }

        override fun onDraw(
            canvas: android.graphics.Canvas
        ) {

            super.onDraw(
                canvas
            )

            val width =
                width.toFloat()

            val center =
                height / 2f

            val count =
                25

            val gap =
                width / count

            paint.color =
                Color.rgb(
                    75,
                    200,
                    255
                )

            for (
                i in 0 until count
            ) {

                val x =
                    gap *
                        i +
                        gap / 2f

                val wave =
                    kotlin.math.sin(
                        (
                            phase +
                                i *
                                0.55f
                        ).toDouble()
                    ).toFloat()

                val heightValue =
                    if (
                        running
                    ) {

                        7f +
                            (
                                kotlin.math.abs(
                                    wave
                                ) *
                                    17f
                            )

                    } else {

                        5f
                    }

                canvas.drawLine(
                    x,
                    center -
                        heightValue,
                    x,
                    center +
                        heightValue,
                    paint
                )
            }
        }
    }
}
