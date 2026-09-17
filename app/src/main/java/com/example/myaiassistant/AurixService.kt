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
import android.content.pm.ServiceInfo
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
import java.net.HttpURLConnection
import java.net.URL

class AurixService :
    Service(),
    TextToSpeech.OnInitListener {

    companion object {

        const val ACTION_START =
            "com.example.myaiassistant.ACTION_START"

        const val ACTION_STOP =
            "com.example.myaiassistant.ACTION_STOP"

        const val ACTION_LISTEN_ONCE =
            "com.example.myaiassistant.ACTION_LISTEN_ONCE"

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

    // =====================================================
    // YOUTUBE COMMAND DUPLICATE PROTECTION
    // =====================================================

    private var lastYouTubeCommand: String = ""

    private var lastYouTubeCommandTime: Long = 0L

    private var speechRecognizer: SpeechRecognizer? = null

    // App/device command layer. All app-opening and app-related actions
    // are delegated to AurixAppCommands so AurixService stays focused on
    // service lifecycle, wake/listening, TTS, history and orchestration.
    private val appCommands by lazy {
        AurixAppCommands(
            context = this,
            speak = { text -> speakOnce(text) },
            status = { text -> sendStatus(text) }
        )
    }

    // Passive wake engine owns wake-mode recognition. Manual tap mode stops it
    // first, so two SpeechRecognizer sessions never overlap.
    
    private val wakeEngine by lazy {
    AurixWakeEngine(
        this,
        object : AurixWakeEngine.Callbacks {

            override fun onWakeDetected(
                commandAfterWake: String
            ) {

                if (
                    !isRunning ||
                    serviceDestroyed
                ) {
                    return
                }

                sendStatus(
                    if (commandAfterWake.isBlank()) {
                        "WAKE DETECTED"
                    } else {
                        "WAKE: $commandAfterWake"
                    }
                )
            }

            override fun onCommandRecognized(
                command: String
            ) {

                if (
                    command.isBlank() ||
                    !isRunning ||
                    serviceDestroyed
                ) {
                    return
                }

                /**
                 * Keep the existing command pipeline.
                 *
                 * Wake engine ONLY recognizes speech.
                 * AurixService decides what the command means.
                 */
                sendCommand(
                    command
                )

                processCommand(
                    command
                )
            }

            override fun onListeningChanged(
                listeningNow: Boolean,
                mode: AurixWakeEngine.Mode
            ) {

                if (
                    !isRunning ||
                    serviceDestroyed
                ) {
                    return
                }

                listening =
                    listeningNow

                when {

                    mode ==
                        AurixWakeEngine.Mode.WAKE &&
                        listeningNow -> {

                        sendStatus(
                            "AURIX READY"
                        )
                    }

                    mode ==
                        AurixWakeEngine.Mode.COMMAND &&
                        listeningNow -> {

                        sendStatus(
                            "LISTENING"
                        )
                    }

                    else -> {

                        sendStatus(
                            "READY"
                        )
                    }
                }
            }

            override fun onWakeError(
                errorCode: Int
            ) {

                if (
                    !isRunning ||
                    serviceDestroyed
                ) {
                    return
                }

                /**
                 * Visible diagnostic status.
                 *
                 * 1001 = command timeout
                 * -1  = recognition unavailable
                 * -2  = recognizer creation failure
                 * -3  = startListening failure
                 * other values = Android SpeechRecognizer error
                 */
                sendStatus(
                    "WAKE ERROR: $errorCode"
                )
            }
        }
    )
    }

    private var textToSpeech: TextToSpeech? = null

    private var listening = false

    // true = passive wake-word mode, false = one-shot command mode
    private var wakeWordMode = true

    // Prevent the delayed cancellation error from the previous recognizer session
    // from changing the state of a new manual tap session.
    private var manualListenTransition = false

    private var restarting = false

    private var serviceDestroyed = false

    private val handler =
        Handler(Looper.getMainLooper())

    private var currentUserCommand = ""

    private var currentResponseSent = false

    // =========================================================
    // AI REQUEST PROTECTION
    // =========================================================

    @Volatile
    private var aiRequestInProgress = false

    @Volatile
    private var waitingForAIConfirmation = false

    private var pendingAICommand = ""
        
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

        wakeEngine.start()
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
            wakeWordMode = true

            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {
            }

            listening = false

            wakeEngine.start()
        }

        ACTION_LISTEN_ONCE -> {

            isRunning = true
            restarting = false
            wakeWordMode = false

            manualListenTransition = true

            /**
             * Passive wake recognition must stop
             * before manual recognition starts.
             */
            wakeEngine.stop()

            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {
            }

            listening = false

            startListening()
        }

        else -> {

            /**
             * IMPORTANT:
             *
             * Service restart/default launch must return
             * to passive AURIX wake mode.
             *
             * Do NOT call startListening() here.
             */
            isRunning = true
            restarting = false
            wakeWordMode = true

            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {
            }

            listening = false

            wakeEngine.start()
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

        wakeEngine.stop()

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                notification
            )
        }
    }

    // =========================================================
    // MANUAL TAP SPEECH RECOGNITION
    // =========================================================

    private fun startListening() {
        if (serviceDestroyed || !isRunning) return
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            sendStatus("Speech recognition unavailable")
            return
        }

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        manualListenTransition = false
                        listening = true
                        sendStatus("LISTENING")
                    }
                    override fun onBeginningOfSpeech() { sendStatus("THINKING") }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        listening = false
                        if (manualListenTransition) {
                            manualListenTransition = false
                            return
                        }
                        sendStatus("READY")
                    }
                    override fun onResults(results: Bundle?) {
                        listening = false
                        val text = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()?.trim()?.lowercase(Locale.ENGLISH).orEmpty()

                        if (text.isBlank()) {
                            wakeWordMode = false
                            manualListenTransition = false
                            sendStatus("READY")
                            handler.postDelayed({
                                if (isRunning && !serviceDestroyed) wakeEngine.start()
                            }, 900)
                            return
                        }

                        sendCommand(text)
                        handler.postDelayed({
                            if (isRunning && !serviceDestroyed) processCommand(text)
                            wakeWordMode = false
                            listening = false
                            manualListenTransition = false
                            try { speechRecognizer?.cancel() } catch (_: Exception) { }
                            sendStatus("READY")
                            handler.postDelayed({
                                if (isRunning && !serviceDestroyed) wakeEngine.start()
                            }, 900)
                        }, 300)
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("hi", "IN"))
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale("hi", "IN"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            listening = false
            sendStatus("READY")
        }
    }

    // =========================================================
    // SAFE RESTART
    // =========================================================
    // Passive wake restarts are owned by AurixWakeEngine.

    // =========================================================
    // MAIN COMMAND ENGINE
    // =========================================================

                private fun processCommand(
    rawCommand: String
) {

    // =====================================================
    // RESET RESPONSE STATE
    // =====================================================

    currentResponseSent = false

    // =====================================================
    // NORMALIZE COMMAND
    // =====================================================

    var command =
        normalizeNumberWords(
            rawCommand
                .lowercase(Locale.ENGLISH)
                .trim()
        )

    if (command.isBlank()) {
        return
    }

    // =====================================================
    // DEBUG - COMMAND RECEIVED
    // =====================================================

    sendStatus(
        "CMD RECEIVED: $command"
    )

    // =====================================================
    // AI SEARCH PERMISSION RESPONSE
    // =====================================================

    if (waitingForAIConfirmation) {

        when (command) {

            "yes",
            "haan",
            "ha",
            "ji haan",
            "ji",
            "bilkul",
            "sure",
            "okay",
            "ok" -> {

                waitingForAIConfirmation = false

                val query =
                    pendingAICommand

                pendingAICommand = ""

                if (
                    query.isNotBlank()
                ) {
                    askFinalAI(
                        query
                    )
                }

                return
            }

            "no",
            "nahi",
            "naa",
            "na",
            "no thanks",
            "rehne do" -> {

                waitingForAIConfirmation = false
                pendingAICommand = ""

                speakOnce(
                    "Theek hai Boss."
                )

                return
            }
        }
    }

    // =====================================================
    // TIME
    // =====================================================

    if (
        command == "time" ||
        command == "what time is it" ||
        command == "what is the time" ||
        command == "current time" ||
        command == "abhi kitne baje hain" ||
        command == "kitne baje hain"
    ) {

        val now =
            java.time.LocalTime.now()

        val timeText =
            now.format(
                java.time.format.DateTimeFormatter.ofPattern(
                    "hh:mm a"
                )
            )

        speakOnce(
            "Abhi time $timeText hai, Boss."
        )

        return
    }

    // =====================================================
    // DATE
    // =====================================================

    if (
        command == "date" ||
        command == "today's date" ||
        command == "todays date" ||
        command == "what is today's date" ||
        command == "aaj ki date kya hai"
    ) {

        val today =
            java.time.LocalDate.now()

        val dateText =
            today.format(
                java.time.format.DateTimeFormatter.ofPattern(
                    "dd MMMM yyyy"
                )
            )

        speakOnce(
            "Aaj $dateText hai, Boss."
        )

        return
    }

    // =====================================================
    // DAY
    // =====================================================

    if (
        command == "day" ||
        command == "what day is today" ||
        command == "which day is today" ||
        command == "aaj kaun sa din hai"
    ) {

        val today =
            java.time.LocalDate.now()

        val dayText =
            today.dayOfWeek
                .getDisplayName(
                    java.time.format.TextStyle.FULL,
                    Locale.ENGLISH
                )

        speakOnce(
            "Aaj $dayText hai, Boss."
        )

        return
    }

    // =====================================================
    // CONTEXT RESOLVER
    // =====================================================

    val resolution =
        AurixContextResolver.resolve(
            this,
            command
        )

    when (resolution) {

        is AurixContextResolver.Resolution.MemoryStatement -> {

            AurixMemoryBridge.rememberThat(
                this,
                resolution.text
            )

            speakOnce(
                "Theek hai Boss, yaad rakh liya."
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

            if (
                resolution.key.isNullOrBlank()
            ) {
                AurixMemoryBridge.clearAll(
                    this
                )
            } else {
                AurixMemoryBridge.clear(
                    this,
                    resolution.key
                )
            }

            speakOnce(
                "Memory clear kar di, Boss."
            )

            return
        }

        is AurixContextResolver.Resolution.Command -> {

            command =
                resolution.command
        }

        else -> {
            // Continue normally
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
            AurixContextEngine.getRecentHistory(
                10
            )

        var target = ""

        for (
            item in history.asReversed()
        ) {

            val text =
                item.toString()
                    .lowercase(Locale.ENGLISH)

            val prefixes =
                listOf(
                    "search youtube",
                    "youtube search",
                    "youtube par",
                    "search"
                )

            for (
                prefix in prefixes
            ) {

                if (
                    text.startsWith(prefix)
                ) {

                    target =
                        text
                            .removePrefix(prefix)
                            .trim()

                    if (
                        target.isNotBlank()
                    ) {
                        break
                    }
                }
            }

            if (
                target.isNotBlank()
            ) {
                break
            }
        }

        if (
            target.isNotBlank()
        ) {

            command =
                "play $target"
        }
    }

    // =====================================================
    // SAVE CURRENT COMMAND
    // =====================================================

    currentUserCommand =
        command

    // =====================================================
    // STOP COMMANDS
    // =====================================================

    if (
        command == "stop" ||
        command == "bas" ||
        command == "chup" ||
        command == "stop listening" ||
        command == "stop aurix"
    ) {

        speakOnce(
            "Okay Boss."
        )

        try {
            wakeEngine.pause()
        } catch (_: Exception) {
        }

        return
    }

    // =====================================================
    // HOME / CLOSE
    // =====================================================

    if (
        isCloseCommand(
            command
        )
    ) {

        speakOnce(
            "Okay Boss."
        )

        try {
            sendBroadcast(
                Intent(
                    Intent.ACTION_CLOSE_SYSTEM_DIALOGS
                )
            )
        } catch (_: Exception) {
        }

        return
    }

    // =====================================================
    // MEMORY QUERY
    // =====================================================

    if (
        command.contains(
            "what do you remember"
        ) ||
        command.contains(
            "what do you know about me"
        ) ||
        command.contains(
            "meri memory"
        ) ||
        command.contains(
            "memory batao"
        )
    ) {

        val memory =
            AurixMemoryBridge.getMemory(
                this
            )

        if (
            memory.isNullOrBlank()
        ) {

            speakOnce(
                "Boss, abhi meri memory mein kuch khaas nahi hai."
            )

        } else {

            speakOnce(
                memory
            )
        }

        return
    }

    // =====================================================
    // CLEAR MEMORY
    // =====================================================

    if (
        command == "clear memory" ||
        command == "forget everything" ||
        command == "sab kuch bhool jao" ||
        command == "memory clear karo"
    ) {

        AurixMemoryBridge.clearAll(
            this
        )

        speakOnce(
            "Theek hai Boss, memory clear kar di."
        )

        return
    }

    // =====================================================
    // DIAGNOSTICS
    // =====================================================

    if (
        command == "aurix diagnostics" ||
        command == "run diagnostics" ||
        command == "system diagnostics"
    ) {

        speakOnce(
            "AURIX diagnostics active hain, Boss."
        )

        return
    }

    // =====================================================
    // AGENT MODE
    // =====================================================

    if (
        isAgentCommand(
            command
        )
    ) {

        val task =
            AurixAgentEngine.createTask(
                command
            )

        if (
            task != null
        ) {

            AurixAgentEngine.executeAgentStep(
                this,
                task
            )

            return
        }
    }

    // =====================================================
    // FLASHLIGHT
    // =====================================================

    if (
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

        try {

            val cameraManager =
                getSystemService(
                    Context.CAMERA_SERVICE
                ) as android.hardware.camera2.CameraManager

            val cameraId =
                cameraManager
                    .cameraIdList
                    .firstOrNull { id ->

                        val characteristics =
                            cameraManager.getCameraCharacteristics(
                                id
                            )

                        characteristics.get(
                            android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                        ) == true
                    }

            if (
                cameraId != null
            ) {

                cameraManager.setTorchMode(
                    cameraId,
                    true
                )

                speakOnce(
                    "Flashlight on kar di, Boss."
                )

            } else {

                speakOnce(
                    "Boss, flashlight available nahi hai."
                )
            }

        } catch (_: Exception) {

            speakOnce(
                "Boss, flashlight on nahi ho paayi."
            )
        }

        return
    }

    if (
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

        try {

            val cameraManager =
                getSystemService(
                    Context.CAMERA_SERVICE
                ) as android.hardware.camera2.CameraManager

            val cameraId =
                cameraManager
                    .cameraIdList
                    .firstOrNull { id ->

                        val characteristics =
                            cameraManager.getCameraCharacteristics(
                                id
                            )

                        characteristics.get(
                            android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE
                        ) == true
                    }

            if (
                cameraId != null
            ) {

                cameraManager.setTorchMode(
                    cameraId,
                    false
                )

                speakOnce(
                    "Flashlight off kar di, Boss."
                )
            }

        } catch (_: Exception) {

            speakOnce(
                "Boss, flashlight off nahi ho paayi."
            )
        }

        return
    }

    // =====================================================
    // TIMER
    // =====================================================

    if (
        command.contains(
            "timer"
        )
    ) {

        // Existing timer implementation
        // remains below if already present.

    }

    // =====================================================
    // APP / DEVICE COMMAND LAYER
    // =====================================================

    sendStatus(
        "APP COMMAND: $command"
    )

    if (
        appCommands.handle(
            command
        )
    ) {

        sendStatus(
            "APP COMMAND EXECUTED"
        )

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
    // DEVICE INFORMATION ENGINE
    // =====================================================

    val deviceResponse =
        AurixDeviceEngine.answer(
            this,
            command
        )

    if (
        !deviceResponse.isNullOrBlank()
    ) {

        speakOnce(
            deviceResponse
        )

        return
    }

    // =====================================================
    // LOCAL INTENT ENGINE
    // =====================================================

    val localIntentResponse =
        AurixLocalIntentEngine.answer(
            command
        )

    if (
        !localIntentResponse.isNullOrBlank()
    ) {

        if (
            localIntentResponse ==
            "TIME_LOCAL"
        ) {

            val now =
                java.time.LocalTime.now()

            val timeText =
                now.format(
                    java.time.format.DateTimeFormatter.ofPattern(
                        "hh:mm a"
                    )
                )

            speakOnce(
                "Abhi time $timeText hai, Boss."
            )

        } else {

            speakOnce(
                localIntentResponse
            )
        }

        return
    }

    // =====================================================
    // LOCAL KNOWLEDGE ENGINE
    // =====================================================

    val knowledgeResponse =
        AurixKnowledgeEngine.answer(
            command
        )

    if (
        !knowledgeResponse.isNullOrBlank()
    ) {

        speakOnce(
            knowledgeResponse
        )

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
    // FINAL AI FALLBACK - PERMISSION FIRST
    // =====================================================

    if (
        aiRequestInProgress
    ) {
        return
    }

    pendingAICommand =
        command

    waitingForAIConfirmation =
        true

    speakOnce(
        "Boss, ultra search karu?"
    )
}
// =========================================================
// FINAL AI REQUEST
// =========================================================

private fun askFinalAI(
    command: String
) {

    if (
        command.isBlank() ||
        aiRequestInProgress
    ) {
        return
    }

    aiRequestInProgress = true

    sendStatus(
        "THINKING"
    )

    AurixAI.ask(
        command
    ) { answer ->

        aiRequestInProgress = false

        if (
            answer.isBlank()
        ) {

            sendStatus(
                "ERROR"
            )

            return@ask
        }

        val finalAnswer =
            aurixResponse(
                answer
            )

        sendStatus(
            "SPEAKING"
        )

        sendSpeak(
            finalAnswer
        )

        textToSpeech?.speak(
            finalAnswer,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "AURIX_AI"
        )

        AurixMemoryBridge.saveTurn(
            this,
            command,
            finalAnswer
        )
    }
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

            val appResult =
                appCommands.executeForAgent(c)

            if (appResult != null) {
                appResult
            } else if (
                c.contains(
                    "bluetooth"
                )
            ) {
                AurixSkillEngine.process(
                    c
                )
            } else {
                AurixCommandRouter.route(
                    c
                )
            }

        } catch (_: Exception) {

            "I couldn't execute this step."
        }
    }

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

    /**
     * Stop passive recognition while
     * AURIX is speaking.
     */
    wakeEngine.pause()

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

    /**
     * Resume passive wake listening after
     * enough time for TTS to finish.
     */
    val resumeDelay =
        (finalText.length * 55L)
            .coerceIn(
                1800L,
                7000L
            )

    handler.postDelayed(
        {
            if (
                isRunning &&
                !serviceDestroyed
            ) {
                wakeEngine.resumeWakeListening()
            }
        },
        resumeDelay
    )
}
// =========================================================
// AURIX RESPONSE LOCALIZATION
// =========================================================

private fun aurixResponse(
    text: String
): String {

    val t = text.trim()

    if (t.isBlank()) return t

    return when {
        t == "Opening YouTube." -> "Boss, YouTube khol raha hoon."
        t == "Opening camera." -> "Boss, camera khol raha hoon."
        t == "Opening gallery." -> "Boss, gallery khol raha hoon."
        t == "Opening music." -> "Boss, music khol raha hoon."
        t == "Opening notes." -> "Boss, notes khol raha hoon."
        t == "Opening calculator." -> "Boss, calculator khol raha hoon."
        t == "Opening Chrome." -> "Boss, Chrome khol raha hoon."
        t == "Opening Maps." -> "Boss, Maps khol raha hoon."
        t == "Opening phone." -> "Boss, phone khol raha hoon."
        t == "Opening settings." -> "Boss, settings khol raha hoon."
        t == "Opening Wi-Fi settings." -> "Boss, Wi-Fi settings khol raha hoon."

        t == "Volume increased." -> "Boss, volume badha diya."
        t == "Volume decreased." -> "Boss, volume kam kar diya."
        t == "Media control executed." -> "Boss, media control kar diya."

        t == "Flashlight turned on." -> "Boss, flashlight on kar di."
        t == "Flashlight turned off." -> "Boss, flashlight off kar di."
        t == "Flashlight is not available." -> "Boss, flashlight available nahi hai."

        t == "Camera is not available." -> "Boss, camera available nahi hai."
        t == "Gallery is not available." -> "Boss, gallery available nahi hai."
        t == "Music app is not available." -> "Boss, music app available nahi hai."
        t == "Notes app is not available." -> "Boss, notes app available nahi hai."
        t == "Calculator is not available." -> "Boss, calculator available nahi hai."
        t == "YouTube is not available." -> "Boss, YouTube available nahi hai."
        t == "Browser is not available." -> "Boss, browser available nahi hai."
        t == "Maps is not available." -> "Boss, Maps available nahi hai."
        t == "Phone app is not available." -> "Boss, phone app available nahi hai."
        t == "Settings is not available." -> "Boss, settings available nahi hain."
        t == "Wi-Fi settings are not available." -> "Boss, Wi-Fi settings available nahi hain."

        t == "I could not control the flashlight." -> "Boss, main flashlight control nahi kar saka."
        t == "I could not change the volume." -> "Boss, main volume change nahi kar saka."
        t == "I could not control media." -> "Boss, main media control nahi kar saka."
        t == "I could not check the battery." -> "Boss, main battery check nahi kar saka."
        t == "I could not search that." -> "Boss, main uski search nahi kar saka."
        t == "I could not open YouTube." -> "Boss, main YouTube nahi khol saka."
        t == "I could not open Maps." -> "Boss, main Maps nahi khol saka."

        t == "Got it. I'll remember that." -> "Got it boss, main ye yaad rakhunga."
        t == "I've cleared my personal memory." -> "Boss, maine tumhari personal memory clear kar di."
        t == "Okay. I'll forget that." -> "Okay boss, main ye bhool jaunga."
        t == "Tell me what you want me to forget." -> "Boss, batao tum kya bhulwana chahte ho."
        t == "I don't have any personal memory about you yet." -> "Boss, mere paas abhi tumhare baare mein koi personal memory nahi hai."
        t == "All personal memory has been cleared." -> "Boss, saari personal memory clear kar di."

        Regex("(\\d+) hour timer started").matches(t) -> {
            val value = Regex("(\\d+) hour timer started").find(t)?.groupValues?.get(1).orEmpty()
            "Boss, $value hour ka timer start kar diya."
        }
        Regex("(\\d+) minute timer started").matches(t) -> {
            val value = Regex("(\\d+) minute timer started").find(t)?.groupValues?.get(1).orEmpty()
            "Boss, $value minute ka timer start kar diya."
        }
        Regex("(\\d+) second timer started").matches(t) -> {
            val value = Regex("(\\d+) second timer started").find(t)?.groupValues?.get(1).orEmpty()
            "Boss, $value second ka timer start kar diya."
        }
        t == "Please tell me the timer duration." -> "Boss, timer kitne time ka lagana hai?"

        t == "That is not a valid alarm time." -> "Boss, ye valid alarm time nahi hai."
        t == "Please tell me the alarm time, for example seven PM." -> "Boss, alarm ka time batao, jaise seven PM."

        t.startsWith("Alarm set for ") && t.endsWith(".") -> {
            val time = t.removePrefix("Alarm set for ").removeSuffix(".")
            "Boss, $time ka alarm set kar diya."
        }
        t.startsWith("Today is ") && t.endsWith(".") -> {
            val value = t.removePrefix("Today is ").removeSuffix(".")
            "Boss, aaj $value hai."
        }
        t.startsWith("The time is ") && t.endsWith(".") -> {
            val value = t.removePrefix("The time is ").removeSuffix(".")
            "Boss, abhi time $value hai."
        }
        t.startsWith("Battery is at ") && t.endsWith(" percent.") -> {
            val value = t.removePrefix("Battery is at ").removeSuffix(" percent.")
            "Boss, battery $value percent hai."
        }

        t.startsWith("Searching YouTube for ") && t.endsWith(".") -> {
            val query = t.removePrefix("Searching YouTube for ").removeSuffix(".")
            "Boss, YouTube par $query search kar raha hoon."
        }
        t.startsWith("Opening YouTube search for ") && t.endsWith(".") -> {
            val query = t.removePrefix("Opening YouTube search for ").removeSuffix(".")
            "Boss, YouTube par $query search khol raha hoon."
        }
        t.startsWith("Searching Maps for ") && t.endsWith(".") -> {
            val query = t.removePrefix("Searching Maps for ").removeSuffix(".")
            "Boss, Maps par $query search kar raha hoon."
        }
        t.startsWith("Searching for ") && t.endsWith(".") -> {
            val query = t.removePrefix("Searching for ").removeSuffix(".")
            "Boss, $query search kar raha hoon."
        }

        t == "Agent completed all planned steps." -> "Boss, saare planned steps complete ho gaye."
        t == "YouTube opened." -> "Boss, YouTube khul gaya."
        t == "Phone opened." -> "Boss, phone khul gaya."
        t == "Settings opened." -> "Boss, settings khul gayi."
        t == "I couldn't open YouTube." -> "Boss, YouTube nahi khol saka."
        t == "I couldn't open phone." -> "Boss, phone nahi khol saka."
        t == "I couldn't open settings." -> "Boss, settings nahi khol saka."
        t == "I couldn't execute this step." -> "Boss, main ye step execute nahi kar saka."

        t.startsWith("I couldn't find ") && t.endsWith(" on your phone.") -> {
            val app = t.removePrefix("I couldn't find ").removeSuffix(" on your phone.")
            "Boss, tumhare phone mein $app nahi mila."
        }
        t.startsWith("Opening ") && t.endsWith(".") -> {
            val app = t.removePrefix("Opening ").removeSuffix(".")
            "Boss, $app khol raha hoon."
        }

        t == "Hello Boss. Main AURIX hoon. बताओ, kya help chahiye?" ->
            "Hello boss, main AURIX hoon. बताओ, kya help chahiye?"
        t == "Main AURIX hoon, aapka personal AI assistant." ->
            "Main AURIX hoon, aapka personal AI assistant."
        t == "Unable to go to home screen." ->
            "Boss, main home screen par nahi ja saka."

        else -> t
    }
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
                "Boss, abhi kaafi raat ho gayi hai. बताओ kya karna hai?"

            hour < 12 ->
                "Good morning Boss. बताओ, AURIX aapke liye kya kare?"

            hour < 17 ->
                "Good afternoon Boss. बताओ, kya karna hai?"

            hour < 22 ->
                "Good evening Boss. बताओ, AURIX aapki kya help kare?"

            else ->
                "Good night Boss. बताओ, AURIX kya kare?"
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
