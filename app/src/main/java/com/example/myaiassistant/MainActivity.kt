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
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var originalUi: AurixOriginalUi
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

                        updateStatus(
                            "THINKING"
                        )

                        if (text.isNotBlank()) {
                            addHistoryItem(
                                "YOU",
                                text
                            )

                            if (
                                ::originalUi.isInitialized
                            ) {
                                originalUi.addUserMessage(
                                    text
                                )
                            }
                        }
                    }

                    AurixService.TYPE_SPEAK -> {

                        updateStatus(
                            "RESPONDING"
                        )

                        if (text.isNotBlank()) {
                            addHistoryItem(
                                "AURIX",
                                text
                            )

                            if (
                                ::originalUi.isInitialized
                            ) {
                                originalUi.addAurixMessage(
                                    text
                                )
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

        super.onCreate(
            savedInstanceState
        )

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
    // MAIN UI
    // =========================================================

    private fun createInterface() {

        root =
            FrameLayout(this)

        root.setBackgroundColor(
            android.graphics.Color.rgb(
                5,
                8,
                28
            )
        )

        setContentView(root)

        // -----------------------------------------------------
        // ORIGINAL APK UI
        // -----------------------------------------------------

        originalUi =
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
                        updateStatus(
                            "READY"
                        )
                    }

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

        root.addView(
            originalUi.build(),
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

            startAurixService()
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

        if (requestCode == 500) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                startAurixService()

            } else {

                active = false

                updateStatus(
                    "READY"
                )
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
    // ONE-TIME VOICE BACKUP
    // =========================================================

    private fun startListeningOnce() {

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

            updateStatus(
                "START FAILED"
            )
        }
    }

    // =========================================================
    // STATUS
    // =========================================================

    private fun updateStatus(
        status: String
    ) {

        runOnUiThread {

            if (
                !::originalUi.isInitialized
            ) {
                return@runOnUiThread
            }

            val clean =
                status.uppercase()

            when {

                clean.contains(
                    "LISTEN"
                ) -> {

                    originalUi.setListeningState()
                }

                clean.contains(
                    "THINK"
                ) ||
                clean.contains(
                    "PROCESS"
                ) -> {

                    originalUi.setThinkingState()
                }

                clean.contains(
                    "RESPOND"
                ) ||
                clean.contains(
                    "SPEAK"
                ) -> {

                    originalUi.setThinkingState()
                }

                else -> {

                    originalUi.setReadyState()
                }
            }
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

    private fun showHomePage() {

        currentPage = "Home"

        root.findViewWithTag<View>(
            "AURIX_PAGE"
        )?.let {
            root.removeView(it)
        }
    }

    // =========================================================
    // HISTORY PAGE
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
            android.widget.TextView(
                this
            ).apply {

                text =
                    "CLEAR LOCAL HISTORY"

                textSize = 10f

                gravity =
                    Gravity.CENTER

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
                topMargin = dp(14)
            }
        )
    }

    // =========================================================
    // SHORTCUTS PAGE
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

            showHomePage()
        }
    }

    // =========================================================
    // SETTINGS PAGE
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
            android.widget.TextView(
                this
            ).apply {

                text =
                    "SIGN OUT OF AURIX"

                textSize = 11f

                gravity =
                    Gravity.CENTER

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
    ):
        Pair<
            FrameLayout,
            LinearLayout
        > {

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
                    android.graphics.drawable
                        .GradientDrawable(
                            android.graphics.drawable
                                .GradientDrawable
                                .Orientation.TL_BR,
                            intArrayOf(
                                android.graphics.Color.rgb(
                                    5,
                                    8,
                                    28
                                ),
                                android.graphics.Color.rgb(
                                    1,
                                    5,
                                    18
                                ),
                                android.graphics.Color.rgb(
                                    9,
                                    2,
                                    25
                                )
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
            android.widget.ScrollView(
                this
            ).apply {
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
            android.widget.TextView(
                this
            ).apply {

                text =
                    "‹   AURIX"

                textSize = 14f

                gravity =
                    Gravity.CENTER_VERTICAL

                typeface =
                    android.graphics.Typeface
                        .DEFAULT_BOLD

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
            android.widget.TextView(
                this
            ).apply {

                text =
                    title

                textSize = 23f

                typeface =
                    android.graphics.Typeface
                        .DEFAULT_BOLD

                letterSpacing =
                    0.12f

                setTextColor(
                    android.graphics.Color.WHITE
                )

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
            android.widget.TextView(
                this
            ).apply {

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

        val titleView =
            android.widget.TextView(
                this
            ).apply {

                text =
                    title

                textSize = 11f

                typeface =
                    android.graphics.Typeface
                        .DEFAULT_BOLD

                setTextColor(
                    android.graphics.Color.WHITE
                )
            }

        details.addView(
            titleView
        )

        val desc =
            android.widget.TextView(
                this
            ).apply {

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
            android.widget.TextView(
                this
            ).apply {

                text =
                    title

                textSize = 8f

                typeface =
                    android.graphics.Typeface
                        .DEFAULT_BOLD

                letterSpacing =
                    0.16f

                setTextColor(
                    android.graphics.Color.rgb(
                        80,
                        220,
                        255
                    )
                )
            }

        card.addView(
            titleView
        )

        val valueView =
            android.widget.TextView(
                this
            ).apply {

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
    // SECTION TITLE
    // =========================================================

    private fun addSectionTitle(
        parent: LinearLayout,
        text: String
    ) {

        val titleView =
            android.widget.TextView(
                this
            ).apply {

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

                typeface =
                    android.graphics.Typeface
                        .DEFAULT_BOLD

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
    // MESSAGE CARD
    // =========================================================

    private fun createMessageCard(
        name: String,
        message: String,
        aurix: Boolean
    ): View {

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
            android.widget.TextView(
                this
            ).apply {

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

                typeface =
                    android.graphics.Typeface
                        .DEFAULT_BOLD

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

        val text =
            android.widget.TextView(
                this
            ).apply {

                this.text =
                    message

                textSize = 14f

                setTextColor(
                    android.graphics.Color.WHITE
                )
            }

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
            createMessageCard(
                name,
                message,
                aurix
            ),
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

                background =
                    android.graphics.drawable
                        .GradientDrawable(
                            android.graphics.drawable
                                .GradientDrawable
                                .Orientation.TL_BR,
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
            android.widget.TextView(
                this
            ).apply {

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

        val logo =
            android.widget.TextView(
                this
            ).apply {

                text =
                    "A U R I X"

                textSize = 27f

                typeface =
                    android.graphics.Typeface
                        .DEFAULT_BOLD

                letterSpacing =
                    0.15f

                setTextColor(
                    android.graphics.Color.WHITE
                )
            }

        drawer.addView(
            logo
        )

        val sub =
            android.widget.TextView(
                this
            ).apply {

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

        drawer.addView(
            sub
        )

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
            android.widget.TextView(
                this
            ).apply {

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

        drawer.addView(
            accountText
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
            android.widget.TextView(
                this
            ).apply {

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
            }

        item.addView(
            iconView,
            LinearLayout.LayoutParams(
                dp(48),
                dp(48)
            )
        )

        val labelView =
            android.widget.TextView(
                this
            ).apply {

                text =
                    label

                textSize = 12f

                setTextColor(
                    android.graphics.Color.WHITE
                )
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
            items.joinToString(
                "\n"
            ) {

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

        if (
            raw.isBlank()
        ) {
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

            startActivity(
                intent
            )

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

            @Suppress(
                "DEPRECATION"
            )

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
            !::originalUi.isInitialized
        ) {
            return
        }

        if (
            AurixService.isRunning
        ) {

            active = true

            updateStatus(
                "LISTENING"
            )

        } else {

            active = false

            updateStatus(
                "READY"
            )
        }
    }

    // =========================================================
    // BACK
    // =========================================================

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
                resources
                    .displayMetrics
                    .density
        ).toInt()
    }

    // =========================================================
    // BACKGROUND HELPER
    // =========================================================

    private fun roundedBackground(
        fill: Int,
        stroke: Int
    ):
        android.graphics.drawable.GradientDrawable {

        return android.graphics.drawable
            .GradientDrawable()
            .apply {

                cornerRadius =
                    dp(18).toFloat()

                setColor(
                    fill
                )

                setStroke(
                    dp(1),
                    stroke
                )
            }
    }
}
