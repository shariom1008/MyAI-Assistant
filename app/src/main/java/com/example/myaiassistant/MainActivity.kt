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
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

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
    // COLORS
    // =========================================================

    private val bgTop =
        android.graphics.Color.rgb(5, 8, 28)

    private val bgBottom =
        android.graphics.Color.rgb(4, 12, 30)

    private val cyan =
        android.graphics.Color.rgb(80, 220, 255)

    private val blue =
        android.graphics.Color.rgb(45, 120, 255)

    private val purple =
        android.graphics.Color.rgb(145, 55, 240)

    private val white =
        android.graphics.Color.WHITE

    private val muted =
        android.graphics.Color.rgb(105, 155, 190)

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

                            if (::aurixUi.isInitialized) {
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

                            if (::aurixUi.isInitialized) {
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
    }

    // =========================================================
    // MAIN INTERFACE
    // =========================================================

    private fun createInterface() {

        root = FrameLayout(this)

        root.setBackgroundColor(
            android.graphics.Color.rgb(
                3,
                6,
                20
            )
        )

        setContentView(root)

        aurixUi =
            AurixOriginalUi(
                this,
                object :
                    AurixOriginalUi.Callbacks {

                    // -----------------------------------------
                    // HEADER
                    // -----------------------------------------

                    override fun onMenu() {
                        toggleDrawer()
                    }

                    override fun onVoice() {
                        startListeningOnce()
                    }

                    // -----------------------------------------
                    // QUICK ACTIONS
                    // -----------------------------------------

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

                    // -----------------------------------------
                    // BOTTOM NAVIGATION
                    // -----------------------------------------

                    override fun onHome() {
                        showHomePage()
                    }

                    override fun onHistory() {
                        showHistoryPage()
                    }

                    override fun onAurix() {
                        showHomePage()
                    }

                    override fun onShortcuts() {
                        showShortcutsPage()
                    }

                    override fun onSettings() {
                        showSettingsPage()
                    }
                }
            )

        val uiView =
            aurixUi.build()

        root.addView(
            uiView,
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

        if (requestCode != 500) {
            return
        }

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

        } catch (_: Exception) {

            active = false

            updateStatus("START FAILED")
        }
    }

    // =========================================================
    // SINGLE VOICE LISTEN
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
    // STATUS
    // =========================================================

    private fun updateStatus(
        status: String
    ) {

        runOnUiThread {

            if (!::aurixUi.isInitialized) {
                return@runOnUiThread
            }

            val clean =
                status
                    .uppercase(
                        java.util.Locale.getDefault()
                    )

            when {

                clean.contains("LISTEN") -> {
                    aurixUi.setListeningState()
                }

                clean.contains("THINK") ||
                        clean.contains("PROCESS") -> {
                    aurixUi.setThinkingState()
                }

                clean.contains("RESPOND") ||
                        clean.contains("SPEAK") -> {
                    aurixUi.voiceStatus.text =
                        "VOICE  •  RESPONDING"
                }

                else -> {
                    aurixUi.setReadyState()
                }
            }
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

                "${it.first}\t" +
                    it.second.replace(
                        "\n",
                        " "
                    )
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

        return raw
            .lines()
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

        if (!::drawer.isInitialized) {
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

                background =
                    android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                        intArrayOf(
                            android.graphics.Color.rgb(
                                7,
                                14,
                                38
                            ),
                            android.graphics.Color.rgb(
                                18,
                                7,
                                39
                            )
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

                text = "×"
                textSize = 30f
                gravity =
                    android.view.Gravity.RIGHT

                setTextColor(white)

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

        val logo =
            TextView(this).apply {

                text = "A U R I X"
                textSize = 27f

                typeface =
                    android.graphics.Typeface.DEFAULT_BOLD

                letterSpacing = 0.15f

                setTextColor(white)
            }

        drawer.addView(logo)

        val sub =
            TextView(this).apply {

                text =
                    "INTELLIGENCE CORE"

                textSize = 8f

                letterSpacing = 0.16f

                setTextColor(cyan)

                setPadding(
                    0,
                    dp(4),
                    0,
                    dp(24)
                )
            }

        drawer.addView(sub)

        addDrawerItem(
            "⌂",
            "Home"
        ) {
            drawer.visibility =
                View.GONE

            showHomePage()
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

            showHomePage()
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

        val accountText =
            TextView(this).apply {

                text =
                    account?.email
                        ?: "Google account"

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

        val item =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    android.view.Gravity.CENTER_VERTICAL

                setPadding(
                    dp(4),
                    dp(4),
                    dp(4),
                    dp(4)
                )

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

        val iconView =
            TextView(this).apply {

                text = icon
                textSize = 19f

                gravity =
                    android.view.Gravity.CENTER

                setTextColor(cyan)
            }

        item.addView(
            iconView,
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        val labelView =
            TextView(this).apply {

                text = label
                textSize = 12f

                setTextColor(white)
            }

        item.addView(
            labelView,
            LinearLayout.LayoutParams(
                0,
                dp(48),
                1f
            )
        )
    }

    // =========================================================
    // NAVIGATION / PAGES
    // =========================================================

    private fun navigate(
        label: String
    ) {

        currentPage =
            label

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

    private fun showHomePage() {

        currentPage =
            "Home"

        root.findViewWithTag<View>(
            "AURIX_PAGE"
        )?.let {
            root.removeView(it)
        }
    }

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
                    item.first ==
                        "AURIX"
                )
            }
        }

        val clear =
            TextView(this).apply {

                text =
                    "CLEAR LOCAL HISTORY"

                textSize = 10f

                gravity =
                    android.view.Gravity.CENTER

                typeface =
                    android.graphics.Typeface.DEFAULT_BOLD

                setTextColor(
                    android.graphics.Color.rgb(
                        255,
                        125,
                        155
                    )
                )

                background =
                    roundedBackground(
                        android.graphics.Color.argb(
                            28,
                            180,
                            40,
                            80
                        ),
                        android.graphics.Color.rgb(
                            180,
                            65,
                            100
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
                topMargin =
                    dp(14)
            }
        )
    }

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

            showHomePage()
        }
    }

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
                    android.view.Gravity.CENTER

                typeface =
                    android.graphics.Typeface.DEFAULT_BOLD

                setTextColor(
                    android.graphics.Color.rgb(
                        255,
                        145,
                        170
                    )
                )

                background =
                    roundedBackground(
                        android.graphics.Color.argb(
                            30,
                            180,
                            40,
                            80
                        ),
                        android.graphics.Color.rgb(
                            180,
                            65,
                            100
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

                background =
                    android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                        intArrayOf(
                            bgTop,
                            android.graphics.Color.rgb(
                                1,
                                5,
                                18
                            ),
                            bgBottom
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
            ScrollView(this).apply {

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
                    android.view.Gravity.CENTER_VERTICAL

                typeface =
                    android.graphics.Typeface.DEFAULT_BOLD

                setTextColor(cyan)

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

                text = title
                textSize = 23f

                typeface =
                    android.graphics.Typeface.DEFAULT_BOLD

                letterSpacing =
                    0.12f

                setTextColor(white)

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
    // SHORTCUT
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
                    android.view.Gravity.CENTER_VERTICAL

                setPadding(
                    dp(14),
                    dp(12),
                    dp(14),
                    dp(12)
                )

                background =
                    roundedBackground(
                        android.graphics.Color.argb(
                            34,
                            40,
                            90,
                            155
                        ),
                        android.graphics.Color.rgb(
                            45,
                            120,
                            185
                        )
                    )

                setOnClickListener {
                    action()
                }
            }

        val iconView =
            TextView(this).apply {

                text = icon
                textSize = 24f

                gravity =
                    android.view.Gravity.CENTER

                setTextColor(cyan)
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

        val titleView =
            TextView(this).apply {

                text = title
                textSize = 11f

                typeface =
                    android.graphics.Typeface.DEFAULT_BOLD

                setTextColor(white)
            }

        details.addView(
            titleView
        )

        val desc =
            TextView(this).apply {

                text = description
                textSize = 9f

                setTextColor(muted)

                setPadding(
                    0,
                    dp(5),
                    0,
                    0
                )
            }

        details.addView(
            desc
        )

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(82)
            ).apply {
                bottomMargin =
                    dp(10)
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

                background =
                    roundedBackground(
                        android.graphics.Color.argb(
                            32,
                            40,
                            85,
                            145
                        ),
                        android.graphics.Color.rgb(
                            35,
                            100,
                            155
                        )
                    )
            }

        val titleView =
            TextView(this).apply {

                text = title
                textSize = 8f

                typeface =
                    android.graphics.Typeface.DEFAULT_BOLD

                letterSpacing =
                    0.16f

                setTextColor(cyan)
            }

        card.addView(
            titleView
        )

        val valueView =
            TextView(this).apply {

                text = value
                textSize = 13f

                setTextColor(white)

                setPadding(
                    0,
                    dp(6),
                    0,
                    0
                )
            }

        card.addView(
            valueView
        )

        parent.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin =
                    dp(10)
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

                background =
                    roundedBackground(
                        if (aurix)
                            android.graphics.Color.argb(
                                35,
                                75,
                                150,
                                255
                            )
                        else
                            android.graphics.Color.argb(
                                25,
                                150,
                                70,
                                240
                            ),
                        if (aurix)
                            android.graphics.Color.rgb(
                                45,
                                145,
                                230
                            )
                        else
                            android.graphics.Color.rgb(
                                110,
                                70,
                                200
                            )
                    )
            }

        val label =
            TextView(this).apply {

                text = name
                textSize = 9f

                setTextColor(
                    if (aurix)
                        cyan
                    else
                        purple
                )

                typeface =
                    android.graphics.Typeface.DEFAULT_BOLD

                letterSpacing =
                    0.18f
            }

        box.addView(
            label,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(22)
            )
        )

        val textView =
            TextView(this).apply {

                text = message
                textSize = 14f

                setTextColor(white)
            }

        box.addView(
            textView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        parent.addView(
            box,
            cardParams()
        )
    }

    // =========================================================
    // SECTION TITLE
    // =========================================================

    private fun addSectionTitle(
        parent: LinearLayout,
        text: String
    ) {

        val titleView =
            TextView(this).apply {

                text = text
                textSize = 9f

                setTextColor(muted)

                typeface =
                    android.graphics.Typeface.DEFAULT_BOLD

                letterSpacing =
                    0.18f
            }

        parent.addView(
            titleView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(30)
            ).apply {
                topMargin =
                    dp(15)
            }
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

    // =========================================================
    // CARD PARAMS
    // =========================================================

    private fun cardParams():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
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
    }

    // =========================================================
    // BACKGROUND
    // =========================================================

    private fun roundedBackground(
        fill: Int,
        stroke: Int
    ):
        android.graphics.drawable.GradientDrawable {

        return android.graphics.drawable.GradientDrawable()
            .apply {

                cornerRadius =
                    dp(18).toFloat()

                setColor(fill)

                setStroke(
                    dp(1),
                    stroke
                )
            }
    }
}
