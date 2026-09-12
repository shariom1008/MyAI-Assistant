package com.example.myaiassistant

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager

class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var statusText: TextView
    private lateinit var coreText: TextView
    private lateinit var systemText: TextView
    private lateinit var waveform: WaveformView

    private var active = false

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

                if (intent?.action != AurixService.ACTION_EVENT) {
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

    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.BLACK

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
        ) != PackageManager.PERMISSION_GRANTED
    ) {

        requestPermissions(
            arrayOf(
                Manifest.permission.RECORD_AUDIO
            ),
            500
        )

    } else {

        startAurixService()
    }
}

// =========================================================
// START AURIX SERVICE
// =========================================================

private fun startAurixService() {

    val intent =
        Intent(
            this,
            AurixService::class.java
        ).apply {
            action =
                AurixService.ACTION_START
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

        updateStatus("LISTENING")
        updateInterface()

    } catch (_: Exception) {

        active = false

        updateStatus("START FAILED")
        updateInterface()
    }
}

    // =========================================================
    // MAIN UI
    // =========================================================

    private fun createInterface() {

        root = FrameLayout(this)

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

        // -----------------------------------------------------
        // SCROLL CONTENT
        // -----------------------------------------------------

        val scroll =
            ScrollView(this)

        scroll.isFillViewport = true
        scroll.overScrollMode = View.OVER_SCROLL_NEVER

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
            dp(28),
            dp(18),
            dp(30)
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
                dp(52)
            )
        )

        val menu =
            TextView(this)

        menu.text = "☰"
        menu.textSize = 25f
        menu.setTextColor(white)
        menu.gravity = Gravity.CENTER

        header.addView(
            menu,
            LinearLayout.LayoutParams(
                dp(45),
                dp(48)
            )
        )

        val brand =
            LinearLayout(this)

        brand.orientation =
            LinearLayout.VERTICAL

        val brandParams =
            LinearLayout.LayoutParams(
                0,
                dp(52),
                1f
            )

        header.addView(
            brand,
            brandParams
        )

        val title =
            TextView(this)

        title.text = "AURIX"
        title.textSize = 24f
        title.setTextColor(white)
        title.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )
        title.letterSpacing = 0.14f

        brand.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )

        val tagline =
            TextView(this)

        tagline.text = "YOUR VOICE  |  YOUR AI"

        tagline.textSize = 8f
        tagline.setTextColor(muted)
        tagline.letterSpacing = 0.16f

        brand.addView(
            tagline,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(20)
            )
        )

        val online =
            TextView(this)

        online.text = "● ONLINE"
        online.textSize = 10f
        online.setTextColor(
            Color.rgb(90, 235, 145)
        )
        online.gravity = Gravity.CENTER

        header.addView(
            online,
            LinearLayout.LayoutParams(
                dp(82),
                dp(40)
            )
        )

        // =====================================================
        // INTELLIGENCE CORE LABEL
        // =====================================================

        val coreLabel =
            TextView(this)
coreLabel.text = "A U R I X   |   INTELLIGENCE CORE""

        coreLabel.textSize = 9f
        coreLabel.setTextColor(
            Color.rgb(100, 175, 225)
        )
        coreLabel.gravity = Gravity.CENTER
        coreLabel.letterSpacing = 0.18f

        val labelParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(45)
            )

        labelParams.topMargin = dp(8)

        content.addView(
            coreLabel,
            labelParams
        )

        // =====================================================
        // CORE AREA
        // =====================================================

        val coreArea =
            FrameLayout(this)

        val coreParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(330)
            )

        content.addView(
            coreArea,
            coreParams
        )

        // Outer glow
        val glow =
            View(this)

        glow.background =
            ovalGradient(
                intArrayOf(
                    Color.rgb(15, 65, 170),
                    Color.rgb(125, 35, 215),
                    Color.rgb(20, 150, 215)
                )
            )

        val glowSize = dp(245)

        val glowLayout =
            FrameLayout.LayoutParams(
                glowSize,
                glowSize
            )

        glowLayout.gravity = Gravity.CENTER

        coreArea.addView(
            glow,
            glowLayout
        )

        // Inner dark ring
        val darkRing =
            View(this)

        darkRing.background =
            ovalGradient(
                intArrayOf(
                    Color.rgb(3, 18, 48),
                    Color.rgb(13, 5, 38),
                    Color.rgb(3, 34, 52)
                )
            )

        val darkSize = dp(218)

        val darkLayout =
            FrameLayout.LayoutParams(
                darkSize,
                darkSize
            )

        darkLayout.gravity = Gravity.CENTER

        coreArea.addView(
            darkRing,
            darkLayout
        )

        // Core
        val orb =
            View(this)

        orb.background =
            createOrb()

        val orbSize = dp(176)

        val orbLayout =
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            )

        orbLayout.gravity = Gravity.CENTER

        coreArea.addView(
            orb,
            orbLayout
        )

        // Core text
        coreText =
            TextView(this)

        coreText.text = "AURIX"
        coreText.textSize = 23f
        coreText.setTextColor(white)
        coreText.gravity = Gravity.CENTER
        coreText.typeface = Typeface.DEFAULT_BOLD
        coreText.letterSpacing = 0.18f

        val coreTextLayout =
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            )

        coreTextLayout.gravity = Gravity.CENTER

        coreArea.addView(
            coreText,
            coreTextLayout
        )

        // Rotating outer ring
        glow.animate()
            .rotationBy(360f)
            .setDuration(9000)
            .withEndAction {
                rotateCore(glow)
            }
            .start()

        pulseCore(orb)

        // =====================================================
        // LISTENING BADGE
        // =====================================================

        val listening =
            TextView(this)

        listening.text = "  ●  VOICE  |  READY  "

        listening.textSize = 11f
        listening.setTextColor(cyan)
        listening.gravity = Gravity.CENTER
        listening.letterSpacing = 0.08f

        listening.background =
            roundedBackground(
                Color.argb(45, 60, 190, 255),
                cyan
            )

        val listeningParams =
            LinearLayout.LayoutParams(
                dp(210),
                dp(38)
            )

        listeningParams.topMargin = dp(-15)

        content.addView(
            listening,
            listeningParams
        )

        // =====================================================
        // STATUS
        // =====================================================

        systemText =
            TextView(this)

        systemText.text =
            "SYSTEM ONLINE"

        systemText.textSize = 9f
        systemText.setTextColor(muted)
        systemText.gravity = Gravity.CENTER
        systemText.letterSpacing = 0.20f

        content.addView(
            systemText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(28)
            )
        )

        statusText =
            TextView(this)

        statusText.text =
            "READY"

        statusText.textSize = 14f
        statusText.setTextColor(cyan)
        statusText.gravity = Gravity.CENTER
        statusText.typeface = Typeface.DEFAULT_BOLD
        statusText.letterSpacing = 0.20f

        content.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(35)
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
                dp(45)
            )

        waveformParams.topMargin = dp(3)

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

        val userCard =
            createMessageCard(
                "YOU",
                "Hey Aurix, Man Bharrya song chalao.",
                false
            )

        content.addView(
            userCard,
            cardParams()
        )

        val aurixCard =
            createMessageCard(
                "AURIX",
                "Sure! Playing Man Bharrya on YouTube.",
                true
            )

        content.addView(
            aurixCard,
            cardParams()
        )

        // =====================================================
        // MEDIA CARD
        // =====================================================

        addSectionTitle(
            content,
            "NOW PLAYING"
        )

        val media =
            createMediaCard()

        content.addView(
            media,
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

        {
        addAction(
    row1,
    "⌕",
    "Search"
)    openUrl(
                "https://www.google.com"
            )
        }

        addAction(
    row1,
    "♪",
    "Music"
){
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
            val intent =
                Intent(
                    Intent.ACTION_DIAL
                )
            startActivity(intent)
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
                Uri.parse("smsto:")
            startActivity(intent)
        }

        addAction(
    row2,
    "▦",
    "Apps"
){
            try {
                startActivity(
                    Intent(
                        android.provider.Settings.ACTION_SETTINGS
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
            updateStatus("READY")
        }

        // =====================================================
        // VOICE INPUT
        // =====================================================

        val input =
            TextView(this)

        input.text =
    "  🎙   Tap to speak to AURIX                         "                         "

        input.textSize = 12f
        input.setTextColor(white)
        input.gravity = Gravity.CENTER_VERTICAL
        input.background =
            roundedBackground(
                Color.argb(35, 70, 190, 255),
                Color.rgb(55, 130, 210)
            )

        input.setOnClickListener {

    if (
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
    ) {

        requestMicrophonePermission()

        return@setOnClickListener
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(
                this,
                intent
            )
        } else {
            startService(intent)
        }

        active = true
        updateStatus("LISTENING")
    } catch (_: Exception) {
        updateStatus("START FAILED")
    }
        }

        val inputParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
            )

        inputParams.topMargin = dp(18)

        content.addView(
            input,
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
                Color.argb(35, 10, 40, 75),
                Color.rgb(35, 80, 125)
            )

        val navParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(68)
            )

        navParams.topMargin = dp(18)

        content.addView(
            nav,
            navParams
        )

        addNavItem(nav, "⌂", "Home")
        addNavItem(nav, "↶", "History")
        addNavItem(nav, "A", "AURIX")
        addNavItem(nav, "★", "Shortcuts")
        addNavItem(nav, "⚙", "Settings")
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
                    Color.argb(35, 75, 150, 255)
                else
                    Color.argb(25, 150, 70, 240),
                if (aurix)
                    Color.rgb(45, 145, 230)
                else
                    Color.rgb(110, 70, 200)
            )

        val label =
            TextView(this)

        label.text = name
        label.textSize = 9f
        label.setTextColor(
            if (aurix) cyan else purple
        )
        label.typeface = Typeface.DEFAULT_BOLD
        label.letterSpacing = 0.18f

        box.addView(
            label,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(22)
            )
        )

        val text =
            TextView(this)

        text.text = message
        text.textSize = 14f
        text.setTextColor(white)

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
    // MEDIA CARD
    // =========================================================

    private fun createMediaCard(): View {

        val box =
            LinearLayout(this)

        box.orientation =
            LinearLayout.HORIZONTAL

        box.gravity =
            Gravity.CENTER_VERTICAL

        box.setPadding(
            dp(14),
            dp(12),
            dp(14),
            dp(12)
        )

        box.background =
            roundedBackground(
                Color.argb(40, 20, 70, 125),
                Color.rgb(40, 110, 175)
            )

        val cover =
            TextView(this)

        cover.text = "♪"
        cover.textSize = 30f
        cover.setTextColor(white)
        cover.gravity = Gravity.CENTER

        cover.background =
            roundedBackground(
                Color.rgb(55, 45, 125),
                purple
            )

        box.addView(
            cover,
            LinearLayout.LayoutParams(
                dp(62),
                dp(62)
            )
        )

        val details =
            LinearLayout(this)

        details.orientation =
            LinearLayout.VERTICAL

        details.gravity =
            Gravity.CENTER_VERTICAL

        val detailsParams =
            LinearLayout.LayoutParams(
                0,
                dp(62),
                1f
            )

        detailsParams.leftMargin = dp(14)

        box.addView(
            details,
            detailsParams
        )

        val song =
            TextView(this)

        song.text = "Man Bharrya"
        song.textSize = 16f
        song.setTextColor(white)
        song.typeface = Typeface.DEFAULT_BOLD

        details.addView(song)

        val artist =
            TextView(this)

        artist.text = "B Praak  |  YouTube"
        artist.textSize = 10f
        artist.setTextColor(muted)

        details.addView(artist)

        val play =
            TextView(this)

        play.text = "▶"
        play.textSize = 22f
        play.setTextColor(cyan)
        play.gravity = Gravity.CENTER

        play.setOnClickListener {
            openUrl(
                "https://www.youtube.com/results?search_query=Man+Bharrya+B+Praak"
            )
        }

        box.addView(
            play,
            LinearLayout.LayoutParams(
                dp(55),
                dp(62)
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
                Color.argb(28, 60, 110, 170),
                Color.rgb(35, 85, 125)
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

        row.addView(tile, params)

        val iconText =
            TextView(this)

        iconText.text = icon
        iconText.textSize = 19f
        iconText.setTextColor(cyan)
        iconText.gravity = Gravity.CENTER

        tile.addView(
            iconText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )

        val labelText =
            TextView(this)

        labelText.text = label
        labelText.textSize = 8f
        labelText.setTextColor(white)
        labelText.gravity = Gravity.CENTER

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

        nav.addView(item, params)

        val iconText =
            TextView(this)

        iconText.text = icon
        iconText.textSize =
            if (label == "AURIX") 20f else 17f

        iconText.setTextColor(
            if (label == "AURIX")
                cyan
            else
                muted
        )

        iconText.gravity = Gravity.CENTER

        item.addView(
            iconText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(34)
            )
        )

        val labelText =
            TextView(this)

        labelText.text = label
        labelText.textSize = 7f
        labelText.setTextColor(
            if (label == "AURIX")
                cyan
            else
                muted
        )
        labelText.gravity = Gravity.CENTER

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

        title.text = text
        title.textSize = 9f
        title.setTextColor(muted)
        title.typeface = Typeface.DEFAULT_BOLD
        title.letterSpacing = 0.18f

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(30)
            )

        params.topMargin = dp(15)

        parent.addView(title, params)
    }

    // =========================================================
    // HELPERS
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

    private fun createOrb():
        GradientDrawable {

        val drawable =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(25, 95, 220),
                    Color.rgb(100, 35, 190),
                    Color.rgb(10, 170, 215)
                )
            )

        drawable.shape =
            GradientDrawable.OVAL

        drawable.setStroke(
            dp(2),
            Color.rgb(170, 235, 255)
        )

        return drawable
    }

    private fun roundedBackground(
        fill: Int,
        stroke: Int
    ): GradientDrawable {

        val drawable =
            GradientDrawable()

        drawable.cornerRadius =
            dp(18).toFloat()

        drawable.setColor(fill)

        drawable.setStroke(
            dp(1),
            stroke
        )

        return drawable
    }

    // =========================================================
    // CORE ANIMATION
    // =========================================================

    private fun rotateCore(
        view: View
    ) {

        if (isFinishing) {
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

                        if (!isFinishing) {
                            pulseCore(view)
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

                clean.contains("LISTEN") -> {

                    statusText.text =
                        "LISTENING"

                    coreText.text =
                        "LISTEN"

                    statusText.setTextColor(
                        cyan
                    )

                    waveform.running = true
                }

                clean.contains("THINK") ||
                        clean.contains("PROCESS") -> {

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

                    waveform.running = true
                }

                clean.contains("EXECUT") -> {

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

                    waveform.running = true
                }

                clean.contains("RESPOND") ||
                        clean.contains("SPEAK") -> {

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

                    waveform.running = true
                }

                else -> {

                    statusText.text =
                        "READY"

                    coreText.text =
                        "AURIX"

                    statusText.setTextColor(
                        cyan
                    )

                    waveform.running = false
                }
            }
        }
    }

    // =========================================================
    // INTERFACE STATE
    // =========================================================

    private fun updateInterface() {

        if (active) {
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

            startActivity(intent)

        } catch (_: Exception) {
        }
    }

    // =========================================================
    // RECEIVER
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

    if (::statusText.isInitialized) {

        if (AurixService.isRunning) {

            active = true

            updateStatus("LISTENING")

        } else {

            active = false

            updateStatus("READY")
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
    // WAVEFORM VIEW
    // =========================================================

    private class WaveformView(
        context: Context
    ) : View(context) {

        private val paint =
            android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG
            )

        private var phase = 0f

        var running = false
            set(value) {
                field = value
                invalidate()
            }

        private val handler =
            android.os.Handler(
                android.os.Looper.getMainLooper()
            )

        private val animator =
            object : Runnable {

                override fun run() {

                    if (running) {
                        phase += 0.22f
                        invalidate()
                    }

                    handler.postDelayed(
                        this,
                        40
                    )
                }
            }

        init {

            paint.strokeWidth = 3f
            paint.strokeCap =
                android.graphics.Paint.Cap.ROUND

            handler.post(animator)
        }

        override fun onDraw(
            canvas: android.graphics.Canvas
        ) {

            super.onDraw(canvas)

            val width =
                width.toFloat()

            val center =
                height / 2f

            val count = 25

            val gap =
                width / count

            paint.color =
                Color.rgb(
                    75,
                    200,
                    255
                )

            for (i in 0 until count) {

                val x =
                    gap * i + gap / 2f

           val wave =
    kotlin.math.sin(
        (phase + i * 0.55f).toDouble()
    ).toFloat()

val heightValue =
    if (running)
        7f + (
            kotlin.math.abs(wave) * 17f
        )
    else
        5f

                canvas.drawLine(
                    x,
                    center - heightValue,
                    x,
                    center + heightValue,
                    paint
                )
            }
        }
    }
}
