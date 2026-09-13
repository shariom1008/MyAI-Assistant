package com.example.myaiassistant

import android.Manifest
import android.animation.ValueAnimator
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
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.min
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private lateinit var root: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var voiceButton: TextView
    private lateinit var voiceStatus: TextView
    private lateinit var conversationBox: LinearLayout
    private lateinit var aurixOrb: AurixOrbView

    companion object {
        private const val REQUEST_AUDIO = 1001
    }

    private var listening = false

    // -------------------------------------------------
    // SIDE DRAWER
    // -------------------------------------------------

    private var drawerView: LinearLayout? = null
    private var drawerOverlay: View? = null

    // -------------------------------------------------
    // AURIX STATUS RECEIVER
    // -------------------------------------------------

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

    // -------------------------------------------------
    // ACTIVITY
    // -------------------------------------------------

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

    override fun onBackPressed() {

        if (drawerView != null) {
            closeSideDrawer()
            return
        }

        super.onBackPressed()
    }

    override fun onDestroy() {

        try {
            unregisterReceiver(statusReceiver)
        } catch (_: Exception) {
        }

        super.onDestroy()
    }

    // =================================================
    // MAIN INTERFACE
    // =================================================

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

    // =================================================
    // HEADER
    // =================================================

    private fun buildHeader() {

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        // MENU BUTTON

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
                    openSideDrawer()
                }
            }

        header.addView(
            menu,
            LinearLayout.LayoutParams(
                dp(42),
                dp(48)
            )
        )

        // BRAND

        val brandBox =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        val brand =
            TextView(this).apply {

                text = "A U R I X"

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

                letterSpacing = 0.18f

                setTextColor(
                    Color.rgb(
                        120,
                        190,
                        255
                    )
                )
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

        // ONLINE

        val online =
            TextView(this).apply {

                text = "● ONLINE"

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

        // VOICE BUTTON

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
                    createVoiceBackground(false)

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

    // =================================================
    // TITLE
    // =================================================

    private fun buildTitle() {

        root.addView(
            TextView(this).apply {

                text =
                    "A U R I X  |  INTELLIGENCE CORE"

                textSize = 9f

                letterSpacing = 0.20f

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

    // =================================================
    // CORE
    // =================================================

    private fun buildCore() {

        val container =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER
            }

        aurixOrb =
            AurixOrbView(this)

        container.addView(
            aurixOrb,
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

                letterSpacing = 0.18f

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

        container.addView(statusText)

        root.addView(
            container,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(285)
            )
        )
    }

    // =================================================
    // CONVERSATION
    // =================================================

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
                                Color.red(accent),
                                Color.green(accent),
                                Color.blue(accent)
                            )
                        )
                    }
            }

        box.addView(
            TextView(this).apply {

                text = title

                textSize = 8f

                letterSpacing = 0.18f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(accent)
            }
        )

        box.addView(
            TextView(this).apply {

                text = message

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

        box.layoutParams = params

        return box
    }

    // =================================================
    // QUICK ACTIONS
    // =================================================

    private fun buildQuickActions() {

        root.addView(
            TextView(this).apply {

                text =
                    "QUICK ACTIONS"

                textSize = 9f

                letterSpacing = 0.18f

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

        root.addView(row1)

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

        root.addView(row2)
    }

    private fun addAction(
        row: LinearLayout,
        label: String,
        action: () -> Unit
    ) {

        val button =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(4),
                    dp(5),
                    dp(4),
                    dp(4)
                )

                background =
                    GradientDrawable().apply {

                        setColor(
                            Color.parseColor(
                                "#10162A"
                            )
                        )

                        cornerRadius =
                            dp(16).toFloat()

                        setStroke(
                            dp(1),
                            Color.parseColor(
                                "#263653"
                            )
                        )
                    }

                elevation =
                    dp(3).toFloat()

                setOnClickListener {
                    action()
                }
            }

        val icon =
            QuickActionIcon(
                this@MainActivity,
                label
            )

        button.addView(
            icon,
            LinearLayout.LayoutParams(
                dp(27),
                dp(27)
            ).apply {
                gravity =
                    Gravity.CENTER
            }
        )

        val text =
            TextView(this).apply {

                this.text = label

                setTextColor(
                    Color.parseColor(
                        "#E8ECF5"
                    )
                )

                textSize = 11f

                gravity =
                    Gravity.CENTER

                maxLines = 1

                ellipsize = null

                includeFontPadding =
                    false

                setPadding(
                    0,
                    dp(3),
                    0,
                    0
                )
            }

        button.addView(
            text,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(18)
            )
        )

        row.addView(
            button,
            LinearLayout.LayoutParams(
                0,
                dp(58),
                1f
            ).apply {

                setMargins(
                    dp(4),
                    dp(4),
                    dp(4),
                    dp(4)
                )
            }
        )
    }

    // =================================================
    // BOTTOM NAVIGATION
    // =================================================

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
            openSideDrawer()
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

        val item =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(2),
                    dp(2),
                    dp(2),
                    dp(2)
                )

                setOnClickListener {
                    action()
                }
            }

        val icon =
            BottomNavIcon(
                this@MainActivity,
                label,
                label == "AURIX"
            )

        item.addView(
            icon,
            LinearLayout.LayoutParams(
                dp(23),
                dp(23)
            ).apply {
                gravity =
                    Gravity.CENTER
            }
        )

        val text =
            TextView(this).apply {

                this.text = label

                textSize = 8f

                gravity =
                    Gravity.CENTER

                maxLines = 1

                includeFontPadding =
                    false

                setTextColor(
                    if (label == "AURIX") {
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

                setPadding(
                    0,
                    dp(2),
                    0,
                    0
                )
            }

        item.addView(
            text,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(17)
            )
        )

        nav.addView(
            item,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )
    }

    // =================================================
    // SIDE DRAWER
    // =================================================

    private fun openSideDrawer() {

        if (drawerView != null) {
            return
        }

        val overlay =
            View(this).apply {

                setBackgroundColor(
                    Color.argb(
                        160,
                        0,
                        0,
                        0
                    )
                )

                setOnClickListener {
                    closeSideDrawer()
                }
            }

        drawerOverlay = overlay

        addContentView(
            overlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val drawer =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(22),
                    dp(24),
                    dp(18),
                    dp(18)
                )

                background =
                    GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        intArrayOf(
                            Color.rgb(
                                8,
                                12,
                                35
                            ),
                            Color.rgb(
                                10,
                                18,
                                48
                            ),
                            Color.rgb(
                                5,
                                8,
                                26
                            )
                        )
                    )

                elevation =
                    dp(20).toFloat()

                translationX =
                    -dp(340).toFloat()
            }

        drawerView = drawer

        addContentView(
            drawer,
            ViewGroup.LayoutParams(
                dp(340),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val header =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        val brandBox =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL
            }

        brandBox.addView(
            TextView(this).apply {

                text =
                    "A U R I X"

                textSize = 22f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.WHITE
                )
            }
        )

        brandBox.addView(
            TextView(this).apply {

                text =
                    "INTELLIGENCE CORE"

                textSize = 8f

                letterSpacing = 0.20f

                setTextColor(
                    Color.rgb(
                        90,
                        210,
                        255
                    )
                )
            }
        )

        header.addView(
            brandBox,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        header.addView(
            TextView(this).apply {

                text =
                    "● ONLINE"

                textSize = 8f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.rgb(
                        70,
                        225,
                        190
                    )
                )
            }
        )

        drawer.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(60)
            )
        )

        drawer.addView(
            drawerDivider()
        )

        addDrawerItem(
            drawer,
            "＋",
            "New Conversation"
        ) {

            conversationBox.removeAllViews()

            addAurixMessage(
                "AURIX ready. Tap the voice icon to speak."
            )

            statusText.text =
                "New conversation started"

            closeSideDrawer()
        }

        addDrawerItem(
            drawer,
            "◷",
            "History"
        ) {

            statusText.text =
                "Conversation history"

            closeSideDrawer()
        }

        addDrawerItem(
            drawer,
            "⚡",
            "Shortcuts"
        ) {

            statusText.text =
                "Quick actions are ready"

            closeSideDrawer()
        }

        addDrawerItem(
            drawer,
            "♪",
            "Music"
        ) {

            closeSideDrawer()

            openMusic()
        }

        addDrawerItem(
            drawer,
            "▦",
            "Apps"
        ) {

            closeSideDrawer()

            openApps()
        }

        addDrawerItem(
            drawer,
            "⚙",
            "Settings"
        ) {

            closeSideDrawer()

            openMore()
        }

        addDrawerItem(
            drawer,
            "ⓘ",
            "About AURIX"
        ) {

            statusText.text =
                "AURIX • Personal AI Assistant"

            closeSideDrawer()
        }

        drawer.addView(
            View(this),
            LinearLayout.LayoutParams(
                1,
                0,
                1f
            )
        )

        drawer.addView(
            drawerDivider()
        )

        drawer.addView(
            TextView(this).apply {

                text =
                    "GOOGLE ACCOUNT"

                textSize = 8f

                letterSpacing = 0.18f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.rgb(
                        100,
                        180,
                        240
                    )
                )

                setPadding(
                    0,
                    dp(8),
                    0,
                    dp(5)
                )
            }
        )

        val accountEmail =
            FirebaseAuth
                .getInstance()
                .currentUser
                ?.email
                ?: "Google account"

        drawer.addView(
            TextView(this).apply {

                text =
                    accountEmail

                textSize = 11f

                setTextColor(
                    Color.WHITE
                )

                setPadding(
                    0,
                    0,
                    0,
                    dp(12)
                )

                maxLines = 1

                ellipsize =
                    android.text.TextUtils.TruncateAt.END
            }
        )

        addDrawerItem(
            drawer,
            "⇥",
            "Sign out"
        ) {

            signOutGoogle()
        }

        drawer.animate()
            .translationX(0f)
            .setDuration(280)
            .setInterpolator(
                android.view.animation.DecelerateInterpolator()
            )
            .start()
    }

    private fun addDrawerItem(
        drawer: LinearLayout,
        icon: String,
        title: String,
        action: () -> Unit
    ) {

        val item =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL

                setPadding(
                    dp(12),
                    0,
                    dp(10),
                    0
                )

                background =
                    GradientDrawable().apply {

                        cornerRadius =
                            dp(14).toFloat()

                        setColor(
                            Color.argb(
                                45,
                                50,
                                80,
                                140
                            )
                        )

                        setStroke(
                            dp(1),
                            Color.argb(
                                60,
                                90,
                                180,
                                255
                            )
                        )
                    }

                isClickable = true

                setOnClickListener {
                    action()
                }
            }

        val iconView =
            TextView(this).apply {

                text = icon

                textSize = 19f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        100,
                        215,
                        255
                    )
                )
            }

        item.addView(
            iconView,
            LinearLayout.LayoutParams(
                dp(42),
                dp(48)
            )
        )

        item.addView(
            TextView(this).apply {

                text = title

                textSize = 11f

                setTextColor(
                    Color.WHITE
                )

                gravity =
                    Gravity.CENTER_VERTICAL
            }
        )

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
            )

        params.setMargins(
            0,
            dp(4),
            0,
            dp(4)
        )

        drawer.addView(
            item,
            params
        )
    }

    private fun drawerDivider(): View {

        return View(this).apply {

            setBackgroundColor(
                Color.argb(
                    70,
                    100,
                    180,
                    255
                )
            )

            layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(1)
                ).apply {

                    setMargins(
                        0,
                        dp(8),
                        0,
                        dp(10)
                    )
                }
        }
    }

    private fun closeSideDrawer() {

        val drawer =
            drawerView
                ?: return

        drawer.animate()
            .translationX(
                -dp(350).toFloat()
            )
            .setDuration(220)
            .setInterpolator(
                android.view.animation.AccelerateInterpolator()
            )
            .withEndAction {

                try {
                    (
                        drawer.parent
                            as? ViewGroup
                    )?.removeView(drawer)
                } catch (_: Exception) {
                }

                try {
                    (
                        drawerOverlay?.parent
                            as? ViewGroup
                    )?.removeView(
                        drawerOverlay
                    )
                } catch (_: Exception) {
                }

                drawerView = null
                drawerOverlay = null
            }
            .start()
    }

    // =================================================
    // GOOGLE SIGN OUT
    // =================================================

    private fun signOutGoogle() {

        val firebaseAuth =
            FirebaseAuth.getInstance()

        firebaseAuth.signOut()

        try {

            val credentialManager =
                CredentialManager.create(
                    this
                )

            lifecycleScope.launch {

                try {

                    credentialManager
                        .clearCredentialState(
                            ClearCredentialStateRequest()
                        )

                } catch (_: Exception) {
                }

                goToAuthActivity()
            }

        } catch (_: Exception) {

            goToAuthActivity()
        }
    }

    private fun goToAuthActivity() {

        closeSideDrawer()

        val intent =
            Intent(
                this,
                AuthActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(intent)

        finish()
    }

    // =================================================
    // VOICE
    // =================================================

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

        voiceButton.text = "●"

        voiceButton.background =
            createVoiceBackground(true)

        voiceStatus.text =
            "VOICE  •  LISTENING"

        statusText.text =
            "Listening for your command..."

        aurixOrb.setMode(
            AurixOrbView.Mode.LISTENING
        )

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

            aurixOrb.setMode(
                AurixOrbView.Mode.READY
            )

            voiceStatus.text =
                "VOICE  •  MIC REQUIRED"

            statusText.text =
                "Microphone permission is required"

            voiceButton.text =
                "◉"

            voiceButton.background =
                createVoiceBackground(false)
        }
    }

    // =================================================
    // VOICE STATES
    // =================================================

    private fun setListeningState() {

        listening = true

        aurixOrb.setMode(
            AurixOrbView.Mode.LISTENING
        )

        voiceButton.text = "●"

        voiceButton.background =
            createVoiceBackground(true)

        voiceStatus.text =
            "VOICE  •  LISTENING"

        statusText.text =
            "Listening for your command..."
    }

    private fun setThinkingState() {

        listening = true

        aurixOrb.setMode(
            AurixOrbView.Mode.THINKING
        )

        voiceButton.text = "●"

        voiceButton.background =
            createVoiceBackground(true)

        voiceStatus.text =
            "VOICE  •  THINKING"

        statusText.text =
            "Processing your command..."
    }

    private fun setReadyState() {

        listening = false

        aurixOrb.setMode(
            AurixOrbView.Mode.READY
        )

        voiceButton.text = "◉"

        voiceButton.background =
            createVoiceBackground(false)

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

    // =================================================
    // QUICK ACTION TARGETS
    // =================================================

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

    // =================================================
    // DP
    // =================================================

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
        ).toInt()
    }

    // =================================================
    // AURIX ADVANCED CORE
    // =================================================

    class AurixOrbView(
        context: Context
    ) : View(context) {

        enum class Mode {
            READY,
            LISTENING,
            THINKING
        }

        private val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private val path =
            Path()

        private var rotation = 0f
        private var pulse = 0f
        private var mode = Mode.READY

        private val animator =
            ValueAnimator.ofFloat(
                0f,
                360f
            ).apply {

                duration = 7000L

                repeatCount =
                    ValueAnimator.INFINITE

                addUpdateListener {

                    rotation =
                        it.animatedValue as Float

                    pulse =
                        (
                            Math.sin(
                                Math.toRadians(
                                    rotation.toDouble()
                                )
                            ) *
                                0.5 +
                                0.5
                        ).toFloat()

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

        fun setMode(
            newMode: Mode
        ) {

            mode = newMode

            when (newMode) {

                Mode.READY -> {
                    animator.duration = 7000L
                }

                Mode.LISTENING -> {
                    animator.duration = 3200L
                }

                Mode.THINKING -> {
                    animator.duration = 1800L
                }
            }

            invalidate()
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

            val baseRadius =
                min(
                    width,
                    height
                ) * 0.285f

            val pulseRadius =
                baseRadius *
                    (
                        1f +
                            pulse *
                            when (mode) {

                                Mode.READY ->
                                    0.025f

                                Mode.LISTENING ->
                                    0.10f

                                Mode.THINKING ->
                                    0.055f
                            }
                    )

            // =================================================
            // CLEAR
            // =================================================

            canvas.drawColor(
                Color.TRANSPARENT,
                PorterDuff.Mode.CLEAR
            )

            // =================================================
            // ATMOSPHERIC GLOW
            // =================================================

            paint.style =
                Paint.Style.FILL

            paint.shader =
                RadialGradient(
                    cx,
                    cy,
                    baseRadius * 3f,
                    intArrayOf(
                        Color.argb(
                            when (mode) {

                                Mode.READY -> 45
                                Mode.LISTENING -> 75
                                Mode.THINKING -> 60
                            },
                            30,
                            190,
                            255
                        ),

                        Color.argb(
                            18,
                            70,
                            90,
                            255
                        ),

                        Color.TRANSPARENT
                    ),
                    floatArrayOf(
                        0f,
                        0.45f,
                        1f
                    ),
                    Shader.TileMode.CLAMP
                )

            canvas.drawCircle(
                cx,
                cy,
                baseRadius * 3f,
                paint
            )

            // =================================================
            // OUTER HUD RINGS
            // =================================================

            paint.shader = null

            paint.style =
                Paint.Style.STROKE

            paint.strokeCap =
                Paint.Cap.ROUND

            canvas.save()

            canvas.rotate(
                rotation,
                cx,
                cy
            )

            // Outer ring

            paint.strokeWidth =
                dp(1.4f)

            paint.color =
                Color.argb(
                    100,
                    80,
                    190,
                    255
                )

            canvas.drawCircle(
                cx,
                cy,
                baseRadius * 1.52f,
                paint
            )

            // Segmented cyan ring

            paint.strokeWidth =
                dp(2.2f)

            paint.color =
                Color.rgb(
                    65,
                    200,
                    255
                )

            canvas.drawArc(
                cx - baseRadius * 1.40f,
                cy - baseRadius * 1.40f,
                cx + baseRadius * 1.40f,
                cy + baseRadius * 1.40f,
                -25f,
                105f,
                false,
                paint
            )

            canvas.drawArc(
                cx - baseRadius * 1.40f,
                cy - baseRadius * 1.40f,
                cx + baseRadius * 1.40f,
                cy + baseRadius * 1.40f,
                155f,
                80f,
                false,
                paint
            )

            // Inner rotating ring

            paint.strokeWidth =
                dp(2.8f)

            paint.color =
                Color.rgb(
                    100,
                    225,
                    255
                )

            canvas.drawArc(
                cx - baseRadius * 1.22f,
                cy - baseRadius * 1.22f,
                cx + baseRadius * 1.22f,
                cy + baseRadius * 1.22f,
                rotation * 1.5f,
                85f,
                false,
                paint
            )

            canvas.restore()

            // =================================================
            // ORBIT RING
            // =================================================

            canvas.save()

            canvas.rotate(
                -rotation * 0.7f,
                cx,
                cy
            )

            paint.strokeWidth =
                dp(2.5f)

            paint.color =
                Color.rgb(
                    45,
                    190,
                    255
                )

            val orbitRect =
                RectF(
                    cx - baseRadius * 1.45f,
                    cy - baseRadius * 0.62f,
                    cx + baseRadius * 1.45f,
                    cy + baseRadius * 0.62f
                )

            canvas.drawOval(
                orbitRect,
                paint
            )

            canvas.restore()

            // =================================================
            // CORE OUTER GLOW
            // =================================================

            paint.style =
                Paint.Style.FILL

            paint.shader =
                RadialGradient(
                    cx - baseRadius * 0.30f,
                    cy - baseRadius * 0.32f,
                    pulseRadius * 1.45f,
                    intArrayOf(
                        Color.rgb(
                            190,
                            250,
                            255
                        ),

                        Color.rgb(
                            40,
                            190,
                            255
                        ),

                        Color.rgb(
                            50,
                            70,
                            220
                        ),

                        Color.rgb(
                            15,
                            12,
                            70
                        )
                    ),
                    floatArrayOf(
                        0f,
                        0.30f,
                        0.68f,
                        1f
                    ),
                    Shader.TileMode.CLAMP
                )

            canvas.drawCircle(
                cx,
                cy,
                pulseRadius,
                paint
            )

            // =================================================
            // INNER GLASS
            // =================================================

            paint.shader =
                RadialGradient(
                    cx - baseRadius * 0.25f,
                    cy - baseRadius * 0.28f,
                    baseRadius * 0.90f,
                    intArrayOf(
                        Color.argb(
                            210,
                            30,
                            130,
                            255
                        ),

                        Color.argb(
                            130,
                            30,
                            55,
                            190
                        ),

                        Color.argb(
                            180,
                            5,
                            10,
                            60
                        )
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )

            canvas.drawCircle(
                cx,
                cy,
                baseRadius * 0.90f,
                paint
            )

            // =================================================
            // CORE BORDER
            // =================================================

            paint.shader = null

            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth =
                when (mode) {

                    Mode.READY ->
                        dp(2f)

                    Mode.LISTENING ->
                        dp(3f)

                    Mode.THINKING ->
                        dp(2.5f)
                }

            paint.color =
                when (mode) {

                    Mode.READY ->
                        Color.rgb(
                            75,
                            215,
                            255
                        )

                    Mode.LISTENING ->
                        Color.rgb(
                            120,
                            245,
                            255
                        )

                    Mode.THINKING ->
                        Color.rgb(
                            145,
                            110,
                            255
                        )
                }

            canvas.drawCircle(
                cx,
                cy,
                baseRadius * 0.90f,
                paint
            )

            // =================================================
            // PROCESSING LIGHT
            // =================================================

            canvas.save()

            canvas.rotate(
                rotation * 2f,
                cx,
                cy
            )

            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth =
                dp(3f)

            paint.color =
                when (mode) {

                    Mode.READY ->
                        Color.argb(
                            160,
                            90,
                            220,
                            255
                        )

                    Mode.LISTENING ->
                        Color.argb(
                            230,
                            100,
                            245,
                            255
                        )

                    Mode.THINKING ->
                        Color.argb(
                            220,
                            170,
                            120,
                            255
                        )
                }

            canvas.drawArc(
                cx - baseRadius * 0.78f,
                cy - baseRadius * 0.78f,
                cx + baseRadius * 0.78f,
                cy + baseRadius * 0.78f,
                0f,
                55f,
                false,
                paint
            )

            canvas.restore()

            // =================================================
            // LISTENING WAVEFORM
            // =================================================

            if (
                mode ==
                Mode.LISTENING
            ) {

                paint.style =
                    Paint.Style.STROKE

                paint.strokeWidth =
                    dp(2f)

                paint.color =
                    Color.rgb(
                        80,
                        235,
                        255
                    )

                val bars = 13

                for (i in 0 until bars) {

                    val x =
                        cx -
                            baseRadius * 1.55f +
                            i *
                            (
                                baseRadius *
                                    3.10f /
                                    (bars - 1)
                            )

                    val wave =
                        Math.sin(
                            (
                                rotation *
                                    0.10f +
                                    i *
                                    38f
                            ).toDouble()
                        ).toFloat()

                    val barHeight =
                        baseRadius *
                            (
                                0.08f +
                                    0.18f *
                                    (
                                        wave + 1f
                                    ) /
                                    2f
                            )

                    canvas.drawLine(
                        x,
                        cy - barHeight,
                        x,
                        cy + barHeight,
                        paint
                    )
                }
            }

            // =================================================
            // THINKING PROCESSING DOTS
            // =================================================

            if (
                mode ==
                Mode.THINKING
            ) {

                paint.style =
                    Paint.Style.FILL

                for (i in 0 until 8) {

                    val angle =
                        Math.toRadians(
                            (
                                rotation * 1.8f +
                                    i * 45f
                            ).toDouble()
                        )

                    val distance =
                        baseRadius * 1.62f

                    val x =
                        cx +
                            Math.cos(angle)
                                .toFloat() *
                            distance

                    val y =
                        cy +
                            Math.sin(angle)
                                .toFloat() *
                            distance

                    val dot =
                        baseRadius *
                            (
                                0.025f +
                                    0.025f *
                                    (
                                        (
                                            Math.sin(
                                                angle
                                            ).toFloat() +
                                                1f
                                        ) / 2f
                                    )
                            )

                    paint.color =
                        Color.rgb(
                            150,
                            120,
                            255
                        )

                    canvas.drawCircle(
                        x,
                        y,
                        dot,
                        paint
                    )
                }
            }

            // =================================================
            // CORE HIGHLIGHT
            // =================================================

            paint.shader = null

            paint.style =
                Paint.Style.FILL

            paint.color =
                Color.argb(
                    180,
                    220,
                    250,
                    255
                )

            canvas.drawCircle(
                cx - baseRadius * 0.34f,
                cy - baseRadius * 0.36f,
                baseRadius * 0.075f,
                paint
            )

            // =================================================
            // AURIX TEXT
            // =================================================

            paint.textAlign =
                Paint.Align.CENTER

            paint.typeface =
                Typeface.DEFAULT_BOLD

            paint.textSize =
                baseRadius * 0.27f

            paint.color =
                Color.WHITE

            canvas.drawText(
                "AURIX",
                cx,
                cy + baseRadius * 0.08f,
                paint
            )

            // =================================================
            // CORE TEXT
            // =================================================

            paint.textSize =
                baseRadius * 0.085f

            paint.color =
                when (mode) {

                    Mode.READY ->
                        Color.rgb(
                            170,
                            230,
                            255
                        )

                    Mode.LISTENING ->
                        Color.rgb(
                            180,
                            250,
                            255
                        )

                    Mode.THINKING ->
                        Color.rgb(
                            205,
                            185,
                            255
                        )
                }

            canvas.drawText(
                "CORE",
                cx,
                cy + baseRadius * 0.31f,
                paint
            )
        }

        private fun dp(
            value: Float
        ): Float {

            return value *
                resources.displayMetrics.density
        }
    }

    // =================================================
    // QUICK ACTION ICON
    // =================================================

    private class QuickActionIcon(
        context: Context,
        private val label: String
    ) : View(context) {

        private val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private val path =
            Path()

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            val w =
                width.toFloat()

            val h =
                height.toFloat()

            paint.style =
                Paint.Style.FILL

            paint.strokeWidth =
                dp(2.2f)

            when (label.lowercase()) {

                // ------------------------------------------------
                // YOUTUBE
                // ------------------------------------------------

                "youtube" -> {

                    paint.color =
                        Color.parseColor(
                            "#FF0033"
                        )

                    val rect =
                        RectF(
                            w * 0.08f,
                            h * 0.20f,
                            w * 0.92f,
                            h * 0.80f
                        )

                    canvas.drawRoundRect(
                        rect,
                        dp(5f),
                        dp(5f),
                        paint
                    )

                    paint.color =
                        Color.WHITE

                    path.reset()

                    path.moveTo(
                        w * 0.43f,
                        h * 0.35f
                    )

                    path.lineTo(
                        w * 0.43f,
                        h * 0.65f
                    )

                    path.lineTo(
                        w * 0.70f,
                        h * 0.50f
                    )

                    path.close()

                    canvas.drawPath(
                        path,
                        paint
                    )
                }

                // ------------------------------------------------
                // SEARCH
                // ------------------------------------------------

                "search" -> {

                    paint.color =
                        Color.parseColor(
                            "#22D3EE"
                        )

                    paint.style =
                        Paint.Style.STROKE

                    paint.strokeWidth =
                        dp(2.5f)

                    canvas.drawCircle(
                        w * 0.43f,
                        h * 0.42f,
                        w * 0.25f,
                        paint
                    )

                    canvas.drawLine(
                        w * 0.61f,
                        h * 0.61f,
                        w * 0.84f,
                        h * 0.84f,
                        paint
                    )

                    paint.style =
                        Paint.Style.FILL
                }

                // ------------------------------------------------
                // MUSIC
                // ------------------------------------------------

                "music" -> {

                    paint.color =
                        Color.parseColor(
                            "#D946EF"
                        )

                    paint.style =
                        Paint.Style.STROKE

                    paint.strokeWidth =
                        dp(2.8f)

                    canvas.drawLine(
                        w * 0.65f,
                        h * 0.20f,
                        w * 0.65f,
                        h * 0.67f,
                        paint
                    )

                    canvas.drawLine(
                        w * 0.65f,
                        h * 0.20f,
                        w * 0.86f,
                        h * 0.14f,
                        paint
                    )

                    paint.style =
                        Paint.Style.FILL

                    canvas.drawCircle(
                        w * 0.48f,
                        h * 0.70f,
                        dp(4f),
                        paint
                    )

                    canvas.drawCircle(
                        w * 0.76f,
                        h * 0.64f,
                        dp(4f),
                        paint
                    )
                }

                // ------------------------------------------------
                // WEATHER
                // ------------------------------------------------

                "weather" -> {

                    paint.color =
                        Color.parseColor(
                            "#FACC15"
                        )

                    canvas.drawCircle(
                        w * 0.38f,
                        h * 0.38f,
                        dp(6f),
                        paint
                    )

                    paint.color =
                        Color.parseColor(
                            "#38BDF8"
                        )

                    canvas.drawCircle(
                        w * 0.43f,
                        h * 0.62f,
                        dp(7f),
                        paint
                    )

                    canvas.drawCircle(
                        w * 0.62f,
                        h * 0.57f,
                        dp(6f),
                        paint
                    )

                    canvas.drawRoundRect(
                        RectF(
                            w * 0.28f,
                            h * 0.57f,
                            w * 0.78f,
                            h * 0.78f
                        ),
                        dp(7f),
                        dp(7f),
                        paint
                    )
                }

                // ------------------------------------------------
                // CALL
                // ------------------------------------------------

                "call" -> {

                    paint.color =
                        Color.parseColor(
                            "#22C55E"
                        )

                    paint.style =
                        Paint.Style.STROKE

                    paint.strokeWidth =
                        dp(3.5f)

                    path.reset()

                    path.moveTo(
                        w * 0.30f,
                        h * 0.25f
                    )

                    path.cubicTo(
                        w * 0.22f,
                        h * 0.42f,
                        w * 0.45f,
                        h * 0.73f,
                        w * 0.70f,
                        h * 0.72f
                    )

                    path.lineTo(
                        w * 0.82f,
                        h * 0.58f
                    )

                    canvas.drawPath(
                        path,
                        paint
                    )

                    paint.style =
                        Paint.Style.FILL
                }

                // ------------------------------------------------
                // MESSAGES
                // ------------------------------------------------

                "messages" -> {

                    paint.color =
                        Color.parseColor(
                            "#3B82F6"
                        )

                    canvas.drawRoundRect(
                        RectF(
                            w * 0.12f,
                            h * 0.20f,
                            w * 0.88f,
                            h * 0.70f
                        ),
                        dp(7f),
                        dp(7f),
                        paint
                    )

                    path.reset()

                    path.moveTo(
                        w * 0.28f,
                        h * 0.68f
                    )

                    path.lineTo(
                        w * 0.23f,
                        h * 0.86f
                    )

                    path.lineTo(
                        w * 0.45f,
                        h * 0.70f
                    )

                    path.close()

                    canvas.drawPath(
                        path,
                        paint
                    )

                    paint.color =
                        Color.WHITE

                    canvas.drawCircle(
                        w * 0.36f,
                        h * 0.45f,
                        dp(2f),
                        paint
                    )

                    canvas.drawCircle(
                        w * 0.50f,
                        h * 0.45f,
                        dp(2f),
                        paint
                    )

                    canvas.drawCircle(
                        w * 0.64f,
                        h * 0.45f,
                        dp(2f),
                        paint
                    )
                }

                // ------------------------------------------------
                // APPS
                // ------------------------------------------------

                "apps" -> {

                    val colors =
                        intArrayOf(
                            Color.parseColor(
                                "#22D3EE"
                            ),
                            Color.parseColor(
                                "#A855F7"
                            ),
                            Color.parseColor(
                                "#F43F5E"
                            ),
                            Color.parseColor(
                                "#FACC15"
                            )
                        )

                    val positions =
                        arrayOf(
                            floatArrayOf(
                                0.30f,
                                0.30f
                            ),
                            floatArrayOf(
                                0.70f,
                                0.30f
                            ),
                            floatArrayOf(
                                0.30f,
                                0.70f
                            ),
                            floatArrayOf(
                                0.70f,
                                0.70f
                            )
                        )

                    for (i in 0..3) {

                        paint.color =
                            colors[i]

                        canvas.drawRoundRect(
                            RectF(
                                w *
                                    (
                                        positions[i][0] -
                                            0.13f
                                    ),
                                h *
                                    (
                                        positions[i][1] -
                                            0.13f
                                    ),
                                w *
                                    (
                                        positions[i][0] +
                                            0.13f
                                    ),
                                h *
                                    (
                                        positions[i][1] +
                                            0.13f
                                    )
                            ),
                            dp(3f),
                            dp(3f),
                            paint
                        )
                    }
                }

                // ------------------------------------------------
                // DEFAULT
                // ------------------------------------------------

                else -> {

                    paint.color =
                        Color.parseColor(
                            "#8B5CF6"
                        )

                    canvas.drawCircle(
                        w / 2f,
                        h / 2f,
                        dp(8f),
                        paint
                    )

                    paint.color =
                        Color.WHITE

                    canvas.drawCircle(
                        w / 2f,
                        h / 2f,
                        dp(3f),
                        paint
                    )
                }
            }
        }

        private fun dp(
            value: Float
        ): Float {

            return value *
                resources.displayMetrics.density
        }
    }

    // =================================================
    // BOTTOM NAV ICON
    // =================================================

    private class BottomNavIcon(
        context: Context,
        private val label: String,
        private val active: Boolean
    ) : View(context) {

        private val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        private val path =
            Path()

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            val w =
                width.toFloat()

            val h =
                height.toFloat()

            val color =
                if (active) {

                    Color.rgb(
                        90,
                        210,
                        255
                    )

                } else {

                    Color.rgb(
                        125,
                        140,
                        175
                    )
                }

            paint.shader = null

            paint.color = color

            paint.style =
                Paint.Style.STROKE

            paint.strokeWidth =
                dp(2f)

            paint.strokeCap =
                Paint.Cap.ROUND

            paint.strokeJoin =
                Paint.Join.ROUND

            when (label.lowercase()) {

                // -----------------------------------------
                // HOME
                // -----------------------------------------

                "home" -> {

                    path.reset()

                    path.moveTo(
                        w * 0.18f,
                        h * 0.46f
                    )

                    path.lineTo(
                        w * 0.50f,
                        h * 0.18f
                    )

                    path.lineTo(
                        w * 0.82f,
                        h * 0.46f
                    )

                    path.moveTo(
                        w * 0.25f,
                        h * 0.40f
                    )

                    path.lineTo(
                        w * 0.25f,
                        h * 0.82f
                    )

                    path.lineTo(
                        w * 0.75f,
                        h * 0.82f
                    )

                    path.lineTo(
                        w * 0.75f,
                        h * 0.40f
                    )

                    canvas.drawPath(
                        path,
                        paint
                    )
                }

                // -----------------------------------------
                // HISTORY
                // -----------------------------------------

                "history" -> {

                    canvas.drawCircle(
                        w * 0.50f,
                        h * 0.50f,
                        w * 0.31f,
                        paint
                    )

                    canvas.drawLine(
                        w * 0.50f,
                        h * 0.50f,
                        w * 0.50f,
                        h * 0.31f,
                        paint
                    )

                    canvas.drawLine(
                        w * 0.50f,
                        h * 0.50f,
                        w * 0.65f,
                        h * 0.59f,
                        paint
                    )

                    path.reset()

                    path.moveTo(
                        w * 0.17f,
                        h * 0.27f
                    )

                    path.lineTo(
                        w * 0.17f,
                        h * 0.45f
                    )

                    path.lineTo(
                        w * 0.32f,
                        h * 0.37f
                    )

                    canvas.drawPath(
                        path,
                        paint
                    )
                }

                // -----------------------------------------
                // AURIX
                // -----------------------------------------

                "aurix" -> {

                    paint.style =
                        Paint.Style.STROKE

                    paint.strokeWidth =
                        dp(2f)

                    canvas.drawCircle(
                        w * 0.50f,
                        h * 0.50f,
                        w * 0.36f,
                        paint
                    )

                    paint.style =
                        Paint.Style.FILL

                    canvas.drawCircle(
                        w * 0.50f,
                        h * 0.50f,
                        w * 0.15f,
                        paint
                    )

                    paint.color =
                        Color.WHITE

                    canvas.drawCircle(
                        w * 0.50f,
                        h * 0.50f,
                        w * 0.055f,
                        paint
                    )
                }

                // -----------------------------------------
                // SHORTCUTS
                // -----------------------------------------

                "shortcuts" -> {

                    paint.style =
                        Paint.Style.FILL

                    path.reset()

                    path.moveTo(
                        w * 0.57f,
                        h * 0.10f
                    )

                    path.lineTo(
                        w * 0.27f,
                        h * 0.55f
                    )

                    path.lineTo(
                        w * 0.49f,
                        h * 0.55f
                    )

                    path.lineTo(
                        w * 0.40f,
                        h * 0.90f
                    )

                    path.lineTo(
                        w * 0.76f,
                        h * 0.42f
                    )

                    path.lineTo(
                        w * 0.54f,
                        h * 0.42f
                    )

                    path.close()

                    canvas.drawPath(
                        path,
                        paint
                    )
                }

                // -----------------------------------------
                // SETTINGS
                // -----------------------------------------

                "settings" -> {

                    paint.style =
                        Paint.Style.STROKE

                    paint.strokeWidth =
                        dp(2.2f)

                    canvas.drawCircle(
                        w * 0.50f,
                        h * 0.50f,
                        w * 0.28f,
                        paint
                    )

                    paint.style =
                        Paint.Style.FILL

                    canvas.drawCircle(
                        w * 0.50f,
                        h * 0.50f,
                        w * 0.10f,
                        paint
                    )

                    for (
                        i in 0 until 8
                    ) {

                        val angle =
                            Math.toRadians(
                                (
                                    i * 45
                                ).toDouble()
                            )

                        val x1 =
                            w * 0.50f +
                                kotlin.math.cos(
                                    angle
                                ).toFloat() *
                                w * 0.34f

                        val y1 =
                            h * 0.50f +
                                kotlin.math.sin(
                                    angle
                                ).toFloat() *
                                h * 0.34f

                        val x2 =
                            w * 0.50f +
                                kotlin.math.cos(
                                    angle
                                ).toFloat() *
                                w * 0.43f

                        val y2 =
                            h * 0.50f +
                                kotlin.math.sin(
                                    angle
                                ).toFloat() *
                                h * 0.43f

                        paint.style =
                            Paint.Style.STROKE

                        paint.strokeWidth =
                            dp(2f)

                        canvas.drawLine(
                            x1,
                            y1,
                            x2,
                            y2,
                            paint
                        )
                    }
                }
            }
        }

        private fun dp(
            value: Float
        ): Float {

            return value *
                resources.displayMetrics.density
        }
    }
}
