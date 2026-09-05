package com.example.myaiassistant

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

        checkMicrophonePermission()

        setupSpeechRecognizer()
    }

    // =========================================================
    // UI
    // =========================================================

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
        title.setTypeface(null, Typeface.BOLD)

        val subtitle = TextView(this)

        subtitle.text = "YOUR PERSONAL AI ASSISTANT"
        subtitle.textSize = 13f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER

        statusText = TextView(this)

        statusText.text = "AURIX is starting..."
        statusText.textSize = 20f
        statusText.setTextColor(Color.WHITE)
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 60, 0, 25)

        commandText = TextView(this)

        commandText.text = "Say: Hello AURIX"
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
        speakButton.setTextColor(Color.WHITE)

        val buttonBackground = GradientDrawable()

        buttonBackground.cornerRadius = 40f
        buttonBackground.setColor(Color.rgb(35, 90, 180))

        speakButton.background = buttonBackground

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

    // =========================================================
    // MICROPHONE PERMISSION
    // =========================================================

    private fun checkMicrophonePermission() {

        if (
            ContextCompat.checkSelfPermission(
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
    }

    // =========================================================
    // TEXT TO SPEECH
    // =========================================================

    override fun onInit(status: Int) {

        if (status != TextToSpeech.SUCCESS) {
            return
        }

        val result = textToSpeech.setLanguage(Locale.US)

        if (
            result == TextToSpeech.LANG_MISSING_DATA ||
            result == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            textToSpeech.language = Locale.getDefault()
        }

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

    private fun speak(message: String) {

        if (!::textToSpeech.isInitialized) {
            return
        }

        if (::speechRecognizer.isInitialized) {
            speechRecognizer.cancel()
        }

        isListening = false
        isSpeaking = true

        runOnUiThread {

            statusText.text = "AURIX speaking..."
            commandText.text = message
        }

        textToSpeech.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "AURIX_${System.currentTimeMillis()}"
        )
    }

    // =========================================================
    // SPEECH RECOGNIZER SETUP
    // =========================================================

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

        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(this)

        speechIntent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

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

        speechRecognizer.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {

                    runOnUiThread {

                        if (!isSpeaking) {
                            statusText.text =
                                "Listening for Hello AURIX..."
                        }
                    }
                }

                override fun onBeginningOfSpeech() {

                    runOnUiThread {

                        if (!isSpeaking) {
                            statusText.text =
                                "I'm listening..."
                        }
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

                        if (!isSpeaking) {
                            statusText.text =
                                "Processing..."
                        }
                    }
                }

                override fun onError(error: Int) {

                    isListening = false

                    if (
                        continuousMode &&
                        !isSpeaking
                    ) {

                        window.decorView.postDelayed(
                            {
                                restartListening()
                            },
                            800
                        )
                    }
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    isListening = false

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    if (!matches.isNullOrEmpty()) {

                        val spokenText =
                            matches[0].trim()

                        runOnUiThread {

                            commandText.text =
                                spokenText

                            handleVoiceInput(
                                spokenText
                            )
                        }

                    } else {

                        if (
                            continuousMode &&
                            !isSpeaking
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

    // =========================================================
    // RESTART LISTENING
    // =========================================================

    private fun restartListening() {

        if (!continuousMode) {
            return
        }

        if (isSpeaking) {
            return
        }

        if (!::speechRecognizer.isInitialized) {
            return
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {

            speechRecognizer.cancel()

            speechRecognizer.startListening(
                speechIntent
            )

            isListening = true

            runOnUiThread {

                statusText.text =
                    "Listening for Hello AURIX..."
            }

        } catch (e: Exception) {

            isListening = false

            window.decorView.postDelayed(
                {
                    if (
                        continuousMode &&
                        !isSpeaking
                    ) {
                        restartListening()
                    }
                },
                1200
            )
        }
    }

    // =========================================================
    // AURIX TOGGLE
    // =========================================================

    private fun toggleAURIX() {

        if (isListening) {

            continuousMode = false

            speechRecognizer.stopListening()

            isListening = false

            statusText.text = "AURIX paused"

            speakButton.text = "ACTIVATE AURIX"

        } else {

            continuousMode = true

            speakButton.text = "STOP AURIX"

            restartListening()
        }
    }

    // =========================================================
    // VOICE INPUT
    // =========================================================

    private fun handleVoiceInput(input: String) {

        val original = input.trim()

        if (original.isEmpty()) {

            restartListening()

            return
        }

        var command =
            original.lowercase(Locale.getDefault())

        var hasWakeWord = false

        for (word in wakeWords) {

            if (command.contains(word)) {

                hasWakeWord = true

                command = command.replace(
                    word,
                    "",
                    ignoreCase = true
                )

                break
            }
        }

        command = command.trim()

        if (!hasWakeWord) {

            if (continuousMode) {
                restartListening()
            }

            return
        }

        if (command.isEmpty()) {

            speak("Yes, I'm listening.")

            return
        }

        processCommand(command)
    }

    // =========================================================
    // COMMAND PROCESSOR
    // =========================================================

    private fun processCommand(command: String) {

        when {

            command.contains("time") ||
                    command.contains("समय") -> {

                val time =
                    SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(Date())

                speak(
                    "The current time is $time"
                )
            }

            command.contains("date") ||
                    command.contains("दिनांक") ||
                    command.contains("तारीख") -> {

                val date =
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                    ).format(Date())

                speak(
                    "Today is $date"
                )
            }

            command.contains("youtube") -> {

                val searchQuery =
                    command
                        .replace("youtube", "")
                        .replace("search", "")
                        .trim()

                if (searchQuery.isEmpty()) {

                    openUrl(
                        "https://www.youtube.com"
                    )

                    speak("Opening YouTube.")

                } else {

                    val url =
                        "https://www.youtube.com/results?search_query=" +
                                Uri.encode(searchQuery)

                    openUrl(url)

                    speak("Searching YouTube.")
                }
            }

            command.contains("google") ||
                    command.contains("search") -> {

                val searchQuery =
                    command
                        .replace("google", "")
                        .replace("search", "")
                        .trim()

                if (searchQuery.isEmpty()) {

                    openUrl(
                        "https://www.google.com"
                    )

                    speak("Opening Google.")

                } else {

                    val url =
                        "https://www.google.com/search?q=" +
                                Uri.encode(searchQuery)

                    openUrl(url)

                    speak("Searching Google.")
                }
            }

            command.contains("chrome") -> {

                openUrl(
                    "https://www.google.com"
                )

                speak("Opening Chrome.")
            }

            command.contains("settings") ||
                    command.contains("setting") -> {

                try {

                    startActivity(
                        Intent(Settings.ACTION_SETTINGS)
                    )

                    speak("Opening settings.")

                } catch (e: Exception) {

                    speak(
                        "I couldn't open settings."
                    )
                }
            }

            command.contains("volume up") ||
                    command.contains("increase volume") ||
                    command.contains("आवाज़ बढ़ा") -> {

                changeVolume(
                    AudioManager.ADJUST_RAISE
                )

                speak("Volume increased.")
            }

            command.contains("volume down") ||
                    command.contains("decrease volume") ||
                    command.contains("आवाज़ कम") -> {

                changeVolume(
                    AudioManager.ADJUST_LOWER
                )

                speak("Volume decreased.")
            }

            command.contains("mute") -> {

                val audioManager =
                    getSystemService(
                        Context.AUDIO_SERVICE
                    ) as AudioManager

                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_MUTE,
                    AudioManager.FLAG_SHOW_UI
                )

                speak("Media volume muted.")
            }

            command.contains("flashlight on") ||
                    command.contains("torch on") ||
                    command.contains("फ्लैशलाइट चालू") -> {

                setFlashlight(true)

                speak(
                    "Flashlight turned on."
                )
            }

            command.contains("flashlight off") ||
                    command.contains("torch off") ||
                    command.contains("फ्लैशलाइट बंद") -> {

                setFlashlight(false)

                speak(
                    "Flashlight turned off."
                )
            }

            command.contains("camera") ||
                    command.contains("कैमरा") -> {

                try {

                    startActivity(
                        Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE
                        )
                    )

                    speak("Opening camera.")

                } catch (e: Exception) {

                    speak(
                        "Camera is not available."
                    )
                }
            }

            command.contains("calculator") ||
                    command.contains("calculate") ||
                    command.contains("कैलकुलेटर") -> {

                openCalculator()
            }

            command.contains("call") ||
                    command.contains("dial") -> {

                openDialer()
            }

            command.contains("battery") ||
                    command.contains("बैटरी") -> {

                val battery =
                    getBatteryPercentage()

                speak(
                    "Your battery level is $battery percent."
                )
            }

            command.contains("map") ||
                    command.contains("maps") ||
                    command.contains("location") -> {

                val destination =
                    command
                        .replace("open maps", "")
                        .replace("open map", "")
                        .replace("maps", "")
                        .replace("map", "")
                        .replace("location", "")
                        .trim()

                if (destination.isEmpty()) {

                    openUrl(
                        "https://www.google.com/maps"
                    )

                    speak(
                        "Opening Google Maps."
                    )

                } else {

                    val url =
                        "https://www.google.com/maps/search/?api=1&query=" +
                                Uri.encode(destination)

                    openUrl(url)

                    speak("Opening maps.")
                }
            }

            command.contains("timer") ||
                    command.contains("टाइमर") -> {

                setTimer(command)
            }

            command.contains("hello") ||
                    command.contains("hi") ||
                    command.contains("hey") -> {

                speak(
                    "Hello. How can I help you?"
                )
            }

            command.contains("who are you") ||
                    command.contains("what is your name") -> {

                speak(
                    "I am AURIX, your personal AI assistant."
                )
            }

            command.contains("stop listening") ||
                    command.contains("go to sleep") ||
                    command.contains("sleep") -> {

                continuousMode = false

                speechRecognizer.stopListening()

                isListening = false

                speakButton.text =
                    "ACTIVATE AURIX"

                speak(
                    "Okay. I am going to sleep."
                )
            }

            else -> {

                val url =
                    "https://www.google.com/search?q=" +
                            Uri.encode(command)

                openUrl(url)

                speak(
                    "I searched Google for $command"
                )
            }
        }
    }

    // =========================================================
    // OPEN URL
    // =========================================================

    private fun openUrl(url: String) {

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )

            startActivity(intent)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to open",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =========================================================
    // VOLUME
    // =========================================================

    private fun changeVolume(direction: Int) {

        val audioManager =
            getSystemService(
                Context.AUDIO_SERVICE
            ) as AudioManager

        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

    // =========================================================
    // FLASHLIGHT
    // =========================================================

    private fun setFlashlight(enable: Boolean) {

        try {

            val cameraManager =
                getSystemService(
                    Context.CAMERA_SERVICE
                ) as CameraManager

            for (
                cameraId in cameraManager.cameraIdList
            ) {

                val characteristics =
                    cameraManager.getCameraCharacteristics(
                        cameraId
                    )

                val hasFlash =
                    characteristics.get(
                        CameraCharacteristics.FLASH_INFO_AVAILABLE
                    ) == true

                if (hasFlash) {

                    cameraManager.setTorchMode(
                        cameraId,
                        enable
                    )

                    return
                }
            }

            Toast.makeText(
                this,
                "Flashlight not available",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Flashlight not available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =========================================================
    // CALCULATOR
    // =========================================================

    private fun openCalculator() {

        try {

            val intent =
                Intent(Intent.ACTION_MAIN)

            intent.addCategory(
                Intent.CATEGORY_APP_CALCULATOR
            )

            startActivity(intent)

            speak(
                "Opening calculator."
            )

        } catch (e: Exception) {

            openUrl(
                "https://www.google.com/search?q=calculator"
            )

            speak(
                "Opening calculator."
            )
        }
    }

    // =========================================================
    // DIALER
    // =========================================================

    private fun openDialer() {

        try {

            val intent =
                Intent(Intent.ACTION_DIAL)

            startActivity(intent)

            speak(
                "Opening phone."
            )

        } catch (e: Exception) {

            speak(
                "I couldn't open the phone app."
            )
        }
    }

    // =========================================================
    // BATTERY
    // =========================================================

    private fun getBatteryPercentage(): Int {

        val batteryIntent =
            registerReceiver(
                null,
                android.content.IntentFilter(
                    Intent.ACTION_BATTERY_CHANGED
                )
            )

        val level =
            batteryIntent?.getIntExtra(
                BatteryManager.EXTRA_LEVEL,
                -1
            ) ?: -1

        val scale =
            batteryIntent?.getIntExtra(
                BatteryManager.EXTRA_SCALE,
                -1
            ) ?: -1

        if (level < 0 || scale <= 0) {
            return 0
        }

        return (level * 100) / scale
    }

    // =========================================================
    // TIMER
    // =========================================================

    private fun setTimer(command: String) {

        try {

            val pattern =
                Pattern.compile(
                    "(\\d+)\\s*(second|seconds|minute|minutes|hour|hours|sec|min|hr)"
                )

            val matcher =
                pattern.matcher(command)

            if (!matcher.find()) {

                speak(
                    "Please say a timer duration, for example, set a timer for 5 minutes."
                )

                return
            }

            val number =
                matcher.group(1)?.toIntOrNull() ?: 1

            val unit =
                matcher.group(2)?.lowercase(
                    Locale.getDefault()
                ) ?: "minutes"

            val seconds =
                when {

                    unit.startsWith("hour") ||
                            unit == "hr" -> {
                        number * 3600
                    }

                    unit.startsWith("second") ||
                            unit == "sec" -> {
                        number
                    }

                    else -> {
                        number * 60
                    }
                }

            val intent =
                Intent(
                    AlarmClock.ACTION_SET_TIMER
                )

            intent.putExtra(
                AlarmClock.EXTRA_LENGTH,
                seconds
            )

            intent.putExtra(
                AlarmClock.EXTRA_MESSAGE,
                "AURIX Timer"
            )

            intent.putExtra(
                AlarmClock.EXTRA_SKIP_UI,
                false
            )

            startActivity(intent)

            speak(
                "Timer set for $number $unit."
            )

        } catch (e: Exception) {

            speak(
                "I couldn't set the timer."
            )
        }
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    override fun onPause() {

        super.onPause()

        if (::speechRecognizer.isInitialized) {
            speechRecognizer.stopListening()
        }

        isListening = false
    }

    override fun onDestroy() {

        continuousMode = false

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
