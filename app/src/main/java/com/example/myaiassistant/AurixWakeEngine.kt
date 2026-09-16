package com.example.myaiassistant

import android.content.Context
import android.content.Intent
import android.os.Build
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * AURIX — Original + Stable Wake Engine
 *
 * AURIX passive wake engine.
 * Designed to be owned by AurixService so only one passive SpeechRecognizer
 * session exists at a time. Wake word is strictly "Aurix" with controlled ASR variants.
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

    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var mode = Mode.IDLE
    private var running = false
    private var starting = false
    private var sessionId = 0L
    private var restartRunnable: Runnable? = null

    private val wakeVariants = setOf(
        "aurix", "auriks", "aurics", "aurik", "aurixx",
        "auryx", "aurex", "orix", "oryx", "ourix",
        "arix", "auric", "aurrix", "aurixs", "aurek"
    )

    fun start() {
        if (running) return
        running = true
        createRecognizerIfNeeded()
        startWakeListening()
    }

    fun stop() {
        running = false
        mode = Mode.IDLE
        starting = false
        sessionId++
        restartRunnable?.let(handler::removeCallbacks)
        restartRunnable = null
        try {
            recognizer?.cancel()
            recognizer?.destroy()
        } catch (_: Exception) {}
        recognizer = null
        callbacks.onListeningChanged(false, Mode.IDLE)
    }

    fun resumeWakeListening() {
        if (!running) return
        cancelCurrentSession()
        scheduleWakeRestart(250L)
    }

    private fun createRecognizerIfNeeded() {
        if (recognizer != null) return
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            callbacks.onWakeError(-1)
            return
        }

        recognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }.also { sr ->
            sr.setRecognitionListener(object : RecognitionListener {
                private fun current(id: Long) = id == sessionId && running

                override fun onReadyForSpeech(params: Bundle?) {
                    starting = false
                    callbacks.onListeningChanged(true, mode)
                }

                override fun onBeginningOfSpeech() {
                    callbacks.onListeningChanged(true, mode)
                }

                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    callbacks.onListeningChanged(false, mode)
                }

                override fun onError(error: Int) {
                    starting = false
                    if (!current(sessionId)) return
                    callbacks.onWakeError(error)
                    if (!running) return

                    when (mode) {
                        Mode.WAKE -> scheduleWakeRestart(350L)
                        Mode.COMMAND -> finishCommand()
                        Mode.IDLE -> Unit
                    }
                }

                override fun onResults(results: Bundle?) {
                    starting = false
                    if (!current(sessionId)) return

                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()

                    // If media starts while a wake session is open, do not
                    // interpret media audio as a wake/command.
                    if (musicIsActive() && mode == Mode.WAKE) {
                        scheduleWakeRestart(3000L)
                        return
                    }

                    when (mode) {
                        Mode.WAKE -> handleWakeText(text)
                        Mode.COMMAND -> handleCommandText(text)
                        Mode.IDLE -> Unit
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }
    }

    private fun buildIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale("hi", "IN")
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                Locale("hi", "IN")
            )
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
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

    private fun startWakeListening() {
        if (!running || starting) return

        // Do not take the microphone while active media is playing.
        if (musicIsActive()) {
            scheduleWakeRestart(3000L)
            return
        }

        createRecognizerIfNeeded()
        if (recognizer == null) return

        restartRunnable?.let(handler::removeCallbacks)
        restartRunnable = null
        mode = Mode.WAKE
        starting = true
        val mySession = ++sessionId

        try { recognizer?.cancel() } catch (_: Exception) {}

        handler.postDelayed({
            if (!running || mySession != sessionId) return@postDelayed
            try {
                recognizer?.startListening(buildIntent())
            } catch (_: Exception) {
                starting = false
                scheduleWakeRestart(500L)
            }
        }, 80L)
    }

    private fun startCommandListening() {
        if (!running || starting) return
        createRecognizerIfNeeded()
        if (recognizer == null) return

        restartRunnable?.let(handler::removeCallbacks)
        restartRunnable = null
        mode = Mode.COMMAND
        starting = true
        val mySession = ++sessionId

        try { recognizer?.cancel() } catch (_: Exception) {}

        handler.postDelayed({
            if (!running || mySession != sessionId) return@postDelayed
            try {
                recognizer?.startListening(buildIntent())
            } catch (_: Exception) {
                starting = false
                finishCommand()
            }
        }, 80L)
    }

    private fun handleWakeText(rawText: String) {
        val command = extractWakeCommand(rawText) ?: run {
            scheduleWakeRestart(250L)
            return
        }

        callbacks.onWakeDetected(command)

        if (command.isNotBlank()) {
            callbacks.onCommandRecognized(command)
            finishCommand()
        } else {
            startCommandListening()
        }
    }

    private fun handleCommandText(rawText: String) {
        val command = normalizeCommand(rawText)
        if (command.isNotBlank()) callbacks.onCommandRecognized(command)
        finishCommand()
    }

    private fun finishCommand() {
        if (!running) {
            mode = Mode.IDLE
            return
        }

        mode = Mode.IDLE
        starting = false
        sessionId++

        try { recognizer?.cancel() } catch (_: Exception) {}
        callbacks.onListeningChanged(false, Mode.IDLE)

        scheduleWakeRestart(400L)
    }

    private fun cancelCurrentSession() {
        starting = false
        sessionId++
        try { recognizer?.cancel() } catch (_: Exception) {}
        mode = Mode.IDLE
        callbacks.onListeningChanged(false, Mode.IDLE)
    }

    private fun scheduleWakeRestart(delayMs: Long) {
        if (!running) return
        restartRunnable?.let(handler::removeCallbacks)

        val runnable = Runnable {
            restartRunnable = null
            if (running && mode != Mode.COMMAND) startWakeListening()
        }
        restartRunnable = runnable
        handler.postDelayed(runnable, delayMs)
    }

    private fun normalizeWakeText(value: String): String =
        value.lowercase(Locale.ENGLISH)
            .replace("’", "'")
            .replace(Regex("[^a-z0-9]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    /**
     * Strict:
     *   "Aurix"              -> accepted
     *   "Aurix play music"  -> accepted
     *   "Hey Aurix"         -> rejected
     *   "Hi Aurix"          -> rejected
     *   "Hello Aurix"       -> rejected
     *   "Hai Aurix"         -> rejected
     */
    private fun extractWakeCommand(value: String): String? {
        val text = normalizeWakeText(value)
        if (text.isBlank()) return null

        val tokens = text.split(" ")
        if (tokens.firstOrNull() !in wakeVariants) return null

        return tokens.drop(1).joinToString(" ").trim()
    }

    private fun normalizeCommand(value: String): String =
        value.lowercase(Locale.ENGLISH)
            .replace(Regex("\\s+"), " ")
            .trim()
}
