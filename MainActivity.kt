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

    private var active = false

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
                        updateStatus("PROCESSING")
                    }

                    AurixService.TYPE_SPEAK -> {
                        updateStatus("LISTENING")
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.BLACK

        createInterface()
        registerAurixReceiver()
        requestPermissionsIfNeeded()

        active = AurixService.isRunning

        updateInterface()
    }

    // =========================================================
    // PREMIUM UI
    // =========================================================

    private fun createInterface() {

        root = FrameLayout(this)

        root.setBackgroundColor(
            Color.rgb(3, 6, 16)
        )

        setContentView(root)

        // Background
        val background = View(this)

        background.background =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(8, 20, 48),
                    Color.rgb(3, 6, 16),
                    Color.rgb(14, 8, 38)
                )
            )

        root.addView(
            background,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Main content
        val content =
            LinearLayout(this)

        content.orientation =
            LinearLayout.VERTICAL

        content.gravity =
            Gravity.CENTER_HORIZONTAL

        val contentParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

        contentParams.setMargins(
            dp(20),
            dp(35),
            dp(20),
            dp(115)
        )

        root.addView(
            content,
            contentParams
        )

        // =====================================================
        // TITLE
        // =====================================================

        val title =
            TextView(this)

        title.text = "AURIX"
        title.textSize = 36f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        title.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        content.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
            )
        )

        val subtitle =
            TextView(this)

        subtitle.text =
            "INTELLIGENT VOICE ASSISTANT"

        subtitle.textSize = 10f

        subtitle.setTextColor(
            Color.rgb(145, 170, 205)
        )

        subtitle.gravity =
            Gravity.CENTER

        subtitle.letterSpacing = 0.2f

        content.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(35)
            )
        )

        // =====================================================
        // CENTER
        // =====================================================

        val center =
            FrameLayout(this)

        content.addView(
            center,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // Outer glow
        val outerGlow =
            View(this)

        outerGlow.background =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(30, 90, 210),
                    Color.rgb(100, 40, 190),
                    Color.rgb(20, 180, 220)
                )
            )

        (outerGlow.background as GradientDrawable)
            .shape = GradientDrawable.OVAL

        val glowSize = dp(205)

        val glowParams =
            FrameLayout.LayoutParams(
                glowSize,
                glowSize
            )

        glowParams.gravity =
            Gravity.CENTER

        center.addView(
            outerGlow,
            glowParams
        )

        // Inner orb
        orb = View(this)

        orb.background =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(55, 105, 225),
                    Color.rgb(90, 45, 175),
                    Color.rgb(10, 180, 215)
                )
            )

        (orb.background as GradientDrawable)
            .shape = GradientDrawable.OVAL

        (orb.background as GradientDrawable)
            .setStroke(
                dp(2),
                Color.argb(
                    190,
                    160,
                    225,
                    255
                )
            )

        val orbSize = dp(175)

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

        // AI text
        val aiText =
            TextView(this)

        aiText.text = "AI"
        aiText.textSize = 30f
        aiText.setTextColor(Color.WHITE)
        aiText.gravity = Gravity.CENTER
        aiText.typeface = Typeface.DEFAULT_BOLD

        val aiParams =
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            )

        aiParams.gravity =
            Gravity.CENTER

        center.addView(
            aiText,
            aiParams
        )

        // Status
        statusText =
            TextView(this)

        statusText.text = "READY"
        statusText.textSize = 14f

        statusText.setTextColor(
            Color.rgb(150, 220, 255)
        )

        statusText.gravity = Gravity.CENTER
        statusText.letterSpacing = 0.15f

        val statusParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(45)
            )

        statusParams.gravity =
            Gravity.BOTTOM

        statusParams.bottomMargin =
            dp(20)

        center.addView(
            statusText,
            statusParams
        )

        // =====================================================
        // FIXED ACTIVATE BUTTON
        // =====================================================

        activateButton =
            TextView(this)

        activateButton.text =
            "ACTIVATE AURIX"

        activateButton.textSize = 16f

        activateButton.setTextColor(
            Color.WHITE
        )

        activateButton.gravity =
            Gravity.CENTER

        activateButton.typeface =
            Typeface.DEFAULT_BOLD

        activateButton.setPadding(
            dp(10),
            0,
            dp(10),
            0
        )

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
                dp(62)
            )

        buttonParams.gravity =
            Gravity.BOTTOM

        buttonParams.leftMargin = dp(22)
        buttonParams.rightMargin = dp(22)
        buttonParams.bottomMargin = dp(18)

        root.addView(
            activateButton,
            buttonParams
        )

        // =====================================================
        // NAVIGATION BAR SAFE AREA
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
    // BUTTON
    // =========================================================

    private fun createButtonBackground():
        GradientDrawable {

        val drawable =
            GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.rgb(35, 100, 225),
                    Color.rgb(115, 50, 205)
                )
            )

        drawable.cornerRadius =
            dp(31).toFloat()

        drawable.setStroke(
            dp(1),
            Color.argb(
                160,
                160,
                220,
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

            updateStatus("START FAILED")
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
        } catch (_: Exception) {}

        active = false

        updateInterface()
    }

    // =========================================================
    // UI
    // =========================================================

    private fun updateStatus(
        status: String
    ) {

        runOnUiThread {

            statusText.text = status

            when (
                status.uppercase(
                    Locale.getDefault()
                )
            ) {

                "LISTENING" -> {

                    statusText.setTextColor(
                        Color.rgb(
                            100,
                            235,
                            255
                        )
                    )

                    orb.animate()
                        .scaleX(1.08f)
                        .scaleY(1.08f)
                        .setDuration(220)
                        .start()
                }

                "PROCESSING" -> {

                    statusText.setTextColor(
                        Color.rgb(
                            200,
                            140,
                            255
                        )
                    )

                    orb.animate()
                        .scaleX(1.04f)
                        .scaleY(1.04f)
                        .setDuration(180)
                        .start()
                }

                else -> {

                    statusText.setTextColor(
                        Color.rgb(
                            155,
                            220,
                            255
                        )
                    )

                    orb.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(180)
                        .start()
                }
            }
        }
    }

    private fun updateInterface() {

        if (active) {

            activateButton.text =
                "DEACTIVATE AURIX"

            statusText.text =
                "STARTING..."

        } else {

            activateButton.text =
                "ACTIVATE AURIX"

            statusText.text =
                "READY"
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

    override fun onResume() {

        super.onResume()

        active =
            AurixService.isRunning

        if (::activateButton.isInitialized) {
            updateInterface()
        }
    }

    override fun onDestroy() {

        try {
            unregisterReceiver(aurixReceiver)
        } catch (_: Exception) {}

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
