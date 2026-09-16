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
 * AURIX V4 Wake Engine
 *
 * Architecture:
 *
 *     IDLE
 *       ↓
 *     WAKE LISTENING
 *       ↓
 *     WAKE DETECTED
 *       ↓
 *     COMMAND LISTENING
 *       ↓
 *     COMMAND RESULT
 *       ↓
 *     IDLE
 *       ↓
 *     NEW WAKE SESSION
 *
 * Important:
 * - Every recognition session gets its own SpeechRecognizer.
 * - Old recognizer is destroyed before a new one is created.
 * - Each session has its own token.
 * - No musicIsActive() blocking.
 * - Service remains the single owner of the passive wake engine.
 */
class AurixWakeEngine(
    private val context: Context,
    private val callbacks: Callbacks
) {

    interface Callbacks {
        fun onWakeDetected(commandAfterWake: String)
        fun onCommandRecognized(command: String)
        fun onListeningChanged(
            listening: Boolean,
            mode: Mode
        )
        fun onWakeError(errorCode: Int)
    }

    enum class Mode {
        IDLE,
        WAKE,
        COMMAND
    }

    private val handler =
        Handler(Looper.getMainLooper())

    private var recognizer: SpeechRecognizer? = null

    private var mode = Mode.IDLE

    private var running = false

    private var starting = false

    /**
     * Unique generation for every recognition session.
     */
    private var generation = 0L

    /**
     * Used to prevent multiple restart callbacks.
     */
    private var restartRunnable: Runnable? = null

    /**
     * Used for command timeout.
     */
    private var commandTimeoutRunnable: Runnable? = null

    /**
     * Prevents duplicate command callbacks.
     */
    private var commandDelivered = false

    /**
     * Prevents rapid restart loops.
     */
    private var consecutiveErrors = 0

    /**
     * When true, recognizer must stay completely paused.
     *
     * Useful while AURIX is speaking.
     */
    private var paused = false

    /**
     * Wake-word variants.
     *
     * Android speech recognition may return slightly different
     * spellings of "Aurix".
     */
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

    // =========================================================
    // PUBLIC API
    // =========================================================

    fun start() {
        runOnMain {
            if (running) {
                return@runOnMain
            }

            running = true
            paused = false
            consecutiveErrors = 0

            beginWakeSession(250L)
        }
    }

    fun stop() {
        runOnMain {
            running = false
            paused = false
            starting = false

            mode = Mode.IDLE

            generation++

            cancelRestart()

            cancelCommandTimeout()

            destroyRecognizer()

            callbacks.onListeningChanged(
                false,
                Mode.IDLE
            )
        }
    }

    /**
     * Pause all recognition.
     *
     * Used by AurixService while TTS is speaking.
     */
    fun pause() {
        runOnMain {
            if (!running) {
                return@runOnMain
            }

            paused = true

            starting = false
            mode = Mode.IDLE

            generation++

            cancelRestart()
            cancelCommandTimeout()

            destroyRecognizer()

            callbacks.onListeningChanged(
                false,
                Mode.IDLE
            )
        }
    }

    /**
     * Resume passive wake listening.
     */
    fun resume() {
        runOnMain {
            if (!running) {
                return@runOnMain
            }

            paused = false
            starting = false

            beginWakeSession(350L)
        }
    }

    /**
     * Kept for compatibility with the existing service.
     */
    fun resumeWakeListening() {
        resume()
    }

    // =========================================================
    // SESSION MANAGEMENT
    // =========================================================

    private fun beginWakeSession(
        delayMs: Long
    ) {
        if (!running || paused) {
            return
        }

        cancelRestart()
        cancelCommandTimeout()

        starting = false
        mode = Mode.IDLE

        destroyRecognizer()

        scheduleRestart(delayMs)
    }

    private fun beginCommandSession() {
        if (!running || paused) {
            return
        }

        cancelRestart()
        cancelCommandTimeout()

        starting = false
        mode = Mode.IDLE

        destroyRecognizer()

        scheduleStartCommand(150L)
    }

    private fun scheduleStartCommand(
        delayMs: Long
    ) {
        val myGeneration = ++generation

        handler.postDelayed(
            {
                if (!running ||
                    paused ||
                    myGeneration != generation
                ) {
                    return@postDelayed
                }

                startRecognitionSession(
                    Mode.COMMAND
                )
            },
            delayMs
        )
    }

    private fun scheduleRestart(
        delayMs: Long
    ) {
        cancelRestart()

        if (!running || paused) {
            return
        }

        restartRunnable =
            Runnable {
                restartRunnable = null

                if (!running || paused) {
                    return@Runnable
                }

                startRecognitionSession(
                    Mode.WAKE
                )
            }

        handler.postDelayed(
            restartRunnable!!,
            delayMs
        )
    }

    private fun cancelRestart() {
        restartRunnable?.let {
            handler.removeCallbacks(it)
        }

        restartRunnable = null
    }

    // =========================================================
    // RECOGNIZER CREATION
    // =========================================================

    private fun createRecognizer(): SpeechRecognizer? {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {

            callbacks.onWakeError(
                -1
            )

            return null
        }

        return try {

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S &&
                SpeechRecognizer
                    .isOnDeviceRecognitionAvailable(context)
            ) {

                SpeechRecognizer
                    .createOnDeviceSpeechRecognizer(
                        context
                    )

            } else {

                SpeechRecognizer
                    .createSpeechRecognizer(
                        context
                    )
            }

        } catch (e: Exception) {

            callbacks.onWakeError(
                -2
            )

            null
        }
    }

    private fun destroyRecognizer() {

        val oldRecognizer =
            recognizer

        recognizer = null

        if (oldRecognizer != null) {

            try {
                oldRecognizer.cancel()
            } catch (_: Exception) {
            }

            try {
                oldRecognizer.destroy()
            } catch (_: Exception) {
            }
        }
    }

    // =========================================================
    // START RECOGNITION SESSION
    // =========================================================

    private fun startRecognitionSession(
        sessionMode: Mode
    ) {

        if (!running || paused) {
            return
        }

        if (starting) {
            return
        }

        /**
         * Hard rule:
         *
         * Never reuse an old SpeechRecognizer.
         */
        destroyRecognizer()

        val newRecognizer =
            createRecognizer()
                ?: run {
                    scheduleRestart(
                        errorBackoff()
                    )
                    return
                }

        recognizer =
            newRecognizer

        mode =
            sessionMode

        starting = true

        commandDelivered = false

        val myGeneration =
            ++generation

        val sessionModeSnapshot =
            sessionMode

        newRecognizer.setRecognitionListener(
            object : RecognitionListener {

                /**
                 * This listener belongs ONLY to this
                 * recognizer instance and generation.
                 */
                private fun isCurrentSession(): Boolean {
                    return running &&
                        !paused &&
                        generation == myGeneration &&
                        recognizer === newRecognizer &&
                        mode == sessionModeSnapshot
                }

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {

                    if (!isCurrentSession()) {
                        return
                    }

                    starting = false

                    consecutiveErrors = 0

                    callbacks.onListeningChanged(
                        true,
                        sessionModeSnapshot
                    )
                }

                override fun onBeginningOfSpeech() {

                    if (!isCurrentSession()) {
                        return
                    }

                    callbacks.onListeningChanged(
                        true,
                        sessionModeSnapshot
                    )
                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) {
                    // Intentionally ignored.
                }

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                    // Intentionally ignored.
                }

                override fun onEndOfSpeech() {

                    if (!isCurrentSession()) {
                        return
                    }

                    callbacks.onListeningChanged(
                        false,
                        sessionModeSnapshot
                    )
                }

                override fun onError(
                    error: Int
                ) {

                    if (!isCurrentSession()) {
                        return
                    }

                    starting = false

                    consecutiveErrors++

                    callbacks.onListeningChanged(
                        false,
                        sessionModeSnapshot
                    )

                    callbacks.onWakeError(
                        error
                    )

                    destroyRecognizerForSession(
                        newRecognizer,
                        myGeneration
                    )

                    if (!running || paused) {
                        return
                    }

                    when (sessionModeSnapshot) {

                        Mode.WAKE -> {

                            scheduleRestart(
                                errorBackoff()
                            )
                        }

                        Mode.COMMAND -> {

                            finishCommand(
                                restartWake = true
                            )
                        }

                        Mode.IDLE -> {
                            // Nothing.
                        }
                    }
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    if (!isCurrentSession()) {
                        return
                    }

                    starting = false

                    val text =
                        results
                            ?.getStringArrayList(
                                SpeechRecognizer
                                    .RESULTS_RECOGNITION
                            )
                            ?.firstOrNull()
                            ?.trim()
                            .orEmpty()

                    destroyRecognizerForSession(
                        newRecognizer,
                        myGeneration
                    )

                    if (!running || paused) {
                        return
                    }

                    when (sessionModeSnapshot) {

                        Mode.WAKE -> {

                            handleWakeText(
                                text
                            )
                        }

                        Mode.COMMAND -> {

                            handleCommandText(
                                text
                            )
                        }

                        Mode.IDLE -> {
                            // Nothing.
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
                    // Not required.
                }
            }
        )

        try {

            newRecognizer.startListening(
                buildIntent()
            )

            if (
                sessionMode ==
                Mode.COMMAND
            ) {

                startCommandTimeout(
                    myGeneration
                )
            }

        } catch (_: Exception) {

            starting = false

            callbacks.onWakeError(
                -3
            )

            destroyRecognizerForSession(
                newRecognizer,
                myGeneration
            )

            if (
                sessionMode ==
                Mode.WAKE
            ) {

                scheduleRestart(
                    errorBackoff()
                )

            } else {

                finishCommand(
                    restartWake = true
                )
            }
        }
    }

    private fun destroyRecognizerForSession(
        sessionRecognizer: SpeechRecognizer,
        sessionGeneration: Long
    ) {

        if (
            recognizer === sessionRecognizer &&
            generation == sessionGeneration
        ) {

            recognizer = null

            try {
                sessionRecognizer.cancel()
            } catch (_: Exception) {
            }

            try {
                sessionRecognizer.destroy()
            } catch (_: Exception) {
            }
        }
    }

    // =========================================================
    // WAKE HANDLING
    // =========================================================

    private fun handleWakeText(
        rawText: String
    ) {

        if (!running || paused) {
            return
        }

        val command =
            extractWakeCommand(
                rawText
            )

        if (command == null) {

            /**
             * SpeechRecognizer heard something,
             * but it wasn't AURIX.
             */
            scheduleRestart(
                250L
            )

            return
        }

        callbacks.onWakeDetected(
            command
        )

        if (command.isNotBlank()) {

            /**
             * Example:
             *
             * "Aurix open YouTube"
             *
             * becomes:
             *
             * "open YouTube"
             */
            callbacks.onCommandRecognized(
                command
            )

            finishCommand(
                restartWake = true
            )

        } else {

            /**
             * User said only:
             *
             * "Aurix"
             *
             * Now listen for the actual command.
             */
            callbacks.onListeningChanged(
                false,
                Mode.WAKE
            )

            beginCommandSession()
        }
    }

    // =========================================================
    // COMMAND HANDLING
    // =========================================================

    private fun handleCommandText(
        rawText: String
    ) {

        if (!running || paused) {
            return
        }

        val command =
            normalizeCommand(
                rawText
            )

        if (
            command.isNotBlank() &&
            !commandDelivered
        ) {

            commandDelivered = true

            callbacks.onCommandRecognized(
                command
            )
        }

        finishCommand(
            restartWake = true
        )
    }

    private fun finishCommand(
        restartWake: Boolean
    ) {

        cancelCommandTimeout()

        starting = false

        if (!running) {

            mode = Mode.IDLE

            destroyRecognizer()

            return
        }

        mode = Mode.IDLE

        destroyRecognizer()

        callbacks.onListeningChanged(
            false,
            Mode.IDLE
        )

        if (
            restartWake &&
            !paused
        ) {

            scheduleRestart(
                500L
            )
        }
    }

    // =========================================================
    // COMMAND TIMEOUT
    // =========================================================

    private fun startCommandTimeout(
        myGeneration: Long
    ) {

        cancelCommandTimeout()

        commandTimeoutRunnable =
            Runnable {

                if (
                    !running ||
                    paused ||
                    generation != myGeneration ||
                    mode != Mode.COMMAND
                ) {
                    return@Runnable
                }

                starting = false

                callbacks.onWakeError(
                    1001
                )

                finishCommand(
                    restartWake = true
                )
            }

        handler.postDelayed(
            commandTimeoutRunnable!!,
            COMMAND_TIMEOUT_MS
        )
    }

    private fun cancelCommandTimeout() {

        commandTimeoutRunnable?.let {
            handler.removeCallbacks(it)
        }

        commandTimeoutRunnable = null
    }

    // =========================================================
    // WAKE WORD EXTRACTION
    // =========================================================

    private fun extractWakeCommand(
        value: String
    ): String? {

        val text =
            normalizeWakeText(
                value
            )

        if (text.isBlank()) {
            return null
        }

        val tokens =
            text.split(
                " "
            )

        if (tokens.isEmpty()) {
            return null
        }

        /**
         * Exact first-token wake word.
         *
         * Accepted:
         *
         * Aurix
         * Aurix open YouTube
         * Auriks open YouTube
         *
         * Not accepted:
         *
         * Hey Aurix
         * Hello Aurix
         *
         * This avoids random speech triggering AURIX.
         */
        val firstToken =
            tokens.firstOrNull()

        if (
            firstToken !in wakeVariants
        ) {
            return null
        }

        return tokens
            .drop(1)
            .joinToString(
                " "
            )
            .trim()
    }

    private fun normalizeWakeText(
        value: String
    ): String {

        return value
            .lowercase(
                Locale.ENGLISH
            )
            .replace(
                "’",
                "'"
            )
            .replace(
                Regex(
                    "[^a-z0-9]+"
                ),
                " "
            )
            .replace(
                Regex(
                    "\\s+"
                ),
                " "
            )
            .trim()
    }

    private fun normalizeCommand(
        value: String
    ): String {

        return value
            .lowercase(
                Locale.ENGLISH
            )
            .replace(
                Regex(
                    "\\s+"
                ),
                " "
            )
            .trim()
    }

    // =========================================================
    // SPEECH INTENT
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
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
            )

            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            )

            putExtra(
                RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.packageName
            )
        }
    }

    // =========================================================
    // ERROR BACKOFF
    // =========================================================

    private fun errorBackoff(): Long {

        return when {

            consecutiveErrors <= 1 ->
                700L

            consecutiveErrors == 2 ->
                1200L

            consecutiveErrors == 3 ->
                2000L

            consecutiveErrors == 4 ->
                3500L

            else ->
                5000L
        }
    }

    // =========================================================
    // MAIN THREAD
    // =========================================================

    private fun runOnMain(
        block: () -> Unit
    ) {

        if (
            Looper.myLooper() ==
            Looper.getMainLooper()
        ) {

            block()

        } else {

            handler.post(
                block
            )
        }
    }

    companion object {

        private const val
            COMMAND_TIMEOUT_MS =
            6000L
    }
}
