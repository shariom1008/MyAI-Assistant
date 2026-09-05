package com.example.myaiassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.graphics.Color
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
        root.setPadding(35, 40, 35, 40)
        root.setBackgroundColor(Color.rgb(250, 245, 250))

        val title = TextView(this)

        title.text = "MY AI"
        title.textSize = 38f
        title.setTextColor(Color.rgb(30, 30, 30))
        title.gravity = Gravity.CENTER

        val subtitle = TextView(this)

        subtitle.text =
            "Aap jo bolenge, MY AI us command ko samajhne ki koshish karega."

        subtitle.textSize = 18f
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(10, 25, 10, 30)

        statusText = TextView(this)

        statusText.text = "Ready"
        statusText.textSize = 16f
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(10, 10, 10, 15)

        resultText = TextView(this)

        resultText.text = "Command yahan dikhega"
        resultText.textSize = 19f
        resultText.gravity = Gravity.CENTER
        resultText.setPadding(20, 10, 20, 20)

        speakButton = Button(this)

        speakButton.text = "🎙️  Speak"
        speakButton.textSize = 18f

        speakButton.setOnClickListener {
            startListening()
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(statusText)
        root.addView(resultText)
        root.addView(speakButton)

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
                    statusText.text = "🎙️ Listening..."
                    speakButton.text = "🎙️ Listening..."
                }

                override fun onBeginningOfSpeech() {
                    statusText.text = "🔴 Sun raha hoon..."
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    statusText.text = "⏳ Processing..."
                    speakButton.text = "🎙️ Speak"
                }

                override fun onError(error: Int) {

                    statusText.text = "❌ Voice error"
                    speakButton.text = "🎙️ Speak"

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
                            "You said:\n$command"

                        statusText.text =
                            "✅ Command received"

                        processCommand(command)
                    }

                    speakButton.text = "🎙️ Speak"
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

        if (
            lowerCommand.contains("hello") ||
            lowerCommand.contains("hi") ||
            lowerCommand.contains("नमस्ते") ||
            lowerCommand.contains("हेलो")
        ) {

            speak(
                "Hello! Main aapka MY AI assistant hoon."
            )

            return
        }

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

                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    )

                startActivity(intent)

            } else {

                speak(
                    "Aap kya search karna chahte hain?"
                )
            }

            return
        }

        if (
            lowerCommand.contains("settings") ||
            lowerCommand.contains("setting") ||
            lowerCommand.contains("सेटिंग")
        ) {

            speak("Settings open kar raha hoon.")

            try {

                val intent =
                    Intent(
                        android.provider.Settings.ACTION_SETTINGS
                    )

                startActivity(intent)

            } catch (e: Exception) {

                speak("Settings open nahi ho paayi.")
            }

            return
        }

        speak(
            "Maine suna: $command. " +
                    "Abhi main is command ko directly perform nahi kar sakta."
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

        statusText.text = "🔊 Speaking..."

        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "MY_AI_RESPONSE"
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
