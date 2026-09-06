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
import android.view.Window
import android.view.WindowInsets
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

class MainActivity : android.app.Activity() {

    private lateinit var root: FrameLayout
    private lateinit var activateButton: Button
    private lateinit var statusText: TextView
    private lateinit var commandText: TextView
    private lateinit var orb: TextView

    private var active = false

    // =========================================================
    // COLORS
    // =========================================================

    private val backgroundColor =
        Color.rgb(7, 10, 18)

    private val cardColor =
        Color.rgb(17, 22, 35)

    private val accentColor =
        Color.rgb(0, 220, 255)

    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        requestPermissionsIfNeeded()

        window.statusBarColor =
            backgroundColor

        window.navigationBarColor =
            backgroundColor

        createInterface()

        registerAurixReceiver()

        active =
            AurixService.isRunning

        updateInterface()
    }

    // =========================================================
    // PERMISSIONS
    // =========================================================

    private fun requestPermissionsIfNeeded() {

        val permissions =
            mutableListOf<String>()

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

        if (permissions.isNotEmpty()) {

            requestPermissions(
                permissions.toTypedArray(),
                501
            )
        }
    }

    // =========================================================
    // PREMIUM UI
    // =========================================================

    private fun createInterface() {

        root =
            FrameLayout(this)

        root.setBackgroundColor(
            backgroundColor
        )

        setContentView(root)

        // -----------------------------------------------------
        // TOP SECTION
        // -----------------------------------------------------

        val topLayout =
            LinearLayout(this)

        topLayout.orientation =
            LinearLayout.VERTICAL

        topLayout.gravity =
            Gravity.CENTER_HORIZONTAL

        val topParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )

        topParams.gravity =
            Gravity.TOP

        topParams.topMargin =
            dp(42)

        root.addView(
            topLayout,
            topParams
        )

        val title =
            TextView(this)

        title.text =
            "A U R I X"

        title.textSize =
            32f

        title.setTextColor(
            accentColor
        )

        title.gravity =
            Gravity.CENTER

        title.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        topLayout.addView(title)

        val subtitle =
            TextView(this)

        subtitle.text =
            "INTELLIGENT VOICE ASSISTANT"

        subtitle.textSize =
            11f

        subtitle.setTextColor(
            Color.LTGRAY
        )

        subtitle.gravity =
            Gravity.CENTER

        val subtitleParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        subtitleParams.topMargin =
            dp(5)

        topLayout.addView(
            subtitle,
            subtitleParams
        )

        // -----------------------------------------------------
        // CENTER ORB
        // -----------------------------------------------------

        orb =
            TextView(this)

        orb.text =
            "A"

        orb.textSize =
            58f

        orb.gravity =
            Gravity.CENTER

        orb.setTextColor(
            Color.WHITE
        )

        orb.background =
            createCircle(
                Color.rgb(
                    15,
                    55,
                    75
                )
            )

        val orbSize =
            dp(170)

        val orbParams =
            FrameLayout.LayoutParams(
                orbSize,
                orbSize
            )

        orbParams.gravity =
            Gravity.CENTER

        orbParams.topMargin =
            dp(35)

        root.addView(
            orb,
            orbParams
        )

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        statusText =
            TextView(this)

        statusText.textSize =
            14f

        statusText.gravity =
            Gravity.CENTER

        statusText.setTextColor(
            accentColor
        )

        val statusParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(40)
            )

        statusParams.gravity =
            Gravity.CENTER_HORIZONTAL

        statusParams.topMargin =
            dp(225)

        root.addView(
            statusText,
            statusParams
        )

        // -----------------------------------------------------
        // COMMAND CARD
        // -----------------------------------------------------

        val commandCard =
            LinearLayout(this)

        commandCard.orientation =
            LinearLayout.VERTICAL

        commandCard.setPadding(
            dp(20),
            dp(15),
            dp(20),
            dp(15)
        )

        commandCard.background =
            roundedBackground(
                cardColor,
                dp(18)
            )

        val commandCardParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(105)
            )

        commandCardParams.gravity =
            Gravity.CENTER_HORIZONTAL

        commandCardParams.leftMargin =
            dp(24)

        commandCardParams.rightMargin =
            dp(24)

        commandCardParams.topMargin =
            dp(285)

        root.addView(
            commandCard,
            commandCardParams
        )

        val commandTitle =
            TextView(this)

        commandTitle.text =
            "LAST COMMAND"

        commandTitle.textSize =
            10f

        commandTitle.setTextColor(
            Color.GRAY
        )

        commandCard.addView(
            commandTitle
        )

        commandText =
            TextView(this)

        commandText.text =
            "No command yet"

        commandText.textSize =
            17f

        commandText.setTextColor(
            Color.WHITE
        )

        commandText.maxLines =
            2

        val commandParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        commandParams.topMargin =
            dp(8)

        commandCard.addView(
            commandText,
            commandParams
        )

        // -----------------------------------------------------
        // FIXED BOTTOM BUTTON
        // -----------------------------------------------------

        activateButton =
            Button(this)

        activateButton.text =
            "ACTIVATE AURIX"

        activateButton.textSize =
            15f

        activateButton.setTextColor(
            Color.WHITE
        )

        activateButton.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        activateButton.background =
            roundedBackground(
                Color.rgb(
                    0,
                    120,
                    150
                ),
                dp(22)
            )

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
            Gravity.BOTTOM

        buttonParams.leftMargin =
            dp(24)

        buttonParams.rightMargin =
            dp(24)

        // IMPORTANT:
        // Initial safe bottom margin
        buttonParams.bottomMargin =
            dp(30)

        root.addView(
            activateButton,
            buttonParams
        )

        // -----------------------------------------------------
        // NAVIGATION BAR INSET FIX
        // -----------------------------------------------------

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.R
        ) {

            root.setOnApplyWindowInsetsListener {
                view,
                insets ->

                val navigationInsets =
                    insets.getInsets(
                        WindowInsets.Type.navigationBars()
                    )

                val params =
                    activateButton.layoutParams
                        as FrameLayout.LayoutParams

                params.bottomMargin =
                    navigationInsets.bottom +
                        dp(18)

                activateButton.layoutParams =
                    params

                insets
            }
        }

        root.requestApplyInsets()
    }

    // =========================================================
    // ACTIVATE
    // =========================================================

    private fun activateAurix() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            ContextCompat.startForegroundService(
                this,
                Intent(
                    this,
                    AurixService::class.java
                ).apply {
                    action =
                        AurixService.ACTION_START
                }
            )

        } else {

            startService(
                Intent(
                    this,
                    AurixService::class.java
                ).apply {
                    action =
                        AurixService.ACTION_START
                }
            )
        }

        active = true

        updateInterface()
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

        startService(intent)

        active = false

        updateInterface()
    }

    // =========================================================
    // RECEIVER
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
                ) return

                val type =
                    intent.getStringExtra(
                        AurixService.EXTRA_TYPE
                    )

                val text =
                    intent.getStringExtra(
                        AurixService.EXTRA_TEXT
                    )
                        ?: ""

                when (type) {

                    AurixService.TYPE_STATUS -> {

                        statusText.text =
                            text

                        active =
                            AurixService.isRunning

                        updateInterface()
                    }

                    AurixService.TYPE_COMMAND -> {

                        commandText.text =
                            text

                        statusText.text =
                            "PROCESSING"
                    }
                }
            }
        }

    // =========================================================
    // REGISTER RECEIVER
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
    // UPDATE UI
    // =========================================================

    private fun updateInterface() {

        if (!::activateButton.isInitialized) {
            return
        }

        if (active) {

            activateButton.text =
                "DEACTIVATE AURIX"

            activateButton.background =
                roundedBackground(
                    Color.rgb(
                        150,
                        45,
                        60
                    ),
                    dp(22)
                )

            statusText.text =
                "LISTENING"

            orb.background =
                createCircle(
                    Color.rgb(
                        0,
                        100,
                        130
                    )
                )

        } else {

            activateButton.text =
                "ACTIVATE AURIX"

            activateButton.background =
                roundedBackground(
                    Color.rgb(
                        0,
                        120,
                        150
                    ),
                    dp(22)
                )

            statusText.text =
                "READY"

            orb.background =
                createCircle(
                    Color.rgb(
                        15,
                        55,
                        75
                    )
                )
        }
    }

    // =========================================================
    // DRAWABLE HELPERS
    // =========================================================

    private fun roundedBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(color)

            cornerRadius =
                radius.toFloat()

            setStroke(
                dp(1),
                Color.argb(
                    80,
                    255,
                    255,
                    255
                )
            )
        }
    }

    private fun createCircle(
        color: Int
    ): GradientDrawable {

        return GradientDrawable().apply {

            shape =
                GradientDrawable.OVAL

            setColor(color)

            setStroke(
                dp(2),
                Color.argb(
                    120,
                    0,
                    220,
                    255
                )
            )
        }
    }

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
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

        super.onDestroy()
    }
}
