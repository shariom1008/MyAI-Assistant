package com.example.myaiassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
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
    private lateinit var orbText: TextView

    private var listening = false
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val MIC_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(4, 7, 18)
        window.navigationBarColor = Color.rgb(4, 7, 18)

        tts = TextToSpeech(this, this)

        createPremiumInterface()

        if (ContextCompat.checkSelfPermission(
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

        setupRecognizer()
    }

    // =========================================================
    // PREMIUM UI
    // =========================================================

    private fun createPremiumInterface() {

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(4, 7, 18))

        // Main vertical content
        val content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.gravity = Gravity.CENTER_HORIZONTAL
        content.setPadding(28, 45, 28, 120)

        val contentParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        root.addView(content, contentParams)

        // AURIX title
        val title = TextView(this)
        title.text = "A U R I X"
        title.textSize = 30f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        title.setTypeface(null, android.graphics.Typeface.BOLD)

        content.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                70
            )
        )

        // Subtitle
        val subtitle = TextView(this)
        subtitle.text = "YOUR PERSONAL AI ASSISTANT"
        subtitle.textSize = 11f
        subtitle.setTextColor(Color.rgb(120, 170, 255))
        subtitle.gravity = Gravity.CENTER

        content.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                35
            )
        )

        // Spacer
        val spacer1 = View(this)

        content.addView(
            spacer1,
            LinearLayout.LayoutParams(
                1,
                30
            )
        )

        // AI Orb
        orbText = TextView(this)
        orbText.text = "A"
        orbText.textSize = 48f
        orbText.setTextColor(Color.WHITE)
        orbText.gravity = Gravity.CENTER
        orbText.setTypeface(null, android.graphics.Typeface.BOLD)

        val orbBackground = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.rgb(30, 90, 180),
                Color.rgb(70, 30, 150)
            )
        )

        orbBackground.shape = GradientDrawable.OVAL
        orbBackground.setStroke(3, Color.rgb(80, 150, 255))

        orbText.background = orbBackground

        val orbParams = LinearLayout.LayoutParams(150, 150)
        orbParams.gravity = Gravity.CENTER

        content.addView(orbText, orbParams)

        // Spacer
        val spacer2 = View(this)

        content.addView(
            spacer2,
            LinearLayout.LayoutParams(
                1,
                28
            )
        )

        // Status
        statusText = TextView(this)
        statusText.text = "READY"
        statusText.textSize = 16f
        statusText.setTextColor(Color.rgb(120, 170, 255))
        statusText.gravity = Gravity.CENTER
        statusText.setTypeface(null, android.graphics.Typeface.BOLD)

        content.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                45
            )
        )

        // Command card
        val commandCard = LinearLayout(this)
        commandCard.orientation = LinearLayout.VERTICAL
        commandCard.setPadding(20, 15, 20, 15)

        val cardBackground = GradientDrawable()
        cardBackground.setColor(Color.rgb(13, 18, 35))
        cardBackground.cornerRadius = 28f
        cardBackground.setStroke(1, Color.rgb(35, 60, 100))

        commandCard.background = cardBackground

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            115
        )

        cardParams.topMargin = 15

        content.addView(commandCard, cardParams)

        val commandLabel = TextView(this)
        commandLabel.text = "LAST COMMAND"
        commandLabel.textSize = 10f
        commandLabel.setTextColor(Color.rgb(100, 140, 190))

        commandCard.addView(
            commandLabel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                25
            )
        )

        commandText = TextView(this)
        commandText.text = "Say something to AURIX..."
        commandText.textSize = 17f
        commandText.setTextColor(Color.WHITE)
        commandText.gravity = Gravity.CENTER_VERTICAL

        commandCard.addView(
            commandText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                55
            )
        )

        setContentView(root)

        // FIXED BUTTON
        activateButton = Button(this)
        activateButton.text = "ACTIVATE AURIX"
        activateButton.textSize = 15f
        activateButton.setTextColor(Color.WHITE)
        activateButton.setTypeface(null, android.graphics.Typeface.BOLD)
        activateButton.isAllCaps = false

        val buttonBackground = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                Color.rgb(25, 95, 210),
                Color.rgb(95, 45, 190)
            )
        )

        buttonBackground.cornerRadius = 50f
        buttonBackground.setStroke(2, Color.rgb(80, 150, 255))

        activateButton.background = buttonBackground

        activateButton.setOnClickListener {
            toggleAURIX()
        }

        val buttonParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            65
        )

        buttonParams.gravity = Gravity.BOTTOM
        buttonParams.setMargins(28, 0, 28, 25)

        root.addView(activateButton, buttonParams)
    }

    // =========================================================
    // TTS
    // =========================================================

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.setSpeechRate(0.95f)
        }
    }

    private fun speak(text: String) {
        if (::tts.isInitialized) {
            tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "AURIX"
            )
        }
    }

    // =========================================================
    // SPEECH RECOGNIZER
    // =========================================================

    private fun setupRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(
                this,
                "Speech recognition unavailable",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        recognizer?.destroy()

        recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        recognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    statusText.text = "LISTENING..."
                    orbText.text = "●"
                }

                override fun onBeginningOfSpeech() {
                    statusText.text = "HEARING YOU..."
                    orbText.text = "●"
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Listening level can be used for animation later
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    statusText.text = "PROCESSING..."
                }

                override fun onError(error: Int) {

                    if (!listening) return

                    handler.postDelayed({

                        if (listening) {
                            startListening()
                        }

                    }, 700)
                }

                override fun onResults(results: Bundle?) {

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
                    }

                    if (listening) {

                        handler.postDelayed({

                            if (listening) {
                                startListening()
                            }

                        }, 800)
                    }
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

        if (!listening) return

        val intent = Intent(
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

        intent.putExtra(
            RecognizerIntent.EXTRA_MAX_RESULTS,
            1
        )

        try {
            recognizer?.startListening(intent)

        } catch (e: Exception) {

            handler.postDelayed({

                if (listening) {
                    startListening()
                }

            }, 1000)
        }
    }

    // =========================================================
    // ACTIVATE / DEACTIVATE
    // =========================================================

    private fun toggleAURIX() {

        if (!listening) {

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

                return
            }

            listening = true

            activateButton.text = "DEACTIVATE AURIX"

            statusText.text = "STARTING..."
            orbText.text = "●"

            speak("AURIX activated")

            handler.postDelayed({

                if (listening) {
                    startListening()
                }

            }, 900)

        } else {

            listening = false

            recognizer?.cancel()

            activateButton.text = "ACTIVATE AURIX"

            statusText.text = "READY"
            orbText.text = "A"

            speak("AURIX deactivated")
        }
    }

    // =========================================================
    // COMMAND PROCESSOR
    // =========================================================

    private fun processCommand(input: String) {

        var command = input.lowercase(Locale.US).trim()

        command = command
            .replace("aurix", "")
            .replace("aurex", "")
            .replace("orix", "")
            .trim()

        if (command.isEmpty()) return

        // -----------------------------------------------------
        // TIMER
        // -----------------------------------------------------

        if (
            command.contains("timer") ||
            command.contains("set a timer") ||
            command.contains("set timer")
        ) {

            val number =
                Regex("""\d+""")
                    .find(command)
                    ?.value
                    ?.toIntOrNull()

            if (number != null) {

                setTimer(number)

                speak("Timer set for $number minutes")

            } else {

                speak("Please tell me the timer duration")
            }

            return
        }

        // -----------------------------------------------------
        // TIME
        // -----------------------------------------------------

        if (
            command == "time" ||
            command.contains("what time") ||
            command.contains("current time")
        ) {

            val time =
                SimpleDateFormat(
                    "h:mm a",
                    Locale.US
                ).format(Date())

            speak("The time is $time")

            return
        }

        // -----------------------------------------------------
        // HELLO
        // -----------------------------------------------------

        if (
            command.contains("hello") ||
            command.contains("hi") ||
            command.contains("hey")
        ) {

            speak("Hello! I am AURIX. How can I help you?")

            return
        }

        // -----------------------------------------------------
        // IDENTITY
        // -----------------------------------------------------

        if (
            command.contains("who are you") ||
            command.contains("your name") ||
            command.contains("what are you")
        ) {

            speak(
                "I am AURIX, your personal AI assistant."
            )

            return
        }

        // -----------------------------------------------------
        // GALLERY
        // -----------------------------------------------------

        if (
            command.contains("gallery") ||
            command.contains("photos") ||
            command.contains("pictures") ||
            command.contains("photo")
        ) {

            openGallery()

            return
        }

        // -----------------------------------------------------
        // MUSIC
        // -----------------------------------------------------

        if (
            command.contains("music") ||
            command.contains("song") ||
            command.contains("songs") ||
            command.contains("gaane")
        ) {

            openMusic()

            return
        }

        // -----------------------------------------------------
        // NOTES
        // -----------------------------------------------------

        if (
            command.contains("notes") ||
            command.contains("note") ||
            command.contains("notepad")
        ) {

            openNotes()

            return
        }

        // -----------------------------------------------------
        // YOUTUBE
        // -----------------------------------------------------

        if (
            command.contains("youtube") ||
            command.contains("play youtube")
        ) {

            openUrl("https://www.youtube.com")

            return
        }

        // -----------------------------------------------------
        // GOOGLE
        // -----------------------------------------------------

        if (
            command.contains("search") ||
            command.contains("google")
        ) {

            val query =
                command
                    .replace("search", "")
                    .replace("google", "")
                    .trim()

            if (query.isNotEmpty()) {

                openUrl(
                    "https://www.google.com/search?q=" +
                            Uri.encode(query)
                )

            } else {

                openUrl("https://www.google.com")
            }

            return
        }

        // -----------------------------------------------------
        // CHROME
        // -----------------------------------------------------

        if (command.contains("chrome")) {

            try {

                val intent =
                    packageManager.getLaunchIntentForPackage(
                        "com.android.chrome"
                    )

                if (intent != null) {

                    startActivity(intent)

                } else {

                    openUrl("https://www.google.com")
                }

            } catch (e: Exception) {

                openUrl("https://www.google.com")
            }

            return
        }

        // -----------------------------------------------------
        // CAMERA
        // -----------------------------------------------------

        if (command.contains("camera")) {

            try {

                val intent =
                    Intent("android.media.action.IMAGE_CAPTURE")

                startActivity(intent)

            } catch (e: Exception) {

                speak("Camera is not available")
            }

            return
        }

        // -----------------------------------------------------
        // CALCULATOR
        // -----------------------------------------------------

        if (
            command.contains("calculator") ||
            command.contains("calculate")
        ) {

            try {

                val intent =
                    Intent(Intent.ACTION_MAIN)

                intent.addCategory(
                    Intent.CATEGORY_APP_CALCULATOR
                )

                startActivity(intent)

            } catch (e: Exception) {

                speak("Calculator is not available")
            }

            return
        }

        // -----------------------------------------------------
        // SETTINGS
        // -----------------------------------------------------

        if (
            command.contains("settings") ||
            command.contains("setting")
        ) {

            try {

                startActivity(
                    Intent(Settings.ACTION_SETTINGS)
                )

            } catch (e: Exception) {

                speak("Settings could not be opened")
            }

            return
        }

        // -----------------------------------------------------
        // MAPS
        // -----------------------------------------------------

        if (
            command.contains("maps") ||
            command.contains("map") ||
            command.contains("location")
        ) {

            try {

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://maps.google.com"
                        )
                    )
                )

            } catch (e: Exception) {

                speak("Maps could not be opened")
            }

            return
        }

        // -----------------------------------------------------
        // PHONE
        // -----------------------------------------------------

        if (
            command.contains("phone") ||
            command.contains("dialer")
        ) {

            try {

                startActivity(
                    Intent(
                        Intent.ACTION_DIAL
                    )
                )

            } catch (e: Exception) {

                speak("Phone app is not available")
            }

            return
        }

        // -----------------------------------------------------
        // VOLUME
        // -----------------------------------------------------

        if (
            command.contains("volume up") ||
            command.contains("increase volume")
        ) {

            val audio =
                getSystemService(
                    AUDIO_SERVICE
                ) as android.media.AudioManager

            audio.adjustVolume(
                android.media.AudioManager.ADJUST_RAISE,
                android.media.AudioManager.FLAG_SHOW_UI
            )

            speak("Volume increased")

            return
        }

        if (
            command.contains("volume down") ||
            command.contains("decrease volume")
        ) {

            val audio =
                getSystemService(
                    AUDIO_SERVICE
                ) as android.media.AudioManager

            audio.adjustVolume(
                android.media.AudioManager.ADJUST_LOWER,
                android.media.AudioManager.FLAG_SHOW_UI
            )

            speak("Volume decreased")

            return
        }

        // -----------------------------------------------------
        // BATTERY
        // -----------------------------------------------------

        if (command.contains("battery")) {

            val batteryManager =
                getSystemService(
                    BATTERY_SERVICE
                ) as android.os.BatteryManager

            val level =
                batteryManager.getIntProperty(
                    android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY
                )

            speak(
                "Your battery is at $level percent"
            )

            return
        }

        // -----------------------------------------------------
        // STOP
        // -----------------------------------------------------

        if (
            command.contains("stop listening") ||
            command.contains("deactivate") ||
            command.contains("stop aurix")
        ) {

            listening = false

            recognizer?.cancel()

            activateButton.text = "ACTIVATE AURIX"

            statusText.text = "READY"
            orbText.text = "A"

            speak("AURIX deactivated")

            return
        }

        // -----------------------------------------------------
        // UNKNOWN COMMAND
        // -----------------------------------------------------

        speak(
            "I will search that for you."
        )

        handler.postDelayed({

            openUrl(
                "https://www.google.com/search?q=" +
                        Uri.encode(command)
            )

        }, 800)
    }

    // =========================================================
    // TIMER
    // =========================================================

    private fun setTimer(minutes: Int) {

        try {

            val intent =
                Intent(AlarmClock.ACTION_SET_TIMER)

            intent.putExtra(
                AlarmClock.EXTRA_LENGTH,
                minutes * 60
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

        } catch (e: Exception) {

            speak("I could not open the timer")
        }
    }

    // =========================================================
    // GALLERY
    // =========================================================

    private fun openGallery() {

        try {

            val intent =
                Intent(Intent.ACTION_VIEW)

            intent.type = "image/*"
            intent.addCategory(
                Intent.CATEGORY_DEFAULT
            )

            startActivity(intent)

        } catch (e: Exception) {

            try {

                val fallback =
                    Intent(Intent.ACTION_PICK)

                fallback.type = "image/*"

                startActivity(fallback)

            } catch (e2: Exception) {

                speak("Gallery is not available")
            }
        }
    }

    // =========================================================
    // MUSIC
    // =========================================================

    private fun openMusic() {

        try {

            val intent =
                Intent(Intent.ACTION_MAIN)

            intent.addCategory(
                Intent.CATEGORY_APP_MUSIC
            )

            startActivity(intent)

        } catch (e: Exception) {

            try {

                val fallback =
                    Intent(Intent.ACTION_VIEW)

                fallback.type = "audio/*"

                startActivity(fallback)

            } catch (e2: Exception) {

                speak("Music player is not available")
            }
        }
    }

    // =========================================================
    // NOTES
    // =========================================================

    private fun openNotes() {

        try {

            val intent =
                Intent(Intent.ACTION_CREATE_DOCUMENT)

            intent.addCategory(
                Intent.CATEGORY_OPENABLE
            )

            intent.type = "text/plain"

            intent.putExtra(
                Intent.EXTRA_TITLE,
                "AURIX Note.txt"
            )

            startActivity(intent)

        } catch (e: Exception) {

            speak("Notes could not be opened")
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

            speak("I could not open that")
        }
    }

    // =========================================================
    // LIFECYCLE
    // =========================================================

    override fun onResume() {
        super.onResume()

        if (listening) {

            handler.postDelayed({

                if (listening) {
                    startListening()
                }

            }, 700)
        }
    }

    override fun onPause() {
        super.onPause()

        recognizer?.cancel()
    }

    override fun onDestroy() {

        listening = false

        handler.removeCallbacksAndMessages(null)

        recognizer?.destroy()
        recognizer = null

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }

    // =========================================================
    // PERMISSION
    // =========================================================

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

        if (requestCode == MIC_PERMISSION) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                Toast.makeText(
                    this,
                    "Microphone permission granted",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Microphone permission is required",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
