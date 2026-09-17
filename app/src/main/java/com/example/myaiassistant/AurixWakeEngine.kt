package com.example.myaiassistant

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.util.Locale

/**
 * AURIX passive voice engine using Vosk.
 *
 * Flow:
 *   continuous Vosk listening -> wake word -> command window -> final command
 *
 * The callback contract intentionally matches the existing AurixService so the
 * command pipeline does not need to change.
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

    enum class Mode { IDLE, WAKE, COMMAND }

    companion object {
        private const val MODEL_ASSET = "model-en-in"
        private const val MODEL_DEST = "aurix-vosk-model"
        private const val SAMPLE_RATE = 16000f
        private const val COMMAND_TIMEOUT_MS = 10000L
        private const val RESTART_DELAY_MS = 600L
    }

    private val handler = Handler(Looper.getMainLooper())

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null

    private var running = false
    private var modelLoading = false
    private var mode = Mode.IDLE
    private var wakeLatched = false
    private var commandDispatched = false
    private var lastDispatchedCommand = ""
    private var lastDispatchedAt = 0L

    private var commandTimeoutRunnable: Runnable? = null
    private var restartRunnable: Runnable? = null

    private val wakeVariants = setOf(
        "aurix", "auriks", "aurics", "aurik", "aurixx",
        "auryx", "aurex", "orix", "oryx", "ourix",
        "arix", "auric", "aurrix", "aurixs", "aurek"
    )

    fun start() {
        if (running) return
        running = true
        loadModelAndStart()
    }

    fun stop() {
        running = false
        mode = Mode.IDLE
        wakeLatched = false
        commandDispatched = false
        modelLoading = false

        commandTimeoutRunnable?.let(handler::removeCallbacks)
        restartRunnable?.let(handler::removeCallbacks)
        commandTimeoutRunnable = null
        restartRunnable = null

        stopSpeech()
        callbacks.onListeningChanged(false, Mode.IDLE)
    }

    /**
     * Temporarily pause passive recognition while AURIX is speaking
     * or another audio operation owns the microphone.
     */
    fun pause() {
        if (!running) return

        commandTimeoutRunnable?.let(handler::removeCallbacks)
        commandTimeoutRunnable = null
        restartRunnable?.let(handler::removeCallbacks)
        restartRunnable = null

        wakeLatched = false
        commandDispatched = false
        mode = Mode.IDLE

        stopSpeech()
        callbacks.onListeningChanged(false, Mode.IDLE)
    }

    /**
     * Resume passive wake recognition after a temporary pause.
     */
    fun resume() {
        if (!running) return

        wakeLatched = false
        commandDispatched = false
        mode = Mode.IDLE

        commandTimeoutRunnable?.let(handler::removeCallbacks)
        commandTimeoutRunnable = null

        restartWakeListening(150L)
    }

    fun resumeWakeListening() {
        if (!running) return
        wakeLatched = false
        commandDispatched = false
        mode = Mode.IDLE
        commandTimeoutRunnable?.let(handler::removeCallbacks)
        commandTimeoutRunnable = null
        restartWakeListening(150L)
    }

    private fun loadModelAndStart() {
        if (!running) return
        if (model != null) {
            startWakeListening()
            return
        }
        if (modelLoading) return

        modelLoading = true
        callbacks.onListeningChanged(false, Mode.IDLE)

        try {
            StorageService.unpack(
                context,
                MODEL_ASSET,
                MODEL_DEST,
                { loadedModel ->
                    handler.post {
                        modelLoading = false
                        if (!running) {
                            try { loadedModel.close() } catch (_: Exception) {}
                            return@post
                        }
                        model = loadedModel
                        startWakeListening()
                    }
                },
                { exception ->
                    handler.post {
                        modelLoading = false
                        callbacks.onWakeError(-20)
                        if (running) restartWakeListening(2000L)
                    }
                }
            )
        } catch (_: Exception) {
            modelLoading = false
            callbacks.onWakeError(-20)
            if (running) restartWakeListening(2000L)
        }
    }

    private fun startWakeListening() {

    if (!running || model == null) return

    stopSpeech()

    try {

        recognizer =
            Recognizer(
                model,
                SAMPLE_RATE
            )

        speechService =
            SpeechService(
                recognizer,
                SAMPLE_RATE
            )

        mode =
            Mode.WAKE

        wakeLatched =
            false

        commandDispatched =
            false

        callbacks.onListeningChanged(
            true,
            Mode.WAKE
        )

        speechService?.startListening(
            voskListener
        )

    } catch (_: Exception) {

        stopSpeech()

        callbacks.onWakeError(
            -21
        )

        restartWakeListening(
            1200L
        )
    }
    }
    private val voskListener = object : RecognitionListener {
        override fun onPartialResult(hypothesis: String?) {
            if (!running) return
            val text = extractText(hypothesis)
            if (text.isBlank()) return

            if (mode == Mode.WAKE && !wakeLatched) {
                val command = extractWakeCommand(text)
                if (command != null) {
                    latchWake(command)
                }
            }
        }

        override fun onResult(hypothesis: String?) {
            if (!running) return
            val text = extractText(hypothesis)
            if (text.isBlank()) return
            handleResult(text)
        }

        override fun onFinalResult(hypothesis: String?) {
            if (!running) return
            val text = extractText(hypothesis)
            if (text.isBlank()) return
            handleResult(text)
        }

        override fun onError(exception: Exception?) {
            if (!running) return
            callbacks.onWakeError(-22)
            stopSpeech()
            if (mode == Mode.COMMAND) {
                finishCommandAndReturnToWake()
            } else {
                restartWakeListening(900L)
            }
        }

        override fun onTimeout() {
            if (!running) return
            if (mode == Mode.COMMAND) {
                finishCommandAndReturnToWake()
            } else {
                restartWakeListening(300L)
            }
        }
    }

    private fun handleResult(text: String) {
        if (!running) return

        if (mode == Mode.WAKE) {
            val command = extractWakeCommand(text)
            if (command != null) {
                latchWake(command)
            }
            return
        }

        if (mode == Mode.COMMAND && wakeLatched) {
            val command = normalizeCommand(text)
            if (command.isNotBlank()) {
                dispatchCommandOnce(command)
            }
        }
    }

    private fun latchWake(commandAfterWake: String) {
        if (!running || wakeLatched) return
        wakeLatched = true
        mode = Mode.COMMAND
        commandDispatched = false

        callbacks.onWakeDetected(commandAfterWake)

        if (commandAfterWake.isNotBlank()) {
            dispatchCommandOnce(commandAfterWake)
            finishCommandAndReturnToWake()
            return
        }

        callbacks.onListeningChanged(true, Mode.COMMAND)
        armCommandTimeout()
    }

    private fun dispatchCommandOnce(command: String) {
        if (!running || commandDispatched) return

        val normalized = normalizeCommand(command)
        if (normalized.isBlank()) return

        val now = System.currentTimeMillis()
        if (normalized == lastDispatchedCommand && now - lastDispatchedAt < 1800L) {
            return
        }

        commandDispatched = true
        lastDispatchedCommand = normalized
        lastDispatchedAt = now
        callbacks.onCommandRecognized(normalized)
    }

    private fun armCommandTimeout() {
        commandTimeoutRunnable?.let(handler::removeCallbacks)
        val runnable = Runnable {
            commandTimeoutRunnable = null
            if (!running || mode != Mode.COMMAND || commandDispatched) return@Runnable
            callbacks.onWakeError(1001)
            finishCommandAndReturnToWake()
        }
        commandTimeoutRunnable = runnable
        handler.postDelayed(runnable, COMMAND_TIMEOUT_MS)
    }

    private fun finishCommandAndReturnToWake() {
        commandTimeoutRunnable?.let(handler::removeCallbacks)
        commandTimeoutRunnable = null
        wakeLatched = false
        commandDispatched = false
        mode = Mode.IDLE
        stopSpeech()
        callbacks.onListeningChanged(false, Mode.IDLE)
        restartWakeListening(RESTART_DELAY_MS)
    }

    private fun restartWakeListening(delayMs: Long) {
        if (!running) return
        restartRunnable?.let(handler::removeCallbacks)

        val runnable = Runnable {
            restartRunnable = null
            if (!running) return@Runnable
            if (model == null) {
                loadModelAndStart()
            } else {
                startWakeListening()
            }
        }
        restartRunnable = runnable
        handler.postDelayed(runnable, delayMs)
    }

    private fun stopSpeech() {
        try { speechService?.cancel() } catch (_: Exception) {}
        try { speechService?.shutdown() } catch (_: Exception) {}
        try { recognizer?.close() } catch (_: Exception) {}
        speechService = null
        recognizer = null
    }

    private fun musicIsActive(): Boolean {
        return try {
            val audioManager =
                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.isMusicActive
        } catch (_: Exception) {
            false
        }
    }

    private fun extractText(value: String?): String {
        if (value.isNullOrBlank()) return ""
        val raw = value.trim()
        return try {
            val json = JSONObject(raw)
            when {
                json.has("text") -> json.optString("text")
                json.has("partial") -> json.optString("partial")
                else -> raw
            }
        } catch (_: Exception) {
            raw
                .replace("{", " ")
                .replace("}", " ")
                .replace("\"text\"", " ")
                .replace("\"partial\"", " ")
                .replace(":", " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
    }

    private fun normalize(value: String): String {
        return value
            .lowercase(Locale.ENGLISH)
            .replace("’", "'")
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun extractWakeCommand(value: String): String? {
        val text = normalize(value)
        if (text.isBlank()) return null

        val tokens = text.split(" ")
        val wakeIndex = tokens.indexOfFirst { it in wakeVariants }
        if (wakeIndex < 0) return null

        return tokens.drop(wakeIndex + 1).joinToString(" ").trim()
    }

    private fun normalizeCommand(value: String): String {
        return value
            .lowercase(Locale.ENGLISH)
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
