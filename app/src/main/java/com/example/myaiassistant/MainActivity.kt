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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
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
    private lateinit var aurixUi: AurixOriginalUi
    private lateinit var drawer: LinearLayout

    private var active = false
    private var currentPage = "Home"

    private val historyPrefs by lazy {
        getSharedPreferences("aurix_history", MODE_PRIVATE)
    }

    private val cyan = Color.rgb(80, 220, 255)
    private val blue = Color.rgb(45, 120, 255)
    private val purple = Color.rgb(145, 55, 240)
    private val white = Color.WHITE
    private val muted = Color.rgb(105, 155, 190)

    private val aurixReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != AurixService.ACTION_EVENT) return

            val type = intent.getStringExtra(AurixService.EXTRA_TYPE)
            val text = intent.getStringExtra(AurixService.EXTRA_TEXT) ?: ""

            when (type) {
                AurixService.TYPE_STATUS -> updateStatus(text)
                AurixService.TYPE_COMMAND -> {
                    updateStatus("THINKING")
                    if (text.isNotBlank()) {
                        aurixUi.addUserMessage(text)
                        addHistoryItem("YOU", text)
                    }
                }
                AurixService.TYPE_SPEAK -> {
                    updateStatus("RESPONDING")
                    if (text.isNotBlank()) {
                        aurixUi.addAurixMessage(text)
                        addHistoryItem("AURIX", text)
                    }
                }
            }
        }
    }

    private val homeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.myaiassistant.GO_HOME") {
                goToHomeScreen()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.BLACK

        createInterface()
        registerAurixReceiver()
        registerReceiver(
            homeReceiver,
            IntentFilter("com.example.myaiassistant.GO_HOME"),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Context.RECEIVER_NOT_EXPORTED
            } else 0
        )

        requestMicrophonePermission()
        updateInterface()
    }

    private fun createInterface() {
        root = FrameLayout(this)
        aurixUi = AurixOriginalUi(this, object : AurixOriginalUi.Callbacks {
            override fun onMenu() = toggleDrawer()
            override fun onVoice() = startListeningOnce()
            override fun onYouTube() = openAppOrUrl("com.google.android.youtube", "https://www.youtube.com")
            override fun onSearch() = openUrl("https://www.google.com")
            override fun onMusic() = openAppOrUrl("com.google.android.apps.youtube.music", "https://music.youtube.com")
            override fun onWeather() = openUrl("https://www.google.com/search?q=weather")
            override fun onCall() = safeStartActivity(Intent(Intent.ACTION_DIAL))
            override fun onMessages() = safeStartActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("smsto:") })
            override fun onApps() = performAppsAction()
            override fun onMore() = showMoreApps()
            override fun onHome() = closePageOverlay()
            override fun onHistory() = showHistoryPage()
            override fun onAurix() = closePageOverlay()
            override fun onShortcuts() = showShortcutsPage()
            override fun onSettings() = showSettingsPage()
        })

        root.addView(
            aurixUi.build(),
            FrameLayout.LayoutParams(-1, -1)
        )
        setContentView(root)
    }

    private fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 500)
        } else {
            startAurixService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 500 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            startAurixService()
        } else if (requestCode == 500) {
            updateStatus("READY")
        }
    }

    private fun startAurixService() {
        val intent = Intent(this, AurixService::class.java).apply {
            action = AurixService.ACTION_START
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
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

    private fun startListeningOnce() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestMicrophonePermission()
            return
        }
        val intent = Intent(this, AurixService::class.java).apply {
            action = AurixService.ACTION_LISTEN_ONCE
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
            } else {
                startService(intent)
            }
            active = true
            updateStatus("LISTENING")
        } catch (_: Exception) {
            updateStatus("START FAILED")
        }
    }

    private fun updateStatus(status: String) {
        runOnUiThread {
            if (!::aurixUi.isInitialized) return@runOnUiThread
            val clean = status.uppercase(Locale.getDefault())
            when {
                clean.contains("LISTEN") -> aurixUi.setListeningState()
                clean.contains("THINK") || clean.contains("PROCESS") || clean.contains("VERIFY") -> aurixUi.setThinkingState()
                clean.contains("RESPOND") || clean.contains("SPEAK") || clean.contains("EXECUT") -> aurixUi.setThinkingState()
                else -> aurixUi.setReadyState()
            }
        }
    }

    private fun updateInterface() {
        if (!::aurixUi.isInitialized) return
        if (AurixService.isRunning) {
            active = true
            aurixUi.setListeningState()
        } else {
            active = false
            aurixUi.setReadyState()
        }
    }

    private fun addHistoryItem(role: String, text: String) {
        val items = getRecentHistory().toMutableList()
        items.add(role to text)
        while (items.size > 50) items.removeAt(0)
        val encoded = items.joinToString("\n") { "${it.first}\t${it.second.replace("\n", " ")}" }
        historyPrefs.edit().putString("items", encoded).apply()
    }

    private fun getRecentHistory(): List<Pair<String, String>> {
        val raw = historyPrefs.getString("items", "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.lines().mapNotNull { line ->
            val split = line.split("\t", limit = 2)
            if (split.size == 2) split[0] to split[1] else null
        }
    }

    private fun showHistoryPage() {
        val overlay = createPageOverlay("HISTORY")
        val content = overlay.second
        addSectionTitle(content, "RECENT AURIX ACTIVITY")
        val history = getRecentHistory().asReversed()
        if (history.isEmpty()) {
            addMessageCard(content, "AURIX", "No conversation history yet.", true)
        } else {
            history.forEach { addMessageCard(content, it.first, it.second, it.first == "AURIX") }
        }
        val clear = TextView(this).apply {
            text = "CLEAR LOCAL HISTORY"
            textSize = 10f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(255, 125, 155))
            background = roundedBackground(Color.argb(28, 180, 40, 80), Color.rgb(180, 65, 100))
            setOnClickListener { historyPrefs.edit().clear().apply(); showHistoryPage() }
        }
        content.addView(clear, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(14) })
    }

    private fun showShortcutsPage() {
        val overlay = createPageOverlay("SHORTCUTS")
        val content = overlay.second
        addSectionTitle(content, "AURIX SHORTCUTS")
        addShortcut(content, "♫", "MUSIC", "Open YouTube Music") { openAppOrUrl("com.google.android.apps.youtube.music", "https://music.youtube.com") }
        addShortcut(content, "⌕", "WEB SEARCH", "Open Google Search") { openUrl("https://www.google.com") }
        addShortcut(content, "☁", "WEATHER", "Open current weather") { openUrl("https://www.google.com/search?q=weather") }
        addShortcut(content, "▶", "YOUTUBE", "Open YouTube") { openAppOrUrl("com.google.android.youtube", "https://www.youtube.com") }
        addShortcut(content, "◎", "CHROME", "Open Chrome") { openAppOrUrl("com.android.chrome", "https://www.google.com") }
        addShortcut(content, "⌖", "MAPS", "Open Google Maps") { openAppOrUrl("com.google.android.apps.maps", "https://maps.google.com") }
        addShortcut(content, "+", "NEW CONVERSATION", "Clear local conversation history") { historyPrefs.edit().clear().apply(); closePageOverlay() }
    }

    private fun showSettingsPage() {
        val overlay = createPageOverlay("SETTINGS")
        val content = overlay.second
        addSectionTitle(content, "ACCOUNT")
        val user = FirebaseAuth.getInstance().currentUser
        addSettingsRow(content, "GOOGLE ACCOUNT", user?.email ?: "Not signed in")
        addSettingsRow(content, "PLAN", "AURIX Free")
        addSettingsRow(content, "VOICE MODE", "Wake word: AURIX • Tap backup available")
        addSettingsRow(content, "SYSTEM", "AURIX Core Online")
        addSectionTitle(content, "ACCOUNT ACTIONS")
        val signOut = TextView(this).apply {
            text = "SIGN OUT OF AURIX"
            textSize = 11f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(255, 145, 170))
            background = roundedBackground(Color.argb(30, 180, 40, 80), Color.rgb(180, 65, 100))
            setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                finish()
            }
        }
        content.addView(signOut, LinearLayout.LayoutParams(-1, dp(52)))
    }

    // More now opens a real AURIX app launcher page instead of doing nothing.
    private fun showMoreApps() {
        val overlay = createPageOverlay("MORE APPS")
        val content = overlay.second
        addSectionTitle(content, "EXTRA APPLICATIONS")

        addAppLauncher(content, "YouTube", "com.google.android.youtube", "https://www.youtube.com")
        addAppLauncher(content, "Chrome", "com.android.chrome", "https://www.google.com")
        addAppLauncher(content, "Maps", "com.google.android.apps.maps", "https://maps.google.com")
        addAppLauncher(content, "WhatsApp", "com.whatsapp", "https://web.whatsapp.com")
        addAppLauncher(content, "Instagram", "com.instagram.android", "https://www.instagram.com")
        addAppLauncher(content, "Gmail", "com.google.android.gm", "https://mail.google.com")
        addAppLauncher(content, "Telegram", "org.telegram.messenger", "https://telegram.org")
        addAppLauncher(content, "Spotify", "com.spotify.music", "https://open.spotify.com")
        addAppLauncher(content, "Calculator", "com.google.android.calculator", null)
        addAppLauncher(content, "Settings", null, null)
        addShortcut(content, "▣", "GALLERY", "Open device photos") { openGallery() }
        addShortcut(content, "▣", "CAMERA", "Open device camera") { openCamera() }
    }

    private fun addAppLauncher(parent: LinearLayout, label: String, packageName: String?, fallbackUrl: String?) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = roundedBackground(Color.argb(30, 35, 90, 145), Color.rgb(45, 100, 160))
            setOnClickListener {
                if (label == "Settings") {
                    safeStartActivity(Intent(Settings.ACTION_SETTINGS))
                } else if (packageName != null) {
                    openAppOrUrl(packageName, fallbackUrl)
                }
            }
        }
        val icon = TextView(this).apply {
            text = when (label) {
                "YouTube" -> "▶"
                "Chrome" -> "◎"
                "Maps" -> "⌖"
                "WhatsApp" -> "◉"
                "Instagram" -> "◎"
                "Gmail" -> "✉"
                "Telegram" -> "➤"
                "Spotify" -> "♫"
                "Calculator" -> "＋"
                "Settings" -> "⚙"
                else -> "•"
            }
            textSize = 21f
            gravity = Gravity.CENTER
            setTextColor(cyan)
        }
        card.addView(icon, LinearLayout.LayoutParams(dp(50), dp(50)))
        card.addView(TextView(this).apply {
            text = label
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }, LinearLayout.LayoutParams(0, dp(50), 1f))
        card.addView(TextView(this).apply {
            text = "›"
            textSize = 25f
            setTextColor(muted)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(35), dp(50)))
        parent.addView(card, LinearLayout.LayoutParams(-1, dp(70)).apply { bottomMargin = dp(8) })
    }

    private fun toggleDrawer() {
        if (::drawer.isInitialized && drawer.visibility == View.VISIBLE) {
            drawer.visibility = View.GONE
            return
        }
        if (!::drawer.isInitialized) createDrawer()
        drawer.visibility = View.VISIBLE
    }

    private fun createDrawer() {
        drawer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(38), dp(16), dp(24))
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(7, 14, 38), Color.rgb(18, 7, 39))
            )
            elevation = dp(18).toFloat()
        }
        root.addView(drawer, FrameLayout.LayoutParams(dp(310), -1))
        drawer.addView(TextView(this).apply {
            text = "×"
            textSize = 30f
            gravity = Gravity.RIGHT
            setTextColor(white)
            setOnClickListener { drawer.visibility = View.GONE }
        }, LinearLayout.LayoutParams(-1, dp(45)))
        drawer.addView(TextView(this).apply {
            text = "A U R I X"
            textSize = 27f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.15f
            setTextColor(white)
        })
        drawer.addView(TextView(this).apply {
            text = "INTELLIGENCE CORE"
            textSize = 8f
            letterSpacing = 0.16f
            setTextColor(cyan)
            setPadding(0, dp(4), 0, dp(24))
        })
        addDrawerItem("⌂", "Home") { drawer.visibility = View.GONE; closePageOverlay() }
        addDrawerItem("◷", "History") { drawer.visibility = View.GONE; showHistoryPage() }
        addDrawerItem("✦", "Shortcuts") { drawer.visibility = View.GONE; showShortcutsPage() }
        addDrawerItem("♫", "Music") { drawer.visibility = View.GONE; openAppOrUrl("com.google.android.apps.youtube.music", "https://music.youtube.com") }
        addDrawerItem("▦", "Apps") { drawer.visibility = View.GONE; performAppsAction() }
        addDrawerItem("•••", "More Apps") { drawer.visibility = View.GONE; showMoreApps() }
        addDrawerItem("+", "New Conversation") { historyPrefs.edit().clear().apply(); drawer.visibility = View.GONE; closePageOverlay() }
        addDrawerItem("⚙", "Settings") { drawer.visibility = View.GONE; showSettingsPage() }
        drawer.addView(View(this), LinearLayout.LayoutParams(1, 0, 1f))
        drawer.addView(TextView(this).apply {
            text = FirebaseAuth.getInstance().currentUser?.email ?: "Google account"
            textSize = 9f
            setTextColor(muted)
        })
    }

    private fun addDrawerItem(icon: String, label: String, action: () -> Unit) {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
            setOnClickListener { action() }
        }
        drawer.addView(item, LinearLayout.LayoutParams(-1, dp(52)))
        item.addView(TextView(this).apply {
            text = icon
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(cyan)
        }, LinearLayout.LayoutParams(dp(48), dp(48)))
        item.addView(TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(white)
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
    }

    private fun performAppsAction() {
        // Keep the Android launcher intent as the Apps action.
        try {
            startActivity(Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) })
        } catch (_: Exception) {
            showMoreApps()
        }
    }

    private fun openGallery() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent(MediaStore.ACTION_PICK_IMAGES)
            } else {
                Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            }
            safeStartActivity(intent)
        } catch (_: Exception) {
            openUrl("https://photos.google.com")
        }
    }

    private fun openCamera() {
        try {
            startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: android.content.ActivityNotFoundException) {
            updateStatus("READY")
        } catch (_: Exception) {
            updateStatus("READY")
        }
    }

    private fun createPageOverlay(title: String): Pair<FrameLayout, LinearLayout> {
        closePageOverlay()
        val overlay = FrameLayout(this).apply {
            tag = "AURIX_PAGE"
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(2, 12, 28), Color.rgb(1, 5, 18), Color.rgb(9, 2, 25))
            )
        }
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))
        val scroll = ScrollView(this).apply { overScrollMode = View.OVER_SCROLL_NEVER }
        overlay.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(28), dp(18), dp(30))
        }
        scroll.addView(content, ViewGroup.LayoutParams(-1, -2))
        content.addView(TextView(this).apply {
            text = "‹   AURIX"
            textSize = 14f
            gravity = Gravity.CENTER_VERTICAL
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(cyan)
            setOnClickListener { closePageOverlay() }
        }, LinearLayout.LayoutParams(-1, dp(48)))
        content.addView(TextView(this).apply {
            this.text = title
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.12f
            setTextColor(white)
            setPadding(0, dp(10), 0, dp(8))
        })
        currentPage = title
        return overlay to content
    }

    private fun closePageOverlay() {
        root.findViewWithTag<View>("AURIX_PAGE")?.let { root.removeView(it) }
        currentPage = "Home"
    }

    private fun addShortcut(parent: LinearLayout, icon: String, title: String, description: String, action: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedBackground(Color.argb(34, 40, 90, 155), Color.rgb(45, 120, 185))
            setOnClickListener { action() }
        }
        card.addView(TextView(this).apply {
            text = icon
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(cyan)
        }, LinearLayout.LayoutParams(dp(55), dp(55)))
        val details = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0) }
        card.addView(details, LinearLayout.LayoutParams(0, dp(55), 1f))
        details.addView(TextView(this).apply { text = title; textSize = 11f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white) })
        details.addView(TextView(this).apply { text = description; textSize = 9f; setTextColor(muted); setPadding(0, dp(5), 0, 0) })
        parent.addView(card, LinearLayout.LayoutParams(-1, dp(82)).apply { bottomMargin = dp(10) })
    }

    private fun addSettingsRow(parent: LinearLayout, title: String, value: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(13), dp(16), dp(13))
            background = roundedBackground(Color.argb(32, 40, 85, 145), Color.rgb(35, 100, 155))
        }
        card.addView(TextView(this).apply { text = title; textSize = 8f; typeface = Typeface.DEFAULT_BOLD; letterSpacing = 0.16f; setTextColor(cyan) })
        card.addView(TextView(this).apply { text = value; textSize = 13f; setTextColor(white); setPadding(0, dp(6), 0, 0) })
        parent.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
    }

    private fun addMessageCard(parent: LinearLayout, name: String, message: String, aurix: Boolean) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = roundedBackground(
                if (aurix) Color.argb(35, 75, 150, 255) else Color.argb(25, 150, 70, 240),
                if (aurix) Color.rgb(45, 145, 230) else Color.rgb(110, 70, 200)
            )
        }
        card.addView(TextView(this).apply {
            text = name
            textSize = 9f
            setTextColor(if (aurix) cyan else purple)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.18f
        }, LinearLayout.LayoutParams(-1, dp(22)))
        card.addView(TextView(this).apply { text = message; textSize = 14f; setTextColor(white) })
        parent.addView(card, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(4), 0, dp(4)) })
    }

    private fun addSectionTitle(parent: LinearLayout, text: String) {
        parent.addView(TextView(this).apply {
            this.text = text
            textSize = 9f
            setTextColor(muted)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.18f
        }, LinearLayout.LayoutParams(-1, dp(30)).apply { topMargin = dp(15) })
    }

    private fun openAppOrUrl(packageName: String, fallbackUrl: String?) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else if (!fallbackUrl.isNullOrBlank()) {
                openUrl(fallbackUrl)
            }
        } catch (_: Exception) {
            if (!fallbackUrl.isNullOrBlank()) openUrl(fallbackUrl)
        }
    }

    private fun safeStartActivity(intent: Intent) {
        try { startActivity(intent) } catch (_: Exception) { }
    }

    private fun openUrl(url: String) {
        safeStartActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun goToHomeScreen() {
        try {
            startActivity(Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            })
        } catch (_: Exception) { }
    }

    private fun registerAurixReceiver() {
        val filter = IntentFilter(AurixService.ACTION_EVENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(aurixReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(aurixReceiver, filter)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::aurixUi.isInitialized) {
            if (AurixService.isRunning) {
                active = true
                aurixUi.setListeningState()
            } else {
                active = false
                aurixUi.setReadyState()
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (::drawer.isInitialized && drawer.visibility == View.VISIBLE) {
            drawer.visibility = View.GONE
            return
        }
        root.findViewWithTag<View>("AURIX_PAGE")?.let {
            root.removeView(it)
            currentPage = "Home"
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        try { unregisterReceiver(aurixReceiver) } catch (_: Exception) { }
        try { unregisterReceiver(homeReceiver) } catch (_: Exception) { }
        super.onDestroy()
    }

    private fun roundedBackground(fill: Int, stroke: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            cornerRadius = dp(18).toFloat()
            setColor(fill)
            setStroke(dp(1), stroke)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
