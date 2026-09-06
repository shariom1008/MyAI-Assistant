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
    // SPEECH RECOGNITION
    // =========================================================

    private fun setupRecognizer() {

        if (
            !SpeechRecognizer.isRecognitionAvailable(this)
        ) {

            sendStatus(
                "VOICE NOT AVAILABLE"
            )

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

                        sendStatus(
                            "LISTENING"
                        )

                        updateNotification(
                            "Listening..."
                        )
                    }
                }

                override fun onBeginningOfSpeech() {

                    sendStatus(
                        "LISTENING"
                    )
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

                    sendStatus(
                        "PROCESSING"
                    )
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

                    if (
                        text.isNotEmpty()
                    ) {

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

            sendStatus(
                "VOICE NOT AVAILABLE"
            )

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

            recognizer?.startListening(
                intent
            )

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

        val command =
            originalCommand
                .lowercase(Locale.getDefault())
                .trim()

        if (command.isEmpty()) {
            return
        }

        // =====================================================
        // STOP AURIX
        // =====================================================

        if (
            command.contains("stop listening") ||
            command.contains("deactivate aurix") ||
            command.contains("stop aurix") ||
            command == "stop"
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
        // CLOSE APP / HOME
        // =====================================================

        if (
            command.startsWith("close ") ||
            command.startsWith("exit ") ||
            command.contains("close calculator") ||
            command.contains("close chrome") ||
            command.contains("close youtube") ||
            command.contains("close gallery") ||
            command.contains("close music") ||
            command.contains("close camera") ||
            command.contains("close settings")
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
            command.contains("torch") ||
            command.contains("flash")
        ) {

            if (
                command.contains("off") ||
                command.contains("disable") ||
                command.contains("turn off") ||
                command.contains("band") ||
                command.contains("close")
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
            command.contains("set a timer") ||
            command.contains("minute timer") ||
            command.contains("second timer")
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
            command.contains("what is today's date") ||
            command.contains("what's today's date") ||
            command.contains("today's date") ||
            command.contains("todays date") ||
            command.contains("tell me the date") ||
            command.contains("what date is today") ||
            command.contains("which date is today") ||
            command.contains("today date") ||
            command.contains("aaj ki date") ||
            command.contains("aaj ki tareekh")
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
        // =====================================================

        if (
            command == "time" ||
            command.contains("what time") ||
            command.contains("current time") ||
            command.contains("tell me the time") ||
            command.contains("what is the time") ||
            command.contains("what's the time") ||
            command.contains("time is it") ||
            command.contains("what time is it") ||
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
       
