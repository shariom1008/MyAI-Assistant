package com.example.myaiassistant

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
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

class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var aurixUi: AurixOriginalUi
    private lateinit var drawer: LinearLayout

    private var active = false
    private var currentPage = "Home"

    private val historyPrefs by lazy {
        getSharedPreferences("aurix_history", MODE_PRIVATE)
    }

    private val bgTop = Color.rgb(2, 12, 28)
    private val bgBottom = Color.rgb(9, 2, 25)
    private val cyan = Color.rgb(80, 220, 255)
    private val muted = Color.rgb(105, 155, 190)
    private val white = Color.WHITE

    private val uiCallbacks =
        object : AurixOriginalUi.Callbacks {
            override fun onMenu() { toggleDrawer() }
            override fun onVoice() { startListeningOnce() }
            override fun onYouTube() { openUrl("https://www.youtube.com") }
            override fun onSearch() { openUrl("https://www.google.com") }
            override fun onMusic() { openUrl("https://music.youtube.com") }
            override fun onWeather() { openUrl("https://www.google.com/search?q=weather") }
            override fun onCall() {
                try { startActivity(Intent(Intent.ACTION_DIAL)) } catch (_: Exception) {}
            }
            override fun onMessages() {
                try { startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("smsto:") }) } catch (_: Exception) {}
            }
            override fun onApps() { performAppsAction() }
            override fun onMore() { showMoreApps() }
            override fun onHome() { navigate("Home") }
            override fun onHistory() { navigate("History") }
            override fun onAurix() { navigate("AURIX") }
            override fun onShortcuts() { navigate("Shortcuts") }
            override fun onSettings() { navigate("Settings") }
        }

    private val aurixReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != AurixService.ACTION_EVENT) return
                val type = intent.getStringExtra(AurixService.EXTRA_TYPE)
                val text = intent.getStringExtra(AurixService.EXTRA_TEXT) ?: ""
                when (type) {
                    AurixService.TYPE_STATUS -> updateStatus(text)
                    AurixService.TYPE_COMMAND -> {
                        updateStatus("THINKING")
                        if (text.isNotBlank()) {
                            addHistoryItem("YOU", text)
                            runOnUiThread {
                                if (::aurixUi.isInitialized) aurixUi.addUserMessage(text)
                            }
                        }
                    }
                    AurixService.TYPE_SPEAK -> {
                        updateStatus("RESPONDING")
                        if (text.isNotBlank()) {
                            addHistoryItem("AURIX", text)
                            runOnUiThread {
                                if (::aurixUi.isInitialized) aurixUi.addAurixMessage(text)
                            }
                        }
                    }
                }
            }
        }

    private val homeReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != "com.example.myaiassistant.GO_HOME") return
                goToHomeScreen()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(3, 6, 15)
        window.navigationBarColor = Color.BLACK
        createInterface()
        registerAurixReceiver()
        registerReceiver(
            homeReceiver,
            IntentFilter("com.example.myaiassistant.GO_HOME"),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Context.RECEIVER_NOT_EXPORTED else 0
        )
        requestMicrophonePermission()
        updateInterface()
    }

    private fun createInterface() {
        root = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(bgTop, Color.rgb(1, 5, 16), bgBottom)
            )
        }

        setContentView(root)

        aurixUi = AurixOriginalUi(this, uiCallbacks)
        val uiView = aurixUi.build()

        // LIVE BACKGROUND ONLY:
        // Keep AurixOriginalUi completely untouched. The animation is a
        // separate sibling behind it. No transparency is applied to the UI.
        val liveBackground = UnderwaterLiveBackground(this).apply {
            isClickable = false
            isFocusable = false
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        root.addView(
            liveBackground,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            uiView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 500)
        else startAurixService()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 500) return
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startAurixService()
        else { active = false; updateStatus("READY") }
    }

    private fun startAurixService() {
        val intent = Intent(this, AurixService::class.java).apply { action = AurixService.ACTION_START }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.startForegroundService(this, intent)
            else startService(intent)
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
        val intent = Intent(this, AurixService::class.java).apply { action = AurixService.ACTION_LISTEN_ONCE }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.startForegroundService(this, intent)
            else startService(intent)
            active = true
            updateStatus("LISTENING")
        } catch (_: Exception) {
            active = false
            updateStatus("READY")
        }
    }

    private fun updateStatus(status: String) {
        runOnUiThread {
            if (!::aurixUi.isInitialized) return@runOnUiThread
            val clean = status.uppercase()
            when {
                clean.contains("LISTEN") -> aurixUi.setListeningState()
                clean.contains("THINK") || clean.contains("PROCESS") -> aurixUi.setThinkingState()
                clean.contains("EXECUT") -> aurixUi.setThinkingState()
                clean.contains("RESPOND") || clean.contains("SPEAK") -> aurixUi.setThinkingState()
                else -> aurixUi.setReadyState()
            }
        }
    }

    private fun updateInterface() {
        if (!::aurixUi.isInitialized) return
        if (active) aurixUi.setListeningState() else aurixUi.setReadyState()
    }

    private fun navigate(label: String) {
        currentPage = label
        when (label) {
            "Home", "AURIX" -> closePageOverlay()
            "History" -> showHistoryPage()
            "Shortcuts" -> showShortcutsPage()
            "Settings" -> showSettingsPage()
        }
    }

    private fun closePageOverlay() {
        root.findViewWithTag<View>("AURIX_PAGE")?.let { root.removeView(it) }
        currentPage = "Home"
    }

    private fun showHistoryPage() {
        val overlay = createPageOverlay("HISTORY")
        val content = overlay.second
        addSectionTitle(content, "RECENT AURIX ACTIVITY")
        val history = getRecentHistory().asReversed()
        if (history.isEmpty()) addMessageCard(content, "AURIX", "No conversation history yet.", true)
        else history.forEach { item -> addMessageCard(content, item.first, item.second, item.first == "AURIX") }
        val clear = TextView(this).apply {
            text = "CLEAR LOCAL HISTORY"; textSize = 10f; gravity = Gravity.CENTER
            setTextColor(Color.rgb(255,125,155))
            background = roundedBackground(Color.argb(28,180,40,80), Color.rgb(180,65,100))
            setOnClickListener { historyPrefs.edit().clear().apply(); showHistoryPage() }
        }
        content.addView(clear, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(50)).apply { topMargin = dp(14) })
    }

    private fun showShortcutsPage() {
        val overlay = createPageOverlay("SHORTCUTS")
        val content = overlay.second
        addSectionTitle(content, "AURIX SHORTCUTS")
        addShortcut(content, "♫", "MUSIC", "Open YouTube Music") { openUrl("https://music.youtube.com") }
        addShortcut(content, "⌕", "WEB SEARCH", "Open Google Search") { openUrl("https://www.google.com") }
        addShortcut(content, "☁", "WEATHER", "Open current weather") { openUrl("https://www.google.com/search?q=weather") }
        addShortcut(content, "+", "NEW CONVERSATION", "Clear local conversation history") { historyPrefs.edit().clear().apply(); navigate("Home") }
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
            text = "SIGN OUT OF AURIX"; textSize = 11f; gravity = Gravity.CENTER
            setTextColor(Color.rgb(255,145,170))
            background = roundedBackground(Color.argb(30,180,40,80), Color.rgb(180,65,100))
            setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                finish()
            }
        }
        content.addView(signOut, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)))
    }

    private fun createPageOverlay(title: String): Pair<FrameLayout, LinearLayout> {
        root.findViewWithTag<View>("AURIX_PAGE")?.let { root.removeView(it) }
        val overlay = FrameLayout(this).apply {
            tag = "AURIX_PAGE"
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(bgTop, Color.rgb(1,5,18), bgBottom)
            )
        }
        root.addView(overlay, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        val scroll = ScrollView(this).apply { overScrollMode = View.OVER_SCROLL_NEVER }
        overlay.addView(scroll, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18),dp(28),dp(18),dp(30))
        }
        scroll.addView(content)
        val back = TextView(this).apply {
            text = "‹   AURIX"; textSize = 14f; gravity = Gravity.CENTER_VERTICAL; setTextColor(cyan)
            setOnClickListener { root.removeView(overlay); currentPage = "Home" }
        }
        content.addView(back, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)))
        val heading = TextView(this).apply {
            text = title; textSize = 23f; letterSpacing = 0.12f; setTextColor(white)
            setPadding(0,dp(10),0,dp(8))
        }
        content.addView(heading)
        return overlay to content
    }

    private fun addShortcut(parent: LinearLayout, icon: String, title: String, description: String, action: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14),dp(12),dp(14),dp(12))
            background = roundedBackground(Color.argb(34,40,90,155), Color.rgb(45,120,185))
            setOnClickListener { action() }
        }
        val iconView = TextView(this).apply { text = icon; textSize = 24f; gravity = Gravity.CENTER; setTextColor(cyan) }
        card.addView(iconView, LinearLayout.LayoutParams(dp(55),dp(55)))
        val details = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12),0,0,0) }
        card.addView(details, LinearLayout.LayoutParams(0,dp(55),1f))
        details.addView(TextView(this).apply { text = title; textSize = 11f; setTextColor(white) })
        details.addView(TextView(this).apply { text = description; textSize = 9f; setTextColor(muted); setPadding(0,dp(5),0,0) })
        parent.addView(card, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dp(82)).apply { bottomMargin=dp(10) })
    }

    private fun addSettingsRow(parent: LinearLayout, title: String, value: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(16),dp(13),dp(16),dp(13))
            background = roundedBackground(Color.argb(32,40,85,145), Color.rgb(35,100,155))
        }
        card.addView(TextView(this).apply { text=title; textSize=8f; setTextColor(cyan) })
        card.addView(TextView(this).apply { text=value; textSize=13f; setTextColor(white); setPadding(0,dp(6),0,0) })
        parent.addView(card, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin=dp(10) })
    }

    private fun addHistoryItem(role: String, text: String) {
        val items = getRecentHistory().toMutableList()
        items.add(role to text)
        while (items.size > 50) items.removeAt(0)
        val encoded = items.joinToString("\n") { "${it.first}\t${it.second.replace("\n"," ")}" }
        historyPrefs.edit().putString("items", encoded).apply()
    }

    private fun getRecentHistory(): List<Pair<String,String>> {
        val raw = historyPrefs.getString("items","").orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.lines().mapNotNull { line ->
            val split=line.split("\t",limit=2)
            if (split.size==2) split[0] to split[1] else null
        }
    }

    private fun toggleDrawer() {
        if (::drawer.isInitialized && drawer.visibility==View.VISIBLE) { drawer.visibility=View.GONE; return }
        if (!::drawer.isInitialized) createDrawer()
        drawer.visibility=View.VISIBLE
    }

    private fun createDrawer() {
        drawer = LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL; setPadding(dp(20),dp(38),dp(16),dp(24))
            background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(Color.rgb(7,14,38),Color.rgb(18,7,39)))
            elevation=dp(18).toFloat()
        }
        root.addView(drawer,FrameLayout.LayoutParams(dp(310),FrameLayout.LayoutParams.MATCH_PARENT))
        drawer.addView(TextView(this).apply {
            text="×"; textSize=30f; gravity=Gravity.RIGHT; setTextColor(white)
            setOnClickListener { drawer.visibility=View.GONE }
        },LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dp(45)))
        drawer.addView(TextView(this).apply { text="A U R I X"; textSize=27f; setTextColor(white) })
        drawer.addView(TextView(this).apply { text="INTELLIGENCE CORE"; textSize=8f; letterSpacing=0.16f; setTextColor(cyan); setPadding(0,dp(4),0,dp(24)) })
        addDrawerItem("⌂","Home"){drawer.visibility=View.GONE;navigate("Home")}
        addDrawerItem("◷","History"){drawer.visibility=View.GONE;navigate("History")}
        addDrawerItem("✦","Shortcuts"){drawer.visibility=View.GONE;navigate("Shortcuts")}
        addDrawerItem("♫","Music"){drawer.visibility=View.GONE;openUrl("https://music.youtube.com")}
        addDrawerItem("▦","Apps"){drawer.visibility=View.GONE;performAppsAction()}
        addDrawerItem("•••","More Apps"){drawer.visibility=View.GONE;performAppsAction()}
        addDrawerItem("+","New Conversation"){historyPrefs.edit().clear().apply();drawer.visibility=View.GONE;navigate("Home")}
        addDrawerItem("⚙","Settings"){drawer.visibility=View.GONE;navigate("Settings")}
        drawer.addView(View(this),LinearLayout.LayoutParams(1,0,1f))
        val account=FirebaseAuth.getInstance().currentUser
        drawer.addView(TextView(this).apply { text=account?.email ?: "Google account"; textSize=9f; setTextColor(muted) })
    }

    private fun addDrawerItem(icon: String,label: String,action:()->Unit) {
        val item=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(4),dp(4),dp(4),dp(4));setOnClickListener{action()} }
        drawer.addView(item,LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dp(52)))
        item.addView(TextView(this).apply{text=icon;textSize=19f;gravity=Gravity.CENTER;setTextColor(cyan)},LinearLayout.LayoutParams(dp(48),dp(48)))
        item.addView(TextView(this).apply{text=label;textSize=12f;setTextColor(white)},LinearLayout.LayoutParams(0,dp(48),1f))
    }

    private fun performAppsAction() {
        try { startActivity(Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }) } catch (_: Exception) {}
    }

    private fun showMoreApps() {
        val overlay=createPageOverlay("MORE APPS"); val content=overlay.second
        addSectionTitle(content,"AURIX APP LAUNCHER")
        addShortcut(content,"▶","YOUTUBE","Open YouTube"){openUrl("https://www.youtube.com")}
        addShortcut(content,"⌕","GOOGLE","Open Google Search"){openUrl("https://www.google.com")}
        addShortcut(content,"♫","YOUTUBE MUSIC","Open YouTube Music"){openUrl("https://music.youtube.com")}
        addShortcut(content,"☁","WEATHER","Open current weather"){openUrl("https://www.google.com/search?q=weather")}
        addShortcut(content,"▦","INSTALLED APPS","Open Android app launcher"){performAppsAction()}
        addShortcut(content,"⚙","SETTINGS","Open AURIX settings"){navigate("Settings")}
    }

    private fun addMessageCard(parent: LinearLayout,name:String,message:String,aurix:Boolean) {
        val box=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(12),dp(16),dp(12))
            background=roundedBackground(if(aurix)Color.argb(35,75,150,255)else Color.argb(25,150,70,240),if(aurix)Color.rgb(45,145,230)else Color.rgb(110,70,200))
        }
        box.addView(TextView(this).apply { text=name;textSize=9f;setTextColor(if(aurix)cyan else Color.rgb(145,80,245)) },LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dp(22)))
        box.addView(TextView(this).apply { text=message;textSize=14f;setTextColor(white) },LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
        parent.addView(box,LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT).apply{setMargins(0,dp(4),0,dp(4))})
    }

    private fun addSectionTitle(parent:LinearLayout,text:String) {
        parent.addView(TextView(this).apply { this.text=text;textSize=9f;setTextColor(muted);setPadding(0,dp(4),0,dp(4)) },LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dp(30)).apply{topMargin=dp(15)})
    }

    private fun openUrl(url:String) {
        try { startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url))) } catch (_:Exception) {}
    }

    fun goToHomeScreen() {
        try { startActivity(Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME);addCategory(Intent.CATEGORY_DEFAULT) }) } catch (_:Exception) {}
    }

    private fun registerAurixReceiver() {
        val filter=IntentFilter(AurixService.ACTION_EVENT)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU) registerReceiver(aurixReceiver,filter,Context.RECEIVER_NOT_EXPORTED)
        else @Suppress("DEPRECATION") registerReceiver(aurixReceiver,filter)
    }

    override fun onResume() {
        super.onResume()
        if(!::aurixUi.isInitialized)return
        if(AurixService.isRunning){active=true;aurixUi.setListeningState()}
        else {active=false;aurixUi.setReadyState()}
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if(::drawer.isInitialized&&drawer.visibility==View.VISIBLE){drawer.visibility=View.GONE;return}
        root.findViewWithTag<View>("AURIX_PAGE")?.let{root.removeView(it);currentPage="Home";return}
        super.onBackPressed()
    }

    override fun onDestroy() {
        try{unregisterReceiver(aurixReceiver)}catch(_:Exception){}
        try{unregisterReceiver(homeReceiver)}catch(_:Exception){}
        super.onDestroy()
    }

    private fun dp(value:Int):Int=(value*resources.displayMetrics.density).toInt()

    private fun roundedBackground(fill:Int,stroke:Int):GradientDrawable=GradientDrawable().apply{
        cornerRadius=dp(18).toFloat();setColor(fill);setStroke(dp(1),stroke)
    }
}