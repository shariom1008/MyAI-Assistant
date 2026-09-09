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

    private var currentUserCommand = ""
    private var currentResponseSent = false

    // =========================================================
    // CREATE
    // =========================================================

    override fun onCreate() {

        super.onCreate()

        serviceDestroyed = false
        isRunning = true
        restarting = false

        AurixMemoryBridge.initialize(this)

        AurixSkillEngine.addSkill(
            PhoneSkill(this)
        )

        AurixSkillEngine.addSkill(
            BluetoothAudioSkill(this)
        )

        createNotificationChannel()
        startForegroundNotification()

        textToSpeech =
            TextToSpeech(
                this,
                this
            )

        startListening()
    }

    // =========================================================
    // START COMMAND
    // =========================================================

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

    // =========================================================
    // STOP AURIX
    // =========================================================

    private fun stopAurix() {

        isRunning = false
        listening = false
        restarting = true

        handler.removeCallbacksAndMessages(
            null
        )

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
    // NOTIFICATION CHANNEL
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

    // =========================================================
    // FOREGROUND NOTIFICATION
    // =========================================================

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
                    .setContentTitle(
                        "AURIX"
                    )
                    .setContentText(
                        "AURIX voice assistant is active"
                    )
                    .setSmallIcon(
                        android.R.drawable.ic_btn_speak_now
                    )
                    .setOngoing(true)
                    .build()

            } else {

                Notification.Builder(
                    this
                )
                    .setContentTitle(
                        "AURIX"
                    )
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
    // SPEECH RECOGNITION
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

            // IMPORTANT:
            // SpeechRecognizer is created only once.
            // Do NOT destroy/recreate it on every listening cycle.

            if (speechRecognizer == null) {

                speechRecognizer =
                    SpeechRecognizer
                        .createSpeechRecognizer(
                            this
                        )

                speechRecognizer
                    ?.setRecognitionListener(
                        object :
                            RecognitionListener {

                            override fun
                                onReadyForSpeech(
                                    params: Bundle?
                                ) {

                                listening = true

                                sendStatus(
                                    "LISTENING"
                                )
                            }

                            override fun
                                onBeginningOfSpeech() {

                                sendStatus(
                                    "THINKING"
                                )
                            }

                            override fun
                                onRmsChanged(
                                    rmsdB: Float
                                ) {
                            }

                            override fun
                                onBufferReceived(
                                    buffer: ByteArray?
                                ) {
                            }

                            override fun
                                onEndOfSpeech() {
                            }

                            override fun
                                onError(
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

                            override fun
                                onResults(
                                    results: Bundle?
                                ) {

                                listening = false

                                val list =
                                    results
                                        ?.getStringArrayList(
                                            SpeechRecognizer
                                                .RESULTS_RECOGNITION
                                        )

                                val command =
                                    list
                                        ?.firstOrNull()
                                        ?.trim()
                                        ?.lowercase(
                                            Locale.getDefault()
                                        )

                                if (
                                    !command
                                        .isNullOrBlank()
                                ) {

                                    sendCommand(
                                        command
                                    )

                                    processCommand(
                                        command
                                    )
                                }

                                if (
                                    isRunning &&
                                    !serviceDestroyed
                                ) {

                                    restartListening()
                                }
                            }

                            override fun
                                onPartialResults(
                                    partialResults:
                                    Bundle?
                                ) {
                            }

                            override fun
                                onEvent(
                                    eventType: Int,
                                    params: Bundle?
                                ) {
                            }
                        }
                    )
            }

            val intent =
                Intent(
                    RecognizerIntent
                        .ACTION_RECOGNIZE_SPEECH
                ).apply {

                    putExtra(
                        RecognizerIntent
                            .EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent
                            .LANGUAGE_MODEL_FREE_FORM
                    )

                    putExtra(
                        RecognizerIntent
                            .EXTRA_LANGUAGE,
                        Locale.getDefault()
                    )

                    putExtra(
                        RecognizerIntent
                            .EXTRA_PARTIAL_RESULTS,
                        false
                    )

                    putExtra(
                        RecognizerIntent
                            .EXTRA_MAX_RESULTS,
                        3
                    )
                }

            speechRecognizer
                ?.startListening(
                    intent
                )

        } catch (_: Exception) {

            restartListening()
        }
    }

    // =========================================================
    // SAFE RESTART
    // =========================================================

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

        }, 1200)
    }

    // =========================================================
    // MAIN COMMAND ENGINE
    // =========================================================

    private fun processCommand(
        rawCommand: String
    ) {

        currentResponseSent = false

        var command =
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

        currentUserCommand =
            rawCommand.trim()

        AurixMemoryBridge.initialize(
            this
        )

        sendStatus(
            "THINKING"
        )

        // =====================================================
        // CONTEXT RESOLUTION
        // =====================================================

        when (
            val resolution =
                AurixContextResolver.resolve(
                    this,
                    command
                )
        ) {

            is AurixContextResolver.Resolution.MemoryStatement -> {

                AurixMemoryBridge.rememberThat(
                    this,
                    resolution.original
                )

                speakOnce(
                    "Got it. I'll remember that."
                )

                return
            }

            is AurixContextResolver.Resolution.DirectResponse -> {

                speakOnce(
                    resolution.response
                )

                return
            }

            is AurixContextResolver.Resolution.ClearMemory -> {

                if (resolution.all) {

                    AurixMemoryBridge.clearAll(
                        this
                    )

                    speakOnce(
                        "I've cleared my personal memory."
                    )

                } else {

                    val key =
                        resolution.key

                    if (key.isNullOrBlank()) {

                        speakOnce(
                            "Tell me what you want me to forget."
                        )

                    } else {

                        AurixMemoryBridge
                            .clearMemoryKey(
                                this,
                                key
                            )

                        speakOnce(
                            "Okay. I'll forget that."
                        )
                    }
                }

                return
            }

            is AurixContextResolver.Resolution.Command -> {

                command =
                    resolution.command
            }
        }

        // =====================================================
        // CONTEXTUAL PLAY
        // =====================================================

        if (
            command == "play it" ||
            command == "play that" ||
            command == "play this" ||
            command == "isko chalao" ||
            command == "ise chalao" ||
            command == "usko chalao"
        ) {

            val history =
                AurixContextEngine
                    .getRecentHistory(10)

            val previous =
                history
                    .asReversed()
                    .firstOrNull {

                        it.role == "user" &&
                            it.text
                                .lowercase()
                                .trim() != command
                    }
                    ?.text
                    ?.trim()

            if (
                !previous.isNullOrBlank()
            ) {

                val previousLower =
                    previous.lowercase()

                val target =
                    when {

                        previousLower.startsWith(
                            "search youtube "
                        ) ->
                            previous.substring(
                                "search youtube ".length
                            ).trim()

                        previousLower.startsWith(
                            "youtube search "
                        ) ->
                            previous.substring(
                                "youtube search ".length
                            ).trim()

                        previousLower.startsWith(
                            "youtube par "
                        ) ->
                            previous.substring(
                                "youtube par ".length
                            ).trim()

                        previousLower.startsWith(
                            "search "
                        ) ->
                            previous.substring(
                                "search ".length
                            ).trim()

                        else ->
                            null
                    }

                if (
                    !target.isNullOrBlank()
                ) {

                    command =
                        "play $target"
                }
            }
        }

        currentUserCommand =
            command

        // =====================================================
        // STOP
        // =====================================================

        if (
            command == "stop" ||
            command == "stop listening" ||
            command == "deactivate aurix" ||
            command == "aurix stop" ||
            command == "aurix deactivate"
        ) {

            speakOnce(
                "Stopping AURIX."
            )

            handler.postDelayed(
                {
                    stopAurix()
                },
                700
            )

            return
        }

        // =====================================================
        // HOME / CLOSE
        // =====================================================

        if (
            isCloseCommand(command)
        ) {

            goHome()
            return
        }

        // =====================================================
        // MEMORY
        // =====================================================

        if (
            command.contains(
                "what did i tell you"
            ) ||
            command.contains(
                "what do you remember"
            ) ||
            command.contains(
                "what do you know about me"
            ) ||
            command.contains(
                "what you know about me"
            ) ||
            command.contains(
                "tell me about myself"
            )
        ) {

            val memory =
                AurixMemoryBridge
                    .getPersonalMemory(
                        this
                    )

            if (
                memory.isBlank()
            ) {

                speakOnce(
                    "I don't have any personal memory about you yet."
                )

            } else {

                speakOnce(
                    memory
                )
            }

            return
        }

        if (
            command.contains(
                "clear memory"
            ) ||
            command.contains(
                "forget everything"
            ) ||
            command.contains(
                "forget all"
            ) ||
            command.contains(
                "delete memory"
            )
        ) {

            AurixMemoryBridge.clearAll(
                this
            )

            speakOnce(
                "All personal memory has been cleared."
            )

            return
        }

        // =====================================================
        // DIAGNOSTICS
        // =====================================================

        if (
            command.contains(
                "aurix status"
            ) ||
            command.contains(
                "aurix diagnostics"
            ) ||
            command.contains(
                "system status"
            ) ||
            command == "diagnostics"
        ) {

            sendStatus(
                "VERIFYING"
            )

            val battery =
                getBatteryLevel()

            val skills =
                AurixSkillEngine.skillCount()

            val memory =
                if (
                    AurixMemoryBridge.hasMemory()
                ) {
                    "available"
                } else {
                    "empty"
                }

            speakOnce(
                "AURIX is online. " +
                    "Voice is active. " +
                    "Memory is $memory. " +
                    "$skills skills are loaded. " +
                    "Battery is $battery percent."
            )

            return
        }

        // =====================================================
        // AGENT MODE
        // =====================================================

        if (
            isAgentCommand(command)
        ) {

            sendStatus(
                "EXECUTING"
            )

            val task =
                AurixAgentEngine
                    .createTask(command)

            val plan =
                AurixAgentEngine
                    .createPlan(task)

            executeAgentStep(
                plan,
                0
            )

            return
        }

        // =====================================================
        // FLASHLIGHT
        // =====================================================

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
            ) ||
            command.contains(
                "torch chalao"
            ) ||
            command.contains(
                "flashlight chalao"
            )
        ) {

            setFlashlight(true)
            return
        }

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
            ) ||
            command.contains(
                "torch band"
            ) ||
            command.contains(
                "flashlight band"
            )
        ) {

            setFlashlight(false)
            return
        }

        // =====================================================
        // TIMER
        // =====================================================

        if (
            command.contains("timer")
        ) {

            setAurixTimer(command)
            return
        }

        // =====================================================
        // ALARM
        // =====================================================

        if (
            command.contains("alarm")
        ) {

            setAurixAlarm(command)
            return
        }

        // =====================================================
        // DATE
        // =====================================================

        if (
            command.contains("date") ||
            command.contains(
                "today's date"
            ) ||
            command.contains(
                "today date"
            )
        ) {

            val date =
                SimpleDateFormat(
                    "EEEE, dd MMMM yyyy",
                    Locale.getDefault()
                ).format(
                    Date()
                )

            speakOnce(
                "Today is $date."
            )

            return
        }

        // =====================================================
        // DAY
        // =====================================================

        if (
            command == "day" ||
            command.contains(
                "what day"
            ) ||
            command.contains(
                "which day"
            )
        ) {

            val day =
                SimpleDateFormat(
                    "EEEE",
                    Locale.getDefault()
                ).format(
                    Date()
                )

            speakOnce(
                "Today is $day."
            )

            return
        }

        // =====================================================
        // TIME
        // =====================================================

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
            ) ||
            command.contains(
                "time kya hai"
            )
        ) {

            val time =
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(
                    Date()
                )

            speakOnce(
                "The time is $time."
            )

            return
        }

        // =====================================================
        // CAMERA
        // =====================================================

        if (
            command.contains(
                "camera"
            ) ||
            command.contains(
                "take a photo"
            ) ||
            command.contains(
                "take photo"
            )
        ) {

            openCamera()
            return
        }

        // =====================================================
        // GALLERY
        // =====================================================

        if (
            command.contains(
                "gallery"
            ) ||
            command.contains(
                "photos"
            ) ||
            command.contains(
                "photo gallery"
            )
        ) {

            openGallery()
            return
        }

        // =====================================================
        // MUSIC
        // =====================================================

        if (
            command == "music" ||
            command.contains(
                "open music"
            ) ||
            command.contains(
                "music app"
            )
        ) {

            openMusic()
            return
        }

        // =====================================================
        // NOTES
        // =====================================================

        if (
            command.contains(
                "notes"
            ) ||
            command.contains(
                "note app"
            )
        ) {

            openNotes()
            return
        }

        // =====================================================
        // CALCULATOR
        // =====================================================

        if (
            command.contains(
                "calculator"
            ) ||
            command.contains(
                "calculate"
            )
        ) {

            openCalculator()
            return
        }

        // =====================================================
        // YOUTUBE PLAY
        // =====================================================

        if (
            command.startsWith("play ")
        ) {

            val target =
                command
                    .removePrefix(
                        "play "
                    )
                    .trim()

            if (
                target.isNotBlank()
            ) {

                searchYouTube(
                    target
                )

            } else {

                openYouTube()
            }

            return
        }

        // =====================================================
        // YOUTUBE SEARCH
        // =====================================================

        if (
            command.startsWith(
                "search youtube"
            ) ||
            command.startsWith(
                "youtube search"
            ) ||
            command.startsWith(
                "youtube par"
            )
        ) {

            val query =
                when {

                    command.startsWith(
                        "search youtube"
                    ) ->
                        command.removePrefix(
                            "search youtube"
                        )

                    command.startsWith(
                        "youtube search"
                    ) ->
                        command.removePrefix(
                            "youtube search"
                        )

                    else ->
                        command.removePrefix(
                            "youtube par"
                        )
                }.trim()

            if (
                query.isNotBlank()
            ) {

                searchYouTube(
                    query
                )

            } else {

                openYouTube()
            }

            return
        }

        // =====================================================
        // OPEN YOUTUBE
        // =====================================================

        if (
            command == "youtube" ||
            command == "open youtube" ||
            command == "launch youtube"
        ) {

            openYouTube()
            return
        }

        // =====================================================
        // MAPS
        // =====================================================

        if (
            command.startsWith(
                "search maps"
            ) ||
            command.startsWith(
                "maps search"
            ) ||
            command.startsWith(
                "navigate to"
            )
        ) {

            val query =
                command
                    .replaceFirst(
                        "search maps",
                        ""
                    )
                    .replaceFirst(
                        "maps search",
                        ""
                    )
                    .replaceFirst(
                        "navigate to",
                        ""
                    )
                    .trim()

            if (
                query.isNotBlank()
            ) {

                searchMaps(
                    query
                )

            } else {

                openMaps()
            }

            return
        }

        if (
            command == "maps" ||
            command == "open maps" ||
            command == "google maps"
        ) {

            openMaps()
            return
        }

        // =====================================================
        // CHROME
        // =====================================================

        if (
            command == "chrome" ||
            command == "open chrome" ||
            command == "browser" ||
            command == "open browser"
        ) {

            openChrome()
            return
        }

        // =====================================================
        // PHONE
        // =====================================================

        if (
            command == "phone" ||
            command == "open phone" ||
            command == "dialer" ||
            command == "open dialer"
        ) {

            openPhone()
            return
        }

        // =====================================================
        // SETTINGS
        // =====================================================

        if (
            command == "settings" ||
            command == "open settings" ||
            command == "phone settings"
        ) {

            openSettings()
            return
        }

        // =====================================================
        // WIFI
        // =====================================================

        if (
            command.contains(
                "wifi"
            ) ||
            command.contains(
                "wi fi"
            )
        ) {

            openWifiSettings()
            return
        }

        // =====================================================
        // VOLUME
        // =====================================================

        if (
            command.contains(
                "volume up"
            ) ||
            command.contains(
                "increase volume"
            ) ||
            command.contains(
                "volume badhao"
            )
        ) {

            changeVolume(true)
            return
        }

        if (
            command.contains(
                "volume down"
            ) ||
            command.contains(
                "decrease volume"
            ) ||
            command.contains(
                "volume kam"
            )
        ) {

            changeVolume(false)
            return
        }

        // =====================================================
        // MEDIA
        // =====================================================

        if (
            command == "play" ||
            command == "pause" ||
            command == "resume" ||
            command.contains(
                "play music"
            ) ||
            command.contains(
                "pause music"
            ) ||
            command.contains(
                "resume music"
            )
        ) {

            controlMedia()
            return
        }

        // =====================================================
        // BATTERY
        // =====================================================

        if (
            command.contains(
                "battery"
            )
        ) {

            tellBattery()
            return
        }

        // =====================================================
        // GREETING
        // =====================================================

        if (
            command == "hello" ||
            command == "hi" ||
            command == "hey aurix" ||
            command == "hello aurix" ||
            command == "hi aurix"
        ) {

            speakOnce(
                "Hello Boss. Main AURIX hoon. Batao, kya help chahiye?"
            )

            return
        }

        // =====================================================
        // IDENTITY
        // =====================================================

        if (
            command.contains(
                "who are you"
            ) ||
            command.contains(
                "your name"
            ) ||
            command.contains(
                "what are you"
            )
        ) {

            speakOnce(
                "Main AURIX hoon, aapka personal AI assistant."
            )

            return
        }

        // =====================================================
        // INSTALLED APP
        // =====================================================

        if (
            isAppOpenCommand(command)
        ) {

            val appName =
                extractAppName(
                    command
                )

            openInstalledApp(
                appName
            )

            return
        }

        // =====================================================
        // GOOGLE SEARCH
        // =====================================================

        if (
            command.startsWith(
                "search "
            ) ||
            command.startsWith(
                "google "
            ) ||
            command.startsWith(
                "search for "
            ) ||
            command.startsWith(
                "google search "
            )
        ) {

            val query =
                command
                    .removePrefix(
                        "search for "
                    )
                    .removePrefix(
                        "search "
                    )
                    .removePrefix(
                        "google search "
                    )
                    .removePrefix(
                        "google "
                    )
                    .trim()

            if (
                query.isNotBlank()
            ) {

                googleSearch(
                    query
                )
            }

            return
        }

        // =====================================================
        // NORMAL AURIX ROUTER
        // =====================================================

        val aurixRouterResponse =
            AurixCommandRouter.route(
                command
            )

        if (
            aurixRouterResponse.isNotBlank() &&
            !aurixRouterResponse.startsWith(
                "I understood:"
            )
        ) {

            speakOnce(
                aurixRouterResponse
            )

            return
        }

        // =====================================================
        // FINAL SEARCH FALLBACK
        // =====================================================

        googleSearch(
            command
        )
    }

    // =========================================================
    // AGENT
    // =========================================================

    private fun isAgentCommand(
        command: String
    ): Boolean {

        val c =
            command
                .lowercase(
                    Locale.getDefault()
                )
                .trim()

        val multipleActions =
            c.contains(" and ") ||
                c.contains(" then ") ||
                c.contains("after that") ||
                c.contains("and then")

        if (!multipleActions) {
            return false
        }

        return c.contains("open") ||
            c.contains("launch") ||
            c.contains("start") ||
            c.contains("play") ||
            c.contains("bluetooth") ||
            c.contains("settings") ||
            c.contains("phone") ||
            c.contains("youtube")
    }

    private fun executeAgentStep(
        plan: AurixAgentEngine.AgentPlan,
        index: Int
    ) {

        if (
            index < 0 ||
            index >= plan.steps.size
        ) {

            speakOnce(
                "Agent completed all planned steps."
            )

            return
        }

        val step =
            plan.steps[index]

        sendStatus(
            "EXECUTING: ${step.description}"
        )

        val response =
            executeLocalCommand(
                step.command
            )

        if (
            response.isBlank() ||
            response.startsWith(
                "I understood:"
            )
        ) {

            sendStatus(
                "STEP FAILED: ${step.description}"
            )

        } else {

            sendStatus(
                "STEP COMPLETE: ${step.description}"
            )
        }

        val next =
            index + 1

        if (
            next >= plan.steps.size
        ) {

            handler.postDelayed(
                {

                    sendStatus(
                        "VERIFYING"
                    )

                    speakOnce(
                        "Agent completed all planned steps."
                    )
                },
                500
            )

            return
        }

        handler.postDelayed(
            {

                executeAgentStep(
                    plan,
                    next
                )

            },
            1500
        )
    }

    private fun executeLocalCommand(
        command: String
    ): String {

        val c =
            command
                .lowercase(
                    Locale.getDefault()
                )
                .trim()

        return try {

            when {

                c == "open youtube" ->
                    openYouTubeForAgent()

                c == "open phone" ||
                    c == "open dialer" ->
                    openPhoneForAgent()

                c == "open settings" ->
                    openSettingsForAgent()

                c.contains(
                    "bluetooth"
                ) ->
                    AurixSkillEngine.process(
                        c
                    )

                else ->
                    AurixCommandRouter.route(
                        c
                    )
            }

        } catch (_: Exception) {

            "I couldn't execute this step."
        }
    }

    private fun openYouTubeForAgent(): String {

        return try {

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://www.youtube.com"
                    )
                ).apply {

                    setPackage(
                        "com.google.android.youtube"
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

            "YouTube opened."

        } catch (_: Exception) {

            "I couldn't open YouTube."
        }
    }

    private fun openPhoneForAgent(): String {

        return try {

            val intent =
                Intent(
                    Intent.ACTION_DIAL
                ).apply {

                    data =
                        Uri.parse(
                            "tel:"
                        )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

            "Phone opened."

        } catch (_: Exception) {

            "I couldn't open phone."
        }
    }

    private fun openSettingsForAgent(): String {

        return try {

            val intent =
                Intent(
                    Settings.ACTION_SETTINGS
                ).apply {

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(intent)

            "Settings opened."

        } catch (_: Exception) {

            "I couldn't open settings."
        }
    }

    // =========================================================
    // APP OPENING
    // =========================================================

    private fun isAppOpenCommand(
        command: String
    ): Boolean {

        val c =
            command
                .lowercase(
                    Locale.getDefault()
                )
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
                .lowercase(
                    Locale.getDefault()
                )
                .trim()

        result =
            result.replace(
                Regex(
                    "^aurix[,:]?\\s*"
                ),
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
            .lowercase(
                Locale.getDefault()
            )
            .replace(
                Regex(
                    "[^a-z0-9]"
                ),
                ""
            )
            .trim()
    }

    private fun openInstalledApp(
        appName: String
    ): Boolean {

        val requestedName =
            appName.trim()

        if (
            requestedName.isBlank()
        ) {

            speakOnce(
                "Which app should I open?"
            )

            return true
        }

        val requested =
            normalizeAppName(
                requestedName
            )

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

        if (
            packages != null
        ) {

            for (
                packageName in packages
            ) {

                try {

                    val launchIntent =
                        packageManager
                            .getLaunchIntentForPackage(
                                packageName
                            )

                    if (
                        launchIntent != null
                    ) {

                        launchIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )

                        launchIntent.addFlags(
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        )

                        startActivity(
                            launchIntent
                        )

                        speakOnce(
                            "Opening $requestedName."
                        )

                        return true
                    }

                } catch (_: Exception) {
                }
            }
        }

        // =====================================================
        // GENERIC APP DISCOVERY
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

            var bestScore =
                0

            for (
                resolveInfo in apps
            ) {

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

                if (
                    label.isBlank()
                ) {
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

                if (
                    normalizedLabel ==
                    requested
                ) {

                    bestActivity =
                        activity

                    bestScore =
                        100

                    break
                }

                if (
                    packagePart ==
                    requested
                ) {

                    bestActivity =
                        activity

                    bestScore =
                        95

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

                if (
                    score > bestScore
                ) {

                    bestScore =
                        score

                    bestActivity =
                        activity
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

                startActivity(
                    launchIntent
                )

                speakOnce(
                    "Opening $requestedName."
                )

                return true
            }

        } catch (_: Exception) {
        }

        speakOnce(
            "I couldn't find $requestedName on your phone."
        )

        return true
    }

    // =========================================================
    // SIMILARITY
    // =========================================================

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

        if (
            a == b
        ) {
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

        if (
            maxLength == 0
        ) {
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

        for (
            i in 0..a.length
        ) {

            dp[i][0] =
                i
        }

        for (
            j in 0..b.length
        ) {

            dp[0][j] =
                j
        }

        for (
            i in 1..a.length
        ) {

            for (
                j in 1..b.length
            ) {

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
    // NUMBER NORMALIZATION
    // =========================================================

    private fun normalizeNumberWords(
        input: String
    ): String {

        var text =
            input

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

        compounds.forEach {
            (word, number) ->

            text =
                text.replace(
                    Regex(
                        "\\b${Regex.escape(word)}\\b"
                    ),
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

        numbers.forEach {
            (word, number) ->

            text =
                text.replace(
                    Regex(
                        "\\b${Regex.escape(word)}\\b"
                    ),
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

        var seconds =
            0L

        val hour =
            Pattern.compile(
                "(\\d+)\\s*(hour|hours|hr|hrs)"
            ).matcher(
                command
            )

        if (
            hour.find()
        ) {

            seconds +=
                hour.group(1)!!
                    .toLong() * 3600L
        }

        val minute =
            Pattern.compile(
                "(\\d+)\\s*(minute|minutes|min|mins)"
            ).matcher(
                command
            )

        if (
            minute.find()
        ) {

            seconds +=
                minute.group(1)!!
                    .toLong() * 60L
        }

        val second =
            Pattern.compile(
                "(\\d+)\\s*(second|seconds|sec|secs)"
            ).matcher(
                command
            )

        if (
            second.find()
        ) {

            seconds +=
                second.group(1)!!
                    .toLong()
        }

        if (
            seconds == 0L
        ) {

            val number =
                Pattern.compile(
                    "(?:timer|for)\\s+(\\d+)"
                ).matcher(
                    command
                )

            if (
                number.find()
            ) {

                val value =
                    number.group(1)!!
                        .toLong()

                seconds =
                    if (
                        command.contains(
                            "second"
                        ) ||
                        command.contains(
                            "sec"
                        )
                    ) {

                        value

                    } else {

                        value * 60L
                    }
            }
        }

        if (
            seconds <= 0L
        ) {

            speakOnce(
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

        speakOnce(
            message
        )
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
            ).matcher(
                command
            )

        if (
            !matcher.find()
        ) {

            speakOnce(
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

            speakOnce(
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
            ).format(
                calendar.time
            )

        speakOnce(
            "Alarm set for $formatted."
        )
    }

    // =========================================================
    // ALERT
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
            if (
                type == "timer"
            ) {
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

                } catch (
                    _: SecurityException
                ) {

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

            var cameraId: String? =
                null

            for (
                id in manager.cameraIdList
            ) {

                val characteristics =
                    manager
                        .getCameraCharacteristics(
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

                    cameraId =
                        id

                    break
                }
            }

            if (
                cameraId == null
            ) {

                speakOnce(
                    "Flashlight is not available."
                )

                return
            }

            manager.setTorchMode(
                cameraId,
                enabled
            )

            speakOnce(
                if (enabled) {
                    "Flashlight turned on."
                } else {
                    "Flashlight turned off."
                }
            )

        } catch (_: Exception) {

            speakOnce(
                "I could not control the flashlight."
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
                    MediaStore
                        .ACTION_IMAGE_CAPTURE
                ),
                Intent(
                    "android.media.action.IMAGE_CAPTURE"
                )
            )

        for (
            intent in intents
        ) {

            try {

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                if (
                    packageManager
                        .queryIntentActivities(
                            intent,
                            PackageManager
                                .MATCH_DEFAULT_ONLY
                        )
                        .isNotEmpty()
                ) {

                    startActivity(
                        intent
                    )

                    speakOnce(
                        "Opening camera."
                    )

                    return
                }

            } catch (_: Exception) {
            }
        }

        speakOnce(
            "Camera is not available."
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

            startActivity(
                intent
            )

            speakOnce(
                "Opening gallery."
            )

        } catch (_: Exception) {

            speakOnce(
                "Gallery is not available."
            )
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

                if (
                    intent != null
                ) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    startActivity(
                        intent
                    )

                    speakOnce(
                        "Opening music."
                    )

                    return
                }

            } catch (_: Exception) {
            }
        }

        speakOnce(
            "Music app is not available."
        )
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

                if (
                    intent != null
                ) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    startActivity(
                        intent
                    )

                    speakOnce(
                        "Opening notes."
                    )

                    return
                }

            } catch (_: Exception) {
            }
        }

        speakOnce(
            "Notes app is not available."
        )
    }

    // =========================================================
    // CALCULATOR
    // =========================================================

    private fun openCalculator() {

        val packages =
            arrayOf(
                "com.miui.calculator",
                "com.android.calculator2",
                "com.google.android.calculator"
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

                if (
                    intent != null
                ) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    startActivity(
                        intent
                    )

                    speakOnce(
                        "Opening calculator."
                    )

                    return
                }

            } catch (_: Exception) {
            }
        }

        speakOnce(
            "Calculator is not available."
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
                ).apply {

                    setPackage(
                        "com.google.android.youtube"
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(
                intent
            )

            speakOnce(
                "Opening YouTube."
            )

        } catch (_: Exception) {

            speakOnce(
                "YouTube is not available."
            )
        }
    }

    private fun searchYouTube(
        query: String
    ) {

        if (
            query.isBlank()
        ) {

            openYouTube()
            return
        }

        val url =
            "https://www.youtube.com/results?search_query=" +
                Uri.encode(
                    query
                )

        try {

            val youtubeIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).apply {

                    setPackage(
                        "com.google.android.youtube"
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(
                youtubeIntent
            )

            speakOnce(
                "Searching YouTube for $query."
            )

        } catch (_: Exception) {

            try {

                val browserIntent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    ).apply {

                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }

                startActivity(
                    browserIntent
                )

                speakOnce(
                    "Opening YouTube search for $query."
                )

            } catch (_: Exception) {

                speakOnce(
                    "I could not open YouTube."
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
                ).apply {

                    setPackage(
                        "com.android.chrome"
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(
                intent
            )

            speakOnce(
                "Opening Chrome."
            )

        } catch (_: Exception) {

            speakOnce(
                "Browser is not available."
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
                ).apply {

                    setPackage(
                        "com.google.android.apps.maps"
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(
                intent
            )

            speakOnce(
                "Opening Maps."
            )

        } catch (_: Exception) {

            speakOnce(
                "Maps is not available."
            )
        }
    }

    private fun searchMaps(
        query: String
    ) {

        try {

            val uri =
                Uri.parse(
                    "geo:0,0?q=" +
                        Uri.encode(
                            query
                        )
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

            startActivity(
                intent
            )

            speakOnce(
                "Searching Maps for $query."
            )

        } catch (_: Exception) {

            speakOnce(
                "I could not open Maps."
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
                ).apply {

                    data =
                        Uri.parse(
                            "tel:"
                        )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(
                intent
            )

            speakOnce(
                "Opening phone."
            )

        } catch (_: Exception) {

            speakOnce(
                "Phone app is not available."
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

            startActivity(
                intent
            )

            speakOnce(
                "Opening settings."
            )

        } catch (_: Exception) {

            speakOnce(
                "Settings is not available."
            )
        }
    }

    // =========================================================
    // WIFI
    // =========================================================

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

            startActivity(
                intent
            )

            speakOnce(
                "Opening Wi-Fi settings."
            )

        } catch (_: Exception) {

            speakOnce(
                "Wi-Fi settings are not available."
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
                if (increase)
                    AudioManager.ADJUST_RAISE
                else
                    AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )

            speakOnce(
                if (increase)
                    "Volume increased."
                else
                    "Volume decreased."
            )

        } catch (_: Exception) {

            speakOnce(
                "I could not change the volume."
            )
        }
    }

    // =========================================================
    // MEDIA
    // =========================================================

    private fun controlMedia() {

        try {

            val audio =
                getSystemService(
                    Context.AUDIO_SERVICE
                ) as AudioManager

            val down =
                android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_DOWN,
                    android.view.KeyEvent
                        .KEYCODE_MEDIA_PLAY_PAUSE
                )

            val up =
                android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_UP,
                    android.view.KeyEvent
                        .KEYCODE_MEDIA_PLAY_PAUSE
                )

            audio.dispatchMediaKeyEvent(
                down
            )

            audio.dispatchMediaKeyEvent(
                up
            )

            speakOnce(
                "Media control executed."
            )

        } catch (_: Exception) {

            speakOnce(
                "I could not control media."
            )
        }
    }

    // =========================================================
    // BATTERY
    // =========================================================

    private fun getBatteryLevel(): Int {

        return try {

            val manager =
                getSystemService(
                    Context.BATTERY_SERVICE
                ) as BatteryManager

            manager.getIntProperty(
                BatteryManager
                    .BATTERY_PROPERTY_CAPACITY
            )

        } catch (_: Exception) {

            -1
        }
    }

    private fun tellBattery() {

        val level =
            getBatteryLevel()

        if (
            level >= 0
        ) {

            speakOnce(
                "Battery is at $level percent."
            )

        } else {

            speakOnce(
                "I could not check the battery."
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
                    Uri.encode(
                        query
                    )

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).apply {

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            startActivity(
                intent
            )

            speakOnce(
                "Searching for $query."
            )

        } catch (_: Exception) {

            speakOnce(
                "I could not search that."
            )
        }
    }

    // =========================================================
    // HOME / CLOSE
    // =========================================================

    private fun isCloseCommand(
        command: String
    ): Boolean {

        val c =
            command
                .lowercase(
                    Locale.getDefault()
                )
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        return c == "home" ||
            c == "go home" ||
            c == "going home" ||
            c == "go to home" ||
            c == "going to home" ||
            c == "home screen" ||
            c == "go home screen" ||
            c == "go to home screen" ||
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

        sendStatus(
            "EXECUTING"
        )

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

            startActivity(
                intent
            )

        } catch (_: Exception) {

            speakOnce(
                "Unable to go to home screen."
            )
        }
    }
// =========================================================
// TTS INIT
// =========================================================

override fun onInit(
    status: Int
) {

    if (
        status != TextToSpeech.SUCCESS
    ) {
        return
    }

    val tts =
        textToSpeech
            ?: return

    try {

        // -------------------------------------------------
        // HINDI INDIA
        // -------------------------------------------------

        val hindiLocale =
            Locale(
                "hi",
                "IN"
            )

        val languageResult =
            tts.setLanguage(
                hindiLocale
            )

        // -------------------------------------------------
        // MALE HINDI VOICE
        // -------------------------------------------------

        val voices =
            tts.voices
                ?.filter {

                    it.locale.language == "hi" &&
                        !it.isNetworkConnectionRequired
                }
                ?: emptyList()

        val maleVoice =
            voices.firstOrNull {

                val name =
                    it.name.lowercase(
                        Locale.getDefault()
                    )

                name.contains("male") ||
                    name.contains("man") ||
                    name.contains("hemant") ||
                    name.contains("ravi") ||
                    name.contains("amit") ||
                    name.contains("hindi")
            }

        if (
            maleVoice != null
        ) {

            tts.voice =
                maleVoice
        }

        // -------------------------------------------------
        // FALLBACK HINDI
        // -------------------------------------------------

        if (
            languageResult ==
            TextToSpeech.LANG_MISSING_DATA ||
            languageResult ==
            TextToSpeech.LANG_NOT_SUPPORTED
        ) {

            // Fallback to default Hindi/India voice
            tts.language =
                Locale(
                    "hi",
                    "IN"
                )
        }

        // -------------------------------------------------
        // AURIX VOICE STYLE
        // -------------------------------------------------

        tts.setSpeechRate(
            0.88f
        )

        tts.setPitch(
            0.82f
        )

        // -------------------------------------------------
        // GREETING
        // -------------------------------------------------

        speakAurixGreeting()

    } catch (_: Exception) {
    }
}

    // =========================================================
    // AURIX RESPONSE LOCALIZATION
    // =========================================================

    private fun aurixResponse(
        text: String
    ): String {

        val t =
            text.trim()

        if (
            t.isBlank()
        ) {
            return t
        }

        return when {

            // -------------------------------------------------
            // APP OPENING
            // -------------------------------------------------

            t == "Opening YouTube." ->
                "Sure Boss, YouTube khol raha hoon."

            t == "Opening camera." ->
                "Boss, camera open kar raha hoon."

            t == "Opening gallery." ->
                "Boss, gallery khol raha hoon."

            t == "Opening music." ->
                "Boss, music app khol raha hoon."

            t == "Opening notes." ->
                "Boss, notes khol raha hoon."

            t == "Opening calculator." ->
                "Boss, calculator khol raha hoon."

            t == "Opening Chrome." ->
                "Boss, Chrome khol raha hoon."

            t == "Opening Maps." ->
                "Boss, Maps khol raha hoon."

            t == "Opening phone." ->
                "Boss, phone open kar raha hoon."

            t == "Opening settings." ->
                "Boss, settings khol raha hoon."

            t == "Opening Wi-Fi settings." ->
                "Boss, Wi-Fi settings khol raha hoon."

            // -------------------------------------------------
            // VOLUME / MEDIA
            // -------------------------------------------------

            t == "Volume increased." ->
                "Sure Boss, volume badha diya."

            t == "Volume decreased." ->
                "Sure Boss, volume kam kar diya."

            t == "Media control executed." ->
                "Done Boss, media control kar diya."

            // -------------------------------------------------
            // FLASHLIGHT
            // -------------------------------------------------

            t == "Flashlight turned on." ->
                "Sure Boss, flashlight on kar di."

            t == "Flashlight turned off." ->
                "Boss, flashlight off kar di."

            t == "Flashlight is not available." ->
                "Sorry Boss, flashlight available nahi hai."

            // -------------------------------------------------
            // DEVICE ERRORS
            // -------------------------------------------------

            t == "Camera is not available." ->
                "Sorry Boss, camera available nahi hai."

            t == "Gallery is not available." ->
                "Sorry Boss, gallery available nahi hai."

            t == "Music app is not available." ->
                "Sorry Boss, music app nahi mili."

            t == "Notes app is not available." ->
                "Sorry Boss, notes app nahi mili."

            t == "Calculator is not available." ->
                "Sorry Boss, calculator nahi mila."

            t == "YouTube is not available." ->
                "Sorry Boss, YouTube available nahi hai."

            t == "Browser is not available." ->
                "Sorry Boss, browser available nahi hai."

            t == "Maps is not available." ->
                "Sorry Boss, Maps available nahi hai."

            t == "Phone app is not available." ->
                "Sorry Boss, phone app available nahi hai."

            t == "Settings is not available." ->
                "Sorry Boss, settings open nahi ho paayi."

            t == "Wi-Fi settings are not available." ->
                "Sorry Boss, Wi-Fi settings available nahi hain."

            // -------------------------------------------------
            // CONTROL ERRORS
            // -------------------------------------------------

            t == "I could not control the flashlight." ->
                "Sorry Boss, flashlight control nahi kar paya."

            t == "I could not change the volume." ->
                "Sorry Boss, volume change nahi kar paya."

            t == "I could not control media." ->
                "Sorry Boss, media control nahi kar paya."

            t == "I could not check the battery." ->
                "Sorry Boss, battery check nahi kar paya."

            t == "I could not search that." ->
                "Sorry Boss, ye search nahi kar paya."

            t == "I could not open YouTube." ->
                "Sorry Boss, YouTube open nahi ho paya."

            t == "I could not open Maps." ->
                "Sorry Boss, Maps open nahi ho paya."

            // -------------------------------------------------
            // MEMORY
            // -------------------------------------------------

            t == "Got it. I'll remember that." ->
                "Got it Boss, main yaad rakhunga."

            t == "I've cleared my personal memory." ->
                "Done Boss, personal memory clear kar di."

            t == "Okay. I'll forget that." ->
                "Okay Boss, main ise bhool jaunga."

            t == "Tell me what you want me to forget." ->
                "Boss, batao kya bhoolna hai."

            t == "I don't have any personal memory about you yet." ->
                "Boss, abhi mere paas aapki koi personal memory nahi hai."

            t == "All personal memory has been cleared." ->
                "Done Boss, saari personal memory clear kar di."

            // -------------------------------------------------
            // TIMER
            // -------------------------------------------------

            Regex(
                "(\\d+) hour timer started"
            ).matches(t) -> {

                val value =
                    Regex(
                        "(\\d+) hour timer started"
                    )
                        .find(t)
                        ?.groupValues
                        ?.get(1)

                "Boss, $value hour ka timer start kar diya."
            }

            Regex(
                "(\\d+) minute timer started"
            ).matches(t) -> {

                val value =
                    Regex(
                        "(\\d+) minute timer started"
                    )
                        .find(t)
                        ?.groupValues
                        ?.get(1)

                "Boss, $value minute ka timer start kar diya."
            }

            Regex(
                "(\\d+) second timer started"
            ).matches(t) -> {

                val value =
                    Regex(
                        "(\\d+) second timer started"
                    )
                        .find(t)
                        ?.groupValues
                        ?.get(1)

                "Boss, $value second ka timer start kar diya."
            }

            t == "Please tell me the timer duration." ->
                "Boss, timer kitne time ka lagana hai?"

            // -------------------------------------------------
            // ALARM
            // -------------------------------------------------

            t == "That is not a valid alarm time." ->
                "Boss, ye valid alarm time nahi hai."

            t == "Please tell me the alarm time, for example seven PM." ->
                "Boss, alarm kis time set karna hai?"

            t.startsWith(
                "Alarm set for "
            ) &&
                t.endsWith(".") -> {

                val time =
                    t.removePrefix(
                        "Alarm set for "
                    ).removeSuffix(".")

                "Boss, alarm $time ke liye set kar diya."
            }

            // -------------------------------------------------
            // DATE / DAY / TIME
            // -------------------------------------------------

            t.startsWith(
                "Today is "
            ) &&
                t.endsWith(".") -> {

                val value =
                    t.removePrefix(
                        "Today is "
                    ).removeSuffix(".")

                "Boss, aaj $value hai."
            }

            t.startsWith(
                "The time is "
            ) &&
                t.endsWith(".") -> {

                val value =
                    t.removePrefix(
                        "The time is "
                    ).removeSuffix(".")

                "Boss, abhi time $value hai."
            }

            // -------------------------------------------------
            // BATTERY
            // -------------------------------------------------

            t.startsWith(
                "Battery is at "
            ) &&
                t.endsWith(
                    " percent."
                ) -> {

                val value =
                    t.removePrefix(
                        "Battery is at "
                    ).removeSuffix(
                        " percent."
                    )

                "Boss, battery abhi $value percent hai."
            }

            // -------------------------------------------------
            // SEARCH
            // -------------------------------------------------

            t.startsWith(
                "Searching YouTube for "
            ) &&
                t.endsWith(".") -> {

// =========================================================
// AURIX RESPONSE LOCALIZATION
// =========================================================

private fun aurixResponse(
    text: String
): String {

    val t =
        text.trim()

    if (
        t.isBlank()
    ) {
        return t
    }

    return when {

        // -------------------------------------------------
        // APP OPENING
        // -------------------------------------------------

        t == "Opening YouTube." ->
            "बॉस, यूट्यूब खोल रहा हूँ।"

        t == "Opening camera." ->
            "बॉस, कैमरा खोल रहा हूँ।"

        t == "Opening gallery." ->
            "बॉस, गैलरी खोल रहा हूँ।"

        t == "Opening music." ->
            "बॉस, म्यूज़िक ऐप खोल रहा हूँ।"

        t == "Opening notes." ->
            "बॉस, नोट्स खोल रहा हूँ।"

        t == "Opening calculator." ->
            "बॉस, कैलकुलेटर खोल रहा हूँ।"

        t == "Opening Chrome." ->
            "बॉस, क्रोम खोल रहा हूँ।"

        t == "Opening Maps." ->
            "बॉस, मैप्स खोल रहा हूँ।"

        t == "Opening phone." ->
            "बॉस, फोन खोल रहा हूँ।"

        t == "Opening settings." ->
            "बॉस, सेटिंग्स खोल रहा हूँ।"

        t == "Opening Wi-Fi settings." ->
            "बॉस, वाई-फाई सेटिंग्स खोल रहा हूँ।"

        // -------------------------------------------------
        // VOLUME / MEDIA
        // -------------------------------------------------

        t == "Volume increased." ->
            "बॉस, वॉल्यूम बढ़ा दिया।"

        t == "Volume decreased." ->
            "बॉस, वॉल्यूम कम कर दिया।"

        t == "Media control executed." ->
            "हो गया बॉस, मीडिया कंट्रोल कर दिया।"

        // -------------------------------------------------
        // FLASHLIGHT
        // -------------------------------------------------

        t == "Flashlight turned on." ->
            "बॉस, फ्लैशलाइट चालू कर दी।"

        t == "Flashlight turned off." ->
            "बॉस, फ्लैशलाइट बंद कर दी।"

        t == "Flashlight is not available." ->
            "सॉरी बॉस, फ्लैशलाइट उपलब्ध नहीं है।"

        // -------------------------------------------------
        // DEVICE ERRORS
        // -------------------------------------------------

        t == "Camera is not available." ->
            "सॉरी बॉस, कैमरा उपलब्ध नहीं है।"

        t == "Gallery is not available." ->
            "सॉरी बॉस, गैलरी उपलब्ध नहीं है।"

        t == "Music app is not available." ->
            "सॉरी बॉस, म्यूज़िक ऐप नहीं मिली।"

        t == "Notes app is not available." ->
            "सॉरी बॉस, नोट्स ऐप नहीं मिली।"

        t == "Calculator is not available." ->
            "सॉरी बॉस, कैलकुलेटर नहीं मिला।"

        t == "YouTube is not available." ->
            "सॉरी बॉस, यूट्यूब उपलब्ध नहीं है।"

        t == "Browser is not available." ->
            "सॉरी बॉस, ब्राउज़र उपलब्ध नहीं है।"

        t == "Maps is not available." ->
            "सॉरी बॉस, मैप्स उपलब्ध नहीं है।"

        t == "Phone app is not available." ->
            "सॉरी बॉस, फोन ऐप उपलब्ध नहीं है।"

        t == "Settings is not available." ->
            "सॉरी बॉस, सेटिंग्स नहीं खुल पाईं।"

        t == "Wi-Fi settings are not available." ->
            "सॉरी बॉस, वाई-फाई सेटिंग्स उपलब्ध नहीं हैं।"

        // -------------------------------------------------
        // CONTROL ERRORS
        // -------------------------------------------------

        t == "I could not control the flashlight." ->
            "सॉरी बॉस, फ्लैशलाइट कंट्रोल नहीं कर पाया।"

        t == "I could not change the volume." ->
            "सॉरी बॉस, वॉल्यूम नहीं बदल पाया।"

        t == "I could not control media." ->
            "सॉरी बॉस, मीडिया कंट्रोल नहीं कर पाया।"

        t == "I could not check the battery." ->
            "सॉरी बॉस, बैटरी चेक नहीं कर पाया।"

        t == "I could not search that." ->
            "सॉरी बॉस, ये सर्च नहीं कर पाया।"

        t == "I could not open YouTube." ->
            "सॉरी बॉस, यूट्यूब नहीं खुल पाया।"

        t == "I could not open Maps." ->
            "सॉरी बॉस, मैप्स नहीं खुल पाया।"

        // -------------------------------------------------
        // MEMORY
        // -------------------------------------------------

        t == "Got it. I'll remember that." ->
            "समझ गया बॉस, मैं इसे याद रखूँगा।"

        t == "I've cleared my personal memory." ->
            "हो गया बॉस, मेरी पर्सनल मेमोरी क्लियर कर दी।"

        t == "Okay. I'll forget that." ->
            "ठीक है बॉस, मैं इसे भूल जाऊँगा।"

        t == "Tell me what you want me to forget." ->
            "बॉस, बताओ क्या भूलना है।"

        t == "I don't have any personal memory about you yet." ->
            "बॉस, अभी मेरे पास आपकी कोई पर्सनल मेमोरी नहीं है।"

        t == "All personal memory has been cleared." ->
            "हो गया बॉस, सारी पर्सनल मेमोरी क्लियर कर दी।"

        // -------------------------------------------------
        // TIMER
        // -------------------------------------------------

        Regex(
            "(\\d+) hour timer started"
        ).matches(t) -> {

            val value =
                Regex(
                    "(\\d+) hour timer started"
                )
                    .find(t)
                    ?.groupValues
                    ?.get(1)
                    ?: ""

            "बॉस, $value घंटे का टाइमर लगा दिया।"
        }

        Regex(
            "(\\d+) minute timer started"
        ).matches(t) -> {

            val value =
                Regex(
                    "(\\d+) minute timer started"
                )
                    .find(t)
                    ?.groupValues
                    ?.get(1)
                    ?: ""

            "बॉस, $value मिनट का टाइमर लगा दिया।"
        }

        Regex(
            "(\\d+) second timer started"
        ).matches(t) -> {

            val value =
                Regex(
                    "(\\d+) second timer started"
                )
                    .find(t)
                    ?.groupValues
                    ?.get(1)
                    ?: ""

            "बॉस, $value सेकंड का टाइमर लगा दिया।"
        }

        t == "Please tell me the timer duration." ->
            "बॉस, कितने समय का टाइमर लगाना है?"

        // -------------------------------------------------
        // ALARM
        // -------------------------------------------------

        t == "That is not a valid alarm time." ->
            "बॉस, ये सही अलार्म टाइम नहीं है।"

        t == "Please tell me the alarm time, for example seven PM." ->
            "बॉस, अलार्म किस समय लगाना है?"

        t.startsWith("Alarm set for ") &&
            t.endsWith(".") -> {

            val time =
                t.removePrefix(
                    "Alarm set for "
                ).removeSuffix(".")

            "बॉस, अलार्म $time के लिए लगा दिया।"
        }

        // -------------------------------------------------
        // DATE / DAY / TIME
        // -------------------------------------------------

        t.startsWith("Today is ") &&
            t.endsWith(".") -> {

            val value =
                t.removePrefix(
                    "Today is "
                ).removeSuffix(".")

            "बॉस, आज $value है।"
        }

        t.startsWith("The time is ") &&
            t.endsWith(".") -> {

            val value =
                t.removePrefix(
                    "The time is "
                ).removeSuffix(".")

            "बॉस, अभी समय $value है।"
        }

        // -------------------------------------------------
        // BATTERY
        // -------------------------------------------------

        t.startsWith("Battery is at ") &&
            t.endsWith(" percent.") -> {

            val value =
                t.removePrefix(
                    "Battery is at "
                ).removeSuffix(
                    " percent."
                )

            "बॉस, बैटरी अभी $value प्रतिशत है।"
        }

        // -------------------------------------------------
        // SEARCH
        // -------------------------------------------------

        t.startsWith("Searching YouTube for ") &&
            t.endsWith(".") -> {

            val query =
                t.removePrefix(
                    "Searching YouTube for "
                ).removeSuffix(".")

            "बॉस, यूट्यूब पर $query सर्च कर रहा हूँ।"
        }

        t.startsWith("Opening YouTube search for ") &&
            t.endsWith(".") -> {

            val query =
                t.removePrefix(
                    "Opening YouTube search for "
                ).removeSuffix(".")

            "बॉस, यूट्यूब पर $query की सर्च खोल रहा हूँ।"
        }

        t.startsWith("Searching Maps for ") &&
            t.endsWith(".") -> {

            val query =
                t.removePrefix(
                    "Searching Maps for "
                ).removeSuffix(".")

            "बॉस, मैप्स पर $query सर्च कर रहा हूँ।"
        }

        t.startsWith("Searching for ") &&
            t.endsWith(".") -> {

            val query =
                t.removePrefix(
                    "Searching for "
                ).removeSuffix(".")

            "बॉस, $query सर्च कर रहा हूँ।"
        }

        // -------------------------------------------------
        // AGENT
        // -------------------------------------------------

        t == "Agent completed all planned steps." ->
            "हो गया बॉस, सारे काम पूरे कर दिए।"

        t == "YouTube opened." ->
            "बॉस, यूट्यूब खोल दिया।"

        t == "Phone opened." ->
            "बॉस, फोन खोल दिया।"

        t == "Settings opened." ->
            "बॉस, सेटिंग्स खोल दी।"

        t == "I couldn't open YouTube." ->
            "सॉरी बॉस, यूट्यूब नहीं खोल पाया।"

        t == "I couldn't open phone." ->
            "सॉरी बॉस, फोन नहीं खोल पाया।"

        t == "I couldn't open settings." ->
            "सॉरी बॉस, सेटिंग्स नहीं खोल पाया।"

        t == "I couldn't execute this step." ->
            "सॉरी बॉस, ये काम पूरा नहीं कर पाया।"

        // -------------------------------------------------
        // APP NOT FOUND
        // -------------------------------------------------

        t.startsWith(
            "I couldn't find "
        ) &&
            t.endsWith(
                " on your phone."
            ) -> {

            val app =
                t.removePrefix(
                    "I couldn't find "
                ).removeSuffix(
                    " on your phone."
                )

            "सॉरी बॉस, आपके फोन में $app नहीं मिला।"
        }

        // -------------------------------------------------
        // GENERIC APP OPENING
        // -------------------------------------------------

        t.startsWith("Opening ") &&
            t.endsWith(".") -> {

            val app =
                t.removePrefix(
                    "Opening "
                ).removeSuffix(".")

            "बॉस, $app खोल रहा हूँ।"
        }

        // -------------------------------------------------
        // GREETING / IDENTITY
        // -------------------------------------------------

        t == "Hello Boss. Main AURIX hoon. Batao, kya help chahiye?" ->
            "नमस्ते बॉस। मैं ऑरिक्स हूँ। बताइए, क्या मदद चाहिए?"

        t == "Main AURIX hoon, aapka personal AI assistant." ->
            "मैं ऑरिक्स हूँ, आपका पर्सनल एआई असिस्टेंट।"

        // -------------------------------------------------
        // HOME
        // -------------------------------------------------

        t == "Unable to go to home screen." ->
            "सॉरी बॉस, होम स्क्रीन पर नहीं जा पाया।"

            // -------------------------------------------------
            // FALLBACK
            // -------------------------------------------------

            else ->
                t
        }
    }

    // =========================================================
    // SPEAK ONCE
    // =========================================================

    private fun speakOnce(
        text: String
    ) {

        if (
            text.isBlank()
        ) {
            return
        }

        if (
            currentResponseSent
        ) {
            return
        }

        currentResponseSent =
            true

        val finalText =
            aurixResponse(
                text
            )

        sendStatus(
            "SPEAKING"
        )

        sendSpeak(
            finalText
        )

        try {

            textToSpeech?.speak(
                finalText,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "AURIX"
            )

        } catch (_: Exception) {
        }

        try {

            AurixMemoryBridge.saveTurn(
                this,
                currentUserCommand,
                finalText
            )

        } catch (_: Exception) {
        }

        // IMPORTANT:
        // Do NOT immediately send LISTENING here.
        // TTS needs time to finish.
    }

 // =========================================================
// AURIX GREETING
// =========================================================

private fun speakAurixGreeting() {

    val hour =
        Calendar.getInstance()
            .get(
                Calendar.HOUR_OF_DAY
            )

    val greeting =
        when {

            hour < 5 ->
                "नमस्ते बॉस। काफी देर हो गई है। कोई जरूरी काम है?"

            hour < 12 ->
                "सुप्रभात बॉस। बताइए, आज क्या काम करना है?"

            hour < 17 ->
                "नमस्कार बॉस। बताइए, मैं आपके लिए क्या करूँ?"

            hour < 22 ->
                "शुभ संध्या बॉस। बताइए, आज क्या काम करना है?"

            else ->
                "नमस्ते बॉस। काफी देर हो गई है। कोई जरूरी काम है?"
        }

    speakOnce(
        greeting
    )
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

        sendBroadcast(
            intent
        )
    }

    // =========================================================
    // DESTROY
    // =========================================================

    override fun onDestroy() {

        serviceDestroyed =
            true

        isRunning =
            false

        listening =
            false

        restarting =
            true

        handler.removeCallbacksAndMessages(
            null
        )

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

        speechRecognizer =
            null

        textToSpeech =
            null

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
