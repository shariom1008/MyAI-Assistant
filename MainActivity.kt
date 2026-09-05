package com.example.myaiassistant

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech

    private lateinit var statusText: TextView
    private lateinit var resultText: TextView
    private lateinit var speakButton: Button

    private val RECORD_AUDIO_REQUEST = 1001

    private var continuousMode = false
    private var isSpeaking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textToSpeech = TextToSpeech(this, this)

        createUI()
        setupSpeechRecognizer()
        setupTextToSpeech()
    }

    // =========================================================
    // UI
    // =========================================================

    private fun createUI() {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(25, 45, 25, 35)

        val background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.rgb(2, 6, 18),
                Color.rgb(5, 20, 38),
                Color.rgb(1, 4, 12)
            )
        )

        root.background = background

        // TITLE
        val title = TextView(this)

        title.text = "A U R I X"
        title.textSize = 34f
        title.setTextColor(Color.rgb(0, 225, 255))
        title.gravity = Gravity.CENTER

        // SUBTITLE
        val subtitle = TextView(this)

        subtitle.text = "VOICE INTELLIGENCE SYSTEM"
        subtitle.textSize = 12f
        subtitle.setTextColor(Color.rgb(130, 210, 230))
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(0, 0, 0, 25)

        // STATUS
        statusText = TextView(this)

        statusText.text = "● STANDBY"
        statusText.textSize = 15f
        statusText.setTextColor(Color.rgb(0, 255, 200))
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(10, 10, 10, 20)

        // ORB
        val orb = TextView(this)

        orb.text = "◉"
        orb.textSize = 95f
        orb.setTextColor(Color.rgb(0, 220, 255))
        orb.gravity = Gravity.CENTER

        orb.setOnClickListener {
            toggleAURIX()
        }

        // RESULT BOX
        resultText = TextView(this)

        resultText.text = "Say:\n\"Hello\""

        resultText.textSize = 17f
        resultText.setTextColor(Color.WHITE)
        resultText.gravity = Gravity.CENTER
        resultText.setPadding(20, 25, 20, 25)

        val resultBackground = GradientDrawable()

        resultBackground.setColor(
            Color.rgb(7, 24, 42)
        )

        resultBackground.cornerRadius = 28f

        resultBackground.setStroke(
            1,
            Color.rgb(0, 145, 190)
        )

        resultText.background = resultBackground

        val resultParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        resultParams.setMargins(
            10,
            15,
            10,
            25
        )

        // ACTIVATE BUTTON
        speakButton = Button(this)

        speakButton.text = "ACTIVATE AURIX"

        // Clipping fix
        speakButton.textSize = 15f
        speakButton.isAllCaps = false
        speakButton.gravity = Gravity.CENTER
        speakButton.setSingleLine(true)
        speakButton.setPadding(0, 0, 0, 0)
        speakButton.minimumHeight = 0
        speakButton.minHeight = 0
        speakButton.setTextColor(Color.WHITE)

        val buttonBackground = GradientDrawable()

        buttonBackground.setColor(
            Color.rgb(0, 115, 170)
        )

        buttonBackground.cornerRadius = 55f

        buttonBackground.setStroke(
            2,
            Color.rgb(0, 225, 255)
        )

        speakButton.background = buttonBackground

        speakButton.setOnClickListener {
            toggleAURIX()
        }

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            72
        )

        buttonParams.setMargins(
            20,
            5,
            20,
            5
        )

        root.addView(title)

        root.addView(subtitle)

        root.addView(statusText)

        root.addView(
            orb,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                140
            )
        )

        root.addView(
            resultText,
            resultParams
        )

        root.addView(
            speakButton,
            buttonParams
        )

        setContentView(root)
    }

    // =========================================================
    // TEXT TO SPEECH
    // =========================================================

    private fun setupTextToSpeech() {

        textToSpeech.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {

                override fun onStart(
                    utteranceId: String?
                ) {
                    runOnUiThread {
                        isSpeaking = true
                        statusText.text =
                            "● AURIX SPEAKING..."
                    }
                }

                override fun onDone(
                    utteranceId: String?
                ) {

                    runOnUiThread {

                        isSpeaking = false

                        if (continuousMode) {

                            statusText.text =
                                "● LISTENING FOR \"HELLO\""

                            restartListening()
                        } else {

                            statusText.text =
                                "● STANDBY"
                        }
                    }
                }

                override fun onError(
                    utteranceId: String?
                ) {

                    runOnUiThread {

                        isSpeaking = false

                        if (continuousMode) {
                            restartListening()
                        }
                    }
                }
            }
        )
    }

    private fun speak(text: String) {

        try {

            if (::speechRecognizer.isInitialized) {
                speechRecognizer.cancel()
            }

            isSpeaking = true

            statusText.text =
                "● AURIX SPEAKING..."

            textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "AURIX_RESPONSE"
            )

        } catch (e: Exception) {

            isSpeaking = false

            if (continuousMode) {
                restartListening()
            }
        }
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result =
                textToSpeech.setLanguage(
                    Locale("hi", "IN")
                )

            textToSpeech.setSpeechRate(
                0.95f
            )

            if (
                result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {

                textToSpeech.language =
                    Locale.US
            }
        }
    }

    // =========================================================
    // SPE
