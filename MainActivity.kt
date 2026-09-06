package com.example.myaiassistant

import android.Manifest
import android.app.AlarmClock
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null

    private lateinit var statusText: TextView
    private lateinit var commandText: TextView
    private lateinit var activateButton: Button

    private var listening = false
    private var visible = false

    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val MIC_PERMISSION = 100
        private const val CAMERA_PERMISSION = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createInterface()

        tts = TextToSpeech(this, this)

        requestMicrophonePermission()

        setupRecognizer()
    }

    private fun createInterface() {

        val root = LinearLayout(this)

        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(30, 60, 30, 40)
        root.setBackgroundColor(android.graphics.Color.rgb(5, 8, 16))

        val title = TextView(this)

        title.text = "AURIX"
        title.textSize = 38f
        title.gravity = Gravity.CENTER
        title.setTextColor(android.graphics.Color.WHITE)
        title.setTypeface(null, android.graphics.Typeface.BOLD)

        val subtitle = TextView(this)

        subtitle.text = "YOUR PERSONAL AI ASSISTANT"
        subtitle.textSize = 12f
        subtitle.gravity = Gravity.CENTER
        subtitle.setTextColor(android.graphics.Color.LTGRAY)
        subtitle.setPadding(0, 5, 0, 40)

        statusText = TextView(this)

        statusText.text = "AURIX is ready"
        statusText.textSize = 18f
        statusText.gravity = Gravity.CENTER
        statusText.setTextColor(android.graphics.Color.rgb(35, 112, 216))

        commandText = TextView(this)

        commandText.text = "Say something..."
        commandText.textSize = 20f
        commandText.gravity = Gravity.CENTER
        commandText.setTextColor(android.graphics.Color.WHITE)

        activateButton = Button(this)

        activateButton.text = "ACTIVATE AURIX"

        activateButton.setOnClickListener {
            toggleAURIX()
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

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

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts.language = Locale.US
            tts.setSpeechRate(0.95f)

            tts.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {

                    override fun onStart(id: String?) {
                    }

                    override fun onDone(id: String?) {

                        if (visible && listening) {

                            handler.postDelayed({

                                if (visible && listening) {
                                    startListening()
                                }

                            }, 500)
                        }
                    }

                    override fun onError(id: String?) {
                    }
                }
            )
        }
    }

    private fun setupRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            statusText.text = "Speech recognition unavailable"
            return
        }

        recognizer?.destroy()

        recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        recognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {

                    statusText.text = "Listening..."
                }

                override fun onBeginningOfSpeech() {

                    statusText.text = "I'm listening..."
                }

                override fun onRmsChanged(value: Float) {
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                }

                override fun onEndOfSpeech() {

                    statusText.text = "Processing..."
                }

                override fun onError(error: Int) {

                    if (!visible || !listening) {
                        return
                    }

                    handler.postDelayed({

                        if (visible && listening) {
                            startListening()
                        }

                    }, 700)
                }

                override fun onResults(results: Bundle?) {

                    val list =
                        results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val command =
                        list?.firstOrNull()?.trim().orEmpty()

                    if (command.isNotEmpty()) {

                        commandText.text = command

                        processCommand(command)

                    } else {

                        startListening()
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

    private fun toggleAURIX() {

        if (listening) {

            listening = false

            recognizer?.cancel()

            activateButton.text = "ACTIVATE AURIX"
            statusText.text = "AURIX sleeping"

            speak("Okay. I am sleeping.")

        } else {

            listening = true

            activateButton.text = "DEACTIVATE AURIX"
            statusText.text = "AURIX activated"

            speak("AURIX is ready.")

            handler.postDelayed({

                if (visible && listening) {
                    startListening()
                }

            }, 1200)
        }
    }

    private fun startListening() {

        if (!visible || !listening) {
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
                Intent(SpeechRecognizer.ACTION_RECOGNIZE_SPEECH)

            intent.putExtra(
                SpeechRecognizer.EXTRA_LANGUAGE_MODEL,
                SpeechRecognizer.LANGUAGE_MODEL_FREE_FORM
            )

            intent.putExtra(
                SpeechRecognizer.EXTRA_LANGUAGE,
                Locale.US
            )

            intent.putExtra(
                SpeechRecognizer.EXTRA_PARTIAL_RESULTS,
                false
            )

            recognizer?.startListening(intent)

        } catch (e: Exception) {

            handler.postDelayed({

                if (visible && listening) {
                    startListening()
                }

            }, 1000)
        }
    }

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

        if (
            command.contains("hello") ||
            command == "hi" ||
            command.contains("namaste")
        ) {

            speak("Hello. I am AURIX. How can I help you?")
            return
        }

        if (
            command.contains("who are you") ||
            command.contains("your name") ||
            command.contains("what are you") ||
            command.contains("tum kon ho") ||
            command.contains("tumhara naam")
        ) {

            speak("I am AURIX, your personal AI assistant.")
            return
        }

        if (
            command.contains("time") ||
            command.contains("what time") ||
            command.contains("kitne baje") ||
            command.contains("टाइम")
        ) {

            val time =
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.US
                ).format(Date())

            speak("The time is $time.")
            return
        }

        if (
            command.contains("date") ||
            command.contains("today") ||
            command.contains("aaj")
        ) {

            val date =
                SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale.US
                ).format(Date())

            speak("Today is $date.")
            return
        }

        if (command.contains("youtube")) {

            val search =
                command
                    .replace("youtube", "")
                    .replace("open", "")
                    .replace("search", "")
                    .trim()

            if (search.isEmpty()) {

                openUrl("https://www.youtube.com")
                speak("Opening YouTube.")

            } else {

                val url =
                    "https://www.youtube.com/results?search_query=" +
                            Uri.encode(search)

                openUrl(url)
                speak("Searching YouTube for $search.")
            }

            return
        }

        if (
            command.startsWith("search ") ||
            command.contains("google search")
        ) {

            val search =
                command
                    .replace("google search", "")
                    .replace("search", "")
                    .trim()

            if (search.isNotEmpty()) {

                val url =
                    "https://www.google.com/search?q=" +
                            Uri.encode(search)

                openUrl(url)
                speak("Searching Google for $search.")
            }

            return
        }

        if (
            command == "open chrome" ||
            command.contains("chrome kholo")
        ) {

            try {

                val chrome =
                    packageManager.getLaunchIntentForPackage(
                        "com.android.chrome"
                    )

                if (chrome != null) {

                    startActivity(chrome)
                    speak("Opening Chrome.")

                } else {

                    openUrl("https://www.google.com")
                    speak("Chrome is not installed.")
                }

            } catch (e: Exception) {

                speak("I could not open Chrome.")
            }

            return
        }

        if (
            command == "camera" ||
            command.contains("open camera") ||
            command.contains("camera kholo")
        ) {

            try {

                val intent =
                    Intent("android.media.action.IMAGE_CAPTURE")

                startActivity(intent)

                speak("Opening camera.")

            } catch (e: Exception) {

                speak("I could not open the camera.")
            }

            return
        }

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

                speak("Opening calculator.")

            } catch (e: Exception) {

                speak("Calculator is not available.")
            }

            return
        }

        if (
            command == "settings" ||
            command.contains("open settings") ||
            command.contains("setting kholo")
        ) {

            try {

                startActivity(
                    Intent(Settings.ACTION_SETTINGS)
                )

                speak("Opening settings.")

            } catch (e: Exception) {

                speak("I could not open settings.")
            }

            return
        }

        if (
            command.contains("volume up") ||
            command.contains("volume badhao")
        ) {

            audioManager().adjustVolume(
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_PLAY_SOUND
            )

            speak("Volume increased.")
            return
        }

        if (
            command.contains("volume down") ||
            command.contains("volume kam")
        ) {

            audioManager().adjustVolume(
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_PLAY_SOUND
            )

            speak("Volume decreased.")
            return
        }

        if (
            command.contains("mute") ||
            command.contains("sound off")
        ) {

            audioManager().adjustVolume(
                AudioManager.ADJUST_MUTE,
                AudioManager.FLAG_PLAY_SOUND
            )

            speak("Muted.")
            return
        }

        if (command.contains("battery")) {

            val manager =
                getSystemService(BATTERY_SERVICE)
                        as android.os.BatteryManager

            val level =
                manager.getIntProperty(
                    android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY
                )

            speak("Battery level is $level percent.")
            return
        }

        if (
            command.contains("open maps") ||
            command.contains("google maps") ||
            command.contains("navigate to")
        ) {

            val destination =
                command
                    .replace("open maps", "")
                    .replace("google maps", "")
                    .replace("navigate to", "")
                    .trim()

            if (destination.isEmpty()) {

                openUrl("https://maps.google.com")
                speak("Opening Google Maps.")

            } else {

                val url =
                    "https://www.google.com/maps/search/?api=1&query=" +
                            Uri.encode(destination)

                openUrl(url)
                speak("Opening maps for $destination.")
            }

            return
        }

        if (
            command.contains("timer") ||
            command.contains("set timer")
        ) {

            val number =
                Regex("\\d+")
                    .find(command)
                    ?.value
                    ?.toLongOrNull()

            if (number != null && number > 0) {

                setTimer(number)

            } else {

                speak("Please tell me the timer duration.")
            }

            return
        }

        if (
            command.contains("phone") ||
            command.contains("dialer")
        ) {

            try {

                startActivity(
                    Intent(Intent.ACTION_DIAL)
                )

                speak("Opening phone.")

            } catch (e: Exception) {

                speak("I could not open the phone.")
            }

            return
        }

        if (
            command.contains("flashlight on") ||
            command.contains("torch on")
        ) {

            setFlashlight(true)
            return
        }

        if (
            command.contains("flashlight off") ||
            command.contains("torch off")
        ) {

            setFlashlight(false)
            return
        }

        if (
            command.contains("stop listening") ||
            command.contains("go to sleep") ||
            command.contains("sleep") ||
            command.contains("so jao")
        ) {

            listening = false

            recognizer?.cancel()

            activateButton.text = "ACTIVATE AURIX"
            statusText.text = "AURIX sleeping"

            speak("Okay. I am going to sleep.")
            return
        }

        val url =
            "https://www.google.com/search?q=" +
                    Uri.encode(input)

        openUrl(url)

        speak("I searched Google for that.")
    }

    private fun audioManager(): AudioManager {

        return getSystemService(AUDIO_SERVICE)
                as AudioManager
    }

    private fun openUrl(url: String) {

        try {

            val intent =
                Intent(Intent.ACTION_VIEW)

            intent.data = Uri.parse(url)

            startActivity(intent)

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Unable to open",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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

            intent.putExtra(
                AlarmClock.EXTRA_SKIP_UI,
                false
            )

            startActivity(intent)

            speak("Timer set for $minutes minutes.")

        } catch (e: Exception) {

            speak("I could not set the timer.")
        }
    }

    private fun setFlashlight(enable: Boolean) {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION
            )

            speak("Camera permission is required for flashlight.")
            return
        }

        try {

            val manager =
                getSystemService(CAMERA_SERVICE)
                        as CameraManager

            val cameraId =
                manager.cameraIdList.firstOrNull()

            if (cameraId == null) {

                speak("Flashlight is not available.")
                return
            }

            manager.setTorchMode(
                cameraId,
                enable
            )

            if (enable) {

                speak("Flashlight turned on.")

            } else {

                speak("Flashlight turned off.")
            }

        } catch (e: Exception) {

            speak("I could not control the flashlight.")
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

    override fun onResume() {

        super.onResume()

        visible = true

        if (listening) {

            handler.postDelayed({

                if (visible && listening) {
                    startListening()
                }

            }, 600)
        }
    }

    override fun onPause() {

        visible = false

        recognizer?.cancel()

        super.onPause()
    }

    override fun onDestroy() {

        listening = false

        recognizer?.destroy()
        recognizer = null

        if (::tts.isInitialized) {

            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        results: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            results
        )

        if (requestCode == MIC_PERMISSION) {

            if (
                results.isNotEmpty() &&
                results[0] == PackageManager.PERMISSION_GRANTED
            ) {

                statusText.text = "Microphone ready"

            } else {

                statusText.text =
                    "Microphone permission required"
            }
        }

        if (requestCode == CAMERA_PERMISSION) {

            if (
                results.isNotEmpty() &&
                results[0] == PackageManager.PERMISSION_GRANTED
            ) {

                speak(
                    "Permission granted. Say flashlight on again."
                )

            } else {

                speak(
                    "Camera permission denied."
                )
            }
        }
    }
}
