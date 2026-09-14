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
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager

class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var statusText: TextView
    private lateinit var coreText: TextView
    private lateinit var systemText: TextView
    private lateinit var waveform: WaveformView
    private lateinit var drawer: LinearLayout

    private var active = false
    private var currentPage = "Home"

    private val historyPrefs by lazy {
        getSharedPreferences("aurix_history", MODE_PRIVATE)
    }

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
                        if (text.isNotBlank()) {
                            addHistoryItem("YOU", text)
                        }
                    }

                    AurixService.TYPE_SPEAK -> {
                        updateStatus("RESPONDING")
                        if (text.isNotBlank()) {
                            addHistoryItem("AURIX", text)
                        }
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

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
        }

        root.addView(
            scroll,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(12)
            )
        }

        scroll.addView(
            content,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // =====================================================
        // HEADER — MASTER
        // =====================================================

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        content.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        )

        val menu = TextView(this).apply {
            text = "☰"
            textSize = 25f
            setTextColor(white)
            gravity = Gravity.CENTER
            setOnClickListener { toggleDrawer() }
        }

        header.addView(
            menu,
            LinearLayout.LayoutParams(dp(45), dp(48))
        )

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        header.addView(
            brand,
            LinearLayout.LayoutParams(
                0,
                dp(52),
                1f
            )
        )

        val title = TextView(this).apply {
            text = "A U R I X"
            textSize = 24f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.14f
        }

        brand.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )

        val tagline = TextView(this).apply {
            text = "INTELLIGENCE CORE"
            textSize = 8f
            setTextColor(muted)
            letterSpacing = 0.16f
        }

        brand.addView(
            tagline,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(20)
            )
        )

        val online = TextView(this).apply {
            text = "● ONLINE"
            textSize = 10f
            setTextColor(Color.rgb(75, 235, 150))
            gravity = Gravity.CENTER
        }

        header.addView(
            online,
            LinearLayout.LayoutParams(dp(82), dp(40))
        )

        val voiceButton = TextView(this).apply {
            text = "◎"
            textSize = 29f
            setTextColor(white)
            gravity = Gravity.CENTER
            background = roundedBackground(
                Color.argb(42, 70, 125, 220),
                blue
            )
            elevation = dp(8).toFloat()
            setOnClickListener { startListeningOnce() }
        }

        header.addView(
            voiceButton,
            LinearLayout.LayoutParams(dp(58), dp(58))
        )

        // =====================================================
        // MASTER CORE LABEL
        // =====================================================

        val coreLabel = TextView(this).apply {
            text = "A U R I X   •   INTELLIGENCE CORE"
            textSize = 9f
            setTextColor(Color.rgb(100, 175, 225))
            gravity = Gravity.CENTER
            letterSpacing = 0.18f
        }

        val coreLabelParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(28)
            )

        coreLabelParams.topMargin = dp(7)

        content.addView(
            coreLabel,
            coreLabelParams
        )

        // =====================================================
        // MASTER CORE PANEL — 220dp
        // =====================================================

        val coreArea = FrameLayout(this).apply {
            background =
                roundedBackground(
                    Color.rgb(4, 7, 30),
                    Color.TRANSPARENT
                )
        }

        val coreParams =
            LinearLayout.LayoutParams(
                dp(220),
                dp(220)
            )

        coreParams.topMargin = dp(5)

        content.addView(
            coreArea,
            coreParams
        )

        // Outer reactor
        val outer = View(this)

        outer.background =
            ovalGradient(
                intArrayOf(
                    Color.rgb(35, 100, 235),
                    Color.rgb(130, 40, 235),
                    Color.rgb(25, 150, 225)
                )
            )

        val outerSize = dp(198)

        coreArea.addView(
            outer,
            FrameLayout.LayoutParams(
                outerSize,
                outerSize
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        // Dark ring
        val ring = View(this)

        ring.background =
            ovalGradient(
                intArrayOf(
                    Color.rgb(3, 14, 42),
                    Color.rgb(7, 5, 32),
                    Color.rgb(3, 25, 45)
                )
            )

        val ringSize = dp(174)

        coreArea.addView(
            ring,
            FrameLayout.LayoutParams(
                ringSize,
                ringSize
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        // Orb
        val orb = View(this)
        orb.background = createOrb()

        val orbSize = dp(145)

        coreArea.addView(
            orb,
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        // Orbital line 1
        val orbit1 = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(dp(2), Color.rgb(55, 205, 255))
            }
            rotation = -18f
        }

        coreArea.addView(
            orbit1,
            FrameLayout.LayoutParams(
                dp(185),
                dp(72)
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        // Orbital line 2
        val orbit2 = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setStroke(dp(2), Color.rgb(45, 180, 255))
            }
            rotation = 32f
        }

        coreArea.addView(
            orbit2,
            FrameLayout.LayoutParams(
                dp(185),
                dp(72)
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        coreText = TextView(this).apply {
            text = "AURIX"
            textSize = 22f
            setTextColor(white)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.16f
        }

        coreArea.addView(
            coreText,
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        rotateCore(outer)
        pulseCore(orb)

        // =====================================================
        // READY BADGE
        // =====================================================

        statusText = TextView(this).apply {
            text = "AURIX  •  READY"
            textSize = 11f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
            background =
                roundedBackground(
                    Color.argb(45, 60, 190, 255),
                    cyan
                )
        }

        val statusParams =
            LinearLayout.LayoutParams(
                dp(210),
                dp(38)
            )

        statusParams.topMargin = dp(7)

        content.addView(
            statusText,
            statusParams
        )

        // Kept initialized for service/status compatibility,
        // but hidden because MASTER UI does not show this line.
        systemText = TextView(this).apply {
            visibility = View.GONE
        }

        content.addView(
            systemText,
            LinearLayout.LayoutParams(1, 1)
        )

        waveform = WaveformView(this).apply {
            visibility = View.GONE
        }

        content.addView(
            waveform,
            LinearLayout.LayoutParams(1, 1)
        )

        // =====================================================
        // SAVED CONVERSATION SUBTITLE
        // =====================================================

        val savedSubtitle = TextView(this).apply {
            text = "Saved conversation"
            textSize = 10f
            setTextColor(muted)
            gravity = Gravity.CENTER
        }

        content.addView(
            savedSubtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(28)
            )
        )

        // =====================================================
        // SAVED CONVERSATION HEADER
        // =====================================================

        val savedHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val savedBack = TextView(this).apply {
            text = "‹"
            textSize = 31f
            setTextColor(white)
            gravity = Gravity.CENTER
            background =
                roundedBackground(
                    Color.argb(35, 45, 90, 180),
                    blue
                )
        }

        savedHeader.addView(
            savedBack,
            LinearLayout.LayoutParams(
                dp(42),
                dp(42)
            )
        )

        val savedTitle = TextView(this).apply {
            text = "SAVED CONVERSATION"
            textSize = 10f
            setTextColor(cyan)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.16f
            gravity = Gravity.CENTER_VERTICAL
        }

        val savedTitleParams =
            LinearLayout.LayoutParams(
                0,
                dp(42),
                1f
            )

        savedTitleParams.leftMargin = dp(8)

        savedHeader.addView(
            savedTitle,
            savedTitleParams
        )

        content.addView(
            savedHeader,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
            )
        )

        // =====================================================
        // SAVED MESSAGE
        // =====================================================

        val savedDate = TextView(this).apply {
            text = "14 Sept • 02:59 am"
            textSize = 8f
            setTextColor(muted)
            gravity = Gravity.CENTER_VERTICAL
        }

        content.addView(
            savedDate,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(24)
            )
        )

        val savedCard = createMessageCard(
            "AURIX",
            "Good night boss, abhi kya karna hai?",
            true
        )

        content.addView(
            savedCard,
            cardParams()
        )

        // =====================================================
        // QUICK ACTIONS
        // =====================================================

        addSectionTitle(
            content,
            "QUICK ACTIONS"
        )

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(
            actions,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val row1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        actions.addView(
            row1,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        addAction(row1, "▶", "YouTube") {
            openUrl("https://www.youtube.com")
        }

        addAction(row1, "⌕", "Search") {
            openUrl("https://www.google.com")
        }

        addAction(row1, "♫", "Music") {
            openUrl("https://music.youtube.com")
        }

        addAction(row1, "☁", "Weather") {
            openUrl("https://www.google.com/search?q=weather")
        }

        val row2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        actions.addView(
            row2,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )
        )

        addAction(row2, "☎", "Call") {
            startActivity(Intent(Intent.ACTION_DIAL))
        }

        addAction(row2, "✉", "Messages") {
            startActivity(
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:")
                }
            )
        }

        addAction(row2, "▦", "Apps") {
            performAppsAction()
        }

        addAction(row2, "•••", "More") {
            updateStatus("READY")
        }

        // =====================================================
        // BOTTOM NAV — MASTER
        // =====================================================

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background =
                roundedBackground(
                    Color.argb(28, 10, 40, 75),
                    Color.rgb(35, 80, 125)
                )
        }

        val navParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
            )

        navParams.topMargin = dp(8)

        content.addView(
            nav,
            navParams
        )

        addNavItem(nav, "⌂", "Home") {
            navigate("Home")
        }

        addNavItem(nav, "◷", "History") {
            navigate("History")
        }

        addNavItem(nav, "A", "AURIX") {
            navigate("AURIX")
        }

        addNavItem(nav, "✦", "Shortcuts") {
            navigate("Shortcuts")
        }

        addNavItem(nav, "⚙", "Settings") {
            navigate("Settings")
        }
    }

    private fun startListeningOnce() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestMicrophonePermission()
            return
        }

        val intent = Intent(
            this,
            AurixService::class.java
        ).apply {
            action = AurixService.ACTION_LISTEN_ONCE
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

    // =========================================================
    // NAVIGATION / PAGES
    // =========================================================

    private fun navigate(label: String) {
        currentPage = label
        when (label) {
            "Home", "AURIX" -> showHomePage()
            "History" -> showHistoryPage()
            "Shortcuts" -> showShortcutsPage()
            "Settings" -> showSettingsPage()
        }
    }

    private fun showHomePage() {
        // Home is already the primary screen.
        currentPage = "Home"
    }

    private fun showHistoryPage() {
        val overlay = createPageOverlay("HISTORY")
        val content = overlay.second

        addSectionTitle(content, "RECENT AURIX ACTIVITY")

        val history = getRecentHistory().asReversed()
        if (history.isEmpty()) {
            addMessageCard(
                content,
                "AURIX",
                "No conversation history yet.",
                true
            )
        } else {
            history.forEach { item ->
                addMessageCard(
                    content,
                    item.first,
                    item.second,
                    item.first == "AURIX"
                )
            }
        }

        val clear = TextView(this).apply {
            text = "CLEAR LOCAL HISTORY"
            textSize = 10f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(255, 125, 155))
            background = roundedBackground(
                Color.argb(28, 180, 40, 80),
                Color.rgb(180, 65, 100)
            )
            setOnClickListener {
                historyPrefs.edit().clear().apply()
                showHistoryPage()
            }
        }
        content.addView(
            clear,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
            ).apply { topMargin = dp(14) }
        )
    }

    private fun showShortcutsPage() {
        val overlay = createPageOverlay("SHORTCUTS")
        val content = overlay.second

        addSectionTitle(content, "AURIX SHORTCUTS")

        addShortcut(content, "♫", "MUSIC", "Open YouTube Music") {
            openUrl("https://music.youtube.com")
        }
        addShortcut(content, "⌕", "WEB SEARCH", "Open Google Search") {
            openUrl("https://www.google.com")
        }
        addShortcut(content, "☁", "WEATHER", "Open current weather") {
            openUrl("https://www.google.com/search?q=weather")
        }
        addShortcut(content, "+", "NEW CONVERSATION", "Clear local conversation history") {
            historyPrefs.edit().clear().apply()
            navigate("Home")
        }
    }

    private fun showSettingsPage() {
        val overlay = createPageOverlay("SETTINGS")
        val content = overlay.second

        addSectionTitle(content, "ACCOUNT")
        val user = FirebaseAuth.getInstance().currentUser
        addSettingsRow(
            content,
            "GOOGLE ACCOUNT",
            user?.email ?: "Not signed in"
        )
        addSettingsRow(
            content,
            "PLAN",
            "AURIX Free"
        )
        addSettingsRow(
            content,
            "VOICE MODE",
            "Wake word: AURIX • Tap backup available"
        )
        addSettingsRow(
            content,
            "SYSTEM",
            "AURIX Core Online"
        )

        addSectionTitle(content, "ACCOUNT ACTIONS")

        val signOut = TextView(this).apply {
            text = "SIGN OUT OF AURIX"
            textSize = 11f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(255, 145, 170))
            background = roundedBackground(
                Color.argb(30, 180, 40, 80),
                Color.rgb(180, 65, 100)
            )
            setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(
                    Intent(
                        this@MainActivity,
                        AuthActivity::class.java
                    )
                )
                finish()
            }
        }
        content.addView(
            signOut,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        )
    }

    private fun createPageOverlay(
        title: String
    ): Pair<FrameLayout, LinearLayout> {
        root.findViewWithTag<View>("AURIX_PAGE")?.let { root.removeView(it) }

        val overlay = FrameLayout(this).apply {
            tag = "AURIX_PAGE"
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(bgTop, Color.rgb(1, 5, 18), bgBottom)
            )
        }

        root.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val scroll = ScrollView(this).apply {
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        overlay.addView(
            scroll,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(28), dp(18), dp(30))
        }
        scroll.addView(content)

        val back = TextView(this).apply {
            text = "‹   AURIX"
            textSize = 14f
            gravity = Gravity.CENTER_VERTICAL
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(cyan)
            setOnClickListener { root.removeView(overlay) }
        }
        content.addView(
            back,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        )

        val heading = TextView(this).apply {
            text = title
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.12f
            setTextColor(white)
            setPadding(0, dp(10), 0, dp(8))
        }
        content.addView(heading)

        return overlay to content
    }

    private fun addShortcut(
        parent: LinearLayout,
        icon: String,
        title: String,
        description: String,
        action: () -> Unit
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedBackground(
                Color.argb(34, 40, 90, 155),
                Color.rgb(45, 120, 185)
            )
            setOnClickListener { action() }
        }

        val iconView = TextView(this).apply {
            text = icon
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(cyan)
        }
        card.addView(iconView, LinearLayout.LayoutParams(dp(55), dp(55)))

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }
        card.addView(
            details,
            LinearLayout.LayoutParams(0, dp(55), 1f)
        )

        val titleView = TextView(this).apply {
            text = title
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
        }
        details.addView(titleView)

        val desc = TextView(this).apply {
            text = description
            textSize = 9f
            setTextColor(muted)
            setPadding(0, dp(5), 0, 0)
        }
        details.addView(desc)

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(82)
            ).apply { bottomMargin = dp(10) }
        )
    }

    private fun addSettingsRow(
        parent: LinearLayout,
        title: String,
        value: String
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(13), dp(16), dp(13))
            background = roundedBackground(
                Color.argb(32, 40, 85, 145),
                Color.rgb(35, 100, 155)
            )
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 8f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.16f
            setTextColor(cyan)
        }
        card.addView(titleView)

        val valueView = TextView(this).apply {
            text = value
            textSize = 13f
            setTextColor(white)
            setPadding(0, dp(6), 0, 0)
        }
        card.addView(valueView)

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
        )
    }

    private fun addHistoryItem(role: String, text: String) {
        val items = getRecentHistory().toMutableList()
        items.add(role to text)
        while (items.size > 50) items.removeAt(0)

        val encoded = items.joinToString("\n") {
            "${it.first}\t${it.second.replace("\n", " ")}"
        }

        historyPrefs.edit()
            .putString("items", encoded)
            .apply()
    }

    private fun getRecentHistory(): List<Pair<String, String>> {
        val raw = historyPrefs.getString("items", "").orEmpty()
        if (raw.isBlank()) return emptyList()

        return raw.lines().mapNotNull { line ->
            val split = line.split("\t", limit = 2)
            if (split.size == 2) split[0] to split[1] else null
        }
    }

    private fun toggleDrawer() {
        if (::drawer.isInitialized && drawer.visibility == View.VISIBLE) {
            drawer.visibility = View.GONE
            return
        }

        if (!::drawer.isInitialized) {
            createDrawer()
        }

        drawer.visibility = View.VISIBLE
    }

    private fun createDrawer() {
        drawer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(38), dp(16), dp(24))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(7, 14, 38), Color.rgb(18, 7, 39))
            )
            elevation = dp(18).toFloat()
        }

        root.addView(
            drawer,
            FrameLayout.LayoutParams(
                dp(310),
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val close = TextView(this).apply {
            text = "×"
            textSize = 30f
            gravity = Gravity.RIGHT
            setTextColor(white)
            setOnClickListener { drawer.visibility = View.GONE }
        }
        drawer.addView(close, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(45)
        ))

        val logo = TextView(this).apply {
            text = "A U R I X"
            textSize = 27f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.15f
            setTextColor(white)
        }
        drawer.addView(logo)

        val sub = TextView(this).apply {
            text = "INTELLIGENCE CORE"
            textSize = 8f
            letterSpacing = 0.16f
            setTextColor(cyan)
            setPadding(0, dp(4), 0, dp(24))
        }
        drawer.addView(sub)

        addDrawerItem("⌂", "Home") { drawer.visibility = View.GONE }
        addDrawerItem("◷", "History") { drawer.visibility = View.GONE; showHistoryPage() }
        addDrawerItem("✦", "Shortcuts") { drawer.visibility = View.GONE; showShortcutsPage() }
        addDrawerItem("♫", "Music") { drawer.visibility = View.GONE; openUrl("https://music.youtube.com") }
        addDrawerItem("▦", "Apps") { drawer.visibility = View.GONE; performAppsAction() }
        addDrawerItem("+", "New Conversation") {
            historyPrefs.edit().clear().apply()
            drawer.visibility = View.GONE
        }
        addDrawerItem("⚙", "Settings") { drawer.visibility = View.GONE; showSettingsPage() }

        val spacer = View(this)
        drawer.addView(spacer, LinearLayout.LayoutParams(1, 0, 1f))

        val account = FirebaseAuth.getInstance().currentUser
        val accountText = TextView(this).apply {
            text = account?.email ?: "Google account"
            textSize = 9f
            setTextColor(muted)
        }
        drawer.addView(accountText)
    }

    private fun addDrawerItem(
        icon: String,
        label: String,
        action: () -> Unit
    ) {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
            setOnClickListener { action() }
        }

        drawer.addView(item, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(52)
        ))

        val iconView = TextView(this).apply {
            text = icon
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(cyan)
        }
        item.addView(iconView, LinearLayout.LayoutParams(dp(48), dp(48)))

        val labelView = TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(white)
        }
        item.addView(labelView, LinearLayout.LayoutParams(0, dp(48), 1f))
    }

    private fun performAppsAction() {
        try {
            startActivity(
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
            )
        } catch (_: Exception) {
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

    private fun addMessageCard(
        parent: LinearLayout,
        name: String,
        message: String,
        aurix: Boolean
    ) {
        parent.addView(
            createMessageCard(name, message, aurix),
            cardParams()
        )
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

        cover.text = "♫"
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

        artist.text = "B Praak  •  YouTube"
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
        iconText.setTextColor(
            when (label) {
                "YouTube" -> Color.rgb(255, 35, 65)
                "Search" -> Color.rgb(35, 210, 255)
                "Music" -> Color.rgb(220, 55, 245)
                "Weather" -> Color.rgb(255, 205, 45)
                "Call" -> Color.rgb(35, 220, 125)
                "Messages" -> Color.rgb(75, 135, 255)
                "Apps" -> Color.rgb(235, 65, 110)
                "More" -> Color.rgb(145, 80, 245)
                else -> cyan
            }
        )
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
        label: String,
        action: () -> Unit
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
                status.uppercase(Locale.getDefault())

            statusText.text =
                when {
                    clean.contains("LISTEN") ->
                        "AURIX  •  LISTENING"

                    clean.contains("THINK") ||
                            clean.contains("PROCESS") ->
                        "AURIX  •  THINKING"

                    clean.contains("EXECUT") ->
                        "AURIX  •  EXECUTING"

                    clean.contains("RESPOND") ||
                            clean.contains("SPEAK") ->
                        "AURIX  •  RESPONDING"

                    else ->
                        "AURIX  •  READY"
                }

            statusText.setTextColor(
                when {
                    clean.contains("THINK") ->
                        Color.rgb(210, 145, 255)

                    clean.contains("EXECUT") ->
                        Color.rgb(110, 175, 255)

                    clean.contains("RESPOND") ||
                            clean.contains("SPEAK") ->
                        Color.rgb(180, 110, 255)

                    else ->
                        cyan
                }
            )

            // MASTER UI keeps the core identity stable.
            coreText.text = "AURIX"

            if (::waveform.isInitialized) {
                waveform.running = false
            }
        }
    }

    // =========================================================
    // INTERFACE STATE
    // =========================================================

    private fun updateInterface() {
        if (::systemText.isInitialized) {
            systemText.text =
                if (active) {
                    "SYSTEM ONLINE  •  ACTIVE"
                } else {
                    "SYSTEM ONLINE"
                }
        }

        if (::statusText.isInitialized) {
            statusText.text = "AURIX  •  READY"
            statusText.setTextColor(cyan)
        }

        if (::coreText.isInitialized) {
            coreText.text = "AURIX"
        }
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

    override fun onBackPressed() {
        if (::drawer.isInitialized && drawer.visibility == View.VISIBLE) {
            drawer.visibility = View.GONE
            return
        }

        root.findViewWithTag<View>("AURIX_PAGE")?.let {
            root.removeView(it)
            return
        }

        super.onBackPressed()
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
