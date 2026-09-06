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
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class AurixService :
    Service(),
    TextToSpeech.OnInitListener {

    companion object {

        const val ACTION_START =
            "com.example.myaiassistant.ACTION_START"

        const val ACTION_STOP =
            "com.example.myaiassistant.ACTION_STOP"

        const val ACTION_EVENT =
            "com.example.myaiassistant.AURIX_EVENT"

        const val EXTRA_TYPE = "type"
        const val EXTRA_TEXT = "text"

        const val TYPE_STATUS = "status"
        const val TYPE_COMMAND = "command"
        const val TYPE_SPEAK = "speak"

        @Volatile
        var isRunning = false

        private const val CHANNEL_ID =
            "aurix_voice_service"

        private const val NOTIFICATION_ID =
            5001
    }

    private var speechRecognizer:
            SpeechRecognizer? = null

    private var textToSpeech:
            TextToSpeech? = null

    private var listening = false
    private var restarting = false
    private var serviceDestroyed = false

    private val handler =
        Handler(Looper.getMainLooper())

    // =========================================================
    // SERVICE
    // =========================================================

    override fun onCreate() {

        super.onCreate()

        serviceDestroyed = false
        isRunning = true

        createNotificationChannel()
        startForegroundNotification()

        textToSpeech =
            TextToSpeech(
                this,
                this
            )

        startListening()
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

                isRunning = true

                if (!listening) {
                    startListening()
                }
            }

            else -> {

                if (!listening) {
                    startListening()
                }
            }
        }

        return START_STICKY
    }

    private fun stopAurix() {

        isRunning = false
        listening = false
        restarting = true

        handler.removeCallbacksAndMessages(null)

        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}

        speechRecognizer = null

        stopForeground(true)

        stopSelf()
    }

    // =========================================================
    // NOTIFICATION
    // =========================================================

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
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(
                channel
            )
        }
    }

    private fun startForegroundNotification() {

        val notification =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                Notification.Builder(
                    this,
                    CHANNEL_ID
                )
                    .setContentTitle("AURIX")
                    .setContentText(
                        "AURIX voice assistant is active"
                    )
                    .setSmallIcon(
                        android.R.drawable.ic_btn_speak_now
                    )
                    .setOngoing(true)
                    .build()

            } else {

                Notification.Builder(this)
                    .setContentTitle("AURIX")
                    .setContentText(
                        "AURIX voice assistant is active"
                    )
                    .setSmallIcon(
                        android.R.drawable.ic_btn_speak_now
                    )
                    .setOngoing(true)
                    .build()
            }

        startForeground(
            NOTIFICATION_ID,
            notification
        )
    }

    // =========================================================
    // SPEECH
    // =========================================================

    private fun startListening() {

        if (serviceDestroyed || !isRunning) {
            return
        }

        if (
            !SpeechRecognizer
                .isRecognitionAvailable(this)
        ) {

            sendStatus(
                "Speech recognition unavailable"
            )

            return
        }

        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}

        speechRecognizer =
            SpeechRecognizer
                .createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {

                    listening = true

                    sendStatus(
                        "LISTENING"
                    )
                }

                override fun onBeginningOfSpeech() {

                    sendStatus(
                        "PROCESSING"
                    )
                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) {}

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {}

                override fun onEndOfSpeech() {}

                override fun onError(
                    error: Int
                ) {

                    listening = false

                    if (
                        isRunning &&
                        !serviceDestroyed
                    ) {

                        restartListening()
                    }
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
                            ?.lowercase(
                                Locale.getDefault()
                            )

                    if (
                        !command.isNullOrBlank()
                    ) {

                        sendCommand(
                            command
                        )

                        processCommand(
                            command
                        )
                    }

                    listening = false

                    restartListening()
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

        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
                )

                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
                )

                putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    3
                )
            }

        try {

            speechRecognizer?.startListening(
                intent
            )

        } catch (_: Exception) {

            restartListening()
        }
    }

    private fun restartListening() {

        if (
            restarting ||
            !isRunning ||
            serviceDestroyed
        ) {
            return
        }

        restarting = true
        listening = false

        handler.postDelayed({

            restarting = false

            if (
                isRunning &&
                !serviceDestroyed
            ) {

                startListening()
            }

        }, 700)
    }

    // =========================================================
    // COMMAND PROCESSOR
    // =========================================================

    private fun processCommand(
        rawCommand: String
    ) {

        val command =
            normalizeNumberWords(
                rawCommand
                    .lowercase(
                        Locale.getDefault()
                    )
                    .trim()
            )

        // STOP
        if (
            command == "stop" ||
            command == "stop listening" ||
            command == "deactivate aurix" ||
            command == "aurix stop"
        ) {

            speak("Stopping AURIX")
            stopAurix()
            return
        }

        // CLOSE / HOME
        if (isCloseCommand(command)) {

            goHome()
            return
        }

        // FLASHLIGHT ON
        if (
            command.contains(
                "turn on flashlight"
            ) ||
            command.contains(
                "switch on flashlight"
            ) ||
            command.contains(
                "flashlight on"
            ) ||
            command.contains(
                "torch on"
            )
        ) {

            setFlashlight(true)
            return
        }

        // FLASHLIGHT OFF
        if (
            command.contains(
                "turn off flashlight"
            ) ||
            command.contains(
                "switch off flashlight"
            ) ||
            command.contains(
                "flashlight off"
            ) ||
            command.contains(
                "torch off"
            )
        ) {

            setFlashlight(false)
            return
        }

        // TIMER
        if (
            command.contains("timer")
        ) {

            setAurixTimer(command)
            return
        }

        // ALARM
        if (
            command.contains("alarm")
        ) {

            setAurixAlarm(command)
            return
        }

        // DATE
        if (
            command.contains("date") ||
            command.contains("today")
        ) {

            val date =
                SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale.getDefault()
                ).format(Date())

            speak(
                "Today is $date"
            )

            return
        }

        // DAY
        if (
            command == "day" ||
            command.contains("what day") ||
            command.contains("which day")
        ) {

            val day =
                SimpleDateFormat(
                    "EEEE",
                    Locale.getDefault()
                ).format(Date())

            speak(
                "Today is $day"
            )

            return
        }

        // TIME
        if (
            command == "time" ||
            command.contains(
                "what is the time"
            ) ||
            command.contains(
                "what's the time"
            ) ||
            command.contains(
                "tell me the time"
            ) ||
            command.contains(
                "current time"
            ) ||
            command.contains(
                "what time is it"
            )
        ) {

            val time =
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

            speak(
                "The time is $time"
            )

            return
        }

        // CAMERA
        if (
            command.contains("camera")
        ) {

            openCamera()
            return
        }

        // GALLERY
        if (
            command.contains("gallery") ||
            command.contains("photos") ||
            command.contains("photo")
        ) {

            openGallery()
            return
        }

        // MUSIC
        if (
            command.contains("music") ||
            command.contains("song") ||
            command.contains("songs") ||
            command.contains("gaane")
        ) {

            openMusic()
            return
        }

        // NOTES
        if (
            command.contains("notes") ||
            command.contains("note")
        ) {

            openNotes()
            return
        }

        // CALCULATOR
        if (
            command.contains("calculator") ||
            command.contains("calculate")
        ) {

            openCalculator()
            return
        }

        // YOUTUBE
        if (
            command.contains("youtube")
        ) {

            openYouTube()
            return
        }

        // CHROME
        if (
            command.contains("chrome") ||
            command.contains("browser")
        ) {

            openChrome()
            return
        }

        // MAPS
        if (
            command.contains("maps") ||
            command.contains("google maps")
        ) {

            openMaps()
            return
        }

        // PHONE
        if (
            command.contains("phone") ||
            command.contains("dialer") ||
            command.contains("call")
        ) {

            openPhone()
            return
        }

        // SETTINGS
        if (
            command.contains("settings") ||
            command.contains("setting")
        ) {

            openSettings()
            return
        }

        // VOLUME UP
        if (
            command.contains("volume up") ||
            command.contains("increase volume")
        ) {

            changeVolume(true)
            return
        }

        // VOLUME DOWN
        if (
            command.contains("volume down") ||
            command.contains("decrease volume")
        ) {

            changeVolume(false)
            return
        }

        // BATTERY
        if (
            command.contains("battery")
        ) {

            val manager =
                getSystemService(
                    Context.BATTERY_SERVICE
                ) as BatteryManager

            val level =
                manager.getIntProperty(
                    BatteryManager
                        .BATTERY_PROPERTY_CAPACITY
                )

            speak(
                "Battery is at $level percent"
            )

            return
        }

        // HELLO
        if (
            command == "hello" ||
            command == "hi" ||
            command.contains(
                "hello aurix"
            ) ||
            command.contains(
                "hi aurix"
            )
        ) {

            speak(
                "Hello. I am AURIX. How can I help you?"
            )

            return
        }

        // IDENTITY
        if (
            command.contains(
                "who are you"
            ) ||
            command.contains(
                "your name"
            )
        ) {

            speak(
                "I am AURIX, your personal AI assistant."
            )

            return
        }

        // GOOGLE SEARCH
        if (
            command.startsWith(
                "search "
            ) ||
            command.startsWith(
                "google "
            )
        ) {

            val query =
                command
                    .removePrefix("search ")
                    .removePrefix("google ")
                    .trim()

            if (query.isNotBlank()) {
                googleSearch(query)
            }

            return
        }

        // UNKNOWN
        googleSearch(command)
    }

    // =========================================================
    // NUMBER WORDS
    // =========================================================

    private fun normalizeNumberWords(
        input: String
    ): String {

        var text = input

        val numbers =
            mapOf(
                "zero" to "0",
                "one" to "1",
                "two" to "2",
                "three" to "3",
                "four" to "4",
                "five" to "5",
                "six" to "6",
                "seven" to "7",
                "eight" to "8",
                "nine" to "9",
                "ten" to "10",
                "eleven" to "11",
                "twelve" to "12",
                "thirteen" to "13",
                "fourteen" to "14",
                "fifteen" to "15",
                "sixteen" to "16",
                "seventeen" to "17",
                "eighteen" to "18",
                "nineteen" to "19",
                "twenty" to "20",
                "thirty" to "30",
                "forty" to "40",
                "fifty" to "50",
                "sixty" to "60"
            )

        val compounds =
            mapOf(
                "twenty one" to "21",
                "twenty two" to "22",
                "twenty three" to "23",
                "twenty four" to "24",
                "twenty five" to "25",
                "twenty six" to "26",
                "twenty seven" to "27",
                "twenty eight" to "28",
                "twenty nine" to "29",

                "thirty one" to "31",
                "thirty two" to "32",
                "thirty three" to "33",
                "thirty four" to "34",
                "thirty five" to "35",
                "thirty six" to "36",
                "thirty seven" to "37",
                "thirty eight" to "38",
                "thirty nine" to "39",

                "forty one" to "41",
                "forty two" to "42",
                "forty three" to "43",
                "forty four" to "44",
                "forty five" to "45",
                "forty six" to "46",
                "forty seven" to "47",
                "forty eight" to "48",
                "forty nine" to "49",

                "fifty one" to "51",
                "fifty two" to "52",
                "fifty three" to "53",
                "fifty four" to "54",
                "fifty five" to "55",
                "fifty six" to "56",
                "fifty seven" to "57",
                "fifty eight" to "58",
                "fifty nine" to "59"
            )

        compounds.forEach { (word, number) ->

            text =
                text.replace(
                    word,
                    number
                )
        }

        numbers.forEach { (word, number) ->

            text =
                text.replace(
                    Regex("\\b$word\\b"),
                    number
                )
        }

        return text
    }

    // =========================================================
    // TIMER
    // =========================================================

    private fun setAurixTimer(
        command: String
    ) {

        var seconds = 0L

        val hour =
            Pattern.compile(
                "(\\d+)\\s*(hour|hours|hr|hrs)"
            ).matcher(command)

        if (hour.find()) {

            seconds +=
                hour.group(1)!!
                    .toLong() * 3600L
        }

        val minute =
            Pattern.compile(
                "(\\d+)\\s*(minute|minutes|min|mins)"
            ).matcher(command)

        if (minute.find()) {

            seconds +=
                minute.group(1)!!
                    .toLong() * 60L
        }

        val second =
            Pattern.compile(
                "(\\d+)\\s*(second|seconds|sec|secs)"
            ).matcher(command)

        if (second.find()) {

            seconds +=
                second.group(1)!!
                    .toLong()
        }

        if (seconds == 0L) {

            val number =
                Pattern.compile(
                    "(?:timer|for)\\s+(\\d+)"
                ).matcher(command)

            if (number.find()) {

                val value =
                    number.group(1)!!
                        .toLong()

                seconds =
                    if (
                        command.contains("second") ||
                        command.contains("sec")
                    ) {
                        value
                    } else {
                        value * 60L
                    }
            }
        }

        if (seconds <= 0L) {

            speak(
                "Please tell me the timer duration."
            )

            return
        }

        val trigger =
            System.currentTimeMillis() +
                    seconds * 1000L

        scheduleAlert(
            trigger,
            "timer",
            "Your AURIX timer is finished."
        )

        val message =
            when {

                seconds >= 3600L ->
                    "${seconds / 3600L} hour timer started"

                seconds >= 60L ->
                    "${seconds / 60L} minute timer started"

                else ->
                    "$seconds second timer started"
            }

        speak(message)
    }

    // =========================================================
    // ALARM
    // =========================================================

    private fun setAurixAlarm(
        command: String
    ) {

        val matcher =
            Pattern.compile(
                "(\\d{1,2})(?:\\s*[:.]\\s*(\\d{1,2}))?\\s*(am|pm)?"
            ).matcher(command)

        if (!matcher.find()) {

            speak(
                "Please tell me the alarm time, for example seven PM."
            )

            return
        }

        var hour =
            matcher.group(1)!!
                .toInt()

        val minute =
            matcher.group(2)
                ?.toIntOrNull()
                ?: 0

        val ampm =
            matcher.group(3)
                ?.lowercase(
                    Locale.getDefault()
                )

        if (
            ampm == "pm" &&
            hour < 12
        ) {
            hour += 12
        }

        if (
            ampm == "am" &&
            hour == 12
        ) {
            hour = 0
        }

        if (
            hour !in 0..23 ||
            minute !in 0..59
        ) {

            speak(
                "That is not a valid alarm time."
            )

            return
        }

        val calendar =
            Calendar.getInstance()

        calendar.set(
            Calendar.HOUR_OF_DAY,
            hour
        )

        calendar.set(
            Calendar.MINUTE,
            minute
        )

        calendar.set(
            Calendar.SECOND,
            0
        )

        calendar.set(
            Calendar.MILLISECOND,
            0
        )

        if (
            calendar.timeInMillis <=
            System.currentTimeMillis()
        ) {

            calendar.add(
                Calendar.DAY_OF_YEAR,
                1
            )
        }

        scheduleAlert(
            calendar.timeInMillis,
            "alarm",
            "Your AURIX alarm is ringing."
        )

        val formatted =
            SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            ).format(calendar.time)

        speak(
            "Alarm set for $formatted"
        )
    }

    // =========================================================
    // ALARM MANAGER
    // =========================================================

    private fun scheduleAlert(
        triggerTime: Long,
        type: String,
        message: String
    ) {

        val alarmManager =
            getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                this,
                AlarmReceiver::class.java
            ).apply {

                putExtra(
                    AlarmReceiver.EXTRA_TYPE,
                    type
                )

                putExtra(
                    AlarmReceiver.EXTRA_MESSAGE,
                    message
                )
            }

        val requestCode =
            if (type == "timer") {
                7001
            } else {
                7002
            }

        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or
                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.M
                    ) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        0
                    }

        val pendingIntent =
            PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                flags
            )

        try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M
            ) {

                try {

                    alarmManager
                        .setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )

                } catch (_: SecurityException) {

                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }

            } else {

                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

        } catch (_: Exception) {

            try {

                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )

            } catch (_: Exception) {}
        }
    }

    // =========================================================
    // FLASHLIGHT
    // =========================================================

    private fun setFlashlight(
        enabled: Boolean
    ) {

        try {

            val manager =
                getSystemService(
                    Context.CAMERA_SERVICE
                ) as CameraManager

            var cameraId: String? = null

            for (
                id in manager.cameraIdList
            ) {

                val characteristics =
                    manager.getCameraCharacteristics(
                        id
                    )

                val flash =
                    characteristics.get(
                        CameraCharacteristics
                            .FLASH_INFO_AVAILABLE
                    ) ?: false

                val facing =
                    characteristics.get(
                        CameraCharacteristics
                            .LENS_FACING
                    )

                if (
                    flash &&
                    facing ==
                    CameraCharacteristics
                        .LENS_FACING_BACK
                ) {

                    cameraId = id
                    break
                }
            }

            if (cameraId == null) {

                speak(
                    "Flashlight is not available"
                )

                return
            }

            manager.setTorchMode(
                cameraId,
                enabled
            )

            if (enabled) {

                speak(
                    "Flashlight turned on"
                )

            } else {

                speak(
                    "Flashlight turned off"
                )
            }

        } catch (_: Exception) {

            speak(
                "I could not control the flashlight"
            )
        }
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private fun openCamera() {

        val intents =
            listOf(
                Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE
                ),
                Intent(
                    "android.media.action.IMAGE_CAPTURE"
                )
            )

        for (intent in intents) {

            try {

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                if (
                    packageManager
                        .queryIntentActivities(
                            intent,
                            PackageManager.MATCH_DEFAULT_ONLY
                        )
                        .isNotEmpty()
                ) {

                    startActivity(intent)
                    return
                }

            } catch (_: Exception) {}
        }

        val packages =
            arrayOf(
                "com.android.camera",
                "com.android.camera2",
                "com.miui.camera"
            )

        for (
            packageName in packages
        ) {

            try {

                val launchIntent =
                    packageManager
                        .getLaunchIntentForPackage(
                            packageName
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

            } catch (_: Exception) {}
        }

        speak(
            "Camera is not available"
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
                ).apply {

                    setDataAndType(
                        MediaStore
                            .Images
                            .Media
                            .EXTERNAL_CONTENT_URI,
                        "image/*"
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

        } catch (_: Exception) {

            try {

                val intent =
                    Intent(
                        Intent.ACTION_VIEW
                    ).apply {

                        data =
                            Uri.parse(
                                "content://media/external/images/media"
                            )

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }

                startActivity(intent)

            } catch (_: Exception) {

                speak(
                    "Gallery is not available"
                )
            }
        }
    }

    // =========================================================
    // MUSIC
    // =========================================================

    private fun openMusic() {

        val packages =
            arrayOf(
                "com.google.android.apps.youtube.music",
                "com.miui.player",
                "com.android.music"
            )

        for (
            packageName in packages
        ) {

            try {

                val intent =
                    packageManager
                        .getLaunchIntentForPackage(
                            packageName
                        )

                if (intent != null) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    startActivity(intent)
                    return
                }

            } catch (_: Exception) {}
        }

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW
                ).apply {

                    data =
                        Uri.parse(
                            "https://music.youtube.com"
                        )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "Music app is not available"
            )
        }
    }

    // =========================================================
    // NOTES
    // =========================================================

    private fun openNotes() {

        val packages =
            arrayOf(
                "com.miui.notes",
                "com.google.android.keep"
            )

        for (
            packageName in packages
        ) {

            try {

                val intent =
                    packageManager
                        .getLaunchIntentForPackage(
                            packageName
                        )

                if (intent != null) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    startActivity(intent)
                    return
                }

            } catch (_: Exception) {}
        }

        speak(
            "Notes app is not available"
        )
    }

    // =========================================================
    // CALCULATOR
    // =========================================================

    private fun openCalculator() {

    val calculatorPackages = arrayOf(
        "com.miui.calculator",
        "com.android.calculator2",
        "com.google.android.calculator"
    )

    for (packageName in calculatorPackages) {

        try {

            val launchIntent =
                packageManager.getLaunchIntentForPackage(
                    packageName
                )

            if (launchIntent != null) {

                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(launchIntent)

                speak("Opening calculator")
                return
            }

        } catch (_: Exception) {
        }
    }

    try {

        val intent =
            Intent().apply {

                action =
                    Intent.ACTION_MAIN

                addCategory(
                    "android.intent.category.APP_CALCULATOR"
                )

                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }

        startActivity(intent)

        speak("Opening calculator")
        return

    } catch (_: Exception) {
    }

    speak("Calculator is not available")
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

            try {

                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://www.youtube.com"
                        )
                    )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(intent)

            } catch (_: Exception) {

                speak(
                    "YouTube is not available"
                )
            }
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

            try {

                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://www.google.com"
                        )
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

            try {

                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://maps.google.com"
                        )
                    )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(intent)

            } catch (_: Exception) {

                speak(
                    "Maps is not available"
                )
            }
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
                ).apply {

                    data =
                        Uri.parse("tel:")

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

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
                ).apply {

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "Settings is not available"
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

            val audio =
                getSystemService(
                    Context.AUDIO_SERVICE
                ) as AudioManager

            audio.adjustVolume(
                if (increase) {
                    AudioManager.ADJUST_RAISE
                } else {
                    AudioManager.ADJUST_LOWER
                },
                AudioManager.FLAG_SHOW_UI
            )

            speak(
                if (increase) {
                    "Volume increased"
                } else {
                    "Volume decreased"
                }
            )

        } catch (_: Exception) {}
    }

    // =========================================================
    // GOOGLE
    // =========================================================

    private fun googleSearch(
        query: String
    ) {

        try {

            val url =
                "https://www.google.com/search?q=" +
                        Uri.encode(query)

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).apply {

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

        } catch (_: Exception) {

            speak(
                "I could not search that"
            )
        }
    }

    // =========================================================
    // CLOSE / HOME
    // =========================================================

    private fun isCloseCommand(
        command: String
    ): Boolean {

        return command == "close" ||
                command == "exit" ||
                command == "quit" ||
                command == "go home" ||
                command == "home" ||
                command == "close app" ||
                command == "close application" ||
                command.startsWith("close ") ||
                command.startsWith("exit ") ||
                command.startsWith("quit ") ||
                command.startsWith("band ")
    }

    private fun goHome() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_MAIN
                ).apply {

                    addCategory(
                        Intent.CATEGORY_HOME
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

        } catch (_: Exception) {}

        speak("Closing")
    }

    // =========================================================
    // TTS
    // =========================================================

    override fun onInit(
        status: Int
    ) {

        if (
            status ==
            TextToSpeech.SUCCESS
        ) {

            try {
                textToSpeech?.language =
                    Locale.US
            } catch (_: Exception) {}
        }
    }

    private fun speak(
        text: String
    ) {

        sendSpeak(text)

        try {

            textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "AURIX"
            )

        } catch (_: Exception) {}
    }

    // =========================================================
    // EVENTS
    // =========================================================

    private fun sendStatus(
        text: String
    ) {

        sendEvent(
            TYPE_STATUS,
            text
        )
    }

    private fun sendCommand(
        text: String
    ) {

        sendEvent(
            TYPE_COMMAND,
            text
        )
    }

    private fun sendSpeak(
        text: String
    ) {

        sendEvent(
            TYPE_SPEAK,
            text
        )
    }

    private fun sendEvent(
        type: String,
        text: String
    ) {

        val intent =
            Intent(
                ACTION_EVENT
            ).apply {

                setPackage(packageName)

                putExtra(
                    EXTRA_TYPE,
                    type
                )

                putExtra(
                    EXTRA_TEXT,
                    text
                )
            }

        sendBroadcast(intent)
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    override fun onDestroy() {

        serviceDestroyed = true
        isRunning = false
        listening = false
        restarting = true

        handler.removeCallbacksAndMessages(null)

        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}

        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}

        speechRecognizer = null
        textToSpeech = null

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}
