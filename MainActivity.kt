package com.example.myaiassistant

import android.Manifest
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
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var statusText: TextView
    private lateinit var commandText: TextView
    private lateinit var orbText: TextView
    private lateinit var activateButton: Button

    private val handler =
        Handler(Looper.getMainLooper())

    private var pendingStart = false

    companion object {
        private const val MIC_PERMISSION = 1001
        private const val NOTIFICATION_PERMISSION = 1002
    }

    private val aurixReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context,
                intent: Intent
            ) {

                when (
                    intent.getStringExtra(
                        AurixService.EXTRA_TYPE
                    )
                ) {

                    AurixService.TYPE_STATUS -> {

                        val status =
                            intent.getStringExtra(
                                AurixService.EXTRA_TEXT
                            ) ?: "READY"

                        updateStatus(status)
                    }

                    AurixService.TYPE_COMMAND -> {

                        val command =
                            intent.getStringExtra(
                                AurixService.EXTRA_TEXT
                            ) ?: ""

                        commandText.text = command
                    }
                }
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        window.statusBarColor =
            Color.rgb(3, 6, 15)

        window.navigationBarColor =
            Color.rgb(3, 6, 15)

        createInterface()

        val filter =
            IntentFilter(
                AurixService.ACTION_EVENT
            )

        ContextCompat.registerReceiver(
            this,
            aurixReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        requestPermissionsIfNeeded()
    }

    // =====================================================
    // PREMIUM INTERFACE
    // =====================================================

    private fun createInterface() {

        val root = FrameLayout(this)

        val background =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(3, 6, 15),
                    Color.rgb(7, 12, 28),
                    Color.rgb(2, 5, 14)
                )
            )

        root.background = background

        // =================================================
        // TOP
        // =================================================

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

        topParams.setMargins(
            0,
            55,
            0,
            0
        )

        root.addView(
            topLayout,
            topParams
        )

        val title =
            TextView(this)

        title.text = "AURIX"
        title.textSize = 34f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        title.typeface =
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

        topLayout.addView(title)

        val subtitle =
            TextView(this)

        subtitle.text =
            "YOUR INTELLIGENT VOICE ASSISTANT"

        subtitle.textSize = 11f

        subtitle.setTextColor(
            Color.rgb(130, 170, 230)
        )

        subtitle.gravity =
            Gravity.CENTER

        subtitle.letterSpacing =
            0.15f

        val subtitleParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        subtitleParams.topMargin = 6

        topLayout.addView(
            subtitle,
            subtitleParams
        )

        // =================================================
        // ORB
        // =================================================

        val orbContainer =
            FrameLayout(this)

        val orbParams =
            FrameLayout.LayoutParams(
                250,
                250
            )

        orbParams.gravity =
            Gravity.CENTER

        orbParams.topMargin = -20

        root.addView(
            orbContainer,
            orbParams
        )

        val outerOrb =
            View(this)

        val outerDrawable =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(25, 90, 190),
                    Color.rgb(70, 25, 180)
                )
            )

        outerDrawable.shape =
            GradientDrawable.OVAL

        outerDrawable.setStroke(
            3,
            Color.rgb(70, 160, 255)
        )

        outerOrb.background =
            outerDrawable

        val outerParams =
            FrameLayout.LayoutParams(
                220,
                220
            )

        outerParams.gravity =
            Gravity.CENTER

        orbContainer.addView(
            outerOrb,
            outerParams
        )

        val innerOrb =
            View(this)

        val innerDrawable =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(5, 30, 75),
                    Color.rgb(15, 100, 190)
                )
            )

        innerDrawable.shape =
            GradientDrawable.OVAL

        innerDrawable.setStroke(
            2,
            Color.rgb(100, 200, 255)
        )

        innerOrb.background =
            innerDrawable

        val innerParams =
            FrameLayout.LayoutParams(
                165,
                165
            )

        innerParams.gravity =
            Gravity.CENTER

        orbContainer.addView(
            innerOrb,
            innerParams
        )

        orbText =
            TextView(this)

        orbText.text = "AI"
        orbText.textSize = 32f
        orbText.setTextColor(Color.WHITE)
        orbText.gravity = Gravity.CENTER

        orbText.typeface =
            Typeface.DEFAULT_BOLD

        val orbTextParams =
            FrameLayout.LayoutParams(
                165,
                165
            )

        orbTextParams.gravity =
            Gravity.CENTER

        orbContainer.addView(
            orbText,
            orbTextParams
        )

        val pulse =
            android.view.animation.AlphaAnimation(
                0.65f,
                1f
            )

        pulse.duration = 1200

        pulse.repeatMode =
            android.view.animation.Animation.REVERSE

        pulse.repeatCount =
            android.view.animation.Animation.INFINITE

        orbContainer.startAnimation(
            pulse
        )

        // =================================================
        // COMMAND CARD
        // =================================================

        val commandCard =
            LinearLayout(this)

        commandCard.orientation =
            LinearLayout.VERTICAL

        commandCard.setPadding(
            28,
            22,
            28,
            22
        )

        val cardDrawable =
            GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(10, 20, 42),
                    Color.rgb(7, 13, 30)
                )
            )

        cardDrawable.cornerRadius =
            30f

        cardDrawable.setStroke(
            1,
            Color.rgb(35, 85, 150)
        )

        commandCard.background =
            cardDrawable

        val cardParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                150
            )

        cardParams.gravity =
            Gravity.CENTER_HORIZONTAL

        cardParams.setMargins(
            25,
            250,
            25,
            0
        )

        root.addView(
            commandCard,
            cardParams
        )

        val commandLabel =
            TextView(this)

        commandLabel.text =
            "LAST COMMAND"

        commandLabel.textSize = 10f

        commandLabel.setTextColor(
            Color.rgb(100, 160, 230)
        )

        commandLabel.letterSpacing =
            0.15f

        commandCard.addView(
            commandLabel
        )

        commandText =
            TextView(this)

        commandText.text =
            "Say something..."

        commandText.textSize =
            18f

        commandText.setTextColor(
            Color.WHITE
        )

        commandText.gravity =
            Gravity.CENTER_VERTICAL

        val commandParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                90
            )

        commandCard.addView(
            commandText,
            commandParams
        )

        // =================================================
        // STATUS
        // =================================================

        statusText =
            TextView(this)

        statusText.text =
            "READY"

        statusText.textSize =
            13f

        statusText.setTextColor(
            Color.rgb(100, 190, 255)
        )

        statusText.gravity =
            Gravity.CENTER

        statusText.letterSpacing =
            0.12f

        val statusParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                45
            )

        statusParams.gravity =
            Gravity.CENTER_HORIZONTAL

        statusParams.setMargins(
            20,
            410,
            20,
            0
        )

        root.addView(
            statusText,
            statusParams
        )

        // =================================================
        // FIXED BOTTOM BUTTON
        // =================================================

        activateButton =
            Button(this)

        activateButton.text =
            "ACTIVATE AURIX"

        activateButton.textSize =
            16f

        activateButton.setTextColor(
            Color.WHITE
        )

        activateButton.typeface =
            Typeface.DEFAULT_BOLD

        activateButton.isAllCaps =
            false

        val buttonDrawable =
            GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(
                    Color.rgb(20, 95, 210),
                    Color.rgb(75, 35, 180)
                )
            )

        buttonDrawable.cornerRadius =
            60f

        activateButton.background =
            buttonDrawable

        activateButton.setOnClickListener {
            toggleAurix()
        }

        val buttonParams =
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                64
            )

        buttonParams.gravity =
            Gravity.BOTTOM

        buttonParams.leftMargin =
            28

        buttonParams.rightMargin =
            28

        buttonParams.bottomMargin =
            18

        root.addView(
            activateButton,
            buttonParams
        )

        // =================================================
        // IMPORTANT: NAVIGATION BAR SAFE AREA
        // =================================================

        root.setOnApplyWindowInsetsListener {
                view,
                insets ->

            val bottomInset =
                if (Build.VERSION.SDK_INT >= 30) {

                    insets.getInsets(
                        WindowInsets.Type.navigationBars()
                    ).bottom

                } else {
                    0
                }

            val params =
                activateButton.layoutParams
                    as FrameLayout.LayoutParams

            params.bottomMargin =
                bottomInset + 18

            activateButton.layoutParams =
                params

            insets
        }

        root.post {
            root.requestApplyInsets()
        }

        setContentView(root)

        updateButton()
    }

    // =====================================================
    // PERMISSIONS
    // =====================================================

    private fun requestPermissionsIfNeeded() {

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
                MIC_PERMISSION
            )

            return
        }

        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {

        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                NOTIFICATION_PERMISSION
            )
        }
    }

    // =====================================================
    // AURIX START / STOP
    // =====================================================

    private fun toggleAurix() {

        if (AurixService.isRunning) {

            stopAurix()

        } else {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                pendingStart = true

                requestPermissions(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO
                    ),
                    MIC_PERMISSION
                )

                return
            }

            startAurix()
        }
    }

    private fun startAurix() {

        try {

            val intent =
                Intent(
                    this,
                    AurixService::class.java
                )

            intent.action =
                AurixService.ACTION_START

            ContextCompat.startForegroundService(
                this,
                intent
            )

            updateStatus(
                "STARTING AURIX..."
            )

        } catch (_: Exception) {

            Toast.makeText(
                this,
                "Could not start AURIX",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun stopAurix() {

        val intent =
            Intent(
                this,
                AurixService::class.java
            )

        intent.action =
            AurixService.ACTION_STOP

        startService(intent)

        updateStatus("READY")
        updateButton()
    }

    // =====================================================
    // UI STATUS
    // =====================================================

    private fun updateStatus(
        status: String
    ) {

        statusText.text =
            status

        if (
            status.contains(
                "LISTEN",
                true
            )
        ) {

            orbText.text =
                "●"

            activateButton.text =
                "DEACTIVATE AURIX"

        } else {

            orbText.text =
                "AI"

            activateButton.text =
                if (
                    AurixService.isRunning
                ) {
                    "DEACTIVATE AURIX"
                } else {
                    "ACTIVATE AURIX"
                }
        }

        updateButton()
    }

    private fun updateButton() {

        if (
            !::activateButton.isInitialized
        ) {
            return
        }

        activateButton.text =
            if (AurixService.isRunning) {
                "DEACTIVATE AURIX"
            } else {
                "ACTIVATE AURIX"
            }
    }

    // =====================================================
    // PERMISSION RESULT
    // =====================================================

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
            requestCode ==
            MIC_PERMISSION
        ) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                requestNotificationPermission()

                if (pendingStart) {

                    pendingStart =
                        false

                    handler.postDelayed(
                        {
                            startAurix()
                        },
                        500
                    )
                }

            } else {

                Toast.makeText(
                    this,
                    "Microphone permission is required",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {

        super.onResume()

        handler.postDelayed(
            {
                updateButton()
            },
            300
        )
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
}
