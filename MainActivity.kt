package com.example.myaiassistant

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : android.app.Activity() {

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

                        updateStatus(
                            text
                        )
                    }
                }
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        window.statusBarColor =
            Color.TRANSPARENT

        window.navigationBarColor =
            Color.BLACK

        createInterface()

        requestPermissionsIfNeeded()

        registerAurixReceiver()

        active =
            AurixService.isRunning

        updateInterface()
    }

    // =========================================================
    // PREMIUM UI
    // =========================================================

    private fun createInterface() {

        root =
            FrameLayout(this)

        root.setBackgroundColor(
            Color.rgb(
                4,
                7,
                18
            )
        )

        setContentView(
            root
        )

        // -----------------------------------------------------
        // BACKGROUND GLOW
        // -----------------------------------------------------

        val glow =
            View(this)

        val glowDrawable =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(
                        12,
                        28,
                        65
                    ),
                    Color.rgb(
                        5,
                        8,
                        20
                    ),
                    Color.rgb(
                        10,
                        18,
                        40
                    )
                )
            )

        glow.background =
            glowDrawable

        root.addView(
            glow,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // -----------------------------------------------------
        // MAIN CONTENT
        // -----------------------------------------------------

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
            dp(45),
            dp(20),
            dp(120)
        )

        root.addView(
            content,
            contentParams
        )

        // -----------------------------------------------------
        // AURIX TITLE
        // -----------------------------------------------------

        val title =
            TextView(this)

        title.text =
            "AURIX"

        title.textSize =
            34f

        title.setTextColor(
            Color.WHITE
        )

        title.gravity =
            Gravity.CENTER

        title.typeface =
            android.graphics.Typeface.create(
                "sans-serif",
                android.graphics.Typeface.BOLD
            )

        content.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
            )
        )

        // -----------------------------------------------------
        // SUBTITLE
        // -----------------------------------------------------

        val subtitle =
            TextView(this)

        subtitle.text =
            "INTELLIGENT VOICE ASSISTANT"

        subtitle.textSize =
            11f

        subtitle.setTextColor(
            Color.rgb(
                150,
                165,
                190
            )
        )

        subtitle.gravity =
            Gravity.CENTER

        subtitle.letterSpacing =
            0.18f

        content.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )

        // -----------------------------------------------------
        // CENTER SPACE
        // -----------------------------------------------------

        val center =
            FrameLayout(this)

        val centerParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )

        content.addView(
            center,
            centerParams
        )

        // -----------------------------------------------------
        // ORB
        // -----------------------------------------------------

        orb =
            View(this)

        val orbDrawable =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(
                        70,
                        130,
                        255
                    ),
                    Color.rgb(
                        120,
                        55,
                        220
                    ),
                    Color.rgb(
                        20,
                        210,
                        240
                    )
                )
            )

        orbDrawable.shape =
            GradientDrawable.OVAL

        orbDrawable.setStroke(
            dp(2),
            Color.argb(
                170,
                120,
                220,
                255
            )
        )

        orb.background =
            orbDrawable

        val orbSize =
            dp(180)

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

        // -----------------------------------------------------
        // AURIX CENTER TEXT
        // -----------------------------------------------------

        val orbText =
            TextView(this)

        orbText.text =
            "AI"

        orbText.textSize =
            30f

        orbText.setTextColor(
            Color.WHITE
        )

        orbText.gravity =
            Gravity.CENTER

        orbText.typeface =
            android.graphics.Typeface.DEFAULT_BOLD

        val orbTextParams =
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            )

        orbTextParams.gravity =
            Gravity.CENTER

        center.addView(
            orbText,
            orbTextParams
        )

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        statusText =
            TextView(this)

        statusText.text =
            "READY"

        statusText.textSize =
            14f

        statusText.setTextColor(
            Color.rgb(
                160,
                220,
                255
            )
        )

        statusText.gravity =
            Gravity.CENTER

        statusText.letterSpacing =
            0.12f

        val statusParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(45)
            )

        statusParams.gravity =
            Gravity.CENTER_HORIZONTAL or
                Gravity.BOTTOM

        statusParams.bottomMargin =
            dp(25)

        center.addView(
            statusText,
            statusParams
        )

        // -----------------------------------------------------
        // FIXED BOTTOM BUTTON
        // -----------------------------------------------------

        activateButton =
            TextView(this)

        activateButton.text =
            "ACTIVATE AURIX"

        activateButton.textSize =
            16f

        activateButton.setTextColor(
            Color.WHITE
        )

        activateButton.gravity =
            Gravity.CENTER

        activateButton.typeface =
            android.graphics.Typeface.DEFAULT_BOLD

        activateButton.isClickable =
            true

        activateButton.isFocusable =
            true

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
                dp(64)
            )

        buttonParams.gravity =
            Gravity.BOTTOM or
                Gravity.CENTER_HORIZONTAL

        buttonParams.leftMargin =
            dp(24)

        buttonParams.rightMargin =
            dp(24)

        buttonParams.bottomMargin =
            dp(18)

        root.addView(
            activateButton,
            buttonParams
        )

        // -----------------------------------------------------
        // SYSTEM NAVIGATION INSETS
        // -----------------------------------------------------

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
                navigation.bottom + dp(18)

            activateButton.layoutParams =
                params

            insets
        }

        ViewCompat.requestApplyInsets(
            root
        )
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
                        45,
                        105,
                        230
                    ),
                    Color.rgb(
                        115,
                        55,
                        210
                    )
                )
            )

        drawable.cornerRadius =
            dp(32).toFloat()

        drawable.setStroke(
            dp(1),
            Color.argb(
                120,
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

                startService(
                    intent
                )
            }

            active = true

            updateInterface()

        } catch (_: Exception) {

            statusText.text =
                "START FAILED"
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

            startService(
                intent
            )

        } catch (_: Exception) {
        }

        active = false

        updateInterface()
    }

    // =========================================================
    // UI STATUS
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
                            100,
                            230,
                            255
                        )
                    )

                    orb.animate()
                        .scaleX(1.08f)
                        .scaleY(1.08f)
                        .setDuration(250)
                        .start()
                }

                "PROCESSING" -> {

                    statusText.setTextColor(
                        Color.rgb(
                            190,
                            140,
                            255
                        )
                    )

                    orb.animate()
                        .scaleX(1.03f)
                        .scaleY(1.03f)
                        .setDuration(200)
                        .start()
                }

                else -> {

                    statusText.setTextColor(
                        Color.rgb(
                            160,
                            220,
                            255
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
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.M
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {

                permissions.add(
                    Manifest.permission.RECORD_AUDIO
                )
            }

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {

                permissions.add(
                    Manifest.permission.CAMERA
                )
            }
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {

                permissions.add(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        if (
            permissions.isNotEmpty()
        ) {

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

    override fun onDestroy() {

        try {

            unregisterReceiver(
                aurixReceiver
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
