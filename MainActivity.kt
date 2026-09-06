package com.example.myaiassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
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

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null

    private lateinit var statusText: TextView
    private lateinit var commandText: TextView
    private lateinit var activateButton: Button

    private var listening = false

    private val handler =
        android.os.Handler(android.os.Looper.getMainLooper())

    companion object {
        private const val MIC_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createInterface()

        tts = TextToSpeech(this, this)

        requestMicrophonePermission()

        setupRecognizer()
    }

    // =========================
    // INTERFACE
    // =========================

    private fun createInterface() {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(30, 70, 30, 40)

        root.setBackgroundColor(
            Color.rgb(5, 8, 18)
        )

        val title = TextView(this)

        title.text = "AURIX"
        title.textSize = 42f
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.WHITE)
        title.setTypeface(
            null,
            android.graphics.Typeface.BOLD
        )

        val subtitle = TextView(this)

        subtitle.text = "YOUR PERSONAL AI ASSISTANT"
        subtitle.textSize = 12f
        subtitle.gravity = Gravity.CENTER
        subtitle.setTextColor(
            Color.rgb(120, 180, 255)
        )

        subtitle.setPadding(0, 5, 0, 35)

        statusText = TextView(this)

        statusText.text = "AURIX READY"
        statusText.textSize = 18f
        statusText.gravity = Gravity.CENTER
        statusText.setTextColor(
            Color.rgb(40, 150, 255)
        )

        statusText.setPadding(0, 15, 0, 15)

        commandText = TextView(this)

        commandText.text = "Say something..."
        commandText.textSize = 22f
        commandText.gravity = Gravity.CENTER
        commandText.setTextColor(Color.WHITE)

        commandText.setPadding(20, 30, 20, 30)

        activateButton = Button(this)

        activateButton.text = "ACTIVATE AURIX"

        activateButton.setTextColor(Color.WHITE)

        activateButton.setOnClickListener {
            toggleAURIX()
        }

        root.addView(title)

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            commandText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(
            activateButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                65
            )
        )

        setContentView(root)
    }

    // =========================
    // PERMISSION
    // =========================

    private fun requestMicrophonePermission() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MIC_PERMISSION
            )
        }
    }

    // =========================
    // TTS
    // =========================

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts.language = Locale.US
            tts.setSpeechRate(0.95f)
        }
    }

    private fun speak(text: String) {

        if (!::tts.isInitialized) {
            return
        }

        statusText.text = text

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "aurix_${System.currentTimeMillis()}"
        )
    }

    // =========================
    // SPEECH RECOGNITION
    // =========================

    private fun setupRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            statusText.text =
                "Speech recognition unavailable"

            return
        }

        recognizer?.destroy()

        recognizer =
            SpeechRecognizer.createSpeechRecognizer(this)

        recognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {
                    statusText.text = "Listening..."
                }

                override fun onBeginningOfSpeech() {
                    statusText.text = "I'm listening..."
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
                    statusText.text = "Processing..."
                }

                override fun onError(
                    error: Int
                ) {

                    if (!listening) {
                        return
                    }

                    handler.postDelayed({

                        if (listening) {
                            startListening()
                        }

                    }, 700)
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    val list =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val command =
                        list?.firstOrNull()
                            ?.trim()
                            .orEmpty()

                    if (command.isNotEmpty()) {

                        commandText.text = command

                        processCommand(command)

                    } else {

                        if (listening) {
                            startListening()
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

    private fun startListening() {

        if (!listening) {
            return
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {

            recognizer?.cancel()

            val intent =
                Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.US
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            )

            recognizer?.startListening(intent)

        } catch (e: Exception) {

            handler.postDelayed({

                if (listening) {
                    startListening()
                }

            }, 1000)
        }
    }

    private fun toggleAURIX() {

        if (listening) {

            listening = false

            recognizer?.cancel()

            activateButton.text =
                "ACTIVATE AURIX"

            statusText.text =
                "AURIX SLEEPING"

            speak("Okay. I am sleeping.")

        } else {

            listening = true

            activateButton.text =
                "DEACTIVATE AURIX"

            speak("AURIX is ready.")

            handler.postDelayed({

                if (listening) {
                    startListening()
                }

            }, 1200)
        }
    }

    // =========================
    // COMMAND PROCESSOR
    // =========================

    private fun processCommand(input: String) {

        var command =
            input.lowercase(Locale.getDefault()).trim()

        command = command
            .replace("aurix", "")
            .replace("aurex", "")
            .replace("orix", "")
            .trim()

        if (command.isEmpty()) {

            speak("Yes, how can I help?")
            return
        }

        // =========================
        // TIMER — MUST COME BEFORE TIME
        // =========================

        if (
            command.contains("timer") ||
            command.contains("set timer") ||
            command.contains("countdown")
        ) {

            val number =
                Regex("\\d+")
                    .find(command)
                    ?.value
                    ?.toLongOrNull()

            if (number != null && number > 0) {

                setTimer(number)

            } else {

                speak(
                    "Please tell me the timer duration."
                )
            }

            return
        }

        // =========================
        // TIME
        // =========================

        if (
            command.contains("what time") ||
            command == "time" ||
            command.contains("kitne baje") ||
            command.contains("time batao") ||
            command.contains("टाइम")
        ) {

            val time =
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.US
                ).format(Date())

            speak(
                "The time is $time."
            )

            return
        }

        // =========================
        // HELLO
        // =========================

        if (
            command.contains("hello") ||
            command == "hi" ||
            command.contains("namaste")
        ) {

            speak(
                "Hello. I am AURIX. How can I help you?"
            )

            return
        }

        // =========================
        // IDENTITY
        // =========================

        if (
            command.contains("who are you") ||
            command.contains("your name") ||
            command.contains("what are you") ||
            command.contains("tum kon ho") ||
            command.contains("tumhara naam")
        ) {

            speak(
                "I am AURIX, your personal AI assistant."
            )

            return
        }

        // =========================
        // GALLERY
        // =========================

        if (
            command.contains("gallery") ||
            command.contains("photos") ||
            command.contains("photo kholo") ||
            command.contains("gallery kholo")
        ) {

            openGallery()

            return
        }

        // =========================
// MUSIC
// =========================

if (
    command.contains("music") ||
    command.contains("song") ||
    command.contains("music player") ||
    command.contains("gaane")
) {

    openMusic()

    return
}

        // =========================
        // NOTES
        // =========================

        if (
            command.contains("notes") ||
            command.contains("note") ||
            command.contains("notepad") ||
            command.contains("notes kholo")
        ) {

            openNotes()

            return
        }

        // =========================
        // UNKNOWN COMMAND
        // =========================

        speak(
            "I don't know that command yet."
        )
    }

    // =========================
    // TIMER
    // =========================

    private fun setTimer(minutes: Long) {

        try {

            val intent =
                Intent(AlarmClock.ACTION_SET_TIMER)

            intent.putExtra(
                AlarmClock.EXTRA_LENGTH,
                (minutes * 60).toInt()
            )

            intent.putExtra(
                AlarmClock.EXTRA_MESSAGE,
                "AURIX Timer"
            )

            startActivity(intent)

            speak(
                "Timer set for $minutes minutes."
            )

        } catch (e: Exception) {

            speak(
                "I could not set the timer."
            )
        }
    }

    // =========================
    // GALLERY
    // =========================

    private fun openGallery() {

        try {

            val intent =
                Intent(Intent.ACTION_VIEW)

            intent.type = "image/*"

            startActivity(intent)

            speak(
                "Opening gallery."
            )

        } catch (e: Exception) {

            speak(
                "I could not open the gallery."
            )
        }
    }

    // =========================
    // MUSIC
    // =========================

    private fun openMusic() {

        try {

            val intent =
                Intent(Intent.ACTION_MAIN)

            intent.addCategory(
                Intent.CATEGORY_APP_MUSIC
            )

            startActivity(intent)

            speak(
                "Opening music."
            )

        } catch (e: Exception) {

            try {

                val intent =
                    Intent(Intent.ACTION_VIEW)

                intent.type = "audio/*"

                startActivity(intent)

                speak(
                    "Opening music."
                )

            } catch (e2: Exception) {

                speak(
                    "I could not find a music app."
                )
            }
        }
    }

    // =========================
    // NOTES
    // =========================

    private fun openNotes() {

        try {

            val intent =
                Intent(Intent.ACTION_CREATE_DOCUMENT)

            intent.type = "text/plain"

            intent.putExtra(
                Intent.EXTRA_TITLE,
                "AURIX Note.txt"
            )

            startActivity(intent)

            speak(
                "Opening notes."
            )

        } catch (e: Exception) {

            speak(
                "I could not open notes."
            )
        }
    }

}
