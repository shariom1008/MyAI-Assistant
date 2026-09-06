package com.example.myaiassistant

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
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

class AurixService : Service(), TextToSpeech.OnInitListener {

    companion object {

        const val ACTION_START =
            "com.example.myaiassistant.AURIX_START"

        const val ACTION_STOP =
            "com.example.myaiassistant.AURIX_STOP"

        const val ACTION_EVENT =
            "com.example.myaiassistant.AURIX_EVENT"

        const val EXTRA_TYPE =
            "type"

        const val EXTRA_TEXT =
            "text"

        const val TYPE_STATUS =
            "status"

        const val TYPE_COMMAND =
            "command"

        private const val CHANNEL_ID =
            "aurix_voice_channel"

        private const val NOTIFICATION_ID =
            9001

        var isRunning = false
            private set
    }

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null

    private val handler =
        Handler(Looper.getMainLooper())

    private var processing = false
    private var stopping = false

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification("AURIX is ready")
        )

        isRunning = true
        stopping = false

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

        when (intent?.action) {

            ACTION_STOP -> {
                stopAurix()
                return START_NOT_STICKY
            }

            ACTION_START -> {
                stopping = false
                isRunning = true

                handler.postDelayed(
                    {
                        startListening()
                    },
                    300
                )
            }
        }

        return START_STICKY
    }

    // =========================================================
    // NOTIFICATION
    // =========================================================

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "AURIX Voice Assistant",
                    NotificationManager.IMPORTANCE_LOW
                )

            channel.description =
                "AURIX background voice assistant"

            channel.setShowBadge(false)

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(
        message: String
    ): Notification {

        val openIntent =
            Intent(
                this,
                MainActivity::class.java
            )

        val openPendingIntent =
            PendingIntent.getActivity(
                this,
                100,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val stopIntent =
            Intent(
                this,
                AurixService::class.java
            )

        stopIntent.action = ACTION_STOP

        val stopPendingIntent =
            PendingIntent.getService(
                this,
                101,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            Notification.Builder(
                this,
                CHANNEL_ID
            )
                .setContentTitle("AURIX")
                .setContentText(message)
                .setSmallIcon(
                    android.R.drawable.ic_btn_speak_now
                )
                .setContentIntent(
                    openPendingIntent
                )
                .addAction(
                    Notification.Action.Builder(
                        null,
                        "STOP",
                        stopPendingIntent
                    ).build()
                )
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()

        } else {

            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("AURIX")
                .setContentText(message)
                .setSmallIcon(
                    android.R.drawable.ic_btn_speak_now
                )
                .setContentIntent(
                    openPendingIntent
                )
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()
        }
    }

    private fun updateNotification(
        message: String
    ) {

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.notify(
            NOTIFICATION_ID,
            createNotification(message)
        )
    }

    // =========================================================
    // SPEECH RECOGNIZER
    // =========================================================

    private fun setupRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            sendStatus("VOICE NOT AVAILABLE")
            return
        }

        try {

            recognizer?.destroy()

        } catch (_: Exception) {
        }

        recognizer =
            SpeechRecognizer.createSpeechRecognizer(this)

        recognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {
                    if (!stopping) {
                        sendStatus("LISTENING")
                        updateNotification(
                            "Listening..."
                        )
                    }
                }

                override fun onBeginningOfSpeech() {
                    sendStatus("LISTENING")
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
                    sendStatus("PROCESSING")
                }

                override fun onError(
                    error: Int
                ) {

                    processing = false

                    if (!stopping) {

                        sendStatus("LISTENING")

                        handler.postDelayed(
                            {
                                startListening()
                            },
                            500
                        )
                    }
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    val list =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val text =
                        list
                            ?.firstOrNull()
                            ?.trim()
                            ?: ""

                    if (text.isNotEmpty()) {

                        sendCommand(text)

                        processCommand(text)
                    }

                    processing = false

                    if (!stopping) {

                        handler.postDelayed(
                            {
                                startListening()
                            },
                            700
                        )
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

        if (!isRunning || stopping) return

        if (processing) return

        if (
            !SpeechRecognizer.isRecognitionAvailable(
                this
            )
        ) {
            sendStatus("VOICE NOT AVAILABLE")
            return
        }

        try {

            processing = true

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
                Locale.getDefault()
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            )

            intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
            )

            recognizer?.startListening(intent)

        } catch (_: Exception) {

            processing = false

            if (!stopping) {

                handler.postDelayed(
                    {
                        startListening()
                    },
                    1000
                )
            }
        }
    }

    // =========================================================
    // COMMAND PROCESSING
    // =========================================================

    private fun processCommand(
        originalCommand: String
    ) {

        val command =
            originalCommand
                .lowercase(Locale.getDefault())
                .trim()

        if (command.isEmpty()) return

        // STOP FIRST
        if (
            command.contains("stop listening") ||
            command.contains("deactivate aurix") ||
            command.contains("stop aurix") ||
            command == "stop"
        ) {

            speak("AURIX deactivated")
            handler.postDelayed(
                {
                    stopAurix()
                },
                1200
            )

            return
        }

        // TIMER BEFORE TIME
        if (
            command.contains("timer") ||
            command.contains("set timer") ||
            command.contains("minute timer") ||
            command.contains("second timer")
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

        // CAMERA
        if (
            command.contains("camera") ||
            command.contains("take a photo") ||
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
            command.contains("photo gallery") ||
            command.contains("open photos")
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
            command.contains("gaane") ||
            command.contains("open music")
        ) {

            speak("Opening music")
            openMusic()
            return
        }

        // NOTES
        if (
            command.contains("notes") ||
            command.contains("note") ||
            command.contains("open notes")
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

        // YOUTUBE
        if (
            command.contains("youtube") ||
            command.contains("open youtube")
        ) {

            speak("Opening YouTube")
            openYouTube()
            return
        }

        // CHROME
        if (
            command.contains("chrome") ||
            command.contains("open browser") ||
            command.contains("browser")
        ) {

            speak("Opening browser")
            openChrome()
            return
        }

        // MAPS
        if (
            command.contains("maps") ||
            command.contains("google maps") ||
            command.contains("open maps")
        ) {

            speak("Opening maps")
            openMaps()
            return
        }

        // PHONE
        if (
            command.contains("phone") ||
            command.contains("dialer") ||
            command.contains("open phone")
        ) {

            speak("Opening phone")
            openPhone()
            return
        }

        // SETTINGS
        if (
            command.contains("settings") ||
            command.contains("open settings")
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
            command.contains("battery") ||
            command.contains("battery status")
        ) {

            batteryStatus()
            return
        }

        // TIME
        if (
            command == "time" ||
            command.contains("what time") ||
            command.contains("current time") ||
            command.contains("tell me the time")
        ) {

            val time =
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

            speak("The time is $time")
            return
        }

        // HELLO
        if (
            command == "hello" ||
            command == "hi" ||
            command.contains("hello aurix") ||
            command.contains("hi aurix")
        ) {

            speak("Hello. I am AURIX. How can I help you?")
            return
        }

        // IDENTITY
        if (
            command.contains("who are you") ||
            command.contains("your name") ||
            command.contains("what are you")
        ) {

            speak(
                "I am AURIX, your intelligent voice assistant."
            )

            return
        }

        // GOOGLE SEARCH
        if (
            command.startsWith("search ") ||
            command.startsWith("google ") ||
            command.contains("search for ")
        ) {

            val query =
                command
                    .replaceFirst(
                        Regex("^search\\s+"),
                        ""
                    )
                    .replaceFirst(
                        Regex("^google\\s+"),
                        ""
                    )
                    .replaceFirst(
                        Regex("^search for\\s+"),
                        ""
                    )
                    .trim()

            if (query.isNotEmpty()) {

                speak("Searching Google")

                openGoogleSearch(query)

                return
            }
        }

        // UNKNOWN COMMAND
        speak(
            "I will search that on Google"
        )

        openGoogleSearch(command)
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private fun openCamera() {

    // 1. Standard camera intent
    try {

        val intent =
            Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
            )

        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )

        if (
            packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            ).isNotEmpty()
        ) {

            startActivity(intent)
            return
        }

    } catch (_: Exception) {
    }

    // 2. Try common Xiaomi / Android camera packages
    val cameraPackages =
        arrayOf(
            "com.android.camera",
            "com.android.camera2",
            "com.miui.camera"
        )

    for (
        cameraPackage in cameraPackages
    ) {

        try {

            val launchIntent =
                packageManager.getLaunchIntentForPackage(
                    cameraPackage
                )

            if (launchIntent != null) {

                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(
                    launchIntent
                )

                return
            }

        } catch (_: Exception) {
        }
    }

    // 3. Last fallback: generic image capture
    try {

        val fallbackIntent =
            Intent(
                "android.media.action.IMAGE_CAPTURE"
            )

        fallbackIntent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )

        startActivity(
            fallbackIntent
        )

        return

    } catch (_: Exception) {
    }

    speak(
        "I could not open the camera"
    )
    }

    // =========================================================
    // GALLERY
    // =========================================================

    private fun openGallery() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW
                )

            intent.type = "image/*"

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

            return

        } catch (_: Exception) {
        }

        try {

            val intent =
                Intent(
                    Intent.ACTION_MAIN
                )

            intent.addCategory(
                Intent.CATEGORY_APP_GALLERY
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "Gallery is not available"
            )
        }
    }

    // =========================================================
    // MUSIC
    // =========================================================

    private fun openMusic() {

        try {

            val intent =
                Intent(
                    MediaStore.INTENT_ACTION_MUSIC_PLAYER
                )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

            return

        } catch (_: Exception) {
        }

        try {

            val intent =
                Intent(
                    Intent.ACTION_MAIN
                )

            intent.addCategory(
                Intent.CATEGORY_APP_MUSIC
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

    // =========================================================
    // NOTES
    // =========================================================

    private fun openNotes() {

        val packages =
            arrayOf(
                "com.google.android.keep",
                "com.miui.notes",
                "com.android.notes"
            )

        for (
            packageName in packages
        ) {

            try {

                val intent =
                    packageManager.getLaunchIntentForPackage(
                        packageName
                    )

                if (intent != null) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    startActivity(intent)

                    return
                }

            } catch (_: Exception) {
            }
        }

        try {

            val intent =
                Intent(
                    Intent.ACTION_INSERT
                )

            intent.type = "text/plain"

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "Notes app is not available"
            )
        }
    }

    // =========================================================
    // CALCULATOR
    // =========================================================

    private fun openCalculator() {

        val packages =
            arrayOf(
                "com.google.android.calculator",
                "com.miui.calculator",
                "com.android.calculator2"
            )

        for (
            packageName in packages
        ) {

            try {

                val intent =
                    packageManager.getLaunchIntentForPackage(
                        packageName
                    )

                if (intent != null) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    startActivity(intent)

                    return
                }

            } catch (_: Exception) {
            }
        }

        try {

            val intent =
                Intent(
                    Intent.ACTION_MAIN
                )

            intent.addCategory(
                Intent.CATEGORY_APP_CALCULATOR
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "Calculator is not available"
            )
        }
    }

    // =========================================================
    // CHROME
    // =========================================================

    private fun openChrome() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com")
                )

            intent.setPackage(
                "com.android.chrome"
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            try {

                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com")
                    )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(intent)

            } catch (_: Exception) {

                speak(
                    "Browser is not available"
                )
            }
        }
    }

    // =========================================================
    // YOUTUBE
    // =========================================================

    private fun openYouTube() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://www.youtube.com"
                    )
                )

            intent.setPackage(
                "com.google.android.youtube"
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            openUrl(
                "https://www.youtube.com"
            )
        }
    }

    // =========================================================
    // MAPS
    // =========================================================

    private fun openMaps() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "geo:0,0?q="
                    )
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

    // =========================================================
    // PHONE
    // =========================================================

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

    // =========================================================
    // SETTINGS
    // =========================================================

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
                "Settings are not available"
            )
        }
    }

    // =========================================================
    // VOLUME
    // =========================================================

    private fun changeVolume(
        increase: Boolean
    ) {

        try {

            val audioManager =
                getSystemService(
                    Context.AUDIO_SERVICE
                ) as AudioManager

            val direction =
                if (increase) {

                    AudioManager.ADJUST_RAISE

                } else {

                    AudioManager.ADJUST_LOWER
                }

            audioManager.adjustVolume(
                direction,
                AudioManager.FLAG_SHOW_UI
            )

        } catch (_: Exception) {
        }
    }

    // =========================================================
    // BATTERY
    // =========================================================

    private fun batteryStatus() {

        try {

            val batteryManager =
                getSystemService(
                    Context.BATTERY_SERVICE
                ) as BatteryManager

            val level =
                batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )

            speak(
                "Battery level is $level percent"
            )

        } catch (_: Exception) {

            speak(
                "I could not read the battery level"
            )
        }
    }

    // =========================================================
    // TIMER
    // =========================================================

    private fun setTimer(
        command: String
    ) {

        var seconds = 0

        val minutePattern =
            Pattern.compile(
                "(\\d+)\\s*(minute|minutes|min|mins)"
            )

        val secondPattern =
            Pattern.compile(
                "(\\d+)\\s*(second|seconds|sec|secs)"
            )

        val minuteMatcher =
            minutePattern.matcher(command)

        val secondMatcher =
            secondPattern.matcher(command)

        if (minuteMatcher.find()) {

            val minutes =
                minuteMatcher
                    .group(1)
                    ?.toIntOrNull()
                    ?: 0

            seconds =
                minutes * 60
        }

        if (secondMatcher.find()) {

            val extraSeconds =
                secondMatcher
                    .group(1)
                    ?.toIntOrNull()
                    ?: 0

            seconds += extraSeconds
        }

        if (seconds <= 0) {

            speak(
                "Please say a timer duration, for example, set timer for 5 minutes"
            )

            return
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
                AlarmClock.EXTRA_MESSAGE,
                "AURIX Timer"
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
                "Timer set for ${formatDuration(seconds)}"
            )

        } catch (_: Exception) {

            speak(
                "I could not set the timer"
            )
        }
    }

    private fun formatDuration(
        seconds: Int
    ): String {

        val minutes =
            seconds / 60

        val remaining =
            seconds % 60

        return when {

            minutes > 0 &&
                remaining > 0 ->
                "$minutes minutes $remaining seconds"

            minutes > 0 ->
                "$minutes minutes"

            else ->
                "$remaining seconds"
        }
    }

    // =========================================================
    // ALARM
    // =========================================================

    private fun setAlarm(
        command: String
    ) {

        val timePattern =
            Pattern.compile(
                "(\\d{1,2})(?::|\\s)(\\d{2})\\s*(am|pm)?"
            )

        val matcher =
            timePattern.matcher(command)

        if (!matcher.find()) {

            speak(
                "Please say the alarm time, for example, set alarm for 7 AM"
            )

            return
        }

        try {

            var hour =
                matcher
                    .group(1)
                    ?.toIntOrNull()
                    ?: return

            val minute =
                matcher
                    .group(2)
                    ?.toIntOrNull()
                    ?: 0

            val amPm =
                matcher
                    .group(3)
                    ?.lowercase()

            if (amPm == "pm" && hour < 12) {
                hour += 12
            }

            if (amPm == "am" && hour == 12) {
                hour = 0
            }

            if (
                hour !in 0..23 ||
                minute !in 0..59
            ) {

                speak(
                    "That is not a valid alarm time"
                )

                return
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

            intent.putExtra(
                AlarmClock.EXTRA_MESSAGE,
                "AURIX Alarm"
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

    // =========================================================
    // GOOGLE SEARCH
    // =========================================================

    private fun openGoogleSearch(
        query: String
    ) {

        try {

            val encoded =
                Uri.encode(query)

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://www.google.com/search?q=$encoded"
                    )
                )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "I could not open Google"
            )
        }
    }

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
        }
    }

    // =========================================================
    // TEXT TO SPEECH
    // =========================================================

    override fun onInit(
        status: Int
    ) {

        if (
            status ==
            TextToSpeech.SUCCESS
        ) {

            tts?.language =
                Locale.US

            tts?.setSpeechRate(
                0.95f
            )
        }
    }

    private fun speak(
        text: String
    ) {

        sendStatus("PROCESSING")

        updateNotification(text)

        try {

            tts?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "AURIX_${System.currentTimeMillis()}"
            )

        } catch (_: Exception) {
        }
    }

    // =========================================================
    // BROADCAST EVENTS
    // =========================================================

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

    // =========================================================
    // STOP SERVICE
    // =========================================================

    private fun stopAurix() {

        stopping = true
        isRunning = false
        processing = false

        try {
            recognizer?.cancel()
        } catch (_: Exception) {
        }

        try {
            recognizer?.destroy()
        } catch (_: Exception) {
        }

        recognizer = null

        try {
            tts?.stop()
        } catch (_: Exception) {
        }

        handler.removeCallbacksAndMessages(null)

        sendStatus("READY")

        if (Build.VERSION.SDK_INT >= 24) {

            stopForeground(
                STOP_FOREGROUND_REMOVE
            )

        } else {

            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        stopSelf()
    }

    // =========================================================
    // SERVICE LIFECYCLE
    // =========================================================

    override fun onDestroy() {

        isRunning = false
        stopping = true

        try {
            recognizer?.cancel()
        } catch (_: Exception) {
        }

        try {
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

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}
