package com.example.myaiassistant

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * AURIX — Stable Wake + Command Voice Engine
 *
 * Wake mode listens for "Aurix".
 * Command mode uses the normal SpeechRecognizer service so Hindi/Hinglish
 * commands and song names are not limited by an on-device ASR model.
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

    // Common ASR spellings of AURIX, including Hindi-script variants.
    private val wakeVariants = setOf(
        "aurix", "auriks", "aurics", "aurik", "aurixx",
        "auryx", "aurex", "orix", "oryx", "ourix",
        "arix", "auric", "aurrix", "aurixs", "aurek",
        "ऑरिक्स", "औरिक्स", "ओरिक्स", "अरिक्स"
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

    fun pause() {
        if (!running) return
        cancelCurrentSession()
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

        // IMPORTANT:
        // Do NOT force createOnDeviceSpeechRecognizer() here.
        // The tap/manual voice path already uses the normal recognizer and
        // handles Hindi/Hinglish song names better. Wake command recognition
        // should use the same ASR path for consistent results.
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).also { sr ->
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

                    val text = bestResult(results)

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

    /**
     * Select the highest-confidence recognition candidate when the recognizer
     * provides confidence values; otherwise use the first candidate.
     */
    private fun bestResult(results: Bundle?): String {
        val candidates = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            .orEmpty()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (candidates.isEmpty()) return ""

        val confidence = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
        if (confidence == null || confidence.isEmpty()) {
            return candidates.first()
        }

        var bestIndex = 0
        var bestScore = Float.NEGATIVE_INFINITY
        candidates.indices.forEach { index ->
            val score = confidence.getOrNull(index) ?: Float.NEGATIVE_INFINITY
            if (score > bestScore) {
                bestScore = score
                bestIndex = index
            }
        }

        return candidates.getOrElse(bestIndex) { candidates.first() }
    }

    private fun buildIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // Hindi/Hinglish command recognition. This matches the working
            // manual/tap recognizer used by AurixService.
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

            // Give a spoken command a little more room before the recognizer
            // decides that the utterance is complete.
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                1400L
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                1800L
            )
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

        try {
            recognizer?.cancel()
        } catch (_: Exception) {}

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

        try {
            recognizer?.cancel()
        } catch (_: Exception) {}

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
            callbacks.onCommandRecognized(normalizeCommand(command))
            finishCommand()
        } else {
            startCommandListening()
        }
    }

    private fun handleCommandText(rawText: String) {
        val command = normalizeCommand(rawText)
        if (command.isNotBlank()) {
            callbacks.onCommandRecognized(command)
        }
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

        try {
            recognizer?.cancel()
        } catch (_: Exception) {}

        callbacks.onListeningChanged(false, Mode.IDLE)
        scheduleWakeRestart(400L)
    }

    private fun cancelCurrentSession() {
        starting = false
        sessionId++
        try {
            recognizer?.cancel()
        } catch (_: Exception) {}
        mode = Mode.IDLE
        callbacks.onListeningChanged(false, Mode.IDLE)
    }

    private fun scheduleWakeRestart(delayMs: Long) {
        if (!running) return
        restartRunnable?.let(handler::removeCallbacks)

        val runnable = Runnable {
            restartRunnable = null
            if (running && mode != Mode.COMMAND) {
                startWakeListening()
            }
        }

        restartRunnable = runnable
        handler.postDelayed(runnable, delayMs)
    }

    private fun normalizeWakeText(value: String): String =
        value.lowercase(Locale.ENGLISH)
            .replace("’", "'")
            // Keep Unicode letters/numbers so Hindi-script ASR output is not
            // deleted before wake-word matching.
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    /**
     * Accepted:
     *   "Aurix"
     *   "Aurix play music"
     *
     * Deliberately does not accept arbitrary leading phrases such as
     * "Hey Aurix" so background audio is less likely to trigger AURIX.
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
