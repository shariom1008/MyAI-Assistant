package com.example.myaiassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech

    private lateinit var statusText: TextView
    private lateinit var resultText: TextView
    private lateinit var speakButton: Button

    private val RECORD_AUDIO_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textToSpeech = TextToSpeech(this, this)

        createUI()
        setupSpeechRecognizer()
    }

    private fun createUI() {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setPadding(30, 50, 30, 40)

        val background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.rgb(3, 8, 20),
                Color.rgb(5, 18, 35),
                Color.rgb(2, 5, 15)
            )
        )

        root.background = background

        // AURIX title
        val title = TextView(this)

        title.text = "A U R I X"
        title.textSize = 34f
        title.setTextColor(Color.rgb(0, 220, 255))
        title.gravity = Gravity.CENTER
        title.setPadding(0, 0, 0, 5)

        // Subtitle
        val subtitle = TextView(this)

        subtitle.text = "ADVANCED VOICE INTELLIGENCE"
        subtitle.textSize = 12f
        subtitle.setTextColor(Color.rgb(130, 210, 230))
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(0, 0, 0, 35)

        // Status
        statusText = TextView(this)

        statusText.text = "● SYSTEM READY"
        statusText.textSize = 16f
        statusText.setTextColor(Color.rgb(0, 255, 200))
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(10, 10, 10, 25)

        // Futuristic Orb
        val orb = TextView(this)

        orb.text = "◉"
        orb.textSize = 100f
        orb.setTextColor(Color.rgb(0, 220, 255))
        orb.gravity = Gravity.CENTER

        orb.setOnClickListener {
            startListening()
        }

        // Command display
        resultText = TextView(this)

        resultText.text = "Awaiting command..."
        resultText.textSize = 18f
        resultText.setTextColor(Color.WHITE)
        resultText.gravity = Gravity.CENTER
        resultText.setPadding(25, 30, 25, 30)

        val resultBackground = GradientDrawable()
        resultBackground.setColor(Color.rgb(8, 25, 42))
        resultBackground.cornerRadius = 30f
        resultBackground.setStroke(1, Color.rgb(0, 150, 190))

        resultText.background = resultBackground

        val resultParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        resultParams.setMargins(15, 20, 15, 30)

        root.addView(title)
        root.addView(subtitle)
        root.addView(statusText)
        root.addView(
            orb,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                150
            )
        )
        root.addView(resultText, resultParams)

        // Speak button
        speakButton = Button(this)

        speakButton.text = "🎙  ACTIVATE AURIX"
        speakButton.textSize = 16f
        speakButton.setTextColor(Color.WHITE)

        val buttonBackground = GradientDrawable()
        buttonBackground.setColor(Color.rgb(0, 120, 170))
        buttonBackground.cornerRadius = 60f
        buttonBackground.setStroke(2, Color.rgb(0, 230, 255))

        speakButton.background = buttonBackground

        speakButton.setOnClickListener {
            startListening()
        }

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            65
        )

        buttonParams.setMargins(30, 10, 30, 10)

        root.addView(speakButton, buttonParams)

        setContentView(root)
    }

    private fun setupSpeechRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            Toast.makeText(
                this,
                "Speech recognition available nahi hai.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    statusText.text = "● LISTENING..."
                    speakButton.text = "🎙  LISTENING..."
                }

                override fun onBeginningOfSpeech() {
                    statusText.text = "● AURIX IS LISTENING"
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    statusText.text = "● PROCESSING..."
                    speakButton.text = "PROCESSING..."
                }

                override fun onError(error: Int) {

                    statusText.text = "● VOICE ERROR"
                    speakButton.text = "🎙  ACTIVATE AURIX"

                    Toast.makeText(
                        this@MainActivity,
                        getErrorMessage(error),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onResults(results: Bundle?) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val command = matches?.firstOrNull()

                    if (!command.isNullOrBlank()) {

                        resultText.text =
                            "COMMAND RECEIVED\n\n$command"

                        statusText.text =
                            "● COMMAND RECEIVED"

                        processCommand(command)
                    }

                    speakButton.text = "🎙  ACTIVATE AURIX"
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {}

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}
            }
        )
    }

    private fun startListening() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST
            )

            return
        }

        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "hi-IN"
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            "hi-IN"
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PARTIAL_RESULTS,
            true
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_MAX_RESULTS,
            5
        )

        speechRecognizer.startListening(intent)
    }

    private fun processCommand(command: String) {

        val lowerCommand =
            command.lowercase(Locale.getDefault()).trim()

        // Greeting
        if (
            lowerCommand.contains("hello") ||
            lowerCommand.contains("hi") ||
            lowerCommand.contains("नमस्ते") ||
            lowerCommand.contains("हेलो")
        ) {

            speak(
                "Hello! Main AURIX hoon. Aapki command ke liye ready hoon."
            )

            return
        }

        // Time
        if (
            lowerCommand.contains("time") ||
            lowerCommand.contains("टाइम") ||
            lowerCommand.contains("समय") ||
            lowerCommand.contains("वक्त")
        ) {

            val time =
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

            speak("Abhi time hai $time")

            return
        }

        // Date
        if (
            lowerCommand.contains("date") ||
            lowerCommand.contains("तारीख") ||
            lowerCommand.contains("आज कौन सा दिन")
        ) {

            val date =
                SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.getDefault()
                ).format(Date())

            speak("Aaj ki date hai $date")

            return
        }

        // YouTube
        if (
            lowerCommand.contains("open youtube") ||
            lowerCommand.contains("youtube kholo") ||
            lowerCommand.contains("youtube khol") ||
            lowerCommand.contains("यूट्यूब खोलो") ||
            lowerCommand.contains("यूट्यूब खोल")
        ) {

            speak("YouTube open kar raha hoon.")

            openApp(
                "com.google.android.youtube",
                "YouTube"
            )

            return
        }

        // Chrome
        if (
            lowerCommand.contains("open chrome") ||
            lowerCommand.contains("chrome kholo") ||
            lowerCommand.contains("chrome khol") ||
            lowerCommand.contains("क्रोम खोलो") ||
            lowerCommand.contains("क्रोम खोल")
        ) {

            speak("Chrome open kar raha hoon.")

            openApp(
                "com.android.chrome",
                "Chrome"
            )

            return
        }

        // Google search
        if (
            lowerCommand.contains("search") ||
            lowerCommand.contains("google par") ||
            lowerCommand.contains("google pe") ||
            lowerCommand.contains("सर्च") ||
            lowerCommand.contains("गूगल पर")
        ) {

            var searchText = lowerCommand

            searchText = searchText
                .replace("google par", "")
                .replace("google pe", "")
                .replace("search", "")
                .replace("सर्च", "")
                .replace("गूगल पर", "")
                .replace("करो", "")
                .replace("karo", "")
                .trim()

            if (searchText.isNotEmpty()) {

                speak(
                    "Google par $searchText search kar raha hoon."
                )

                val url =
                    "https://www.google.com/search?q=" +
                            Uri.encode(searchText)

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    )
                )

            } else {

                speak(
                    "Aap kya search karna chahte hain?"
                )
            }

            return
        }

        // Settings
        if (
            lowerCommand.contains("settings") ||
            lowerCommand.contains("setting") ||
            lowerCommand.contains("सेटिंग")
        ) {

            speak("Settings open kar raha hoon.")

            try {

                startActivity(
                    Intent(Settings.ACTION_SETTINGS)
                )

            } catch (e: Exception) {

                speak("Settings open nahi ho paayi.")
            }

            return
        }

        speak(
            "Maine suna: $command. " +
                    "Abhi ye command meri capabilities mein nahi hai."
        )
    }

    private fun openApp(
        packageName: String,
        appName: String
    ) {

        try {

            val intent =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            if (intent != null) {

                startActivity(intent)

            } else if (appName == "YouTube") {

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com")
                    )
                )

            } else {

                speak(
                    "$appName app installed nahi hai."
                )
            }

        } catch (e: Exception) {

            speak(
                "$appName open nahi ho paaya."
            )
        }
    }

    private fun speak(text: String) {

        statusText.text = "● AURIX SPEAKING..."

        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "AURIX_RESPONSE"
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result =
                textToSpeech.setLanguage(
                    Locale("hi", "IN")
                )

            textToSpeech.setSpeechRate(0.95f)

            if (
                result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {

                textToSpeech.language = Locale.US
            }
        }
    }

    private fun getErrorMessage(error: Int): String {

        return when (error) {

            SpeechRecognizer.ERROR_AUDIO ->
                "Microphone error"

            SpeechRecognizer.ERROR_NETWORK ->
                "Network error"

            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Network timeout"

            SpeechRecognizer.ERROR_NO_MATCH ->
                "Kuch samajh nahi aaya, dobara bolo."

            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "Aapne kuch bola nahi."

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "Microphone permission required."

            else ->
                "Voice recognition error"
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

        if (requestCode == RECORD_AUDIO_REQUEST) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                startListening()

            } else {

                Toast.makeText(
                    this,
                    "Microphone permission deni hogi.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {

        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }

        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }

        super.onDestroy()
    }
}
