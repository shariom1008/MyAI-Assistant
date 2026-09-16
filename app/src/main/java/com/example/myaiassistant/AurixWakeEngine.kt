package com.example.myaiassistant

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * AURIX V2 Wake Engine
 *
 * Responsibilities:
 * - Passive wake recognition
 * - Command capture after wake
 * - Single SpeechRecognizer ownership
 * - Session/generation protection
 * - Automatic recovery with backoff
 * - Command timeout
 * - Duplicate/debounce protection
 *
 * IMPORTANT:
 * SpeechRecognizer is ASR, not a true low-power hotword engine.
 * This version stabilizes the existing architecture.
 */
class AurixWakeEngine(
    private val context: Context,
    private val callbacks: Callbacks
) {

    interface Callbacks {
        fun onWakeDetected(commandAfterWake: String)
        fun onCommandRecognized(command: String)
        fun onListeningChanged(listening: Boolean, mode: Mode)
        fun onWakeError(errorCode: Int)
    }

    enum class Mode {
        IDLE,
        WAKE,
        COMMAND
    }

    private val handler = Handler(Looper.getMainLooper())

    private var recognizer: SpeechRecognizer? = null

    private var mode = Mode.IDLE

    private var running = false
    private var starting = false

    /**
     * Every recognition session gets a unique generation.
     * Old callbacks are ignored.
     */
    private var generation = 0L

    private var restartRunnable: Runnable? = null
    private var commandTimeoutRunnable: Runnable? = null

    /**
     * Consecutive failures.
     * Used for progressive recovery instead of hammering
     * SpeechRecognizer continuously.
     */
    private var consecutiveErrors = 0

    private var lastCommand: String = ""
    private var lastCommandTime = 0L

    private var lastWakeTime = 0L

    private val wakeVariants = setOf(
        "aurix",
        "auriks",
        "aurics",
        "aurik",
        "aurixx",
        "auryx",
        "aurex",
        "orix",
        "oryx",
        "ourix",
        "arix",
        "auric",
        "aurrix",
        "aurixs",
        "aurek"
    )

    companion object {
        private const val COMMAND_TIMEOUT_MS = 6000L

        private const val INITIAL_RESTART_MS = 350L
        private const val MAX_RESTART_MS = 8000L

        private const val COMMAND_DEBOUNCE_MS = 900L
        private const val WAKE_DEBOUNCE_MS = 700L

        private const val START_DELAY_MS = 100L
    }

    // =========================================================
    // PUBLIC API
    // =========================================================

    fun start() {
        runOnMain {
            if (running) {
                /*
                 * If the service is already running, don't create
                 * another recognizer/session.
                 */
                return@runOnMain
            }

            running = true
            starting = false

            consecutiveErrors = 0
            lastCommand = ""
            lastCommandTime = 0L

            createRecognizerIfNeeded()
            scheduleWakeRestart(100L)
        }
    }

    fun stop() {
        runOnMain {
            stopInternal()
        }
    }

    /**
     * Existing service compatibility.
     */
    fun resumeWakeListening() {
        runOnMain {
            if (!running) return@runOnMain

            cancelRestart()
            cancelCommandTimeout()

            invalidateSession()

            safeCancelRecognizer()

            mode = Mode.IDLE
            starting = false

            callbacks.onListeningChanged(false, Mode.IDLE)

            scheduleWakeRestart(250L)
        }
    }

    /**
     * Optional pause API.
     *
     * Useful later when AURIX speaks through TTS.
     */
    fun pause() {
        runOnMain {
            if (!running) return@runOnMain

            cancelRestart()
            cancelCommandTimeout()

            invalidateSession()

            safeCancelRecognizer()

            starting = false
            mode = Mode.IDLE

            callbacks.onListeningChanged(false, Mode.IDLE)
        }
    }

    // =========================================================
    // RECOGNIZER CREATION
    // =========================================================

    private fun createRecognizerIfNeeded(): Boolean {

        if (recognizer != null) {
            return true
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            callbacks.onWakeError(-1)
            return false
        }

        return try {

            recognizer =
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
                ) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                } else {
                    SpeechRecognizer.createSpeechRecognizer(context)
                }

            recognizer?.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(params: Bundle?) {

                        if (!isCurrentSession()) return

                        starting = false
                        consecutiveErrors = 0

                        callbacks.onListeningChanged(
                            true,
                            mode
                        )
                    }

                    override fun onBeginningOfSpeech() {

                        if (!isCurrentSession()) return

                        callbacks.onListeningChanged(
                            true,
                            mode
                        )
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Reserved for future waveform/UI.
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {
                        // Not used.
                    }

                    override fun onEndOfSpeech() {

                        if (!isCurrentSession()) return

                        callbacks.onListeningChanged(
                            false,
                            mode
                        )
                    }

                    override fun onError(error: Int) {

                        if (!isCurrentSession()) return

                        starting = false
                        consecutiveErrors++

                        callbacks.onWakeError(error)

                        when (mode) {

                            Mode.WAKE -> {
                                scheduleRecovery()
                            }

                            Mode.COMMAND -> {
                                finishCommand()
                            }

                            Mode.IDLE -> {
                                scheduleWakeRestart(
                                    calculateBackoff()
                                )
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {

                        if (!isCurrentSession()) return

                        starting = false
                        consecutiveErrors = 0

                        val text =
                            results
                                ?.getStringArrayList(
                                    SpeechRecognizer.RESULTS_RECOGNITION
                                )
                                ?.firstOrNull()
                                ?.trim()
                                .orEmpty()

                        when (mode) {

                            Mode.WAKE -> {
                                handleWakeResult(text)
                            }

                            Mode.COMMAND -> {
                                handleCommandResult(text)
                            }

                            Mode.IDLE -> {
                                scheduleWakeRestart(250L)
                            }
                        }
                    }

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {
                        // Disabled intentionally.
                    }

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {
                        // Not used.
                    }
                }
            )

            true

        } catch (_: Exception) {

            recognizer = null
            callbacks.onWakeError(-2)

            false
        }
    }

    // =========================================================
    // INTENT
    // =========================================================

    private fun buildIntent(): Intent {

        return Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale("en", "IN")
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                Locale("en", "IN")
            )

            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            )

            putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
            )

            /*
             * Don't terminate command capture too aggressively.
             */
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                1000L
            )
        }
    }

    // =========================================================
    // WAKE LISTENING
    // =========================================================

    private fun startWakeListening() {

        if (!running) return

        if (starting) return

        if (mode == Mode.COMMAND) return

        cancelRestart()
        cancelCommandTimeout()

        if (!createRecognizerIfNeeded()) {

            scheduleWakeRestart(
                calculateBackoff()
            )

            return
        }

        mode = Mode.WAKE
        starting = true

        val myGeneration = ++generation

        safeCancelRecognizer()

        handler.postDelayed(
            {

                if (!running) return@postDelayed

                if (myGeneration != generation) {
                    return@postDelayed
                }

                if (mode != Mode.WAKE) {
                    return@postDelayed
                }

                try {

                    recognizer?.startListening(
                        buildIntent()
                    )

                } catch (_: Exception) {

                    if (myGeneration != generation) {
                        return@postDelayed
                    }

                    starting = false
                    consecutiveErrors++

                    recreateRecognizer()

                    scheduleWakeRestart(
                        calculateBackoff()
                    )
                }

            },
            START_DELAY_MS
        )
    }

    // =========================================================
    // COMMAND LISTENING
    // =========================================================

    private fun startCommandListening() {

        if (!running) return

        if (starting) return

        if (!createRecognizerIfNeeded()) {

            finishCommand()

            return
        }

        cancelRestart()

        mode = Mode.COMMAND
        starting = true

        cancelCommandTimeout()

        val myGeneration = ++generation

        safeCancelRecognizer()

        /*
         * Give Android a tiny amount of time to complete
         * cancellation before starting the next recognition.
         */
        handler.postDelayed(
            {

                if (!running) return@postDelayed

                if (myGeneration != generation) {
                    return@postDelayed
                }

                if (mode != Mode.COMMAND) {
                    return@postDelayed
                }

                try {

                    recognizer?.startListening(
                        buildIntent()
                    )

                    commandTimeoutRunnable =
                        Runnable {

                            if (
                                running &&
                                mode == Mode.COMMAND &&
                                myGeneration == generation
                            ) {
                                finishCommand()
                            }
                        }

                    handler.postDelayed(
                        commandTimeoutRunnable!!,
                        COMMAND_TIMEOUT_MS
                    )

                } catch (_: Exception) {

                    starting = false
                    finishCommand()
                }

            },
            START_DELAY_MS
        )
    }

    // =========================================================
    // WAKE RESULT
    // =========================================================

    private fun handleWakeResult(
        rawText: String
    ) {

        val command =
            extractWakeCommand(rawText)

        if (command == null) {

            scheduleWakeRestart(
                calculateBackoff()
            )

            return
        }

        val now = System.currentTimeMillis()

        /*
         * Prevent immediate duplicate wake events.
         */
        if (
            now - lastWakeTime <
            WAKE_DEBOUNCE_MS
        ) {
            scheduleWakeRestart(250L)
            return
        }

        lastWakeTime = now
        consecutiveErrors = 0

        callbacks.onWakeDetected(
            command
        )

        if (command.isNotBlank()) {

            if (isDuplicateCommand(command)) {
                finishCommand()
                return
            }

            rememberCommand(command)

            callbacks.onCommandRecognized(
                command
            )

            finishCommand()

        } else {

            startCommandListening()
        }
    }

    // =========================================================
    // COMMAND RESULT
    // =========================================================

    private fun handleCommandResult(
        rawText: String
    ) {

        cancelCommandTimeout()

        val command =
            normalizeCommand(rawText)

        if (command.isBlank()) {

            finishCommand()

            return
        }

        if (isDuplicateCommand(command)) {

            finishCommand()

            return
        }

        rememberCommand(command)

        callbacks.onCommandRecognized(
            command
        )

        finishCommand()
    }

    // =========================================================
    // COMMAND FINISH
    // =========================================================

    private fun finishCommand() {

        cancelCommandTimeout()

        invalidateSession()

        safeCancelRecognizer()

        starting = false
        mode = Mode.IDLE

        callbacks.onListeningChanged(
            false,
            Mode.IDLE
        )

        if (!running) return

        /*
         * Small cooldown prevents:
         *
         * AURIX command
         * ↓
         * TTS
         * ↓
         * recognizer hears itself
         *
         * from immediately causing another cycle.
         */
        scheduleWakeRestart(700L)
    }

    // =========================================================
    // RECOVERY
    // =========================================================

    private fun scheduleRecovery() {

        if (!running) return

        val delay =
            calculateBackoff()

        /*
         * After several failures, recreate the recognizer.
         * This is important for OEM-specific SpeechRecognizer
         * failures where the same instance stops behaving.
         */
        if (consecutiveErrors >= 3) {
            recreateRecognizer()
        }

        scheduleWakeRestart(delay)
    }

    private fun calculateBackoff(): Long {

        val exponent =
            (consecutiveErrors - 1)
                .coerceAtLeast(0)
                .coerceAtMost(4)

        val delay =
            INITIAL_RESTART_MS *
                (1L shl exponent)

        return delay.coerceAtMost(
            MAX_RESTART_MS
        )
    }

    // =========================================================
    // RESTART
    // =========================================================

    private fun scheduleWakeRestart(
        delayMs: Long
    ) {

        if (!running) return

        cancelRestart()

        val safeDelay =
            delayMs.coerceAtLeast(100L)

        val runnable =
            Runnable {

                restartRunnable = null

                if (!running) return@Runnable

                if (mode == Mode.COMMAND) {
                    return@Runnable
                }

                startWakeListening()
            }

        restartRunnable = runnable

        handler.postDelayed(
            runnable,
            safeDelay
        )
    }

    private fun cancelRestart() {

        restartRunnable?.let {
            handler.removeCallbacks(it)
        }

        restartRunnable = null
    }

    // =========================================================
    // COMMAND TIMEOUT
    // =========================================================

    private fun cancelCommandTimeout() {

        commandTimeoutRunnable?.let {
            handler.removeCallbacks(it)
        }

        commandTimeoutRunnable = null
    }

    // =========================================================
    // SESSION CONTROL
    // =========================================================

    private fun invalidateSession() {
        generation++
    }

    private fun isCurrentSession(): Boolean {

        return running
    }

    // =========================================================
    // RECOGNIZER CONTROL
    // =========================================================

    private fun safeCancelRecognizer() {

        try {
            recognizer?.cancel()
        } catch (_: Exception) {
        }
    }

    private fun recreateRecognizer() {

        safeCancelRecognizer()

        try {
            recognizer?.destroy()
        } catch (_: Exception) {
        }

        recognizer = null
        starting = false

        createRecognizerIfNeeded()
    }

    // =========================================================
    // DUPLICATE PROTECTION
    // =========================================================

    private fun isDuplicateCommand(
        command: String
    ): Boolean {

        val normalized =
            normalizeCommand(command)

        if (normalized.isBlank()) {
            return true
        }

        val now =
            System.currentTimeMillis()

        return normalized == lastCommand &&
            now - lastCommandTime <
            COMMAND_DEBOUNCE_MS
    }

    private fun rememberCommand(
        command: String
    ) {

        lastCommand =
            normalizeCommand(command)

        lastCommandTime =
            System.currentTimeMillis()
    }

    // =========================================================
    // WAKE EXTRACTION
    // =========================================================

    private fun normalizeWakeText(
        value: String
    ): String {

        return value
            .lowercase(Locale.ENGLISH)
            .replace("’", "'")
            .replace(
                Regex("[^a-z0-9]+"),
                " "
            )
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()
    }

    /**
     * Current strict wake behavior:
     *
     * "Aurix"
     * "Aurix play music"
     *
     * accepted.
     *
     * "Hey Aurix"
     * "Hi Aurix"
     *
     * rejected.
     */
    private fun extractWakeCommand(
        value: String
    ): String? {

        val text =
            normalizeWakeText(value)

        if (text.isBlank()) {
            return null
        }

        val tokens =
            text.split(" ")

        if (
            tokens.firstOrNull()
                !in wakeVariants
        ) {
            return null
        }

        return tokens
            .drop(1)
            .joinToString(" ")
            .trim()
    }

    private fun normalizeCommand(
        value: String
    ): String {

        return value
            .lowercase(Locale.ENGLISH)
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()
    }

    // =========================================================
    // STOP
    // =========================================================

    private fun stopInternal() {

        running = false

        cancelRestart()
        cancelCommandTimeout()

        invalidateSession()

        starting = false
        mode = Mode.IDLE

        safeCancelRecognizer()

        try {
            recognizer?.destroy()
        } catch (_: Exception) {
        }

        recognizer = null

        callbacks.onListeningChanged(
            false,
            Mode.IDLE
        )
    }

    // =========================================================
    // MAIN THREAD
    // =========================================================

    private fun runOnMain(
        block: () -> Unit
    ) {

        if (Looper.myLooper() ==
            Looper.getMainLooper()
        ) {
            block()
        } else {
            handler.post(block)
        }
    }
}
