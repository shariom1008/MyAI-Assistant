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
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var statusText: TextView
    private lateinit var activateButton: TextView
    private lateinit var orb: View
    private lateinit var coreText: TextView
    private lateinit var systemText: TextView

    private var active = false

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

                try {

                    val homeIntent =
                        Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            addCategory(Intent.CATEGORY_DEFAULT)
                        }

                    startActivity(homeIntent)

                } catch (_: Exception) {
                }
            }
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
                        updateStatus("PROCESSING")
                    }

                    AurixService.TYPE_SPEAK -> {
                        updateStatus("LISTENING")
                    }
                }
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
        window.navigationBarColor =
            Color.rgb(2, 4, 10)

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

        requestPermissionsIfNeeded()

        active = AurixService.isRunning

        updateInterface()
    }

    // =========================================================
    // AURIX 2.0 FUTURISTIC INTERFACE
    // =========================================================

    private fun createInterface() {

        root = FrameLayout(this)

        root.setBackgroundColor(
            Color.rgb(2, 5, 14)
        )

        setContentView(root)

        // =====================================================
        // BACKGROUND
        // =====================================================

        val background = View(this)

        background.background =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(3, 15, 35),
                    Color.rgb(2, 5, 14),
                    Color.rgb(15, 4, 35)
                )
            )

        root.addView(
            background,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =====================================================
        // TOP HEADER
        // =====================================================

        val header =
            LinearLayout(this)

        header.orientation =
            LinearLayout.VERTICAL

        header.gravity =
            Gravity.CENTER_HORIZONTAL

        val headerParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(105)
            )

        headerParams.topMargin = dp(22)
        headerParams.leftMargin = dp(20)
        headerParams.rightMargin = dp(20)

        root.addView(
            header,
            headerParams
        )

        val title =
            TextView(this)

        title.text = "AURIX"

        title.textSize = 34f

        title.setTextColor(
            Color.WHITE
        )

        title.gravity =
            Gravity.CENTER

        title.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        title.letterSpacing = 0.12f

        header.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        )

        val subtitle =
            TextView(this)

        subtitle.text =
            "A U R I X   •   INTELLIGENCE CORE"

        subtitle.textSize = 9f

        subtitle.setTextColor(
            Color.rgb(
                105,
                180,
                225
            )
        )

        subtitle.gravity =
            Gravity.CENTER

        subtitle.letterSpacing = 0.18f

        header.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(35)
            )
        )

        // =====================================================
        // MAIN CENTER
        // =====================================================

        val center =
            FrameLayout(this)

        val centerParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                0
            )

        centerParams.topMargin = dp(95)
        centerParams.bottomMargin = dp(160)
        centerParams.leftMargin = dp(15)
        centerParams.rightMargin = dp(15)

        centerParams.height =
            FrameLayout.LayoutParams.MATCH_PARENT

        root.addView(
            center,
            centerParams
        )

        // =====================================================
        // OUTER RING
        // =====================================================

        val outerRing =
            View(this)

        outerRing.background =
            createOval(
                intArrayOf(
                    Color.rgb(20, 75, 170),
                    Color.rgb(95, 35, 190),
                    Color.rgb(15, 150, 200)
                )
            )

        val outerSize =
            dp(235)

        val outerParams =
            FrameLayout.LayoutParams(
                outerSize,
                outerSize
            )

        outerParams.gravity =
            Gravity.CENTER

        center.addView(
            outerRing,
            outerParams
        )

        // =====================================================
        // INNER RING
        // =====================================================

        val innerRing =
            View(this)

        innerRing.background =
            createOval(
                intArrayOf(
                    Color.rgb(8, 22, 65),
                    Color.rgb(22, 10, 55),
                    Color.rgb(5, 50, 65)
                )
            )

        val innerSize =
            dp(207)

        val innerParams =
            FrameLayout.LayoutParams(
                innerSize,
                innerSize
            )

        innerParams.gravity =
            Gravity.CENTER

        center.addView(
            innerRing,
            innerParams
        )

        // =====================================================
        // CORE ORB
        // =====================================================

        orb = View(this)

        orb.background =
            createOrb()

        val orbSize =
            dp(168)

        val orbParams =
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            )

        orbParams.gravity =
            Gravity.CENTER

        center.addView(
            orb,
            orbParams
        )

        // =====================================================
        // CORE TEXT
        // =====================================================

        coreText =
            TextView(this)

        coreText.text =
            "AURIX"

        coreText.textSize = 23f

        coreText.setTextColor(
            Color.WHITE
        )

        coreText.gravity =
            Gravity.CENTER

        coreText.typeface =
            Typeface.DEFAULT_BOLD

        coreText.letterSpacing =
            0.18f

        val coreParams =
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            )

        coreParams.gravity =
            Gravity.CENTER

        center.addView(
            coreText,
            coreParams
        )

        // =====================================================
        // STATUS
        // =====================================================

        statusText =
            TextView(this)

        statusText.text =
            "READY"

        statusText.textSize = 14f

        statusText.setTextColor(
            Color.rgb(
                120,
                215,
                255
            )
        )

        statusText.gravity =
            Gravity.CENTER

        statusText.typeface =
            Typeface.DEFAULT_BOLD

        statusText.letterSpacing =
            0.20f

        val statusParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(40)
            )

        statusParams.gravity =
            Gravity.CENTER_HORIZONTAL or
                    Gravity.BOTTOM

        statusParams.bottomMargin =
            dp(12)

        center.addView(
            statusText,
            statusParams
        )

        // =====================================================
        // SYSTEM STATUS
        // =====================================================

        systemText =
            TextView(this)

        systemText.text =
            "SYSTEM ONLINE"

        systemText.textSize = 9f

        systemText.setTextColor(
            Color.rgb(
                75,
                145,
                180
            )
        )

        systemText.gravity =
            Gravity.CENTER

        systemText.letterSpacing =
            0.20f

        val systemParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(28)
            )

        systemParams.gravity =
            Gravity.CENTER_HORIZONTAL or
                    Gravity.BOTTOM

        systemParams.bottomMargin =
            dp(43)

        center.addView(
            systemText,
            systemParams
        )

        // =====================================================
        // CORE ANIMATION
        // =====================================================

        startCoreAnimation(
            outerRing
        )

        // =====================================================
        // ACTIVATE BUTTON
        // =====================================================

        activateButton =
            TextView(this)

        activateButton.text =
            "ACTIVATE AURIX"

        activateButton.textSize =
            15f

        activateButton.setTextColor(
            Color.WHITE
        )

        activateButton.gravity =
            Gravity.CENTER

        activateButton.typeface =
            Typeface.DEFAULT_BOLD

        activateButton.letterSpacing =
            0.08f

        activateButton.background =
            createButtonBackground()

        activateButton.setOnClickListener {

            if (active) {

                deactivateAurix()

            } else {

                activateAurix()
            }
        }

        val buttonParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(60)
            )

        buttonParams.gravity =
            Gravity.BOTTOM

        buttonParams.leftMargin =
            dp(25)

        buttonParams.rightMargin =
            dp(25)

        buttonParams.bottomMargin =
            dp(18)
            
// =====================================================
// AURIX 2.0 QUICK ACTION DASHBOARD
// =====================================================

val dashboard =
    AurixDashboard.create(
        this,
        object : AurixDashboard.ActionListener {

            override fun onAction(
                action: String
            ) {

                when (action) {

                    "ASK" -> {
                        updateStatus("LISTENING")
                    }

                    "PHONE" -> {
                        AurixCommandRouter.route(
                            "open phone"
                        )
                    }

                    "MUSIC" -> {
                        AurixCommandRouter.route(
                            "play music"
                        )
                    }

                    "BLUETOOTH" -> {
                        AurixCommandRouter.route(
                            "show my paired bluetooth devices"
                        )
                    }

                    "VISION" -> {
                        updateStatus("VISION")
                    }

                    "WEB" -> {
                        AurixCommandRouter.route(
                            "open chrome"
                        )
                    }

                    "WEARABLES" -> {
                        updateStatus("WEARABLES")
                    }

                    "SYSTEM" -> {
                        AurixCommandRouter.route(
                            "open settings"
                        )
                    }
                }
            }
        }
    )

val dashboardParams =
    FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        dp(230)
    )

dashboardParams.gravity =
    Gravity.BOTTOM

dashboardParams.leftMargin =
    dp(12)

dashboardParams.rightMargin =
    dp(12)

dashboardParams.bottomMargin =
    dp(82)

root.addView(
    dashboard,
    dashboardParams
)
        root.addView(
            activateButton,
            buttonParams
        )

        // =====================================================
        // NAVIGATION SAFE AREA
        // =====================================================

        ViewCompat.setOnApplyWindowInsetsListener(
            root
        ) { _, insets ->

            val navigation =
                insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                )

            val params =
                activateButton.layoutParams
                    as FrameLayout.LayoutParams

            params.bottomMargin =
                navigation.bottom + dp(14)

            activateButton.layoutParams =
                params

            insets
        }

        ViewCompat.requestApplyInsets(root)
    }

    // =========================================================
    // ORB
    // =========================================================

    private fun createOval(
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
                    Color.rgb(
                        25,
                        95,
                        210
                    ),
                    Color.rgb(
                        95,
                        35,
                        185
                    ),
                    Color.rgb(
                        5,
                        170,
                        210
                    )
                )
            )

        drawable.shape =
            GradientDrawable.OVAL

        drawable.setStroke(
            dp(2),
            Color.argb(
                210,
                175,
                235,
                255
            )
        )

        return drawable
    }

    // =========================================================
    // CORE ANIMATION
    // =========================================================

    private fun startCoreAnimation(
        ring: View
    ) {

        ring.animate()
            .rotationBy(360f)
            .setDuration(9000)
            .withEndAction {

                startCoreAnimation(ring)
            }
            .start()

        orb.animate()
            .scaleX(1.04f)
            .scaleY(1.04f)
            .setDuration(1800)
            .withEndAction {

                orb.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1800)
                    .withEndAction {

                        startCorePulse()
                    }
                    .start()
            }
            .start()
    }

    private fun startCorePulse() {

        orb.animate()
            .scaleX(1.035f)
            .scaleY(1.035f)
            .setDuration(1600)
            .withEndAction {

                orb.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1600)
                    .withEndAction {

                        if (!isFinishing) {
                            startCorePulse()
                        }
                    }
                    .start()
            }
            .start()
    }

    // =========================================================
    // GO HOME
    // =========================================================

    fun goToHomeScreen() {

        try {

            val homeIntent =
                Intent(Intent.ACTION_MAIN).apply {

                    addCategory(
                        Intent.CATEGORY_HOME
                    )

                    addCategory(
                        Intent.CATEGORY_DEFAULT
                    )
                }

            startActivity(homeIntent)

        } catch (_: Exception) {
        }
    }

    // =========================================================
    // BUTTON BACKGROUND
    // =========================================================

    private fun createButtonBackground():
        GradientDrawable {

        val drawable =
            GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.rgb(
                        25,
                        95,
                        220
                    ),
                    Color.rgb(
                        110,
                        40,
                        200
                    )
                )
            )

        drawable.cornerRadius =
            dp(30).toFloat()

        drawable.setStroke(
            dp(1),
            Color.argb(
                190,
                165,
                225,
                255
            )
        )

        return drawable
    }

    // =========================================================
    // ACTIVATE
    // =========================================================

    private fun activateAurix() {

        requestPermissionsIfNeeded()

        val intent =
            Intent(
                this,
                AurixService::class.java
            )

        intent.action =
            AurixService.ACTION_START

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

            updateInterface()

        } catch (_: Exception) {

            updateStatus(
                "START FAILED"
            )
        }
    }

    // =========================================================
    // DEACTIVATE
    // =========================================================

    private fun deactivateAurix() {

        val intent =
            Intent(
                this,
                AurixService::class.java
            )

        intent.action =
            AurixService.ACTION_STOP

        try {

            startService(intent)

        } catch (_: Exception) {
        }

        active = false

        updateInterface()
    }

    // =========================================================
    // STATUS
    // =========================================================

    private fun updateStatus(
        status: String
    ) {

        runOnUiThread {

            statusText.text =
                status

            when (
                status.uppercase(
                    Locale.getDefault()
                )
            ) {

                "LISTENING" -> {

                    statusText.setTextColor(
                        Color.rgb(
                            90,
                            235,
                            255
                        )
                    )

                    coreText.text =
                        "LISTEN"

                    orb.animate()
                        .scaleX(1.10f)
                        .scaleY(1.10f)
                        .setDuration(250)
                        .start()
                }

                "PROCESSING" -> {

                    statusText.setTextColor(
                        Color.rgb(
                            205,
                            145,
                            255
                        )
                    )

                    coreText.text =
                        "THINK"

                    orb.animate()
                        .scaleX(1.07f)
                        .scaleY(1.07f)
                        .setDuration(200)
                        .start()
                }

                "READY" -> {

                    statusText.setTextColor(
                        Color.rgb(
                            120,
                            215,
                            255
                        )
                    )

                    coreText.text =
                        "AURIX"

                    orb.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }

                else -> {

                    statusText.setTextColor(
                        Color.rgb(
                            145,
                            210,
                            245
                        )
                    )

                    orb.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
            }
        }
    }

    // =========================================================
    // UPDATE INTERFACE
    // =========================================================

    private fun updateInterface() {

        if (active) {

            activateButton.text =
                "DEACTIVATE AURIX"

            systemText.text =
                "SYSTEM ACTIVE"

            statusText.text =
                "STARTING..."

            coreText.text =
                "AURIX"

        } else {

            activateButton.text =
                "ACTIVATE AURIX"

            systemText.text =
                "SYSTEM ONLINE"

            statusText.text =
                "READY"

            coreText.text =
                "AURIX"
        }
    }

    // =========================================================
    // PERMISSIONS
    // =========================================================

    private fun requestPermissionsIfNeeded() {

        val permissions =
            ArrayList<String>()

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            permissions.add(
                Manifest.permission.RECORD_AUDIO
            )
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            permissions.add(
                Manifest.permission.CAMERA
            )
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                permissions.add(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        if (permissions.isNotEmpty()) {

            requestPermissions(
                permissions.toTypedArray(),
                500
            )
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

        active =
            AurixService.isRunning

        if (
            ::activateButton.isInitialized
        ) {

            updateInterface()
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
}
