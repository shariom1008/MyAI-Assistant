package com.example.myaiassistant

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class MainActivity : Activity() {

    // =========================================================
    // ROOT / UI
    // =========================================================

    private lateinit var root: FrameLayout
    private lateinit var aurixUi: AurixOriginalUi
    private lateinit var drawer: LinearLayout

    private var active = false
    private var currentPage = "Home"

    private val historyPrefs by lazy {
        getSharedPreferences(
            "aurix_history",
            MODE_PRIVATE
        )
    }

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

                        if (text.isNotBlank()) {

                            addHistoryItem(
                                "YOU",
                                text
                            )

                            if (
                                ::aurixUi.isInitialized
                            ) {
                                aurixUi.addUserMessage(text)
                            }
                        }
                    }

                    AurixService.TYPE_SPEAK -> {

                        updateStatus("RESPONDING")

                        if (text.isNotBlank()) {

                            addHistoryItem(
                                "AURIX",
                                text
                            )

                            if (
                                ::aurixUi.isInitialized
                            ) {
                                aurixUi.addAurixMessage(text)
                            }
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

        window.statusBarColor =
            android.graphics.Color.TRANSPARENT

        window.navigationBarColor =
            android.graphics.Color.BLACK

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
    // MASTER UI
    // =========================================================

    private fun createInterface() {

        root = FrameLayout(this)

        setContentView(root)

        aurixUi =
            AurixOriginalUi(
                this,
                object :
                    AurixOriginalUi.Callbacks {

                    override fun onMenu() {
                        toggleDrawer()
                    }

                    override fun onVoice() {
                        startListeningOnce()
                    }

                    override fun onYouTube() {
                        openUrl(
                            "https://www.youtube.com"
                        )
                    }

                    override fun onSearch() {
                        openUrl(
                            "https://www.google.com"
                        )
                    }

                    override fun onMusic() {
                        openUrl(
                            "https://music.youtube.com"
                        )
                    }

                    override fun onWeather() {
                        openUrl(
                            "https://www.google.com/search?q=weather"
                        )
                    }

                    override fun onCall() {

                        try {

                            startActivity(
                                Intent(
                                    Intent.ACTION_DIAL
                                )
                            )

                        } catch (_: Exception) {
                        }
                    }

                    override fun onMessages() {

                        try {

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

                        } catch (_: Exception) {
                        }
                    }

                    override fun onApps() {
                        performAppsAction()
                    }

                    override fun onMore() {
                        updateStatus("READY")
                    }

                    override fun onHome() {
                        navigate("Home")
                    }

                    override fun onHistory() {
                        navigate("History")
                    }

                    override fun onAurix() {
                        navigate("AURIX")
                    }

                    override fun onShortcuts() {
                        navigate("Shortcuts")
                    }

                    override fun onSettings() {
                        navigate("Settings")
                    }
                }
            )

        root.addView(
            aurixUi.build(),
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
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

        if (requestCode == 500) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                startAurixService()

            } else {

                active = false
                updateStatus("READY")
            }
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
    // BACKUP VOICE LISTENER
    // =========================================================

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

            updateStatus("LISTENING")

        } catch (_: Exception) {

            updateStatus("START FAILED")
        }
    }

    // =========================================================
    // STATUS → MASTER UI
    // =========================================================

    private fun updateStatus(
        status: String
    ) {

        runOnUiThread {

            if (
                !::aurixUi.isInitialized
            ) {
                return@runOnUiThread
            }

            val clean =
                status.uppercase(
                    Locale.getDefault()
                )

            when {

                clean.contains("LISTEN") -> {

                    aurixUi.setListeningState()
                }

                clean.contains("THINK") ||
                clean.contains("PROCESS") -> {

                    aurixUi.setThinkingState()
                }

                clean.contains("EXECUT") -> {

                    aurixUi.setThinkingState()
                }

                clean.contains("RESPOND") ||
                clean.contains("SPEAK") ||
                clean.contains("SPEAKING") -> {

                    aurixUi.setThinkingState()
                }

                else -> {

                    aurixUi.setReadyState()
                }
            }
        }
    }

    // =========================================================
    // INTERFACE STATE
    // =========================================================

    private fun updateInterface() {

        if (
            !::aurixUi.isInitialized
        ) {
            return
        }

        if (active) {

            when {

                AurixService.isRunning -> {
                    aurixUi.setListeningState()
                }

                else -> {
                    aurixUi.setReadyState()
                }
            }

        } else {

            aurixUi.setReadyState()
        }
    }

    // =========================================================
    // NAVIGATION
    // =========================================================

    private fun navigate(
        label: String
    ) {

        currentPage = label

        when (label) {

            "Home",
            "AURIX" -> {
                showHomePage()
            }

            "History" -> {
                showHistoryPage()
            }

            "Shortcuts" -> {
                showShortcutsPage()
            }

            "Settings" -> {
                showSettingsPage()
            }
        }
    }

    // =========================================================
    // HOME
    // =========================================================

    private fun showHomePage() {

        currentPage = "Home"

        root.findViewWithTag<View>(
            "AURIX_PAGE"
        )?.let {
            root.removeView(it)
        }
    }

    // =========================================================
    // HISTORY
    // =========================================================

    private fun showHistoryPage() {

        val overlay =
            createPageOverlay(
                "HISTORY"
            )

        val content =
            overlay.second

        addSectionTitle(
            content,
            "RECENT AURIX ACTIVITY"
        )

        val history =
            getRecentHistory()
                .asReversed()

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

        val clear =
            TextView(this).apply {

                text =
                    "CLEAR LOCAL HISTORY"

                textSize = 10f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    android.graphics.Color.rgb(
                        255,
                        125,
                        155
                    )
                )

                setOnClickListener {

                    historyPrefs
                        .edit()
                        .clear()
                        .apply()

                    showHistoryPage()
                }
            }

        content.addView(
            clear,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
            ).apply {
                topMargin = dp(14)
            }
        )
    }

    // =========================================================
    // SHORTCUTS
    // =========================================================

    private fun showShortcutsPage() {

        val overlay =
            createPageOverlay(
                "SHORTCUTS"
            )

        val content =
            overlay.second

        addSectionTitle(
            content,
            "AURIX SHORTCUTS"
        )

        addShortcut(
            content,
            "♫",
            "MUSIC",
            "Open YouTube Music"
        ) {
            openUrl(
                "https://music.youtube.com"
            )
        }

        addShortcut(
            content,
            "⌕",
            "WEB SEARCH",
            "Open Google Search"
        ) {
            openUrl(
                "https://www.google.com"
            )
        }

        addShortcut(
            content,
            "☁",
            "WEATHER",
            "Open current weather"
        ) {
            openUrl(
                "https://www.google.com/search?q=weather"
            )
        }

        addShortcut(
            content,
            "+",
            "NEW CONVERSATION",
            "Clear local conversation history"
        ) {

            historyPrefs
                .edit()
                .clear()
                .apply()

            navigate("Home")
        }
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private fun showSettingsPage() {

        val overlay =
            createPageOverlay(
                "SETTINGS"
            )

        val content =
            overlay.second

        addSectionTitle(
            content,
            "ACCOUNT"
        )

        val user =
            FirebaseAuth
                .getInstance()
                .currentUser

        addSettingsRow(
            content,
            "GOOGLE ACCOUNT",
            user?.email
                ?: "Not signed in"
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

        addSectionTitle(
            content,
            "ACCOUNT ACTIONS"
        )

        val signOut =
            TextView(this).apply {

                text =
                    "SIGN OUT OF AURIX"

                textSize = 11f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    android.graphics.Color.rgb(
                        255,
                        145,
                        170
                    )
                )

                setOnClickListener {

                    FirebaseAuth
                        .getInstance()
                        .signOut()

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

    // =========================================================
    // PAGE OVERLAY
    // =========================================================

    private fun createPageOverlay(
        title: String
    ): Pair<FrameLayout, LinearLayout> {

        root.findViewWithTag<View>(
            "AURIX_PAGE"
        )?.let {
            root.removeView(it)
        }

        val overlay =
            FrameLayout(this).apply {

                tag =
                    "AURIX_PAGE"

                setBackgroundColor(
                    android.graphics.Color.rgb(
                        5,
                        8,
                        28
                    )
                )
            }

        root.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val scroll =
            android.widget.ScrollView(this).apply {
                overScrollMode =
                    View.OVER_SCROLL_NEVER
            }

        overlay.addView(
            scroll,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val content =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(18),
                    dp(28),
                    dp(18),
                    dp(30)
                )
            }

        scroll.addView(
            content
        )

        val back =
            TextView(this).apply {

                text =
                    "‹   AURIX"

                textSize = 14f

                gravity =
                    Gravity.CENTER_VERTICAL

                setTextColor(
                    android.graphics.Color.rgb(
                        80,
                        220,
                        255
                    )
                )

                setOnClickListener {
                    root.removeView(
                        overlay
                    )
                }
            }

        content.addView(
            back,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            )
        )

        val heading =
            TextView(this).apply {

                text =
                    title

                textSize = 23f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
                )

                letterSpacing =
                    0.12f

                setPadding(
                    0,
                    dp(10),
                    0,
                    dp(8)
                )
            }

        content.addView(
            heading
        )

        return overlay to content
    }

    // =========================================================
    // SHORTCUT CARD
    // =========================================================

    private fun addShortcut(
        parent: LinearLayout,
        icon: String,
        title: String,
        description: String,
        action: () -> Unit
    ) {

        val card =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(14),
                    dp(12),
                    dp(14),
                    dp(12)
                )

                setOnClickListener {
                    action()
                }
            }

        val iconView =
            TextView(this).apply {

                text =
                    icon

                textSize = 24f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    android.graphics.Color.rgb(
                        80,
                        220,
                        255
                    )
                )
            }

        card.addView(
            iconView,
            LinearLayout.LayoutParams(
                dp(55),
                dp(55)
            )
        )

        val details =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(12),
                    0,
                    0,
                    0
                )
            }

        card.addView(
            details,
            LinearLayout.LayoutParams(
                0,
                dp(55),
                1f
            )
        )

        details.addView(
            TextView(this).apply {

                text =
                    title

                textSize = 11f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
                )
            }
        )

        details.addView(
            TextView(this).apply {

                text =
                    description

                textSize = 9f

                setTextColor(
                    android.graphics.Color.rgb(
                        105,
                        155,
                        190
                    )
                )

                setPadding(
                    0,
                    dp(5),
                    0,
                    0
                )
            }
        )

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(82)
            ).apply {
                bottomMargin = dp(10)
            }
        )
    }

    // =========================================================
    // SETTINGS ROW
    // =========================================================

    private fun addSettingsRow(
        parent: LinearLayout,
        title: String,
        value: String
    ) {

        val card =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(13),
                    dp(16),
                    dp(13)
                )
            }

        card.addView(
            TextView(this).apply {

                text =
                    title

                textSize = 8f

                letterSpacing =
                    0.16f

                setTextColor(
                    android.graphics.Color.rgb(
                        80,
                        220,
                        255
                    )
                )

                setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
                )
            }
        )

        card.addView(
            TextView(this).apply {

                text =
                    value

                textSize = 13f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setPadding(
                    0,
                    dp(6),
                    0,
                    0
                )
            }
        )

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }
        )
    }

    // =========================================================
    // MESSAGE CARD
    // =========================================================

    private fun addMessageCard(
        parent: LinearLayout,
        name: String,
        message: String,
        aurix: Boolean
    ) {

        val box =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(16),
                    dp(12),
                    dp(16),
                    dp(12)
                )
            }

        box.addView(
            TextView(this).apply {

                text =
                    name

                textSize = 9f

                setTextColor(
                    if (aurix)
                        android.graphics.Color.rgb(
                            80,
                            220,
                            255
                        )
                    else
                        android.graphics.Color.rgb(
                            145,
                            55,
                            240
                        )
                )

                setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
                )
            }
        )

        box.addView(
            TextView(this).apply {

                text =
                    message

                textSize = 14f

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setPadding(
                    0,
                    dp(5),
                    0,
                    0
                )
            }
        )

        parent.addView(
            box,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    0,
                    dp(4),
                    0,
                    dp(4)
                )
            }
        )
    }

    // =========================================================
    // SECTION TITLE
    // =========================================================

    private fun addSectionTitle(
        parent: LinearLayout,
        text: String
    ) {

        parent.addView(
            TextView(this).apply {

                this.text =
                    text

                textSize = 9f

                setTextColor(
                    android.graphics.Color.rgb(
                        105,
                        170,
                        225
                    )
                )

                setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
                )

                letterSpacing =
                    0.18f

                setPadding(
                    0,
                    dp(12),
                    0,
                    dp(8)
                )
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(35)
            )
        )
    }

    // =========================================================
    // DRAWER
    // =========================================================

    private fun toggleDrawer() {

        if (
            ::drawer.isInitialized &&
            drawer.visibility ==
            View.VISIBLE
        ) {

            drawer.visibility =
                View.GONE

            return
        }

        if (
            !::drawer.isInitialized
        ) {
            createDrawer()
        }

        drawer.visibility =
            View.VISIBLE
    }

    private fun createDrawer() {

        drawer =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(20),
                    dp(38),
                    dp(16),
                    dp(24)
                )

                setBackgroundColor(
                    android.graphics.Color.rgb(
                        7,
                        14,
                        38
                    )
                )

                elevation =
                    dp(18).toFloat()
            }

        root.addView(
            drawer,
            FrameLayout.LayoutParams(
                dp(310),
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val close =
            TextView(this).apply {

                text =
                    "×"

                textSize = 30f

                gravity =
                    Gravity.RIGHT

                setTextColor(
                    android.graphics.Color.WHITE
                )

                setOnClickListener {
                    drawer.visibility =
                        View.GONE
                }
            }

        drawer.addView(
            close,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(45)
            )
        )

        drawer.addView(
            TextView(this).apply {

                text =
                    "A U R I X"

                textSize = 27f

                setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
                )

                letterSpacing =
                    0.15f

                setTextColor(
                    android.graphics.Color.WHITE
                )
            }
        )

        drawer.addView(
            TextView(this).apply {

                text =
                    "INTELLIGENCE CORE"

                textSize = 8f

                letterSpacing =
                    0.16f

                setTextColor(
                    android.graphics.Color.rgb(
                        80,
                        220,
                        255
                    )
                )

                setPadding(
                    0,
                    dp(4),
                    0,
                    dp(24)
                )
            }
        )

        addDrawerItem(
            "⌂",
            "Home"
        ) {
            drawer.visibility =
                View.GONE

            navigate("Home")
        }

        addDrawerItem(
            "◷",
            "History"
        ) {
            drawer.visibility =
                View.GONE

            showHistoryPage()
        }

        addDrawerItem(
            "✦",
            "Shortcuts"
        ) {
            drawer.visibility =
                View.GONE

            showShortcutsPage()
        }

        addDrawerItem(
            "♫",
            "Music"
        ) {
            drawer.visibility =
                View.GONE

            openUrl(
                "https://music.youtube.com"
            )
        }

        addDrawerItem(
            "▦",
            "Apps"
        ) {
            drawer.visibility =
                View.GONE

            performAppsAction()
        }

        addDrawerItem(
            "+",
            "New Conversation"
        ) {

            historyPrefs
                .edit()
                .clear()
                .apply()

            drawer.visibility =
                View.GONE
        }

        addDrawerItem(
            "⚙",
            "Settings"
        ) {
            drawer.visibility =
                View.GONE

            showSettingsPage()
        }

        val spacer =
            View(this)

        drawer.addView(
            spacer,
            LinearLayout.LayoutParams(
                1,
                0,
                1f
            )
        )

        val account =
            FirebaseAuth
                .getInstance()
                .currentUser

        drawer.addView(
            TextView(this).apply {

                text =
                    account?.email
                        ?: "Google account"

                textSize = 9f

                setTextColor(
                    android.graphics.Color.rgb(
                        105,
                        155,
                        190
                    )
                )
            }
        )
    }

    private fun addDrawerItem(
        icon: String,
        label: String,
        action: () -> Unit
    ) {

        val item =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setOnClickListener {
                    action()
                }
            }

        drawer.addView(
            item,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        )

        item.addView(
            TextView(this).apply {

                text =
                    icon

                textSize = 19f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    android.graphics.Color.rgb(
                        80,
                        220,
                        255
                    )
                )
            },
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        item.addView(
            TextView(this).apply {

                text =
                    label

                textSize = 12f

                setTextColor(
                    android.graphics.Color.WHITE
                )
            },
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )
    }

    // =========================================================
    // APPS
    // =========================================================

    private fun performAppsAction() {

        try {

            startActivity(
                Intent(
                    Intent.ACTION_MAIN
                ).apply {

                    addCategory(
                        Intent.CATEGORY_LAUNCHER
                    )
                }
            )

        } catch (_: Exception) {
        }
    }

    // =========================================================
    // HISTORY
    // =========================================================

    private fun addHistoryItem(
        role: String,
        text: String
    ) {

        val items =
            getRecentHistory()
                .toMutableList()

        items.add(
            role to text
        )

        while (
            items.size > 50
        ) {
            items.removeAt(0)
        }

        val encoded =
            items.joinToString("\n") {

                "${it.first}\t${
                    it.second.replace(
                        "\n",
                        " "
                    )
                }"
            }

        historyPrefs
            .edit()
            .putString(
                "items",
                encoded
            )
            .apply()
    }

    private fun getRecentHistory():
        List<Pair<String, String>> {

        val raw =
            historyPrefs
                .getString(
                    "items",
                    ""
                )
                .orEmpty()

        if (raw.isBlank()) {
            return emptyList()
        }

        return raw.lines()
            .mapNotNull { line ->

                val split =
                    line.split(
                        "\t",
                        limit = 2
                    )

                if (
                    split.size == 2
                ) {
                    split[0] to split[1]
                } else {
                    null
                }
            }
    }

    // =========================================================
    // URL
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
    // HOME SCREEN
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

        if (
            !::aurixUi.isInitialized
        ) {
            return
        }

        if (
            AurixService.isRunning
        ) {

            active = true

            aurixUi.setListeningState()

        } else {

            active = false

            aurixUi.setReadyState()
        }
    }

    // =========================================================
    // BACK
    // =========================================================

    @Suppress("DEPRECATION")
    override fun onBackPressed() {

        if (
            ::drawer.isInitialized &&
            drawer.visibility ==
            View.VISIBLE
        ) {

            drawer.visibility =
                View.GONE

            return
        }

        root.findViewWithTag<View>(
            "AURIX_PAGE"
        )?.let {

            root.removeView(it)

            currentPage =
                "Home"

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
}
