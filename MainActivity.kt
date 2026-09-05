package com.example.myaiassistant

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
import android.provider.AlarmClock
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
    private lateinit var speechIntent: Intent
    private lateinit var textToSpeech: TextToSpeech

    private lateinit var statusText: TextView
    private lateinit var commandText: TextView
    private lateinit var speakButton: Button

    private var isListening = false
    private var isSpeaking = false
    private var continuousMode = true
    private var torchOn = false

    private val wakeWords = listOf(
        "hello",
        "हेलो",
        "हेल्लो",
        "हलो"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUI()

        textToSpeech = TextToSpeech(this, this)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                100
            )
        }

        setupSpeechRecognizer()
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------

    private fun createUI() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(35, 45, 35, 45)
        root.setBackgroundColor(Color.rgb(8, 10, 18))

        val title = TextView(this)
        title.text = "AURIX"
        title.textSize = 42f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        title.setTypeface(null, android.graphics.Typeface.BOLD)

        val subtitle = TextView(this)
        subtitle.text = "YOUR PERSONAL AI ASSISTANT"
        subtitle.textSize = 13f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER

        statusText = TextView(this)
        statusText.text = "AURIX is ready"
        statusText.textSize = 20f
        statusText.setTextColor(Color.WHITE)
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 60, 0, 25)

        commandText = TextView(this)
        commandText.text = "Say:  Hello AURIX"
        commandText.textSize = 16f
        commandText.setTextColor(Color.GRAY)
        commandText.gravity = Gravity.CENTER
        commandText.setPadding(0, 0, 0, 50)

        speakButton = Button(this)
        speakButton.text = "ACTIVATE AURIX"
        speakButton.textSize = 15f
        speakButton.isAllCaps = false
        speakButton.setSingleLine(true)
        speakButton.gravity = Gravity.CENTER
        speakButton.setPadding(0, 0, 0, 0)

        val buttonBackground = GradientDrawable()
        buttonBackground.cornerRadius = 40f
        buttonBackground.setColor(Color.rgb(35, 90, 180))
        speakButton.background = buttonBackground

        speakButton.setTextColor(Color.WHITE)

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            72
        )

        buttonParams.setMargins(0, 20, 0, 0)

        speakButton.layoutParams = buttonParams

        speakButton.setOnClickListener {
            toggleAURIX()
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(statusText)
        root.addView(commandText)
        root.addView(speakButton)

        setContentView(root)
    }

    // ---------------------------------------------------------
    // TEXT TO SPEECH
    // ---------------------------------------------------------

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            textToSpeech.language = Locale.US

            textToSpeech.setSpeechRate(0.95f)

            textToSpeech.setPitch(0.85f)

            textToSpeech.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {

                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }

                    override fun onDone(utteranceId: String?) {

                        runOnUiThread {

                            isSpeaking = false

                            if (continuousMode) {
                                restartListening()
                            }
                        }
                    }

                    override fun onError(utteranceId: String?) {

                        runOnUiThread {

                            isSpeaking = false

                            if (continuousMode) {
                                restartListening()
                            }
                        }
                    }
                }
            )

            if (continuousMode) {
                restartListening()
            }
        }
    }

    private fun speak(message: String) {

        if (!::textToSpeech.isInitialized) {
            return
        }

        speechRecognizer.stopListening()

        isListening = false
        isSpeaking = true

        statusText.text = "AURIX speaking..."
        commandText.text = message

        textToSpeech.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "AURIX_${System.currentTimeMillis()}"
        )
    }

    // ---------------------------------------------------------
    // SPEECH RECOGNIZER
    // ---------------------------------------------------------

    private fun setupSpeechRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            statusText.text = "Speech recognition unavailable"

            Toast.makeText(
                this,
                "Speech recognition is not available",
                Toast.LENGTH_LONG
            ).show()

            return
        }

       
