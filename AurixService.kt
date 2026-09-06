package com.example.myaiassistant

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class AurixService : Service(),
    TextToSpeech.OnInitListener {

    companion object {

        const val ACTION_START =
            "com.example.myaiassistant.START"

        const val ACTION_STOP =
            "com.example.myaiassistant.STOP"

        const val ACTION_EVENT =
            "com.example.myaiassistant.EVENT"

        const val EXTRA_TYPE = "type"
        const val EXTRA_TEXT = "text"

        const val TYPE_STATUS = "status"
        const val TYPE_COMMAND = "command"

        private const val CHANNEL_ID =
            "aurix_voice_channel"

        private const val NOTIFICATION_ID = 9001

        var isRunning = false
            private set
    }

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null

    private lateinit var handler: Handler

    private var processing = false
    private var restarting = false

    override fun onCreate() {

        super.onCreate()

        handler = Handler(
            Looper.getMainLooper()
        )

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification("AURIX is starting...")
        )

        isRunning = true

        tts = TextToSpeech(
            applicationContext,
            this
        )

        setupRecognizer()

        sendStatus("READY")
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (
            intent?.action ==
            ACTION_STOP
        ) {

            stopAurixService()

            return START_NOT_STICKY
        }

        if (
            intent?.action ==
            ACTION_START ||
            intent == null
        ) {

            isRunning = true

            startListening()
        }

        return START_STICKY
    }

    // =====================================================
    // NOTIFICATION
    // =====================================================

    private fun createNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "AURIX Voice Assistant",
                    NotificationManager.IMPORTANCE_LOW
                )

            channel.description =
                "AURIX background voice assistant"

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }

    private fun createNotification(
        text: String
    ): Notification {

        val openIntent = Intent(
            this,
            MainActivity::class.java
        )

        val openPendingIntent =
            PendingIntent.getActivity(
                this,
                10,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val stopIntent = Intent(
            this,
            AurixService::class.java
        )

        stopIntent.action = ACTION_STOP

        val stopPendingIntent =
            PendingIntent.getService(
                this,
                11,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val builder =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                android.app.Notification.Builder(
                    this,
                    CHANNEL_ID
                )

            } else {

                android.app.Notification.Builder(
                    this
                )
            }

        return builder
            .setContentTitle("AURIX")
            .setContentText(text)
            .setSmallIcon(
                android.R.drawable.ic_btn_speak_now
            )
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                Notification.Action.Builder(
                    null,
                    "STOP",
                    stopPendingIntent
                ).build()
            )
            .build()
    }

    private fun updateNotification(
        text: String
    ) {

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            NOTIFICATION_ID,
            createNotification(text)
        )
    }

    // =====================================================
    // SPEECH RECOGNIZER
    // =====================================================

    private fun setupRecognizer() {

        if (
            !SpeechRecognizer.isRecognitionAvailable(
                this
            )
        ) {

            sendStatus(
                "Speech recognition unavailable"
            )

            return
        }

        recognizer?.destroy()

        recognizer =
            SpeechRecognizer.createSpeechRecognizer(
                this
            )

        recognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {

                    processing = false

                    sendStatus("LISTENING")

                    updateNotification(
                        "Listening for commands..."
                    )
                }

                override fun onBeginningOfSpeech() {

                    sendStatus(
                        "LISTENING..."
                    )
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

                    sendStatus(
                        "PROCESSING..."
                    )
                }

                override fun onError(
                    error: Int
                ) {

                    if (!isRunning) return

                    processing = false

                    scheduleRestart(700)
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    if (!isRunning) return

                    val list =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val command =
                        list?.firstOrNull()
                            ?.trim()
                            ?: ""

                    if (command.isNotEmpty()) {

                        sendCommand(command)

                        processing = true

                        processCommand(command)
                    }

                    scheduleRestart(
                        if (command.isNotEmpty())
                            1600
                        else
                            500
                    )
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

        if (!isRunning) return

        if (processing) return

        if (
            recognizer == null
        ) {

            setupRecognizer()
        }

        try {

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
                Locale.getDefault()
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                1
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            )

            recognizer?.startListening(
                intent
            )

        } catch (e: Exception) {

            scheduleRestart(1000)
        }
    }

    private fun scheduleRestart(
        delay: Long
    ) {

        if (!isRunning) return

        handler.removeCallbacksAndMessages(
            "AURIX_RESTART"
        )

        handler.postDelayed(
            {
                if (
                    isRunning &&
                    !processing
                ) {
                    startListening()
                } else if (
                    isRunning
                ) {
                    processing = false
                    startListening()
                }
            },
            delay
        )
    }

    // =====================================================
    // COMMAND PROCESSOR
    // =====================================================

    private fun processCommand(
        rawCommand: String
    ) {

        var command =
            rawCommand
                .lowercase(Locale.getDefault())
                .trim()

        command = command
            .replace("aurix", "")
            .replace("aurex", "")
            .replace("orix", "")
            .trim()

        if (command.isEmpty()) {

            speak("Yes, I am listening")
            return
        }

        // TIMER FIRST
        if (
            command.contains("timer") ||
            command.contains("countdown")
        ) {

            setTimer(command)
            return
        }

        // ALARM
        if (
            command.contains("alarm") ||
            command.contains("wake me")
        ) {

            setAlarm(command)
            return
        }

        // TIME
        if (
            command == "time" ||
            command.contains("what time") ||
            command.contains("current time")
        ) {

            val time =
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

            speak(
                "The current time is $time"
            )

            return
        }

        // HELLO
        if (
            command == "hello" ||
            command == "hi" ||
            command.contains("hello aurix") ||
            command.contains("hey aurix")
        ) {

            speak(
                "Hello. I am AURIX. How can I help you?"
            )

            return
        }

        // IDENTITY
        if (
            command.contains("who are you") ||
            command.contains("your name")
        ) {

            speak(
                "I am AURIX, your intelligent voice assistant."
            )

            return
        }

        // CAMERA
        if (
            command.contains("camera") ||
            command.contains("take photo") ||
            command.contains("open camera")
        ) {

            speak("Opening camera")
            openCamera()
            return
        }

        // GALLERY
        if (
            command.contains("gallery") ||
            command.contains("photos") ||
            command.contains("open gallery")
        ) {

            speak("Opening gallery")
            openGallery()
            return
        }

        // MUSIC
        if (
            command.contains("music") ||
            command.contains("song") ||
            command.contains("songs") ||
            command.contains("gaana") ||
            command.contains("gaane")
        ) {

            speak("Opening music")
            openMusic()
            return
        }

        // NOTES
        if (
            command.contains("notes") ||
            command.contains("note")
        ) {

            speak("Opening notes")
            openNotes()
            return
        }

        // CALCULATOR
        if (
            command.contains("calculator") ||
            command.contains("calculate")
        ) {

            speak("Opening calculator")
            openCalculator()
            return
        }

        // CHROME
        if (
            command.contains("chrome") ||
            command.contains("browser")
        ) {

            speak("Opening browser")
            openChrome()
            return
        }

        // YOUTUBE
        if (
            command.contains("youtube")
        ) {

            speak("Opening YouTube")
            openUrl(
                "https://www.youtube.com"
            )

            return
        }

        // GOOGLE SEARCH
        if (
            command.startsWith("search") ||
            command.startsWith("google")
        ) {

            val query =
                command
                    .replaceFirst(
                        "search",
                        ""
                    )
                    .replaceFirst(
                        "google",
                        ""
                    )
                    .trim()

            if (query.isNotEmpty()) {

                speak(
                    "Searching for $query"
                )

                openUrl(
                    "https://www.google.com/search?q=" +
                            Uri.encode(query)
                )

            } else {

                openUrl(
                    "https://www.google.com"
                )
            }

            return
        }

        // MAPS
        if (
            command.contains("maps") ||
            command.contains("map") ||
            command.contains("directions") ||
            command.contains("navigate")
        ) {

            speak("Opening maps")
            openMaps(command)
            return
        }

        // PHONE
        if (
            command.contains("phone") ||
            command.contains("dialer") ||
            command.contains("call")
        ) {

            speak("Opening phone")
            openPhone()
            return
        }

        // SETTINGS
        if (
            command.contains("settings")
        ) {

            speak("Opening settings")
            openSettings()
            return
        }

        // VOLUME UP
        if (
            command.contains("volume up") ||
            command.contains("increase volume") ||
            command.contains("volume increase")
        ) {

            changeVolume(true)
            speak("Volume increased")
            return
        }

        // VOLUME DOWN
        if (
            command.contains("volume down") ||
            command.contains("decrease volume") ||
            command.contains("volume decrease")
        ) {

            changeVolume(false)
            speak("Volume decreased")
            return
        }

        // BATTERY
        if (
            command.contains("battery")
        ) {

            batteryStatus()
            return
        }

        // STOP
        if (
            command.contains("stop listening") ||
            command.contains("deactivate") ||
            command.contains("stop aurix")
        ) {

            speak("AURIX is going offline")
            handler.postDelayed(
                {
                    stopAurixService()
                },
                1000
            )

            return
        }

        // UNKNOWN COMMAND
        speak(
            "I will search that for you"
        )

        handler.postDelayed(
            {

                openUrl(
                    "https://www.google.com/search?q=" +
                            Uri.encode(command)
                )

            },
            700
        )
    }

    // =====================================================
    // CAMERA
    // =====================================================

    private fun openCamera() {

        try {

            val intent =
                Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE
                )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            if (
                intent.resolveActivity(
                    packageManager
                ) != null
            ) {

                startActivity(intent)
                return
            }

        } catch (_: Exception) {
        }

        try {

            val intent =
                Intent(
                    "android.intent.action.MAIN"
                )

            intent.setClassName(
                "com.android.camera",
                "com.android.camera.CameraLauncher"
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "Camera app is not available"
            )
        }
    }

    // =====================================================
    // GALLERY
    // =====================================================

    private fun openGallery() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW
                )

            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            try {

                val intent =
                    Intent(
                        Intent.ACTION_PICK
                    )

                intent.type = "image/*"

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(intent)

            } catch (_: Exception) {

                speak(
                    "Gallery app is not available"
                )
            }
        }
    }

    // =====================================================
    // MUSIC
    // =====================================================

    private fun openMusic() {

        try {

            val intent =
                Intent(
                    Intent.CATEGORY_APP_MUSIC
                )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            try {

                val intent =
                    Intent(
                        Intent.ACTION_VIEW
                    )

                intent.setDataAndType(
                    Uri.parse(
                        "content://media/internal/audio/media"
                    ),
                    "audio/*"
                )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(intent)

            } catch (_: Exception) {

                speak(
                    "Music player is not available"
                )
            }
        }
    }

    // =====================================================
    // NOTES
    // =====================================================

    private fun openNotes() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_CREATE_DOCUMENT
                )

            intent.type =
                "text/plain"

            intent.putExtra(
                Intent.EXTRA_TITLE,
                "AURIX Note.txt"
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            try {

                openUrl(
                    "https://keep.google.com"
                )

            } catch (_: Exception) {

                speak(
                    "Notes app is not available"
                )
            }
        }
    }

    // =====================================================
    // CALCULATOR
    // =====================================================

    private fun openCalculator() {

        try {

            val intent =
                Intent(
                    Intent.CATEGORY_APP_CALCULATOR
                )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            openUrl(
                "https://www.google.com/search?q=calculator"
            )
        }
    }

    // =====================================================
    // CHROME
    // =====================================================

    private fun openChrome() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://www.google.com"
                    )
                )

            intent.setPackage(
                "com.android.chrome"
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            openUrl(
                "https://www.google.com"
            )
        }
    }

    // =====================================================
    // MAPS
    // =====================================================

    private fun openMaps(
        command: String
    ) {

        val destination =
            command
                .replace(
                    "open maps",
                    ""
                )
                .replace(
                    "open map",
                    ""
                )
                .replace(
                    "navigate",
                    ""
                )
                .replace(
                    "directions",
                    ""
                )
                .trim()

        try {

            val uri =
                if (destination.isNotEmpty()) {

                    Uri.parse(
                        "geo:0,0?q=" +
                                Uri.encode(
                                    destination
                                )
                    )

                } else {

                    Uri.parse(
                        "geo:0,0"
                    )
                }

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    uri
                )

            intent.setPackage(
                "com.google.android.apps.maps"
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            openUrl(
                "https://maps.google.com"
            )
        }
    }

    // =====================================================
    // PHONE
    // =====================================================

    private fun openPhone() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_DIAL
                )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "Phone app is not available"
            )
        }
    }

    // =====================================================
    // SETTINGS
    // =====================================================

    private fun openSettings() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_SETTINGS
                )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "Settings could not be opened"
            )
        }
    }

    // =====================================================
    // VOLUME
    // =====================================================

    private fun changeVolume(
        increase: Boolean
    ) {

        val audioManager =
            getSystemService(
                AUDIO_SERVICE
            ) as AudioManager

        val direction =
            if (increase)
                AudioManager.ADJUST_RAISE
            else
                AudioManager.ADJUST_LOWER

        audioManager.adjustVolume(
            direction,
            AudioManager.FLAG_SHOW_UI
        )
    }

    // =====================================================
    // BATTERY
    // =====================================================

    private fun batteryStatus() {

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
    }

    // =====================================================
    // TIMER
    // =====================================================

    private fun setTimer(
        command: String
    ) {

        val pattern =
            Pattern.compile(
                "(\\d+)\\s*(second|seconds|sec|minute|minutes|min|hour|hours|hr)"
            )

        val matcher =
            pattern.matcher(command)

        if (!matcher.find()) {

            speak(
                "Please tell me the timer duration, for example, set timer for 5 minutes"
            )

            return
        }

        val value =
            matcher.group(1)?.toIntOrNull()
                ?: return

        val unit =
            matcher.group(2)
                ?.lowercase()
                ?: "minute"

        val seconds =
            when {

                unit.startsWith("second") ||
                        unit == "sec" ->
                    value

                unit.startsWith("minute") ||
                        unit == "min" ->
                    value * 60

                unit.startsWith("hour") ||
                        unit == "hr" ->
                    value * 3600

                else ->
                    value * 60
            }

        try {

            val intent =
                Intent(
                    AlarmClock.ACTION_SET_TIMER
                )

            intent.putExtra(
                AlarmClock.EXTRA_LENGTH,
                seconds
            )

            intent.putExtra(
                AlarmClock.EXTRA_SKIP_UI,
                false
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

            speak(
                "Timer set for $value $unit"
            )

        } catch (_: Exception) {

            speak(
                "I could not set the timer"
            )
        }
    }

    // =====================================================
    // ALARM
    // =====================================================

    private fun setAlarm(
        command: String
    ) {

        val pattern =
            Pattern.compile(
                "(\\d{1,2})(?::|\\s)(\\d{2})?\\s*(am|pm)?",
                Pattern.CASE_INSENSITIVE
            )

        val matcher =
            pattern.matcher(command)

        if (!matcher.find()) {

            speak(
                "Please say the alarm time, for example, set alarm for 7 30 AM"
            )

            return
        }

        try {

            var hour =
                matcher.group(1)
                    ?.toInt()
                    ?: return

            val minute =
                matcher.group(2)
                    ?.toIntOrNull()
                    ?: 0

            val ampm =
                matcher.group(3)
                    ?.lowercase()

            if (ampm == "pm" && hour < 12) {
                hour += 12
            }

            if (ampm == "am" && hour == 12) {
                hour = 0
            }

            val intent =
                Intent(
                    AlarmClock.ACTION_SET_ALARM
                )

            intent.putExtra(
                AlarmClock.EXTRA_HOUR,
                hour
            )

            intent.putExtra(
                AlarmClock.EXTRA_MINUTES,
                minute
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

            speak(
                "Alarm set for $hour $minute"
            )

        } catch (_: Exception) {

            speak(
                "I could not set the alarm"
            )
        }
    }

    // =====================================================
    // OPEN URL
    // =====================================================

    private fun openUrl(
        url: String
    ) {

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "I could not open that"
            )
        }
    }

    // =====================================================
    // TTS
    // =====================================================

    override fun onInit(
        status: Int
    ) {

        if (
            status ==
            TextToSpeech.SUCCESS
        ) {

            tts?.language =
                Locale.getDefault()
        }
    }

    private fun speak(
        text: String
    ) {

        sendStatus("SPEAKING")

        updateNotification(text)

        try {

            recognizer?.cancel()

        } catch (_: Exception) {
        }

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "AURIX_RESPONSE"
        )

        handler.postDelayed(
            {

                if (isRunning) {

                    processing = false
                    startListening()
                }

            },
            1700
        )
    }

    // =====================================================
    // EVENTS TO MAIN ACTIVITY
    // =====================================================

    private fun sendStatus(
        text: String
    ) {

        val intent =
            Intent(ACTION_EVENT)

        intent.setPackage(
            packageName
        )

        intent.putExtra(
            EXTRA_TYPE,
            TYPE_STATUS
        )

        intent.putExtra(
            EXTRA_TEXT,
            text
        )

        sendBroadcast(intent)
    }

    private fun sendCommand(
        text: String
    ) {

        val intent =
            Intent(ACTION_EVENT)

        intent.setPackage(
            packageName
        )

        intent.putExtra(
            EXTRA_TYPE,
            TYPE_COMMAND
        )

        intent.putExtra(
            EXTRA_TEXT,
            text
        )

        sendBroadcast(intent)
    }

    // =====================================================
    // STOP SERVICE
    // =====================================================

    private fun stopAurixService() {

        isRunning = false
        processing = true

        try {
            recognizer?.cancel()
            recognizer?.destroy()
        } catch (_: Exception) {
        }

        recognizer = null

        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }

        tts = null

        handler.removeCallbacksAndMessages(null)

        sendStatus("READY")

        stopForeground(
            STOP_FOREGROUND_REMOVE
        )

        stopSelf()
    }

    override fun onDestroy() {

        isRunning = false

        try {
            recognizer?.destroy()
        } catch (_: Exception) {
        }

        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }

        handler.removeCallbacksAndMessages(null)

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}
