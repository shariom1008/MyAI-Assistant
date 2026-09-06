package com.example.myaiassistant

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
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.text.SimpleDateFormat
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

    private val handler =
        Handler(Looper.getMainLooper())

    private var processing = false
    private var stopping = false

    // =========================================================
    // CREATE
    // =========================================================

    override fun onCreate() {

        super.onCreate()

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            createNotification("AURIX is ready")
        )

        isRunning = true
        stopping = false

        tts =
            TextToSpeech(
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

            channel.setShowBadge(false)

            getSystemService(
                NotificationManager::class.java
            ).createNotificationChannel(channel)
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

        stopIntent.action =
            ACTION_STOP

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

        getSystemService(
            NotificationManager::class.java
        ).notify(
            NOTIFICATION_ID,
            createNotification(message)
        )
    }

    // =========================================================
    // SPEECH
    // =========================================================

    private fun setupRecognizer() {

        if (
            !SpeechRecognizer.isRecognitionAvailable(this)
        ) {

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

                        handler.postDelayed(
                            {
                                startListening()
                            },
                            600
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
                            1000
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

        if (
            !isRunning ||
            stopping ||
            processing
        ) {
            return
        }

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

        var command =
            originalCommand
                .lowercase(Locale.getDefault())
                .trim()

        if (command.isEmpty()) {
            return
        }

        // Convert common spoken numbers into digits.
        command = normalizeNumberWords(command)

        // =====================================================
        // STOP AURIX
        // =====================================================

        if (
            command == "stop" ||
            command.contains("stop listening") ||
            command.contains("stop aurix") ||
            command.contains("deactivate aurix") ||
            command.contains("turn off aurix")
        ) {

            speak(
                "AURIX deactivated"
            )

            handler.postDelayed(
                {
                    stopAurix()
                },
                1200
            )

            return
        }

        // =====================================================
        // CLOSE APP
        // IMPORTANT: MUST COME BEFORE OPEN COMMANDS
        // =====================================================

        if (
            isCloseCommand(command)
        ) {

            goHome()

            return
        }

        // =====================================================
        // FLASHLIGHT
        // =====================================================

        if (
            command.contains("flashlight") ||
            command.contains("flash light") ||
            command.contains("torch")
        ) {

            if (
                command.contains("off") ||
                command.contains("disable") ||
                command.contains("turn off") ||
                command.contains("band")
            ) {

                setFlashlight(false)

            } else {

                setFlashlight(true)
            }

            return
        }

        // =====================================================
        // TIMER
        // =====================================================

        if (
            command.contains("timer") ||
            command.contains("set timer") ||
            command.contains("set a timer")
        ) {

            setTimer(command)

            return
        }

        // =====================================================
        // ALARM
        // =====================================================

        if (
            command.contains("alarm") ||
            command.contains("wake me")
        ) {

            setAlarm(command)

            return
        }

        // =====================================================
        // DATE
        // =====================================================

        if (
            command == "date" ||
            command.contains("what is the date") ||
            command.contains("what's the date") ||
            command.contains("today's date") ||
            command.contains("todays date") ||
            command.contains("what is today's date") ||
            command.contains("what's today's date") ||
            command.contains("tell me the date") ||
            command.contains("what date is today") ||
            command.contains("which date is today") ||
            command.contains("today date") ||
            command.contains("aaj ki date") ||
            command.contains("aaj ki tareekh") ||
            command.contains("aaj ki tarikh")
        ) {

            val date =
                SimpleDateFormat(
                    "EEEE, MMMM d, yyyy",
                    Locale.US
                ).format(Date())

            speak(
                "Today is $date"
            )

            return
        }

        // =====================================================
        // DAY
        // =====================================================

        if (
            command.contains("what day is today") ||
            command.contains("which day is today") ||
            command.contains("what day today") ||
            command.contains("today's day") ||
            command.contains("today day") ||
            command.contains("aaj ka din")
        ) {

            val day =
                SimpleDateFormat(
                    "EEEE",
                    Locale.US
                ).format(Date())

            speak(
                "Today is $day"
            )

            return
        }

        // =====================================================
        // TIME
        // TIMER IS ALREADY HANDLED ABOVE
        // =====================================================

        if (
            command == "time" ||
            command.contains("what time") ||
            command.contains("what is the time") ||
            command.contains("what's the time") ||
            command.contains("what time is it") ||
            command.contains("current time") ||
            command.contains("tell me the time") ||
            command.contains("time is it") ||
            command.contains("kitne baje") ||
            command.contains("kitna baje")
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

        // =====================================================
        // CAMERA
        // =====================================================

        if (
            command.contains("camera") ||
            command.contains("take a photo") ||
            command.contains("take photo") ||
            command.contains("open camera")
        ) {

            speak(
                "Opening camera"
            )

            openCamera()

            return
        }

        // =====================================================
        // GALLERY
        // =====================================================

        if (
            command.contains("gallery") ||
            command.contains("photos") ||
            command.contains("photo gallery") ||
            command.contains("open photos")
        ) {

            speak(
                "Opening gallery"
            )

            openGallery()

            return
        }

        // =====================================================
        // MUSIC
        // =====================================================

        if (
            command.contains("music") ||
            command.contains("song") ||
            command.contains("songs") ||
            command.contains("gaane")
        ) {

            speak(
                "Opening music"
            )

            openMusic()

            return
        }

        // =====================================================
        // NOTES
        // =====================================================

        if (
            command.contains("notes") ||
            command.contains("note")
        ) {

            speak(
                "Opening notes"
            )

            openNotes()

            return
        }

        // =====================================================
        // CALCULATOR
        // =====================================================

        if (
            command.contains("calculator") ||
            command.contains("calculate")
        ) {

            speak(
                "Opening calculator"
            )

            openCalculator()

            return
        }

        // =====================================================
        // YOUTUBE
        // =====================================================

        if (
            command.contains("youtube")
        ) {

            speak(
                "Opening YouTube"
            )

            openYouTube()

            return
        }

        // =====================================================
        // CHROME
        // =====================================================

        if (
            command.contains("chrome") ||
            command.contains("browser") ||
            command.contains("open browser")
        ) {

            speak(
                "Opening browser"
            )

            openChrome()

            return
        }

        // =====================================================
        // MAPS
        // =====================================================

        if (
            command.contains("maps") ||
            command.contains("google maps")
        ) {

            speak(
                "Opening maps"
            )

            openMaps()

            return
        }

        // =====================================================
        // PHONE
        // =====================================================

        if (
            command.contains("phone") ||
            command.contains("dialer")
        ) {

            speak(
                "Opening phone"
            )

            openPhone()

            return
        }

        // =====================================================
        // SETTINGS
        // =====================================================

        if (
            command.contains("settings")
        ) {

            speak(
                "Opening settings"
            )

            openSettings()

            return
        }

        // =====================================================
        // VOLUME
        // =====================================================

        if (
            command.contains("volume up") ||
            command.contains("increase volume") ||
            command.contains("volume increase")
        ) {

            changeVolume(true)

            speak(
                "Volume increased"
            )

            return
        }

        if (
            command.contains("volume down") ||
            command.contains("decrease volume") ||
            command.contains("volume decrease")
        ) {

            changeVolume(false)

            speak(
                "Volume decreased"
            )

            return
        }

        // =====================================================
        // BATTERY
        // =====================================================

        if (
            command.contains("battery") ||
            command.contains("battery level")
        ) {

            batteryStatus()

            return
        }

        // =====================================================
        // HELLO
        // =====================================================

        if (
            command == "hello" ||
            command == "hi" ||
            command.contains("hello aurix") ||
            command.contains("hi aurix") ||
            command.contains("hey aurix")
        ) {

            speak(
                "Hello. I am AURIX. How can I help you?"
            )

            return
        }

        // =====================================================
        // IDENTITY
        // =====================================================

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

        // =====================================================
        // GOOGLE SEARCH
        // =====================================================

        if (
            command.startsWith("search ") ||
            command.startsWith("google ") ||
            command.startsWith("search for ")
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

                speak(
                    "Searching Google"
                )

                openGoogleSearch(query)

                return
            }
        }

        // =====================================================
        // UNKNOWN COMMAND
        // =====================================================

        speak(
            "I will search that on Google"
        )

        openGoogleSearch(command)
    }

    // =========================================================
    // CLOSE COMMAND DETECTION
    // =========================================================

    private fun isCloseCommand(
        command: String
    ): Boolean {

        val closeWords =
            arrayOf(
                "close ",
                "exit ",
                "quit ",
                "band "
            )

        for (word in closeWords) {

            if (
                command.startsWith(word)
            ) {

                return true
            }
        }

        return command.contains("close calculator") ||
            command.contains("close chrome") ||
            command.contains("close youtube") ||
            command.contains("close gallery") ||
            command.contains("close music") ||
            command.contains("close camera") ||
            command.contains("close notes") ||
            command.contains("close settings") ||
            command.contains("exit calculator") ||
            command.contains("exit chrome") ||
            command.contains("exit youtube") ||
            command.contains("exit gallery") ||
            command.contains("exit music") ||
            command.contains("exit camera") ||
            command.contains("exit notes") ||
            command.contains("exit settings")
    }

    // =========================================================
    // NUMBER WORD NORMALIZER
    // =========================================================

    private fun normalizeNumberWords(
        input: String
    ): String {

        var text =
            input
                .lowercase(Locale.getDefault())
                .replace(
                    "-",
                    " "
                )

        val numbers =
            linkedMapOf(
                "sixty" to "60",
                "fifty nine" to "59",
                "fifty eight" to "58",
                "fifty seven" to "57",
                "fifty six" to "56",
                "fifty five" to "55",
                "fifty four" to "54",
                "fifty three" to "53",
                "fifty two" to "52",
                "fifty one" to "51",
                "fifty" to "50",
                "forty nine" to "49",
                "forty eight" to "48",
                "forty seven" to "47",
                "forty six" to "46",
                "forty five" to "45",
                "forty four" to "44",
                "forty three" to "43",
                "forty two" to "42",
                "forty one" to "41",
                "forty" to "40",
                "thirty nine" to "39",
                "thirty eight" to "38",
                "thirty seven" to "37",
                "thirty six" to "36",
                "thirty five" to "35",
                "thirty four" to "34",
                "thirty three" to "33",
                "thirty two" to "32",
                "thirty one" to "31",
                "thirty" to "30",
                "twenty nine" to "29",
                "twenty eight" to "28",
                "twenty seven" to "27",
                "twenty six" to "26",
                "twenty five" to "25",
                "twenty four" to "24",
                "twenty three" to "23",
                "twenty two" to "22",
                "twenty one" to "21",
                "twenty" to "20",
                "nineteen" to "19",
                "eighteen" to "18",
                "seventeen" to "17",
                "sixteen" to "16",
                "fifteen" to "15",
                "fourteen" to "14",
                "thirteen" to "13",
                "twelve" to "12",
                "eleven" to "11",
                "ten" to "10",
                "nine" to "9",
                "eight" to "8",
                "seven" to "7",
                "six" to "6",
                "five" to "5",
                "four" to "4",
                "three" to "3",
                "two" to "2",
                "one" to "1"
            )

        for (
            entry in numbers
        ) {

            text =
                text.replace(
                    Regex(
                        "\\b${Pattern.quote(entry.key)}\\b"
                    ),
                    entry.value
                )
        }

        return text
    }

    // =========================================================
    // HOME
    // =========================================================

    private fun goHome() {

        try {

            val intent =
                Intent(
                    Intent.ACTION_MAIN
                )

            intent.addCategory(
                Intent.CATEGORY_HOME
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

            speak(
                "Closing"
            )

        } catch (_: Exception) {

            speak(
                "I cannot close that app"
            )
        }
    }

    // =========================================================
    // FLASHLIGHT
    // =========================================================

    private fun setFlashlight(
        enabled: Boolean
    ) {

        try {

            if (
                Build.VERSION.SDK_INT <
                Build.VERSION_CODES.M
            ) {

                speak(
                    "Flashlight is not supported on this device"
                )

                return
            }

            val cameraManager =
                getSystemService(
                    Context.CAMERA_SERVICE
                ) as CameraManager

            var cameraId: String? = null

            for (
                id in cameraManager.cameraIdList
            ) {

                try {

                    val characteristics =
                        cameraManager.getCameraCharacteristics(
                            id
                        )

                    val hasFlash =
                        characteristics.get(
                            CameraCharacteristics.FLASH_INFO_AVAILABLE
                        ) == true

                    val facing =
                        characteristics.get(
                            CameraCharacteristics.LENS_FACING
                        )

                    if (
                        hasFlash &&
                        (
                            facing ==
                                CameraCharacteristics.LENS_FACING_BACK ||
                                facing == null
                        )
                    ) {

                        cameraId = id
                        break
                    }

                } catch (_: Exception) {
                }
            }

            if (
                cameraId == null
            ) {

                speak(
                    "Flashlight is not available"
                )

                return
            }

            cameraManager.setTorchMode(
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

        } catch (_: SecurityException) {

            speak(
                "Camera permission is required for flashlight"
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

                if (
                    launchIntent != null
                ) {

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

        try {

            val fallback =
                Intent(
                    "android.media.action.IMAGE_CAPTURE"
                )

            fallback.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(fallback)

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

            intent.setDataAndType(
                Uri.parse(
                    "content://media/external/images/media"
                ),
                "image/*"
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)

            return

        } catch (_: Exception) {
        }

        speak(
            "Gallery is not available"
        )
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
                    packageManager.getLaunchIntentForPackage(
                        packageName
                    )

                if (
                    intent != null
                ) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    startActivity(intent)

                    return
                }

            } catch (_: Exception) {
            }
        }

        speak(
            "Music player is not available"
        )
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

                if (
                    intent != null
                ) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    startActivity(intent)

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

                if (
                    intent != null
                ) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    startActivity(intent)

                    return
                }

            } catch (_: Exception) {
            }
        }

        speak(
            "Calculator is not available"
        )
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

            openUrl(
                "https://www.google.com"
            )
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

            audioManager.adjustVolume(
                if (increase)
                    AudioManager.ADJUST_RAISE
                else
                    AudioManager.ADJUST_LOWER,
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

        var totalSeconds = 0

        // HOURS
        val hourMatcher =
            Pattern.compile(
                "(\\d+)\\s*(hour|hours|hr|hrs)"
            ).matcher(command)

        if (hourMatcher.find()) {

            val hours =
                hourMatcher
                    .group(1)
                    ?.toIntOrNull()
                    ?: 0

            totalSeconds +=
                hours * 3600
        }

        // MINUTES
        val minuteMatcher =
            Pattern.compile(
                "(\\d+)\\s*(minute|minutes|min|mins)"
            ).matcher(command)

        if (minuteMatcher.find()) {

            val minutes =
                minuteMatcher
                    .group(1)
                    ?.toIntOrNull()
                    ?: 0

            totalSeconds +=
                minutes * 60
        }

        // SECONDS
        val secondMatcher =
            Pattern.compile(
                "(\\d+)\\s*(second|seconds|sec|secs)"
            ).matcher(command)

        if (secondMatcher.find()) {

            val seconds =
                secondMatcher
                    .group(1)
                    ?.toIntOrNull()
                    ?: 0

            totalSeconds += seconds
        }

        if (totalSeconds <= 0) {

            speak(
                "Please say a duration, for example, set timer for five minutes"
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
                totalSeconds
            )

            intent.putExtra(
                AlarmClock.EXTRA_MESSAGE,
                "AURIX Timer"
            )

            // IMPORTANT:
            // true = ask Clock app to set timer directly
            intent.putExtra(
                AlarmClock.EXTRA_SKIP_UI,
                true
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            if (
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                ).isEmpty()
            ) {

                speak(
                    "Timer app is not available"
                )

                return
            }

            startActivity(intent)

            speak(
                "Timer set for ${formatDuration(totalSeconds)}"
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

        val hours =
            seconds / 3600

        val minutes =
            (seconds % 3600) / 60

        val remainingSeconds =
            seconds % 60

        return when {

            hours > 0 ->
                if (
                    minutes > 0
                ) {
                    "$hours hours $minutes minutes"
                } else {
                    "$hours hours"
                }

            minutes > 0 &&
                remainingSeconds > 0 ->
                "$minutes minutes $remainingSeconds seconds"

            minutes > 0 ->
                "$minutes minutes"

            else ->
                "$remainingSeconds seconds"
        }
    }

    // =========================================================
    // ALARM
    // =========================================================

    private fun setAlarm(
        command: String
    ) {

        /*
         * Examples supported:
         *
         * set alarm for 7 pm
         * set alarm for 7:30 pm
         * set alarm for 07:30
         * set alarm for seven pm
         * set alarm for seven thirty pm
         */

        val pattern =
            Pattern.compile(
                "(\\d{1,2})(?:\\s*[:.]\\s*(\\d{1,2}))?\\s*(am|pm)?"
            )

        val matcher =
            pattern.matcher(command)

        var found = false
        var hour = 0
        var minute = 0
        var amPm: String? = null

        while (matcher.find()) {

            val possibleHour =
                matcher
                    .group(1)
                    ?.toIntOrNull()
                    ?: continue

            val possibleMinute =
                matcher
                    .group(2)
                    ?.toIntOrNull()
                    ?: 0

            val possibleAmPm =
                matcher
                    .group(3)
                    ?.lowercase()

            // Avoid treating random numbers as alarm time.
            if (
                possibleHour in 1..23 &&
                possibleMinute in 0..59
            ) {

                hour = possibleHour
                minute = possibleMinute
                amPm = possibleAmPm
                found = true
                break
            }
        }

        if (!found) {

            // Support "7 30 pm"
            val twoNumberPattern =
                Pattern.compile(
                    "(\\d{1,2})\\s+(\\d{1,2})\\s*(am|pm)"
                )

            val twoMatcher =
                twoNumberPattern.matcher(command)

            if (twoMatcher.find()) {

                hour =
                    twoMatcher
                        .group(1)
                        ?.toIntOrNull()
                        ?: 0

                minute =
                    twoMatcher
                        .group(2)
                        ?.toIntOrNull()
                        ?: 0

                amPm =
                    twoMatcher
                        .group(3)
                        ?.lowercase()

                found = true
            }
        }

        if (!found) {

            speak(
                "Please say the alarm time, for example, set alarm for seven PM"
            )

            return
        }

        try {

            if (
                amPm == "pm" &&
                hour < 12
            ) {
                hour += 12
            }

            if (
                amPm == "am" &&
                hour == 12
            ) {
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

            intent.putExtra(
                AlarmClock.EXTRA_SKIP_UI,
                true
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            if (
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                ).isEmpty()
            ) {

                speak(
                    "Alarm app is not available"
                )

                return
            }

            startActivity(intent)

            val displayHour =
                if (
                    hour % 12 == 0
                ) {
                    12
                } else {
                    hour % 12
                }

            val displayAmPm =
                if (hour >= 12) {
                    "PM"
                } else {
                    "AM"
                }

            speak(
                "Alarm set for $displayHour:${String.format(Locale.US, "%02d", minute)} $displayAmPm"
            )

        } catch (_: Exception) {

            speak(
                "I could not set the alarm"
            )
        }
    }

    // =========================================================
    // GOOGLE
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
    // TTS
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

        sendStatus(
            "PROCESSING"
        )

        updateNotification(
            text
        )

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
    // EVENTS
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
    // STOP
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

        handler.removeCallbacksAndMessages(
            null
        )

        sendStatus(
            "READY"
        )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.N
        ) {

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
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        isRunning = false
        stopping = true

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

        handler.removeCallbacksAndMessages(
            null
        )

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {

        return null
    }
}
