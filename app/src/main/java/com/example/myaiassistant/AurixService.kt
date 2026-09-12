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

    private var textToSpeech: TextToSpeech? = null

    private var listening = false

    // One command per user tap. Listening is never restarted automatically
    // after a command, which prevents media/YouTube audio from being
    // captured as the next voice command.
    private var listenRequested = false

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

        // Do not start the microphone automatically.
        // The UI's "Tap Here to Speak" action sends ACTION_START.
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

                listenRequested = false
                stopAurix()

                return START_NOT_STICKY
            }

            ACTION_START -> {

                isRunning = true
                restarting = false

                // One tap = one recognition session.
                if (!listening) {
                    listenRequested = true
                    startListening()
                }
            }

            else -> {

                // Do not silently open the microphone for unrelated service starts.
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
        listenRequested = false
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
            !isRunning ||
            !listenRequested
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
                                listenRequested = false

                                // IMPORTANT: never auto-restart here.
                                // A retry must come from a fresh user tap.
                            }

override fun
    onResults(
        results: Bundle?
    ) {

    listening = false
    listenRequested = false

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
        !command.isNullOrBlank()
    ) {

        sendCommand(
            command
        )

        // Give the recognizer result a moment
        // before processing the command.
        handler.postDelayed({

            if (
                isRunning &&
                !serviceDestroyed
            ) {

                processCommand(
                    command
                )
            }

        }, 500)
    }

    // IMPORTANT: do not restart listening automatically.
    // The next command begins only after the user taps the voice button again.
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

            listening = false
            listenRequested = false
        }
    }

    // =========================================================
    // SAFE RESTART
    // =========================================================

    private fun restartListening() {

        // Automatic microphone restart is intentionally disabled.
        // A new recognition session must be explicitly requested by the UI.
        restarting = false
        listening = false
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

        // =====================================================
// AI SEARCH PERMISSION RESPONSE
// =====================================================

if (waitingForAIConfirmation) {

    when (command) {

        "yes",
        "haan",
        "ha",
        "han" -> {

            waitingForAIConfirmation = false

            val aiCommand =
                pendingAICommand

            pendingAICommand = ""

            if (aiCommand.isNotBlank()) {
                askFinalAI(aiCommand)
            }

            return
        }

        "no",
        "nahi",
        "nahin",
        "naa",
        "na" -> {

            waitingForAIConfirmation = false
            pendingAICommand = ""

            speakOnce(
                "Theek hai Boss."
            )

            return
        }

        else -> {
            return
        }
    }
}

        
        // =========================================================
// AURIX LOCAL PRIORITY COMMANDS
// OWNER + DATE + DAY + TIME
// =========================================================

// =========================================================
// OWNER / CREATOR
// =========================================================

if (
    command.contains("kisne banaya") ||
    command.contains("kisne create kiya") ||
    command.contains("kisne create kara") ||
    command.contains("who made you") ||
    command.contains("who created you") ||
    command.contains("who is your creator") ||
    command.contains("who is your owner") ||
    command.contains("tumhara owner kaun") ||
    command.contains("tumhara malik kaun") ||
    command.contains("tumhara creator kaun") ||
    (
        command.contains("aurix") &&
        (
            command.contains("banaya") ||
            command.contains("create") ||
            command.contains("owner")
        )
    )
) {

    speakOnce(
        "Mujhe mere owner Kushal Haryana ne banaya hai."
    )

    return
}

// =====================================================
// PERSONAL & GENERAL KNOWLEDGE
// =====================================================

if (
    command == "mera naam kya hai" ||
    command == "mera name kya hai" ||
    command == "what is my name" ||
    command == "do you know my name"
) {
    speakOnce(
        "Aapka naam Kushal Haryana hai, Boss."
    )
    return
}

if (
    command.contains("india ki capital") ||
    command.contains("india ki rajdhani") ||
    command.contains("bharat ki rajdhani") ||
    command.contains("bharat ki capital") ||
    command.contains("capital of india") ||
    command.contains("capital india")
) {
    speakOnce(
        "India ki capital New Delhi hai, Boss."
    )
    return
}

  // =====================================================
// BASIC MATH & FACTS - LOCAL
// =====================================================

if (
    command == "5 plus 5" ||
    command == "5 + 5" ||
    command == "5 and 5"
) {
    speakOnce(
        "5 plus 5 equals 10, Boss."
    )
    return
}

if (
    command.contains("1 kilometer mein kitne meter") ||
    command.contains("1 km mein kitne meter") ||
    command.contains("one kilometer mein kitne meter")
) {
    speakOnce(
        "1 kilometer mein 1000 meter hote hain, Boss."
    )
    return
}

if (
    command.contains("suraj kis direction se ugta hai") ||
    command.contains("sun rises from which direction") ||
    command.contains("sun kis direction se ugta hai")
) {
    speakOnce(
        "Suraj East, yani Purab ki direction se ugta hai, Boss."
    )
    return
}

if (
    command.contains("human body mein kitni bones") ||
    command.contains("insaan ke sharir mein kitni haddiyan") ||
    command.contains("how many bones in human body")
) {
    speakOnce(
        "Ek adult human body mein normally 206 bones hoti hain, Boss."
    )
    return
}

// =========================================================
// TIME
// =========================================================

if (
    command == "time" ||
    command.contains("what is the time") ||
    command.contains("what's the time") ||
    command.contains("tell me the time") ||
    command.contains("current time") ||
    command.contains("what time is it") ||
    command.contains("time kya hai") ||
    command.contains("abhi time") ||
    command.contains("abhi kitne baje") ||
    command.contains("kitne baje") ||
    command.contains("kitne baj rahe") ||
    command.contains("samay kya hai")
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


// =========================================================
// DATE
// =========================================================

if (
    command.contains("date") ||
    command.contains("today's date") ||
    command.contains("today date") ||
    command.contains("aaj ki date") ||
    command.contains("aaj ka date") ||
    command.contains("aaj ki tarikh") ||
    command.contains("aaj ki tareekh") ||
    command.contains("tarikh kya hai") ||
    command.contains("tareekh kya hai")
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


// =========================================================
// DAY
// =========================================================

if (
    command == "day" ||
    command.contains("what day") ||
    command.contains("which day") ||
    command.contains("aaj kaun sa din") ||
    command.contains("aaj konsa din") ||
    command.contains("aaj ka din") ||
    command.contains("kaunsa din hai") ||
    command.contains("konsa din hai")
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

    command.contains("what is the time") ||
    command.contains("what's the time") ||
    command.contains("tell me the time") ||
    command.contains("current time") ||
    command.contains("what time is it") ||

    command.contains("time kya hai") ||
    command.contains("abhi time kya hai") ||
    command.contains("abhi kya time hai") ||
    command.contains("abhi kitne baje hain") ||
    command.contains("abhi kitne baje hai") ||
    command.contains("kitne baje hain") ||
    command.contains("kitne baje hai") ||
    command.contains("kitne baj rahe hain") ||
    command.contains("kitne baj rahe hai") ||
    command.contains("abhi kitne baj rahe") ||
    command.contains("samay kya hai")
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
        // SMART YOUTUBE COMMANDS
        // =====================================================
if (
    command.contains("youtube") ||
    command.contains("ka song chalao") ||
    command.contains("ka gana chalao") ||
    command.contains("ka gaana chalao") ||
    command.contains("song chalao") ||
    command.contains("gana chalao") ||
    command.contains("gaana chalao")
){

            // -------------------------------------------------
            // DIRECT YOUTUBE SEARCH / PLAY
            // -------------------------------------------------

            var youtubeQuery: String? = null

            when {
                
                // Smart Hindi YouTube song command
command.contains("ka song chalao") ||
command.contains("ka gana chalao") ||
command.contains("ka gaana chalao") ||
command.contains("song chalao") ||
command.contains("gana chalao") ||
command.contains("gaana chalao") -> {

    youtubeQuery =
        command
            .replace(
                "ka song chalao",
                ""
            )
            .replace(
                "ka gana chalao",
                ""
            )
            .replace(
                "ka gaana chalao",
                ""
            )
            .replace(
                "song chalao",
                ""
            )
            .replace(
                "gana chalao",
                ""
            )
            .replace(
                "gaana chalao",
                ""
            )
            .replace(
                "aurix",
                ""
            )
            .trim()
            val now = System.currentTimeMillis()

if (
    youtubeQuery == lastYouTubeCommand &&
    now - lastYouTubeCommandTime < 5000L
) {
    return
}

lastYouTubeCommand = youtubeQuery ?: ""
lastYouTubeCommandTime = now

}

                // YouTube kholo aur Arijit Singh search karo
                command.contains("youtube kholo") &&
                    command.contains("search karo") -> {

                    youtubeQuery =
                        command
                            .substringAfter(
                                "youtube kholo"
                            )
                            .substringBefore(
                                "search karo"
                            )
                            .trim()

                }

                // YouTube kholo aur Arijit Singh chalao
                command.contains("youtube kholo") &&
                    command.contains("chalao") -> {

                    youtubeQuery =
                        command
                            .substringAfter(
                                "youtube kholo"
                            )
                            .substringBeforeLast(
                                "chalao"
                            )
                            .trim()

                }

                // YouTube par Arijit Singh search karo
                command.contains("youtube par") &&
                    command.contains("search karo") -> {

                    youtubeQuery =
                        command
                            .substringAfter(
                                "youtube par"
                            )
                            .substringBefore(
                                "search karo"
                            )
                            .trim()
                    }

                // YouTube pe Arijit Singh search karo
                command.contains("youtube pe") &&
                    command.contains("search karo") -> {

                    youtubeQuery =
                        command
                            .substringAfter(
                                "youtube pe"
                            )
                            .substringBefore(
                                "search karo"
                            )
                            .trim()
                    }

                // Search YouTube Arijit Singh
                command.startsWith(
                    "search youtube "
                ) -> {

                    youtubeQuery =
                        command
                            .removePrefix(
                                "search youtube "
                            )
                            .trim()
                }

                // YouTube search Arijit Singh
                command.startsWith(
                    "youtube search "
                ) -> {

                    youtubeQuery =
                        command
                            .removePrefix(
                                "youtube search "
                            )
                            .trim()
                }

                // YouTube par Arijit Singh
                command.startsWith(
                    "youtube par "
                ) -> {

                    youtubeQuery =
                        command
                            .removePrefix(
                                "youtube par "
                            )
                            .trim()
                }

                // Play Arijit Singh
                command.startsWith(
                    "play "
                ) -> {

                    youtubeQuery =
                        command
                            .removePrefix(
                                "play "
                            )
                            .trim()
                }
            }

            // -------------------------------------------------
            // CLEAN QUERY
            // -------------------------------------------------

            if (
                !youtubeQuery.isNullOrBlank()
            ) {

                var cleanQuery =
                    youtubeQuery
                        .trim()

                cleanQuery =
                    cleanQuery
                        .removeSuffix(
                            "search"
                        )
                        .removeSuffix(
                            "search karo"
                        )
                        .removeSuffix(
                            "chalao"
                        )
                        .removeSuffix(
                            "play karo"
                        )
                        .removeSuffix(
                            "gana chalao"
                        )
                        .trim()

                // Remove common Hindi filler words
                cleanQuery =
                    cleanQuery
                        .removePrefix(
                            "ka "
                        )
                        .removePrefix(
                            "ki "
                        )
                        .removePrefix(
                            "ko "
                        )
                        .trim()

                if (
                    cleanQuery.isNotBlank()
                ) {

                    sendStatus(
                        "EXECUTING"
                    )

                    searchYouTube(
                        cleanQuery
                    )

                } else {

                    openYouTube()
                }

                return
            }

            // -------------------------------------------------
            // OPEN YOUTUBE ONLY
            // -------------------------------------------------

            if (
                command == "youtube" ||
                command == "open youtube" ||
                command == "launch youtube" ||
                command == "youtube kholo" ||
                command == "youtube open karo" ||
                command == "youtube khol do"
            ) {

                openYouTube()

                return
            }
        }

// =====================================================
// DIALER
// =====================================================

if (
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

        val localIntentResponse =
         AurixLocalIntentEngine.answer(command)

        if (
             !localIntentResponse.isNullOrBlank()
) {
    if (localIntentResponse == "TIME_LOCAL") {
        val now = java.time.LocalTime.now()
        val timeText =
            now.format(
                java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
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


// =========================================================
// FINAL AI FALLBACK - PERMISSION FIRST
// =========================================================

if (aiRequestInProgress) {
    return
}

pendingAICommand = command
waitingForAIConfirmation = true

speakOnce(
    "Boss, ultra search karu?"
)

return
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

val isLatest = query.lowercase().contains("latest")

val cleanQuery = query
    .replace("latest song", "song", ignoreCase = true)
    .replace("latest", "", ignoreCase = true)
    .trim()

val url =
    "https://www.googleapis.com/youtube/v3/search" +
    "?part=snippet" +
    "&q=" + Uri.encode(cleanQuery) +
    "&type=video" +
    "&maxResults=5" +
    if (isLatest) "&order=date" else "" +
    "&key=" + BuildConfig.YOUTUBE_API_KEY
        
Thread {
    try {
        val response = java.net.URL(url).readText()

        val items =
    org.json.JSONObject(response)
        .getJSONArray("items")

var videoId: String? = null

for (i in 0 until items.length()) {
    val idObject =
        items.getJSONObject(i).getJSONObject("id")

    if (idObject.optString("kind") == "youtube#video") {
        videoId = idObject.optString("videoId")
        if (!videoId.isNullOrBlank()) {
            break
        }
    }
}

if (videoId.isNullOrBlank()) {
    throw Exception("No video found")
}

        android.os.Handler(android.os.Looper.getMainLooper()).post {
            val youtubeIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=$videoId")
                ).apply {
                    setPackage("com.google.android.youtube")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

            try {
                startActivity(youtubeIntent)
                speakOnce("Playing $query on YouTube.")
            } catch (_: Exception) {
                val browserIntent =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=$videoId")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                startActivity(browserIntent)
                speakOnce("Opening $query on YouTube.")
            }
        }

    } catch (_: Exception) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            speakOnce("I could not find that video on YouTube.")
        }
    }
}.start()

return
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
            "啶啶�, 啶啶熰啶啶� 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        t == "Opening camera." ->
            "啶啶�, 啶曕啶ぐ啶� 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        t == "Opening gallery." ->
            "啶啶�, 啶椸啶侧ぐ啷€ 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        t == "Opening music." ->
            "啶啶�, 啶啶啶溹ぜ啶苦 啶愢お 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        t == "Opening notes." ->
            "啶啶�, 啶ㄠ啶熰啶� 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        t == "Opening calculator." ->
            "啶啶�, 啶曕啶侧啷佮げ啷囙啶� 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        t == "Opening Chrome." ->
            "啶啶�, 啶曕啶班啶� 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        t == "Opening Maps." ->
            "啶啶�, 啶啶啶� 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        t == "Opening phone." ->
            "啶啶�, 啶啶� 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        t == "Opening settings." ->
            "啶啶�, 啶膏啶熰た啶傕啷嵿じ 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        t == "Opening Wi-Fi settings." ->
            "啶啶�, 啶掂ぞ啶�-啶ぞ啶� 啶膏啶熰た啶傕啷嵿じ 啶栢啶� 啶班す啶� 啶灌啶佮イ"

        // -------------------------------------------------
        // VOLUME / MEDIA
        // -------------------------------------------------

        t == "Volume increased." ->
            "啶啶�, 啶掂啶侧啶啶� 啶あ啶监ぞ 啶︵た啶ぞ啷�"

        t == "Volume decreased." ->
            "啶啶�, 啶掂啶侧啶啶� 啶曕ぎ 啶曕ぐ 啶︵た啶ぞ啷�"

        t == "Media control executed." ->
            "啶灌 啶椸く啶� 啶啶�, 啶啶∴た啶ぞ 啶曕啶熰啶班啶� 啶曕ぐ 啶︵た啶ぞ啷�"

        // -------------------------------------------------
        // FLASHLIGHT
        // -------------------------------------------------

        t == "Flashlight turned on." ->
            "啶啶�, 啶啶侧啶多げ啶距啶� 啶氞ぞ啶侧 啶曕ぐ 啶︵啷�"

        t == "Flashlight turned off." ->
            "啶啶�, 啶啶侧啶多げ啶距啶� 啶啶� 啶曕ぐ 啶︵啷�"

        t == "Flashlight is not available." ->
            "啶膏啶班 啶啶�, 啶啶侧啶多げ啶距啶� 啶夃お啶侧が啷嵿ぇ 啶ㄠす啷€啶� 啶灌啷�"

        // -------------------------------------------------
        // DEVICE ERRORS
        // -------------------------------------------------

        t == "Camera is not available." ->
            "啶膏啶班 啶啶�, 啶曕啶ぐ啶� 啶夃お啶侧が啷嵿ぇ 啶ㄠす啷€啶� 啶灌啷�"

        t == "Gallery is not available." ->
            "啶膏啶班 啶啶�, 啶椸啶侧ぐ啷€ 啶夃お啶侧が啷嵿ぇ 啶ㄠす啷€啶� 啶灌啷�"

        t == "Music app is not available." ->
            "啶膏啶班 啶啶�, 啶啶啶溹ぜ啶苦 啶愢お 啶ㄠす啷€啶� 啶た啶侧啷�"

        t == "Notes app is not available." ->
            "啶膏啶班 啶啶�, 啶ㄠ啶熰啶� 啶愢お 啶ㄠす啷€啶� 啶た啶侧啷�"

        t == "Calculator is not available." ->
            "啶膏啶班 啶啶�, 啶曕啶侧啷佮げ啷囙啶� 啶ㄠす啷€啶� 啶た啶侧ぞ啷�"

        t == "YouTube is not available." ->
            "啶膏啶班 啶啶�, 啶啶熰啶啶� 啶夃お啶侧が啷嵿ぇ 啶ㄠす啷€啶� 啶灌啷�"

        t == "Browser is not available." ->
            "啶膏啶班 啶啶�, 啶啶班ぞ啶夃啶监ぐ 啶夃お啶侧が啷嵿ぇ 啶ㄠす啷€啶� 啶灌啷�"

        t == "Maps is not available." ->
            "啶膏啶班 啶啶�, 啶啶啶� 啶夃お啶侧が啷嵿ぇ 啶ㄠす啷€啶� 啶灌啷�"

        t == "Phone app is not available." ->
            "啶膏啶班 啶啶�, 啶啶� 啶愢お 啶夃お啶侧が啷嵿ぇ 啶ㄠす啷€啶� 啶灌啷�"

        t == "Settings is not available." ->
            "啶膏啶班 啶啶�, 啶膏啶熰た啶傕啷嵿じ 啶ㄠす啷€啶� 啶栢啶� 啶ぞ啶堗啷�"

        t == "Wi-Fi settings are not available." ->
            "啶膏啶班 啶啶�, 啶掂ぞ啶�-啶ぞ啶� 啶膏啶熰た啶傕啷嵿じ 啶夃お啶侧が啷嵿ぇ 啶ㄠす啷€啶� 啶灌啶傕イ"

        // -------------------------------------------------
        // CONTROL ERRORS
        // -------------------------------------------------

        t == "I could not control the flashlight." ->
            "啶膏啶班 啶啶�, 啶啶侧啶多げ啶距啶� 啶曕啶熰啶班啶� 啶ㄠす啷€啶� 啶曕ぐ 啶ぞ啶ぞ啷�"

        t == "I could not change the volume." ->
            "啶膏啶班 啶啶�, 啶掂啶侧啶啶� 啶ㄠす啷€啶� 啶う啶� 啶ぞ啶ぞ啷�"

        t == "I could not control media." ->
            "啶膏啶班 啶啶�, 啶啶∴た啶ぞ 啶曕啶熰啶班啶� 啶ㄠす啷€啶� 啶曕ぐ 啶ぞ啶ぞ啷�"

        t == "I could not check the battery." ->
            "啶膏啶班 啶啶�, 啶啶熰ぐ啷€ 啶氞啶� 啶ㄠす啷€啶� 啶曕ぐ 啶ぞ啶ぞ啷�"

        t == "I could not search that." ->
            "啶膏啶班 啶啶�, 啶 啶膏ぐ啷嵿 啶ㄠす啷€啶� 啶曕ぐ 啶ぞ啶ぞ啷�"

        t == "I could not open YouTube." ->
            "啶膏啶班 啶啶�, 啶啶熰啶啶� 啶ㄠす啷€啶� 啶栢啶� 啶ぞ啶ぞ啷�"

        t == "I could not open Maps." ->
            "啶膏啶班 啶啶�, 啶啶啶� 啶ㄠす啷€啶� 啶栢啶� 啶ぞ啶ぞ啷�"

        // -------------------------------------------------
        // MEMORY
        // -------------------------------------------------

        t == "Got it. I'll remember that." ->
            "啶膏ぎ啶� 啶椸く啶� 啶啶�, 啶啶� 啶囙じ啷� 啶ぞ啶� 啶班啷傕啶椸ぞ啷�"

        t == "I've cleared my personal memory." ->
            "啶灌 啶椸く啶� 啶啶�, 啶啶班 啶ぐ啷嵿じ啶ㄠげ 啶啶啶班 啶曕啶侧た啶ぐ 啶曕ぐ 啶︵啷�"

        t == "Okay. I'll forget that." ->
            "啶犩啶� 啶灌 啶啶�, 啶啶� 啶囙じ啷� 啶啶� 啶溹ぞ啶娻啶椸ぞ啷�"

        t == "Tell me what you want me to forget." ->
            "啶啶�, 啶い啶距 啶曕啶ぞ 啶啶侧え啶� 啶灌啷�"

        t == "I don't have any personal memory about you yet." ->
            "啶啶�, 啶呧き啷€ 啶啶班 啶ぞ啶� 啶嗋お啶曕 啶曕啶� 啶ぐ啷嵿じ啶ㄠげ 啶啶啶班 啶ㄠす啷€啶� 啶灌啷�"

        t == "All personal memory has been cleared." ->
            "啶灌 啶椸く啶� 啶啶�, 啶膏ぞ啶班 啶ぐ啷嵿じ啶ㄠげ 啶啶啶班 啶曕啶侧た啶ぐ 啶曕ぐ 啶︵啷�"

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

            "啶啶�, $value 啶樴啶熰 啶曕ぞ 啶熰ぞ啶囙ぎ啶� 啶侧啶� 啶︵た啶ぞ啷�"
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

            "啶啶�, $value 啶た啶ㄠ 啶曕ぞ 啶熰ぞ啶囙ぎ啶� 啶侧啶� 啶︵た啶ぞ啷�"
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

            "啶啶�, $value 啶膏啶曕啶� 啶曕ぞ 啶熰ぞ啶囙ぎ啶� 啶侧啶� 啶︵た啶ぞ啷�"
        }

        t == "Please tell me the timer duration." ->
            "啶啶�, 啶曕た啶むえ啷� 啶膏ぎ啶� 啶曕ぞ 啶熰ぞ啶囙ぎ啶� 啶侧啶距え啶� 啶灌?"

        // -------------------------------------------------
        // ALARM
        // -------------------------------------------------

        t == "That is not a valid alarm time." ->
            "啶啶�, 啶 啶膏す啷€ 啶呧げ啶距ぐ啷嵿ぎ 啶熰ぞ啶囙ぎ 啶ㄠす啷€啶� 啶灌啷�"

        t == "Please tell me the alarm time, for example seven PM." ->
            "啶啶�, 啶呧げ啶距ぐ啷嵿ぎ 啶曕た啶� 啶膏ぎ啶� 啶侧啶距え啶� 啶灌?"

        t.startsWith("Alarm set for ") &&
            t.endsWith(".") -> {

            val time =
                t.removePrefix(
                    "Alarm set for "
                ).removeSuffix(".")

            "啶啶�, 啶呧げ啶距ぐ啷嵿ぎ $time 啶曕 啶侧た啶� 啶侧啶� 啶︵た啶ぞ啷�"
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

            "啶啶�, 啶嗋 $value 啶灌啷�"
        }

        t.startsWith("The time is ") &&
            t.endsWith(".") -> {

            val value =
                t.removePrefix(
                    "The time is "
                ).removeSuffix(".")

            "啶啶�, 啶呧き啷€ 啶膏ぎ啶� $value 啶灌啷�"
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

            "啶啶�, 啶啶熰ぐ啷€ 啶呧き啷€ $value 啶啶班い啶苦ざ啶� 啶灌啷�"
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

            "啶啶�, 啶啶熰啶啶� 啶ぐ $query 啶膏ぐ啷嵿 啶曕ぐ 啶班す啶� 啶灌啶佮イ"
        }

        t.startsWith("Opening YouTube search for ") &&
            t.endsWith(".") -> {

            val query =
                t.removePrefix(
                    "Opening YouTube search for "
                ).removeSuffix(".")

            "啶啶�, 啶啶熰啶啶� 啶ぐ $query 啶曕 啶膏ぐ啷嵿 啶栢啶� 啶班す啶� 啶灌啶佮イ"
        }

        t.startsWith("Searching Maps for ") &&
            t.endsWith(".") -> {

            val query =
                t.removePrefix(
                    "Searching Maps for "
                ).removeSuffix(".")

            "啶啶�, 啶啶啶� 啶ぐ $query 啶膏ぐ啷嵿 啶曕ぐ 啶班す啶� 啶灌啶佮イ"
        }

        t.startsWith("Searching for ") &&
            t.endsWith(".") -> {

            val query =
                t.removePrefix(
                    "Searching for "
                ).removeSuffix(".")

            "啶啶�, $query 啶膏ぐ啷嵿 啶曕ぐ 啶班す啶� 啶灌啶佮イ"
        }

        // -------------------------------------------------
        // AGENT
        // -------------------------------------------------

        t == "Agent completed all planned steps." ->
            "啶灌 啶椸く啶� 啶啶�, 啶膏ぞ啶班 啶曕ぞ啶� 啶啶班 啶曕ぐ 啶︵た啶忇イ"

        t == "YouTube opened." ->
            "啶啶�, 啶啶熰啶啶� 啶栢啶� 啶︵た啶ぞ啷�"

        t == "Phone opened." ->
            "啶啶�, 啶啶� 啶栢啶� 啶︵た啶ぞ啷�"

        t == "Settings opened." ->
            "啶啶�, 啶膏啶熰た啶傕啷嵿じ 啶栢啶� 啶︵啷�"

        t == "I couldn't open YouTube." ->
            "啶膏啶班 啶啶�, 啶啶熰啶啶� 啶ㄠす啷€啶� 啶栢啶� 啶ぞ啶ぞ啷�"

        t == "I couldn't open phone." ->
            "啶膏啶班 啶啶�, 啶啶� 啶ㄠす啷€啶� 啶栢啶� 啶ぞ啶ぞ啷�"

        t == "I couldn't open settings." ->
            "啶膏啶班 啶啶�, 啶膏啶熰た啶傕啷嵿じ 啶ㄠす啷€啶� 啶栢啶� 啶ぞ啶ぞ啷�"

        t == "I couldn't execute this step." ->
            "啶膏啶班 啶啶�, 啶 啶曕ぞ啶� 啶啶班ぞ 啶ㄠす啷€啶� 啶曕ぐ 啶ぞ啶ぞ啷�"

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

            "啶膏啶班 啶啶�, 啶嗋お啶曕 啶啶� 啶啶� $app 啶ㄠす啷€啶� 啶た啶侧ぞ啷�"
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

            "啶啶�, $app 啶栢啶� 啶班す啶� 啶灌啶佮イ"
        }

        // -------------------------------------------------
        // GREETING / IDENTITY
        // -------------------------------------------------

        t == "Hello Boss. Main AURIX hoon. Batao, kya help chahiye?" ->
            "啶ㄠぎ啶膏啶む 啶啶膏イ 啶啶� 啶戉ぐ啶苦啷嵿じ 啶灌啶佮イ 啶い啶距啶�, 啶曕啶ぞ 啶う啶� 啶氞ぞ啶灌た啶�?"

        t == "Main AURIX hoon, aapka personal AI assistant." ->
            "啶啶� 啶戉ぐ啶苦啷嵿じ 啶灌啶�, 啶嗋お啶曕ぞ 啶ぐ啷嵿じ啶ㄠげ 啶忇啶� 啶呧じ啶苦じ啷嵿啷囙啶熰イ"

        // -------------------------------------------------
        // HOME
        // -------------------------------------------------

        t == "Unable to go to home screen." ->
            "啶膏啶班 啶啶�, 啶灌啶� 啶膏啶曕啶班啶� 啶ぐ 啶ㄠす啷€啶� 啶溹ぞ 啶ぞ啶ぞ啷�"

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
                "啶ㄠぎ啶膏啶む 啶啶膏イ 啶曕ぞ啶 啶︵啶� 啶灌 啶椸 啶灌啷� 啶曕啶� 啶溹ぐ啷傕ぐ啷€ 啶曕ぞ啶� 啶灌 啶曕啶ぞ?"

            hour < 12 ->
                "啶膏啶啶班き啶距い 啶啶膏イ 啶い啶距啶�, 啶嗋 啶曕啶ぞ 啶曕ぞ啶� 啶曕ぐ啶ㄠぞ 啶灌?"

            hour < 17 ->
                "啶ㄠぎ啶膏啶曕ぞ啶� 啶啶膏イ 啶い啶距啶�, 啶啶� 啶嗋お啶曕 啶侧た啶� 啶曕啶ぞ 啶曕ぐ啷傕?"

            hour < 22 ->
                "啶多啶� 啶膏啶о啶ぞ 啶啶膏イ 啶い啶距啶�, 啶嗋 啶曕啶ぞ 啶曕ぞ啶� 啶曕ぐ啶ㄠぞ 啶灌?"

            else ->
                "啶ㄠぎ啶膏啶む 啶啶膏イ 啶曕ぞ啶 啶︵啶� 啶灌 啶椸 啶灌啷� 啶曕啶� 啶溹ぐ啷傕ぐ啷€ 啶曕ぞ啶� 啶灌 啶曕啶ぞ?"
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
