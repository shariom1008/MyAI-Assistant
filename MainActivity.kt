package com.example.myaiassistant

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
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
import android.view.View
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

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private lateinit var tts: TextToSpeech

    private lateinit var statusText: TextView
    private lateinit var commandText: TextView
    private lateinit var responseText: TextView
    private lateinit var button: Button

    private var listening = false
    private var speaking = false
    private var continuousMode = true
    private var activityVisible = false

    private val wakeWords = listOf(
        "hello aurix",
        "hello",
        "hey aurix",
        "aurix",
        "हेलो ऑरिक्स",
        "हेलो",
        "ऑरिक्स"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUI()

        tts = TextToSpeech(this, this)

        checkMicrophonePermission()

        setupSpeechRecognizer()
    }

    override fun onResume() {
        super.onResume()
        activityVisible = true

        if (
            ::recognizer.isInitialized &&
            continuousMode &&
            !speaking
        ) {
            window.decorView.postDelayed({
                restartListening()
            }, 700)
        }
    }

    override fun onPause() {
        activityVisible = false

        if (::recognizer.isInitialized) {
            recognizer.cancel()
        }

        listening = false

        super.onPause()
    }

    private fun createUI() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(30, 35, 30, 35)
        root.setBackgroundColor(Color.rgb(5, 8, 16))

        val title = TextView(this)
        title.text = "AURIX"
        title.textSize = 44f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        title.setTypeface(null, Typeface.BOLD)

        val subtitle = TextView(this)
        subtitle.text = "YOUR PERSONAL AI ASSISTANT"
        subtitle.textSize = 12f
        subtitle.setTextColor(Color.rgb(150, 170, 200))
        subtitle.gravity = Gravity.CENTER

        val line = View(this)
        val lineBg = GradientDrawable()
        lineBg.setColor(Color.rgb(45, 100, 190))
        lineBg.cornerRadius = 10f
        line.background = lineBg

        val lineParams = LinearLayout.LayoutParams(
            100,
            4
        )
        lineParams.setMargins(0, 18, 0, 35)
        line.layoutParams = lineParams

        statusText = TextView(this)
        statusText.text = "Initializing AURIX..."
        statusText.textSize = 19f
        statusText.setTextColor(Color.WHITE)
        statusText.gravity = Gravity.CENTER
        statusText.setTypeface(null, Typeface.BOLD)

        commandText = TextView(this)
        commandText.text = "Say a command..."
        commandText.textSize = 16f
        commandText.setTextColor(Color.LTGRAY)
        commandText.gravity = Gravity.CENTER
        commandText.setPadding(15, 30, 15, 15)

        responseText = TextView(this)
        responseText.text = ""
        responseText.textSize = 15f
        responseText.setTextColor(Color.rgb(130, 180, 255))
        responseText.gravity = Gravity.CENTER
        responseText.setPadding(15, 10, 15, 45)

        button = Button(this)
        button.text = "ACTIVATE AURIX"
        button.textSize = 15f
        button.isAllCaps = false
        button.setTextColor(Color.WHITE)

        val buttonBg = GradientDrawable()
        buttonBg.setColor(Color.rgb(35, 90, 180))
        buttonBg.cornerRadius = 50f

        button.background = buttonBg

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            70
        )

        buttonParams.setMargins(0, 10, 0, 0)
        button.layoutParams = buttonParams

        button.setOnClickListener {
            toggleAURIX()
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(line)
        root.addView(statusText)
        root.addView(commandText)
        root.addView(responseText)
        root.addView(button)

        setContentView(root)
    }

    private fun checkMicrophonePermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    override fun onInit(status: Int) {

        if (status != TextToSpeech.SUCCESS) {
            return
        }

        val result = tts.setLanguage(Locale.US)

        if (
            result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            tts.language = Locale.getDefault()
        }

        tts.setSpeechRate(0.92f)
        tts.setPitch(0.82f)

        tts.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {

                override fun onStart(id: String?) {
                    speaking = true
                }

                override fun onDone(id: String?) {
                    runOnUiThread {
                        speaking = false

                        if (
                            continuousMode &&
                            activityVisible
                        ) {
                            restartListening()
                        }
                    }
                }

                override fun onError(id: String?) {
                    runOnUiThread {
                        speaking = false

                        if (
                            continuousMode &&
                            activityVisible
                        ) {
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

    private fun speak(message: String) {

        if (!::tts.isInitialized) return

        if (::recognizer.isInitialized) {
            recognizer.cancel()
        }

        listening = false
        speaking = true

        runOnUiThread {
            statusText.text = "AURIX speaking..."
            responseText.text = message
        }

        tts.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "AURIX_${System.currentTimeMillis()}"
        )
    }

    private fun setupSpeechRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            statusText.text = "Speech recognition unavailable"

            Toast.makeText(
                this,
                "Speech recognition unavailable",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        recognizer =
            SpeechRecognizer.createSpeechRecognizer(this)

        speechIntent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            )

        speechIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        speechIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        speechIntent.putExtra(
            RecognizerIntent.EXTRA_PARTIAL_RESULTS,
            false
        )

        speechIntent.putExtra(
            RecognizerIntent.EXTRA_MAX_RESULTS,
            3
        )

        recognizer.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {
                    runOnUiThread {
                        if (!speaking) {
                            statusText.text =
                                "Listening..."
                        }
                    }
                }

                override fun onBeginningOfSpeech() {
                    runOnUiThread {
                        statusText.text =
                            "I'm listening..."
                    }
                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) {
                }

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                }

                override fun onEndOfSpeech() {
                    runOnUiThread {
                        if (!speaking) {
                            statusText.text =
                                "Processing..."
                        }
                    }
                }

                override fun onError(
                    error: Int
                ) {

                    listening = false

                    if (
                        continuousMode &&
                        activityVisible &&
                        !speaking
                    ) {
                        window.decorView.postDelayed({
                            restartListening()
                        }, 900)
                    }
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    listening = false

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    if (!matches.isNullOrEmpty()) {

                        val spoken =
                            matches[0].trim()

                        runOnUiThread {
                            commandText.text = spoken
                        }

                        handleVoiceInput(spoken)

                    } else {

                        if (
                            continuousMode &&
                            activityVisible &&
                            !speaking
                        ) {
                            restartListening()
                        }
                    }
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {
                }

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {
                }
            }
        )
    }

    private fun restartListening() {

        if (!continuousMode) return
        if (speaking) return
        if (!activityVisible) return

        if (!::recognizer.isInitialized) return

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {

            recognizer.cancel()

            recognizer.startListening(
                speechIntent
            )

            listening = true

            runOnUiThread {
                statusText.text =
                    "Listening for commands..."
            }

        } catch (e: Exception) {

            listening = false

            window.decorView.postDelayed({
                restartListening()
            }, 1200)
        }
    }

    private fun toggleAURIX() {

        if (listening) {

            continuousMode = false

            recognizer.cancel()

            listening = false

            statusText.text =
                "AURIX paused"

            button.text =
                "ACTIVATE AURIX"

        } else {

            continuousMode = true

            button.text =
                "STOP AURIX"

            restartListening()
        }
    }

    private fun handleVoiceInput(input: String) {

        var command =
            input.lowercase(Locale.getDefault()).trim()

        // Wake word optional
        for (wake in wakeWords) {

            if (command.contains(wake)) {

                command =
                    command.replace(
                        wake,
                        "",
                        ignoreCase = true
                    )

                break
            }
        }

        command = cleanCommand(command)

        if (command.isEmpty()) {

            speak(
                "Yes, I'm listening. What can I do for you?"
            )

            return
        }

        processCommand(command)
    }

    private fun cleanCommand(text: String): String {

        return text
            .replace("please", "")
            .replace("can you", "")
            .replace("could you", "")
            .replace("would you", "")
            .replace("kya tum", "")
            .replace("please", "")
            .trim()
    }

    private fun processCommand(command: String) {

        when {

            // TIME
            command.contains("time") ||
                    command.contains("what time") ||
                    command.contains("समय") ||
                    command.contains("कितने बजे") -> {

                val time =
                    SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(Date())

                speak(
                    "The current time is $time"
                )
            }

            // DATE
            command.contains("date") ||
                    command.contains("today") ||
                    command.contains("तारीख") ||
                    command.contains("आज की तारीख") -> {

                val date =
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                    ).format(Date())

                speak(
                    "Today is $date"
                )
            }

            // YOUTUBE
            command.contains("youtube") ||
                    command.contains("यूट्यूब") -> {

                val query =
                    command
                        .replace("youtube", "")
                        .replace("यूट्यूब", "")
                        .replace("open", "")
                        .replace("खोलो", "")
                        .replace("search", "")
                        .replace("सर्च", "")
                        .trim()

                if (query.isEmpty()) {

                    openUrl(
                        "https://www.youtube.com"
                    )

                    speak(
                        "Opening YouTube
