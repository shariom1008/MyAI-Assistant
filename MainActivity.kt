package com.example.myaiassistant

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.speech.*
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var speechRecognizer: SpeechRecognizer? = null

    private lateinit var statusText: TextView
    private lateinit var commandText: TextView
    private lateinit var activateButton: Button

    private var isListening = false
    private var activityVisible = false
    private var torchOn = false

    private val audioManager by lazy {
        getSystemService(AUDIO_SERVICE) as AudioManager
    }

    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val AUDIO_PERMISSION = 1001
        private const val CAMERA_PERMISSION = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUI()

        tts = TextToSpeech(this, this)

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                AUDIO_PERMISSION
            )
        }

        setupSpeechRecognizer()
    }

    private fun createUI() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.setPadding(35, 60, 35, 40)
        root.setBackgroundColor(Color.rgb(5, 8, 16))

        val title = TextView(this)
        title.text = "AURIX"
        title.textSize = 38f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        title.setTypeface(null, android.graphics.Typeface.BOLD)

        val subtitle = TextView(this)
        subtitle.text = "YOUR PERSONAL AI ASSISTANT"
        subtitle.textSize = 12f
        subtitle.setTextColor(Color.rgb(150, 170, 200))
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(0, 5, 0, 45)

        statusText = TextView(this)
        statusText.text = "AURIX is ready"
        statusText.textSize = 18f
        statusText.setTextColor(Color.rgb(35, 112, 216))
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 20, 0, 25)

        commandText = TextView(this)
        commandText.text = "Say something..."
        commandText.textSize = 20f
        commandText.setTextColor(Color.WHITE)
        commandText.gravity = Gravity.CENTER
        commandText.setPadding(15, 30, 15, 30)

        activateButton = Button(this)
        activateButton.text = "ACTIVATE AURIX"
        activateButton.textSize = 16f
        activateButton.setTextColor(Color.WHITE)
        activateButton.setBackgroundColor(Color.rgb(25, 70, 130))

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

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts.language = Locale.US
            tts.setSpeechRate(0.95f)

            tts.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {

                    override fun onStart(utteranceId: String?) {}

                    override fun onDone(utteranceId: String?) {
                        if (activityVisible && isListening) {
                            handler.postDelayed({
                                if (activityVisible && isListening) {
                                    restartListening()
                                }
                            }, 500)
                        }
                    }

                    override fun onError(utteranceId: String?) {}
                }
            )

        }
    }

    private fun setupSpeechRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.text = "Speech recognition unavailable"
            return
        }

        speechRecognizer?.destroy()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    statusText.text = "Listening..."
                }

                override fun onBeginningOfSpeech() {
                    statusText.text = "I'm listening..."
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    statusText.text = "Processing..."
                }

                override fun onError(error: Int) {

                    if (!activityVisible || !isListening) return

                    statusText.text = "Ready"

                    handler.postDelayed({
                        if (activityVisible && isListening) {
                            restartListening()
                        }
                    }, 700)
                }

                override fun onResults(results: Bundle?) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val spokenText =
                        matches?.firstOrNull()?.trim().orEmpty()

                    if (spokenText.isNotEmpty()) {

                        commandText.text = spokenText

                        processCommand(spokenText
