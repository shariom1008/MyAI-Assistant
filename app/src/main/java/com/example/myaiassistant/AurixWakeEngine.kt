package com.example.myaiassistant

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AURIX offline voice engine.
 *
 * IMPORTANT:
 * - This file intentionally does NOT use android.speech.SpeechRecognizer.
 * - Vosk handles microphone audio and offline speech recognition.
 * - Put a Vosk model directory named "model-en-in" in app/src/main/assets/.
 * - Existing AurixService command routing remains unchanged.
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
        private const val SAMPLE_RATE = 16000.0f
        private const val COMMAND_GRACE_MS = 8000L
        private const val RESTART_DELAY_MS = 250L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var recognizer: Recognizer? = null
    private var mode = Mode.IDLE
    private var running = false
    private var modelLoading = false
    private var commandTimeout: Runnable? = null
    private var lastWakeAt = 0L
    private val destroyed = AtomicBoolean(false)

    private val wakeVariants = setOf(
        "aurix", "auriks", "aurics", "aurik", "aurixx",
        "auryx", "aurex", "orix", "oryx", "ourix",
        "arix", "auric", "aurrix", "aurixs", "aurek"
    )

    fun start() {
        if (destroyed.get() || running) return
        running = true
        loadModelAndStart()
    }

    fun stop() {
        running = false
        mode = Mode.IDLE
        commandTimeout?.let(handler::removeCallbacks)
        commandTimeout = null
        try { speechService?.stop() } catch (_: Exception) {}
        try { speechService?.shutdown() } catch (_: Exception) {}
        speechService = null
        try { recognizer?.close() } catch (_: Exception) {}
        recognizer = null
        callbacks.onListeningChanged(false, Mode.IDLE)
    }

    fun pause() {
        if (!running) return
        stopListeningOnly()
    }

    fun resumeWakeListening() {
        if (!running) return
        stopListeningOnly()
        handler.postDelayed({ if (running) startRecognition() }, RESTART_DELAY_MS)
    }

    private fun loadModelAndStart() {
        if (model != null) {
            startRecognition()
            return
        }
        if (modelLoading) return
        modelLoading = true

        StorageService.unpack(
            context,
            MODEL_ASSET,
            "aurix-vosk-model",
            { loaded ->
                modelLoading = false
                model = loaded
                if (running) startRecognition()
            },
            { error ->
                modelLoading = false
                callbacks.onWakeError(-1001)
            }
        )
    }

    private fun startRecognition() {
        if (!running || model == null || speechService != null) return

        if (musicIsActive()) {
            handler.postDelayed({ if (running) startRecognition() }, 1500L)
            return
        }

        try {
            recognizer = Recognizer(model, SAMPLE_RATE)
            speechService = SpeechService(recognizer, SAMPLE_RATE)
            mode = Mode.WAKE
            callbacks.onListeningChanged(true, Mode.WAKE)
            speechService?.startListening(listener)
        } catch (_: Exception) {
            speechService = null
            recognizer = null
            callbacks.onWakeError(-1002)
            handler.postDelayed({ if (running) startRecognition() }, 1000L)
        }
    }

    private val listener = object : RecognitionListener {
        override fun onPartialResult(hypothesis: String?) {
            val text = extractText(hypothesis)
            if (text.isBlank()) return

            when (mode) {
                Mode.WAKE -> {
                    val command = extractWakeCommand(text)
                    if (command != null) {
                        lastWakeAt = System.currentTimeMillis()
                        callbacks.onWakeDetected(command)
                        if (command.isNotBlank()) {
                            callbacks.onCommandRecognized(command)
                            resetCommandWindow()
                        } else {
                            enterCommandMode()
                        }
                    }
                }
                Mode.COMMAND -> {
                    // Vosk partials are only used as a responsiveness signal.
                    // Final results are dispatched to the existing command router.
                    callbacks.onListeningChanged(true, Mode.COMMAND)
                }
                Mode.IDLE -> Unit
            }
        }

        override fun onResult(hypothesis: String?) {
            handleRecognizedText(extractText(hypothesis), false)
        }

        override fun onFinalResult(hypothesis: String?) {
            handleRecognizedText(extractText(hypothesis), true)
        }

        override fun onError(exception: Exception?) {
            if (!running) return
            callbacks.onWakeError(-1003)
            restartRecognition()
        }

        override fun onTimeout() {
            if (!running) return
            restartRecognition()
        }
    }

    private fun handleRecognizedText(text: String, finalResult: Boolean) {
        if (!running || text.isBlank()) return

        when (mode) {
            Mode.WAKE -> {
                val command = extractWakeCommand(text)
                if (command != null) {
                    lastWakeAt = System.currentTimeMillis()
                    callbacks.onWakeDetected(command)
                    if (command.isNotBlank()) {
                        callbacks.onCommandRecognized(command)
                        restartRecognition()
                    } else {
                        enterCommandMode()
                    }
                }
            }
            Mode.COMMAND -> {
                val command = normalizeCommand(text)
                if (command.isNotBlank()) {
                    callbacks.onCommandRecognized(command)
                    restartRecognition()
                } else if (finalResult) {
                    restartRecognition()
                }
            }
            Mode.IDLE -> Unit
        }
    }

    private fun enterCommandMode() {
        if (!running) return
        mode = Mode.COMMAND
        callbacks.onListeningChanged(true, Mode.COMMAND)
        resetCommandWindow()
    }

    private fun resetCommandWindow() {
        commandTimeout?.let(handler::removeCallbacks)
        val timeout = Runnable {
            if (running && mode == Mode.COMMAND) restartRecognition()
        }
        commandTimeout = timeout
        handler.postDelayed(timeout, COMMAND_GRACE_MS)
    }

    private fun restartRecognition() {
        if (!running) return
        commandTimeout?.let(handler::removeCallbacks)
        commandTimeout = null
        stopListeningOnly()
        handler.postDelayed({ if (running) startRecognition() }, RESTART_DELAY_MS)
    }

    private fun stopListeningOnly() {
        try { speechService?.stop() } catch (_: Exception) {}
        try { speechService?.shutdown() } catch (_: Exception) {}
        speechService = null
        try { recognizer?.close() } catch (_: Exception) {}
        recognizer = null
        mode = Mode.IDLE
        callbacks.onListeningChanged(false, Mode.IDLE)
    }

    private fun extractWakeCommand(value: String): String? {
        val text = normalizeCommand(value)
        if (text.isBlank()) return null

        val tokens = text.split(" ")
        val first = tokens.firstOrNull() ?: return null
        if (first !in wakeVariants) return null
        return tokens.drop(1).joinToString(" ").trim()
    }

    private fun extractText(hypothesis: String?): String {
        if (hypothesis.isNullOrBlank()) return ""
        return Regex("\\\"text\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"")
            .find(hypothesis)?.groupValues?.getOrNull(1)
            ?: hypothesis
                .replace(Regex("[{}\\\"]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
    }

    private fun normalizeCommand(value: String): String =
        value.lowercase(Locale.ENGLISH)
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun musicIsActive(): Boolean = try {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isMusicActive
    } catch (_: Exception) {
        false
    }
}
