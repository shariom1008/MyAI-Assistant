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

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

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
        restarting = false

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
                restarting = false

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
        } catch (_: Exception) {
        }

        speechRecognizer = null

        try {
            textToSpeech?.stop()
        } catch (_: Exception) {
        }

        stopForeground(true)
        stopSelf()
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

            val manager =
                getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundNotification() {

        val notification =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

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
    // SPEECH ENGINE
    // =========================================================

    private fun startListening() {

        if (
            serviceDestroyed ||
            !isRunning
        ) {
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
        } catch (_: Exception) {
        }

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
                ) {
                }

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                }

                override fun onEndOfSpeech() {
                }

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
                        list
                            ?.firstOrNull()
                            ?.trim()
                            ?.lowercase(
                                Locale.getDefault()
                            )

                    if (
                        !command.isNullOrBlank()
                    ) {

                        sendCommand(command)
                        processCommand(command)
                    }

                    listening = false

                    if (
                        isRunning &&
                        !serviceDestroyed
                    ) {
                        restartListening()
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

            speechRecognizer?.startListening(intent)

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
    // COMMAND ENGINE
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

        if (command.isBlank()) {
            return
        }

        // -----------------------------------------------------
        // STOP AURIX
        // -----------------------------------------------------

        if (
            command == "stop" ||
            command == "stop listening" ||
            command == "deactivate aurix" ||
            command == "aurix stop" ||
            command == "aurix deactivate"
        ) {

            speak("Stopping AURIX")
            stopAurix()
            return
        }

        // -----------------------------------------------------
        // HOME
        // -----------------------------------------------------

        if (isCloseCommand(command)) {

            goHome()
            return
        }

        // -----------------------------------------------------
        // FLASHLIGHT
        // -----------------------------------------------------

        if (
            command.contains("turn on flashlight") ||
            command.contains("switch on flashlight") ||
            command.contains("flashlight on") ||
            command.contains("torch on") ||
            command.contains("torch chalao") ||
            command.contains("flashlight chalao")
        ) {

            setFlashlight(true)
            return
        }

        if (
            command.contains("turn off flashlight") ||
            command.contains("switch off flashlight") ||
            command.contains("flashlight off") ||
            command.contains("torch off") ||
            command.contains("torch band") ||
            command.contains("flashlight band")
        ) {

            setFlashlight(false)
            return
        }

        // -----------------------------------------------------
        // TIMER
        // -----------------------------------------------------

        if (command.contains("timer")) {

            setAurixTimer(command)
            return
        }

        // -----------------------------------------------------
        // ALARM
        // -----------------------------------------------------

        if (command.contains("alarm")) {

            setAurixAlarm(command)
            return
        }

        // -----------------------------------------------------
        // DATE
        // -----------------------------------------------------

        if (
            command.contains("date") ||
            command.contains("today's date") ||
            command.contains("today date")
        ) {

            val date =
                SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale.getDefault()
                ).format(Date())

            speak("Today is $date")
            return
        }

        // -----------------------------------------------------
        // DAY
        // -----------------------------------------------------

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

            speak("Today is $day")
            return
        }

        // -----------------------------------------------------
        // TIME
        // -----------------------------------------------------

        if (
            command == "time" ||
            command.contains("what is the time") ||
            command.contains("what's the time") ||
            command.contains("tell me the time") ||
            command.contains("current time") ||
            command.contains("what time is it") ||
            command.contains("time kya hai")
        ) {

            val time =
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date())

            speak("The time is $time")
            return
        }

        // -----------------------------------------------------
        // CAMERA
        // -----------------------------------------------------

        if (
            command.contains("camera") ||
            command.contains("take a photo")
        ) {

            openCamera()
            return
        }

        // -----------------------------------------------------
        // GALLERY
        // -----------------------------------------------------

        if (
            command.contains("gallery") ||
            command.contains("photos") ||
            command.contains("photo gallery")
        ) {

            openGallery()
            return
        }

        // -----------------------------------------------------
        // MUSIC
        // -----------------------------------------------------

        if (
            command == "music" ||
            command.contains("open music") ||
            command.contains("music app")
        ) {

            openMusic()
            return
        }

        // -----------------------------------------------------
        // NOTES
        // -----------------------------------------------------

        if (
            command.contains("notes") ||
            command.contains("note app")
        ) {

            openNotes()
            return
        }

        // -----------------------------------------------------
        // CALCULATOR
        // -----------------------------------------------------

        if (
            command.contains("calculator") ||
            command.contains("calculate")
        ) {

            openCalculator()
            return
        }

        // -----------------------------------------------------
        // YOUTUBE SEARCH
        // -----------------------------------------------------

        if (
            command.startsWith("search youtube") ||
            command.startsWith("youtube search") ||
            command.startsWith("youtube par")
        ) {

            val query =
                command
                    .replaceFirst("search youtube", "")
                    .replaceFirst("youtube search", "")
                    .replaceFirst("youtube par", "")
                    .trim()

            if (query.isNotBlank()) {
                searchYouTube(query)
            } else {
                openYouTube()
            }

            return
        }

        // -----------------------------------------------------
        // YOUTUBE
        // -----------------------------------------------------

        if (
            command == "youtube" ||
            command == "open youtube" ||
            command == "launch youtube"
        ) {

            openYouTube()
            return
        }

        // -----------------------------------------------------
        // MAP SEARCH
        // -----------------------------------------------------

        if (
            command.startsWith("search maps") ||
            command.startsWith("maps search") ||
            command.startsWith("navigate to")
        ) {

            val query =
                command
                    .replaceFirst("search maps", "")
                    .replaceFirst("maps search", "")
                    .replaceFirst("navigate to", "")
                    .trim()

            if (query.isNotBlank()) {
                searchMaps(query)
            } else {
                openMaps()
            }

            return
        }

        // -----------------------------------------------------
        // MAPS
        // -----------------------------------------------------

        if (
            command == "maps" ||
            command == "open maps" ||
            command == "google maps"
        ) {

            openMaps()
            return
        }

        // -----------------------------------------------------
        // CHROME
        // -----------------------------------------------------

        if (
            command == "chrome" ||
            command == "open chrome" ||
            command == "browser" ||
            command == "open browser"
        ) {

            openChrome()
            return
        }

        // -----------------------------------------------------
        // PHONE
        // -----------------------------------------------------

        if (
            command == "phone" ||
            command == "open phone" ||
            command == "dialer" ||
            command == "open dialer"
        ) {

            openPhone()
            return
        }

        // -----------------------------------------------------
        // SETTINGS
        // -----------------------------------------------------

        if (
            command == "settings" ||
            command == "open settings" ||
            command == "phone settings"
        ) {

            openSettings()
            return
        }

        // -----------------------------------------------------
        // WI-FI
        // -----------------------------------------------------

        if (
            command.contains("wifi") ||
            command.contains("wi fi")
        ) {

            openWifiSettings()
            return
        }

        // -----------------------------------------------------
        // BLUETOOTH
        // -----------------------------------------------------

        if (command.contains("bluetooth")) {

            openBluetoothSettings()
            return
        }

        // -----------------------------------------------------
        // NOTIFICATION SETTINGS
        // -----------------------------------------------------

        if (command.contains("notification settings")) {

            openNotificationSettings()
            return
        }

        // -----------------------------------------------------
        // VOLUME
        // -----------------------------------------------------

        if (
            command.contains("volume up") ||
            command.contains("increase volume") ||
            command.contains("volume badhao")
        ) {

            changeVolume(true)
            return
        }

        if (
            command.contains("volume down") ||
            command.contains("decrease volume") ||
            command.contains("volume kam")
        ) {

            changeVolume(false)
            return
        }

        // -----------------------------------------------------
        // MEDIA
        // -----------------------------------------------------

        if (
            command == "play" ||
            command == "pause" ||
            command == "resume" ||
            command.contains("play music") ||
            command.contains("pause music") ||
            command.contains("resume music")
        ) {

            controlMedia()
            return
        }

        // -----------------------------------------------------
        // BATTERY
        // -----------------------------------------------------

        if (
            command.contains("battery")
        ) {

            tellBattery()
            return
        }

        // -----------------------------------------------------
        // HELLO
        // -----------------------------------------------------

        if (
            command == "hello" ||
            command == "hi" ||
            command == "hey aurix" ||
            command == "hello aurix" ||
            command == "hi aurix"
        ) {

            speak(
                "Hello. I am AURIX. How can I help you?"
            )

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

        // =====================================================
        // SMART APP CONTROL
        // IMPORTANT: BEFORE GOOGLE FALLBACK
        // =====================================================

        if (isAppOpenCommand(command)) {

            val appName =
                extractAppName(command)

            openInstalledApp(appName)
            return
        }

        // -----------------------------------------------------
        // GOOGLE SEARCH
        // -----------------------------------------------------

        if (
            command.startsWith("search ") ||
            command.startsWith("google ") ||
            command.startsWith("search for ") ||
            command.startsWith("google search ")
        ) {

            val query =
                command
                    .removePrefix("search for ")
                    .removePrefix("search ")
                    .removePrefix("google search ")
                    .removePrefix("google ")
                    .trim()

            if (query.isNotBlank()) {
                googleSearch(query)
            }

            return
        }

        // -----------------------------------------------------
        // UNKNOWN COMMAND
        // -----------------------------------------------------

        googleSearch(command)
    }

    // =========================================================
    // SMART APP CONTROL
    // =========================================================

    private fun isAppOpenCommand(
        command: String
    ): Boolean {

        val c =
            command
                .lowercase(Locale.getDefault())
                .trim()

        return c.startsWith("open ") ||
                c.startsWith("launch ") ||
                c.startsWith("start ") ||
                c.startsWith("run ") ||
                c.startsWith("use ") ||
                c.startsWith("show ") ||
                c.startsWith("khol ") ||
                c.startsWith("kholo ") ||
                c.startsWith("chalao ") ||
                c.startsWith("chala ") ||
                c.contains(" kholo") ||
                c.contains(" open karo") ||
                c.contains(" launch karo")
    }

    private fun extractAppName(
        command: String
    ): String {

        var result =
            command
                .lowercase(Locale.getDefault())
                .trim()

        result =
            result.replace(
                Regex("^aurix[,:]?\\s*"),
                ""
            )

        result =
            result.replace(
                Regex(
                    "^(please\\s+)?(open|launch|start|run|use|show)\\s+"
                ),
                ""
            )

        result =
            result.replace(
                Regex(
                    "^(please\\s+)?(khol|kholo|chalao|chala)\\s+"
                ),
                ""
            )

        result =
            result.replace(
                Regex(
                    "\\s+(app|application|karo|kar\\s+do|please|ko)$"
                ),
                ""
            )

        result =
            result.replace(
                Regex(
                    "\\s+(khol|kholo|chalao|chala|open|launch|start|karo|kar\\s+do)$"
                ),
                ""
            )

        return result.trim()
    }

    private fun normalizeAppName(
        value: String
    ): String {

        return value
            .lowercase(Locale.getDefault())
            .replace(
                Regex("[^a-z0-9]"),
                ""
            )
            .trim()
    }

    private fun openInstalledApp(
        appName: String
    ): Boolean {

        val requestedName =
            appName.trim()

        if (requestedName.isBlank()) {

            speak(
                "Which app should I open?"
            )

            return true
        }

        val requested =
            normalizeAppName(
                requestedName
            )

        // =====================================================
        // KNOWN PACKAGES
        // =====================================================

        val knownPackages =
            mapOf(

                "whatsapp" to listOf(
                    "com.whatsapp",
                    "com.whatsapp.w4b"
                ),

                "instagram" to listOf(
                    "com.instagram.android"
                ),

                "gmail" to listOf(
                    "com.google.android.gm"
                ),

                "facebook" to listOf(
                    "com.facebook.katana"
                ),

                "telegram" to listOf(
                    "org.telegram.messenger"
                ),

                "snapchat" to listOf(
                    "com.snapchat.android"
                ),

                "spotify" to listOf(
                    "com.spotify.music"
                ),

                "netflix" to listOf(
                    "com.netflix.mediaclient"
                ),

                "amazon" to listOf(
                    "in.amazon.mShop.android.shopping"
                ),

                "flipkart" to listOf(
                    "com.flipkart.android"
                ),

                "paytm" to listOf(
                    "net.one97.paytm"
                ),

                "phonepe" to listOf(
                    "com.phonepe.app"
                ),

                "linkedin" to listOf(
                    "com.linkedin.android"
                ),

                "twitter" to listOf(
                    "com.twitter.android"
                ),

                "x" to listOf(
                    "com.twitter.android"
                ),

                "drive" to listOf(
                    "com.google.android.apps.docs"
                ),

                "googledrive" to listOf(
                    "com.google.android.apps.docs"
                ),

                "photos" to listOf(
                    "com.google.android.apps.photos"
                ),

                "googlephotos" to listOf(
                    "com.google.android.apps.photos"
                ),

                "youtube" to listOf(
                    "com.google.android.youtube"
                ),

                "chrome" to listOf(
                    "com.android.chrome"
                ),

                "maps" to listOf(
                    "com.google.android.apps.maps"
                )
            )

        val packages =
            knownPackages[requested]

        if (packages != null) {

            for (packageName in packages) {

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

                        launchIntent.addFlags(
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        )

                        startActivity(
                            launchIntent
                        )

                        speak(
                            "Opening $requestedName"
                        )

                        return true
                    }

                } catch (_: Exception) {
                }
            }
        }

        // =====================================================
        // DYNAMIC INSTALLED APP SEARCH
        // =====================================================

        try {

            val launcherIntent =
                Intent(
                    Intent.ACTION_MAIN
                ).apply {

                    addCategory(
                        Intent.CATEGORY_LAUNCHER
                    )
                }

            val apps =
                packageManager
                    .queryIntentActivities(
                        launcherIntent,
                        PackageManager.MATCH_ALL
                    )

            var bestActivity:
                    android.content.pm.ActivityInfo? =
                null

            var bestScore = 0

            for (resolveInfo in apps) {

                val activity =
                    resolveInfo.activityInfo
                        ?: continue

                val label =
                    try {

                        activity
                            .loadLabel(
                                packageManager
                            )
                            ?.toString()
                            ?: ""

                    } catch (_: Exception) {

                        ""
                    }

                if (label.isBlank()) {
                    continue
                }

                val normalizedLabel =
                    normalizeAppName(
                        label
                    )

                val packagePart =
                    normalizeAppName(
                        activity
                            .packageName
                            .substringAfterLast(".")
                    )

                // Exact label
                if (
                    normalizedLabel ==
                    requested
                ) {

                    bestActivity = activity
                    bestScore = 100
                    break
                }

                // Exact package part
                if (
                    packagePart ==
                    requested
                ) {

                    bestActivity = activity
                    bestScore = 95
                    break
                }

                val labelScore =
                    similarityScore(
                        requested,
                        normalizedLabel
                    )

                val packageScore =
                    similarityScore(
                        requested,
                        packagePart
                    )

                val score =
                    maxOf(
                        labelScore,
                        packageScore
                    )

                if (score > bestScore) {

                    bestScore = score
                    bestActivity = activity
                }
            }

            if (
                bestActivity != null &&
                bestScore >= 60
            ) {

                val launchIntent =
                    Intent(
                        Intent.ACTION_MAIN
                    ).apply {

                        addCategory(
                            Intent.CATEGORY_LAUNCHER
                        )

                        component =
                            android.content.ComponentName(
                                bestActivity.packageName,
                                bestActivity.name
                            )

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )

                        addFlags(
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        )
                    }

                try {

                    startActivity(
                        launchIntent
                    )

                    speak(
                        "Opening $requestedName"
                    )

                    return true

                } catch (_: Exception) {
                }
            }

        } catch (_: Exception) {
        }

        // =====================================================
        // DIRECT PACKAGE FALLBACK
        // =====================================================

        try {

            val directIntent =
                packageManager
                    .getLaunchIntentForPackage(
                        requestedName
                    )

            if (directIntent != null) {

                directIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                startActivity(
                    directIntent
                )

                speak(
                    "Opening $requestedName"
                )

                return true
            }

        } catch (_: Exception) {
        }

        speak(
            "I couldn't find $requestedName on your phone."
        )

        return true
    }

    private fun similarityScore(
        a: String,
        b: String
    ): Int {

        if (
            a.isBlank() ||
            b.isBlank()
        ) {
            return 0
        }

        if (a == b) {
            return 100
        }

        if (
            a.contains(b) ||
            b.contains(a)
        ) {
            return 85
        }

        val distance =
            levenshteinDistance(
                a,
                b
            )

        val maxLength =
            maxOf(
                a.length,
                b.length
            )

        if (maxLength == 0) {
            return 0
        }

        return (
            (
                1.0 -
                    distance.toDouble() /
                    maxLength
            ) * 100
        ).toInt()
    }

    private fun levenshteinDistance(
        a: String,
        b: String
    ): Int {

        val dp =
            Array(
                a.length + 1
            ) {
                IntArray(
                    b.length + 1
                )
            }

        for (i in 0..a.length) {
            dp[i][0] = i
        }

        for (j in 0..b.length) {
            dp[0][j] = j
        }

        for (i in 1..a.length) {

            for (j in 1..b.length) {

                val cost =
                    if (
                        a[i - 1] ==
                        b[j - 1]
                    ) {
                        0
                    } else {
                        1
                    }

                dp[i][j] =
                    minOf(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1,
                        dp[i - 1][j - 1] + cost
                    )
            }
        }

        return dp[a.length][b.length]
    }

    // =========================================================
    // NUMBER WORDS
    // =========================================================

    private fun normalizeNumberWords(
        input: String
    ): String {

        var text = input

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
                    Regex("\\b${Regex.escape(word)}\\b"),
                    number
                )
        }

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

        numbers.forEach { (word, number) ->

            text =
                text.replace(
                    Regex("\\b${Regex.escape(word)}\\b"),
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

                    alarmManager.setExactAndAllowWhileIdle(
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

            } catch (_: Exception) {
            }
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

            for (id in manager.cameraIdList) {

                val characteristics =
                    manager.getCameraCharacteristics(id)

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

            speak(
                if (enabled) {
                    "Flashlight turned on"
                } else {
                    "Flashlight turned off"
                }
            )

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

                    speak(
                        "Opening camera"
                    )

                    return
                }

            } catch (_: Exception) {
            }
        }

        val packages =
            arrayOf(
                "com.android.camera",
                "com.android.camera2",
                "com.miui.camera"
            )

        for (packageName in packages) {

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

                    speak(
                        "Opening camera"
                    )

                    return
                }

            } catch (_: Exception) {
            }
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
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/*"
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

            speak(
                "Opening gallery"
            )

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

        for (packageName in packages) {

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

                    speak(
                        "Opening music"
                    )

                    return
                }

            } catch (_: Exception) {
            }
        }

        try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://music.youtube.com"
                    )
                ).apply {

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
    // MEDIA CONTROL
    // =========================================================

    private fun controlMedia() {

        try {

            val audio =
                getSystemService(
                    Context.AUDIO_SERVICE
                ) as AudioManager

            val keyEvent =
                android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_DOWN,
                    android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                )

            audio.dispatchMediaKeyEvent(
                keyEvent
            )

            val up =
                android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_UP,
                    android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                )

            audio.dispatchMediaKeyEvent(
                up
            )

            speak(
                "Media control executed"
            )

        } catch (_: Exception) {

            speak(
                "I could not control media"
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

        for (packageName in packages) {

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

                    speak(
                        "Opening notes"
                    )

                    return
                }

            } catch (_: Exception) {
            }
        }

        speak(
            "Notes app is not available"
        )
    }

    // =========================================================
    // CALCULATOR
    // =========================================================

    private fun openCalculator() {

        val calculatorPackages =
            arrayOf(
                "com.miui.calculator",
                "com.android.calculator2",
                "com.google.android.calculator"
            )

        for (packageName in calculatorPackages) {

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

                    speak(
                        "Opening calculator"
                    )

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

            speak(
                "Opening calculator"
            )

            return

        } catch (_: Exception) {
        }

        speak(
            "Calculator is not available"
        )
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

            speak(
                "Opening YouTube"
            )

        } catch (_: Exception) {

            try {

                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://www.youtube.com"
                        )
                    ).apply {

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }

                startActivity(intent)

                speak(
                    "Opening YouTube"
                )

            } catch (_: Exception) {

                speak(
                    "YouTube is not available"
                )
            }
        }
    }

    private fun searchYouTube(
        query: String
    ) {

        try {

            val url =
                "https://www.youtube.com/results?search_query=" +
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

            speak(
                "Searching YouTube for $query"
            )

        } catch (_: Exception) {

            speak(
                "I could not search YouTube"
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

            speak(
                "Opening Chrome"
            )

        } catch (_: Exception) {

            try {

                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://www.google.com"
                        )
                    ).apply {

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }

                startActivity(intent)

                speak(
                    "Opening browser"
                )

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

            speak(
                "Opening Maps"
            )

        } catch (_: Exception) {

            try {

                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://maps.google.com"
                        )
                    ).apply {

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }

                startActivity(intent)

            } catch (_: Exception) {

                speak(
                    "Maps is not available"
                )
            }
        }
    }

    private fun searchMaps(
        query: String
    ) {

        try {

            val uri =
                Uri.parse(
                    "geo:0,0?q=" +
                        Uri.encode(query)
                )

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    uri
                ).apply {

                    setPackage(
                        "com.google.android.apps.maps"
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

            speak(
                "Searching Maps for $query"
            )

        } catch (_: Exception) {

            try {

                val uri =
                    Uri.parse(
                        "https://www.google.com/maps/search/?api=1&query=" +
                            Uri.encode(query)
                    )

                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        uri
                    ).apply {

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }

                startActivity(intent)

            } catch (_: Exception) {

                speak(
                    "I could not open Maps"
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

            speak(
                "Opening phone"
            )

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

            speak(
                "Opening settings"
            )

        } catch (_: Exception) {

            speak(
                "Settings is not available"
            )
        }
    }

    private fun openWifiSettings() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_WIFI_SETTINGS
                ).apply {

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

            speak(
                "Opening Wi-Fi settings"
            )

        } catch (_: Exception) {

            speak(
                "Wi-Fi settings are not available"
            )
        }
    }

    private fun openBluetoothSettings() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_BLUETOOTH_SETTINGS
                ).apply {

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

            speak(
                "Opening Bluetooth settings"
            )

        } catch (_: Exception) {

            speak(
                "Bluetooth settings are not available"
            )
        }
    }

    private fun openNotificationSettings() {

        try {

            val intent =
                Intent(
                    Settings.ACTION_APP_NOTIFICATION_SETTINGS
                ).apply {

                    putExtra(
                        Settings.EXTRA_APP_PACKAGE,
                        packageName
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

            speak(
                "Opening notification settings"
            )

        } catch (_: Exception) {

            speak(
                "Notification settings are not available"
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

        } catch (_: Exception) {

            speak(
                "I could not change the volume"
            )
        }
    }

    // =========================================================
    // BATTERY
    // =========================================================

    private fun tellBattery() {

        try {

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

        } catch (_: Exception) {

            speak(
                "I could not check the battery"
            )
        }
    }

    // =========================================================
    // GOOGLE SEARCH
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

            speak(
                "Searching for $query"
            )

        } catch (_: Exception) {

            speak(
                "I could not search that"
            )
        }
    }

    // =========================================================
    // HOME
    // =========================================================

    private fun isCloseCommand(command: String): Boolean {

    val c = command
        .lowercase(Locale.getDefault())
        .trim()
        .replace(Regex("\\s+"), " ")

    return c == "home" ||
            c == "go home" ||
            c == "going home" ||
            c == "go to home" ||
            c == "going to home" ||
            c == "home screen" ||
            c == "go home screen" ||
            c == "go to home screen" ||
            c == "going to home screen" ||
            c == "back to home" ||
            c == "return home" ||
            c == "return to home" ||
            c == "close" ||
            c == "exit" ||
            c == "quit" ||
            c == "close app" ||
            c == "close application" ||
            c == "band app" ||
            c == "aurix home"
    }
    private fun goHome() {

        try {

            val homeIntent =
                Intent(
                    Intent.ACTION_MAIN
                ).apply {

                    addCategory(
                        Intent.CATEGORY_HOME
                    )

                    addCategory(
                        Intent.CATEGORY_DEFAULT
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                }

            val resolved =
                packageManager.resolveActivity(
                    homeIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )

            if (
                resolved?.activityInfo != null
            ) {

                val explicitHome =
                    Intent(
                        Intent.ACTION_MAIN
                    ).apply {

                        addCategory(
                            Intent.CATEGORY_HOME
                        )

                        addCategory(
                            Intent.CATEGORY_DEFAULT
                        )

                        component =
                            android.content.ComponentName(
                                resolved.activityInfo.packageName,
                                resolved.activityInfo.name
                            )

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )

                        addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                        )
                    }

                startActivity(
                    explicitHome
                )

            } else {

                startActivity(
                    homeIntent
                )
            }

        } catch (_: Exception) {

            try {

                val fallback =
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

                startActivity(
                    fallback
                )

            } catch (_: Exception) {

                speak(
                    "Unable to go to home"
                )
            }
        }
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

            } catch (_: Exception) {
            }
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

        } catch (_: Exception) {
        }
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

                setPackage(
                    packageName
                )

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

        } catch (_: Exception) {
        }

        try {

            textToSpeech?.stop()
            textToSpeech?.shutdown()

        } catch (_: Exception) {
        }

        speechRecognizer = null
        textToSpeech = null

        super.onDestroy()
    }

    // =========================================================
    // BIND
    // =========================================================

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}
