package com.example.myaiassistant

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import java.net.URL
import java.util.Locale
import kotlin.math.max
import org.json.JSONObject

/**
 * AURIX application/device command layer.
 *
 * AurixService owns the service lifecycle, wake/listening pipeline,
 * command history and TTS. This class owns app launching and other
 * app/device actions that used to live inside AurixService.
 */
class AurixAppCommands(
    private val context: Context,
    private val speak: (String) -> Unit,
    private val status: (String) -> Unit = {}
) {

    private val packageManager: PackageManager
        get() = context.packageManager

    // =========================================================
    // MAIN COMMAND DISPATCH
    // =========================================================

    fun handle(rawCommand: String): Boolean {

        val command = normalizeBilingualCommand(rawCommand)

        if (command.isBlank()) {
            return false
        }

        // ---------------------------------------------------------
        // CAMERA
        // ---------------------------------------------------------

        if (
            command.contains("camera") ||
            command.contains("take a photo") ||
            command.contains("take photo") ||
            command.contains("photo kheecho") ||
            command.contains("tasveer kheecho") ||
            command.contains("camera kholo") ||
            command.contains("camera khol do")
        ) {
            status("EXECUTING")
            openCamera()
            return true
        }

        // ---------------------------------------------------------
        // GALLERY
        // ---------------------------------------------------------

        if (
            command.contains("gallery") ||
            command == "photos" ||
            command.contains("photo gallery") ||
            command.contains("gallery kholo") ||
            command.contains("photos dikhao") ||
            command.contains("tasveer dikhao")
        ) {
            status("EXECUTING")
            openGallery()
            return true
        }

        // ---------------------------------------------------------
        // MUSIC
        // ---------------------------------------------------------

        if (
            command == "music" ||
            command.contains("open music") ||
            command.contains("music app") ||
            command.contains("music kholo") ||
            command.contains("gaane kholo") ||
            command.contains("music chalao")
        ) {
            status("EXECUTING")
            openMusic()
            return true
        }

        // ---------------------------------------------------------
        // NOTES
        // ---------------------------------------------------------

        if (
            command.contains("notes") ||
            command.contains("note app") ||
            command.contains("notes kholo") ||
            command.contains("note kholo")
        ) {
            status("EXECUTING")
            openNotes()
            return true
        }

        // ---------------------------------------------------------
        // CALCULATOR
        // ---------------------------------------------------------

        if (
            command.contains("calculator") ||
            command.contains("calculate") ||
            command.contains("hisab karo") ||
            command.contains("hisab kar do") ||
            command.contains("ganit karo")
        ) {
            status("EXECUTING")
            openCalculator()
            return true
        }

        // ---------------------------------------------------------
        // YOUTUBE
        // ---------------------------------------------------------

        if (isYouTubeCommand(command)) {
            status("EXECUTING")
            handleYouTube(command)
            return true
        }

        // ---------------------------------------------------------
        // DIALER
        // ---------------------------------------------------------

        if (
            command == "dialer" ||
            command == "open dialer" ||
            command == "phone" ||
            command == "open phone" ||
            command == "phone kholo" ||
            command == "phone khol do" ||
            command == "dialer kholo"
        ) {
            status("EXECUTING")
            openPhone()
            return true
        }

        // ---------------------------------------------------------
        // SETTINGS
        // ---------------------------------------------------------

        if (
            command == "settings" ||
            command == "open settings" ||
            command == "phone settings" ||
            command == "settings kholo" ||
            command == "setting kholo" ||
            command == "settings khol do"
        ) {
            status("EXECUTING")
            openSettings()
            return true
        }

        // ---------------------------------------------------------
        // WIFI
        // ---------------------------------------------------------

        if (
            command.contains("wifi") ||
            command.contains("wi fi")
        ) {
            status("EXECUTING")
            openWifiSettings()
            return true
        }

        // ---------------------------------------------------------
        // VOLUME
        // ---------------------------------------------------------

        if (
            command.contains("volume up") ||
            command.contains("increase volume") ||
            command.contains("volume badhao")
        ) {
            status("EXECUTING")
            changeVolume(true)
            return true
        }

        if (
            command.contains("volume down") ||
            command.contains("decrease volume") ||
            command.contains("volume kam")
        ) {
            status("EXECUTING")
            changeVolume(false)
            return true
        }

        // ---------------------------------------------------------
        // MEDIA
        // ---------------------------------------------------------

        if (
            command == "play" ||
            command == "pause" ||
            command == "resume" ||
            command.contains("play music") ||
            command.contains("pause music") ||
            command.contains("resume music")
        ) {
            status("EXECUTING")
            controlMedia()
            return true
        }

        // ---------------------------------------------------------
        // BATTERY
        // ---------------------------------------------------------

        if (command.contains("battery")) {
            status("EXECUTING")
            tellBattery()
            return true
        }

        // ---------------------------------------------------------
        // CHROME
        // ---------------------------------------------------------

        if (
            command == "chrome" ||
            command == "open chrome" ||
            command == "launch chrome"
        ) {
            status("EXECUTING")
            openChrome()
            return true
        }

        // ---------------------------------------------------------
        // MAPS
        // ---------------------------------------------------------

        if (
            command == "maps" ||
            command == "open maps" ||
            command == "launch maps"
        ) {
            status("EXECUTING")
            openMaps()
            return true
        }

        // ---------------------------------------------------------
        // MAP SEARCH
        // ---------------------------------------------------------

        val mapQuery = extractMapsQuery(command)

        if (!mapQuery.isNullOrBlank()) {
            status("EXECUTING")
            searchMaps(mapQuery)
            return true
        }

        // ---------------------------------------------------------
        // GOOGLE SEARCH
        // ---------------------------------------------------------

        if (
            command.startsWith("search ") ||
            command.startsWith("google ") ||
            command.startsWith("search for ") ||
            command.startsWith("google search ")
        ) {

            val query =
                command
                    .removePrefix("search for ")
                    .removePrefix("search ")
                    .removePrefix("google search ")
                    .removePrefix("google ")
                    .trim()

            if (query.isNotBlank()) {
                status("EXECUTING")
                googleSearch(query)
            }

            return true
        }

        // ---------------------------------------------------------
        // GENERIC INSTALLED APP
        // ---------------------------------------------------------

        if (isAppOpenCommand(command)) {

            val appName = extractAppName(command)

            status("EXECUTING")
            openInstalledApp(appName)

            return true
        }

        return false
    }

    // =========================================================
    // AGENT APP ACTIONS
    // =========================================================

    /**
     * Used by AurixService's agent/local command executor.
     * Returns null when this class does not own the command.
     */
    fun executeForAgent(command: String): String? {

        val c = normalizeCommand(command)

        return try {

            when {

                c == "open youtube" ||
                    c == "youtube" -> {

                    openYouTube(speakResult = false)
                    "YouTube opened."
                }

                c == "open phone" ||
                    c == "open dialer" ||
                    c == "phone" -> {

                    openPhone(speakResult = false)
                    "Phone opened."
                }

                c == "open settings" ||
                    c == "settings" -> {

                    openSettings(speakResult = false)
                    "Settings opened."
                }

                isAppOpenCommand(c) -> {

                    val appName = extractAppName(c)
                    openInstalledApp(
                        appName,
                        speakResult = false
                    )

                    "${appName.ifBlank { "App" }} opened."
                }

                else -> null
            }

        } catch (_: Exception) {
            "I couldn't execute this step."
        }
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private fun openCamera() {

        val intents =
            listOf(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                Intent("android.media.action.STILL_IMAGE_CAMERA"),
                Intent("android.media.action.IMAGE_CAPTURE")
            )

        for (cameraIntent in intents) {

            try {

                cameraIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                )

                if (
                    cameraIntent.resolveActivity(
                        packageManager
                    ) != null
                ) {

                    context.startActivity(
                        cameraIntent
                    )

                    speak(
                        "Boss, camera khol raha hoon."
                    )

                    return
                }

            } catch (_: Exception) {
                // Try next camera intent.
            }
        }

        speak(
            "Boss, camera app nahi mil raha."
        )
    }

    // =========================================================
    // GALLERY
    // =========================================================

    private fun openGallery() {

        try {

            val intent =
                Intent(Intent.ACTION_VIEW).apply {

                    setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/*"
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            context.startActivity(intent)

            speak(
                "Opening gallery."
            )

        } catch (_: Exception) {

            speak(
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

        for (packageName in packages) {

            try {

                val intent =
                    packageManager
                        .getLaunchIntentForPackage(
                            packageName
                        )

                if (intent != null) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    context.startActivity(intent)

                    speak(
                        "Opening music."
                    )

                    return
                }

            } catch (_: Exception) {
            }
        }

        speak(
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

        for (packageName in packages) {

            try {

                val intent =
                    packageManager
                        .getLaunchIntentForPackage(
                            packageName
                        )

                if (intent != null) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    context.startActivity(intent)

                    speak(
                        "Opening notes."
                    )

                    return
                }

            } catch (_: Exception) {
            }
        }

        speak(
            "Notes app is not available."
        )
    }

    // =========================================================
    // CALCULATOR
    // =========================================================

    private fun openCalculator() {

        // Known OEM calculator packages first.
        val packages =
            arrayOf(
                "com.miui.calculator",
                "com.android.calculator2",
                "com.google.android.calculator",
                "com.sec.android.app.popupcalculator",
                "com.sec.android.app.calculator",
                "com.oneplus.calculator",
                "com.oppo.calculator",
                "com.coloros.calculator",
                "com.vivo.calculator",
                "com.realme.calculator",
                "com.motorola.calculator",
                "com.asus.calculator"
            )

        for (packageName in packages) {

            try {

                val intent =
                    packageManager
                        .getLaunchIntentForPackage(
                            packageName
                        )

                if (intent != null) {

                    intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )

                    context.startActivity(intent)

                    speak(
                        "Opening calculator."
                    )

                    return
                }

            } catch (_: Exception) {
            }
        }

        // Generic Android calculator category.
        try {

            val calculatorIntent =
                Intent(Intent.ACTION_MAIN).apply {

                    addCategory(
                        Intent.CATEGORY_APP_CALCULATOR
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            // Launch the calculator category directly. On newer Android
            // versions, resolveActivity() can be affected by package
            // visibility and falsely report that no calculator exists.
            context.startActivity(
                calculatorIntent
            )

            speak(
                "Opening calculator."
            )

            return

        } catch (_: Exception) {
        }

        // Launcher-label fallback for OEMs with unusual package names.
        try {

            val launcherIntent =
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }

            val apps =
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.MATCH_ALL
                )

            for (info in apps) {

                val label =
                    try {
                        info.loadLabel(
                            packageManager
                        ).toString()
                    } catch (_: Exception) {
                        ""
                    }

                val normalized =
                    normalizeAppName(label)

                if (
                    normalized.contains("calculator") ||
                    normalized == "calc"
                ) {

                    val launchIntent =
                        Intent(Intent.ACTION_MAIN).apply {

                            addCategory(
                                Intent.CATEGORY_LAUNCHER
                            )

                            component =
                                ComponentName(
                                    info.activityInfo.packageName,
                                    info.activityInfo.name
                                )

                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }

                    context.startActivity(
                        launchIntent
                    )

                    speak(
                        "Opening calculator."
                    )

                    return
                }
            }

        } catch (_: Exception) {
        }

        speak(
            "Calculator is not available."
        )
    }

    // =========================================================
    // YOUTUBE
    // =========================================================

    private fun isYouTubeCommand(
        command: String
    ): Boolean {

        return command.contains("youtube") ||
            command.contains("song chalao") ||
            command.contains("gana chalao") ||
            command.contains("gaana chalao") ||
            command.contains("gana baja do") ||
            command.contains("gaana baja do") ||
            command.contains("gana chala do") ||
            command.contains("gaana chala do") ||
            command.contains("gana bajao") ||
            command.contains("gaana bajao") ||
            command.contains("song chala do") ||
            command.contains("song bajao") ||
            command.startsWith("play ")
    }

    private fun handleYouTube(
        command: String
    ) {

        var youtubeQuery: String? = null

        when {

            command.contains("youtube kholo") &&
                command.contains("search karo") -> {

                youtubeQuery =
                    command
                        .substringAfter("youtube kholo")
                        .substringBefore("search karo")
                        .trim()
            }

            command.contains("youtube kholo") &&
                command.contains("chalao") -> {

                youtubeQuery =
                    command
                        .substringAfter("youtube kholo")
                        .substringBeforeLast("chalao")
                        .trim()
            }

            command.contains("youtube par") &&
                command.contains("search karo") -> {

                youtubeQuery =
                    command
                        .substringAfter("youtube par")
                        .substringBefore("search karo")
                        .trim()
            }

            command.contains("youtube pe") &&
                command.contains("search karo") -> {

                youtubeQuery =
                    command
                        .substringAfter("youtube pe")
                        .substringBefore("search karo")
                        .trim()
            }

            command.startsWith("search youtube ") -> {

                youtubeQuery =
                    command
                        .removePrefix("search youtube ")
                        .trim()
            }

            command.startsWith("youtube search ") -> {

                youtubeQuery =
                    command
                        .removePrefix("youtube search ")
                        .trim()
            }

            command.startsWith("youtube par ") -> {

                youtubeQuery =
                    command
                        .removePrefix("youtube par ")
                        .trim()
            }

            command.startsWith("youtube pe ") -> {

                youtubeQuery =
                    command
                        .removePrefix("youtube pe ")
                        .trim()
            }

            command.startsWith("play ") -> {

                youtubeQuery =
                    command
                        .removePrefix("play ")
                        .trim()
            }

            command.contains("gana baja do") -> {
                youtubeQuery = command.replace("gana baja do", "", ignoreCase = true).trim()
            }

            command.contains("gaana baja do") -> {
                youtubeQuery = command.replace("gaana baja do", "", ignoreCase = true).trim()
            }

            command.contains("gana chala do") -> {
                youtubeQuery = command.replace("gana chala do", "", ignoreCase = true).trim()
            }

            command.contains("gaana chala do") -> {
                youtubeQuery = command.replace("gaana chala do", "", ignoreCase = true).trim()
            }

            command.contains("gana bajao") -> {
                youtubeQuery = command.replace("gana bajao", "", ignoreCase = true).trim()
            }

            command.contains("gaana bajao") -> {
                youtubeQuery = command.replace("gaana bajao", "", ignoreCase = true).trim()
            }

            command.contains("song chala do") -> {
                youtubeQuery = command.replace("song chala do", "", ignoreCase = true).trim()
            }

            command.contains("song bajao") -> {
                youtubeQuery = command.replace("song bajao", "", ignoreCase = true).trim()
            }

            command.contains("song chalao") -> {

                youtubeQuery =
                    command
                        .replace(
                            "song chalao",
                            "",
                            ignoreCase = true
                        )
                        .trim()
            }

            command.contains("gana chalao") -> {

                youtubeQuery =
                    command
                        .replace(
                            "gana chalao",
                            "",
                            ignoreCase = true
                        )
                        .trim()
            }

            command.contains("gaana chalao") -> {

                youtubeQuery =
                    command
                        .replace(
                            "gaana chalao",
                            "",
                            ignoreCase = true
                        )
                        .trim()
            }
        }

        if (!youtubeQuery.isNullOrBlank()) {

            var cleanQuery =
                youtubeQuery
                    .trim()

            cleanQuery =
                cleanQuery
                    .removeSuffix("search")
                    .removeSuffix("search karo")
                    .removeSuffix("chalao")
                    .removeSuffix("play karo")
                    .removeSuffix("gana chalao")
                    .trim()

            cleanQuery =
                cleanQuery
                    .removePrefix("ka ")
                    .removePrefix("ki ")
                    .removePrefix("ko ")
                    .trim()

            if (cleanQuery.isNotBlank()) {
                searchYouTube(cleanQuery)
            } else {
                openYouTube()
            }

            return
        }

        openYouTube()
    }

    private fun openYouTube(
        speakResult: Boolean = true
    ) {

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

            context.startActivity(intent)

            if (speakResult) {
                speak("Opening YouTube.")
            }

        } catch (_: Exception) {

            try {

                val fallback =
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://www.youtube.com"
                        )
                    ).apply {
                        addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    }

                context.startActivity(fallback)

                if (speakResult) {
                    speak("Opening YouTube.")
                }

            } catch (_: Exception) {

                if (speakResult) {
                    speak(
                        "YouTube is not available."
                    )
                }
            }
        }
    }

    private fun searchYouTube(
        query: String
    ) {

        if (query.isBlank()) {
            openYouTube()
            return
        }

        val isLatest =
            query
                .lowercase(Locale.getDefault())
                .contains("latest")

        val cleanQuery =
            query
                .replace(
                    "latest song",
                    "song",
                    ignoreCase = true
                )
                .replace(
                    "latest",
                    "",
                    ignoreCase = true
                )
                .trim()

        val url =
            "https://www.googleapis.com/youtube/v3/search" +
                "?part=snippet" +
                "&q=" +
                Uri.encode(cleanQuery) +
                "&type=video" +
                "&maxResults=5" +
                if (isLatest) "&order=date" else "" +
                "&key=" +
                BuildConfig.YOUTUBE_API_KEY

        Thread {

            try {

                val response =
                    URL(url).readText()

                val items =
                    JSONObject(response)
                        .getJSONArray("items")

                var videoId: String? = null

                for (i in 0 until items.length()) {

                    val idObject =
                        items
                            .getJSONObject(i)
                            .getJSONObject("id")

                    if (
                        idObject.optString("kind") ==
                        "youtube#video"
                    ) {

                        videoId =
                            idObject.optString(
                                "videoId"
                            )

                        if (!videoId.isNullOrBlank()) {
                            break
                        }
                    }
                }

                if (videoId.isNullOrBlank()) {
                    throw Exception("No video found")
                }

                Handler(
                    Looper.getMainLooper()
                ).post {

                    val youtubeIntent =
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                "https://www.youtube.com/watch?v=$videoId"
                            )
                        ).apply {

                            setPackage(
                                "com.google.android.youtube"
                            )

                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }

                    try {

                        context.startActivity(
                            youtubeIntent
                        )

                        speak(
                            "Playing $query on YouTube."
                        )

                    } catch (_: Exception) {

                        val browserIntent =
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://www.youtube.com/watch?v=$videoId"
                                )
                            ).apply {

                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                )
                            }

                        context.startActivity(
                            browserIntent
                        )

                        speak(
                            "Opening $query on YouTube."
                        )
                    }
                }

            } catch (_: Exception) {

                // If the YouTube Data API is unavailable (for example
                // missing/invalid key or network error), still perform
                // the user's requested YouTube action instead of falling
                // through to the AI/ultra-search fallback.
                Handler(
                    Looper.getMainLooper()
                ).post {

                    try {

                        val searchIntent =
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://www.youtube.com/results?search_query=" +
                                        Uri.encode(cleanQuery)
                                )
                            ).apply {
                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                )
                            }

                        context.startActivity(
                            searchIntent
                        )

                        speak(
                            "Playing $query on YouTube."
                        )

                    } catch (_: Exception) {

                        speak(
                            "YouTube is not available."
                        )
                    }
                }
            }

        }.start()
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

            context.startActivity(intent)

            speak(
                "Opening Chrome."
            )

        } catch (_: Exception) {

            speak(
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
                    Uri.parse("geo:0,0?q=")
                ).apply {

                    setPackage(
                        "com.google.android.apps.maps"
                    )

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            context.startActivity(intent)

            speak(
                "Opening Maps."
            )

        } catch (_: Exception) {

            speak(
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
                        Uri.encode(query)
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

            context.startActivity(intent)

            speak(
                "Searching Maps for $query."
            )

        } catch (_: Exception) {

            speak(
                "I could not open Maps."
            )
        }
    }

    private fun extractMapsQuery(
        command: String
    ): String? {

        return when {

            command.startsWith("maps search ") ->
                command
                    .removePrefix("maps search ")
                    .trim()

            command.startsWith("search maps ") ->
                command
                    .removePrefix("search maps ")
                    .trim()

            command.startsWith("maps par ") ->
                command
                    .removePrefix("maps par ")
                    .trim()

            command.startsWith("maps pe ") ->
                command
                    .removePrefix("maps pe ")
                    .trim()

            else -> null
        }
    }

    // =========================================================
    // PHONE
    // =========================================================

    private fun openPhone(
        speakResult: Boolean = true
    ) {

        try {

            val intent =
                Intent(
                    Intent.ACTION_DIAL
                ).apply {

                    data = Uri.parse("tel:")

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            context.startActivity(intent)

            if (speakResult) {
                speak("Opening phone.")
            }

        } catch (_: Exception) {

            if (speakResult) {
                speak(
                    "Phone app is not available."
                )
            }
        }
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private fun openSettings(
        speakResult: Boolean = true
    ) {

        try {

            val intent =
                Intent(
                    Settings.ACTION_SETTINGS
                ).apply {

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            context.startActivity(intent)

            if (speakResult) {
                speak("Opening settings.")
            }

        } catch (_: Exception) {

            if (speakResult) {
                speak(
                    "Settings is not available."
                )
            }
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

            context.startActivity(intent)

            speak(
                "Opening Wi-Fi settings."
            )

        } catch (_: Exception) {

            speak(
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
                context.getSystemService(
                    Context.AUDIO_SERVICE
                ) as AudioManager

            audio.adjustVolume(
                if (increase)
                    AudioManager.ADJUST_RAISE
                else
                    AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )

            speak(
                if (increase)
                    "Volume increased."
                else
                    "Volume decreased."
            )

        } catch (_: Exception) {

            speak(
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
                context.getSystemService(
                    Context.AUDIO_SERVICE
                ) as AudioManager

            val down =
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                )

            val up =
                KeyEvent(
                    KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                )

            audio.dispatchMediaKeyEvent(down)
            audio.dispatchMediaKeyEvent(up)

            speak(
                "Media control executed."
            )

        } catch (_: Exception) {

            speak(
                "I could not control media."
            )
        }
    }

    // =========================================================
    // BATTERY
    // =========================================================

    fun getBatteryLevelForDiagnostics(): Int {
        return getBatteryLevel()
    }

    private fun getBatteryLevel(): Int {

        return try {

            val manager =
                context.getSystemService(
                    Context.BATTERY_SERVICE
                ) as BatteryManager

            manager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CAPACITY
            )

        } catch (_: Exception) {

            -1
        }
    }

    private fun tellBattery() {

        val level =
            getBatteryLevel()

        if (level >= 0) {

            speak(
                "Battery is at $level percent."
            )

        } else {

            speak(
                "I could not check the battery."
            )
        }
    }

    // =========================================================
    // INSTALLED APP
    // =========================================================

    private fun isAppOpenCommand(
        command: String
    ): Boolean {

        val c = normalizeCommand(command)

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

        var result = normalizeCommand(command)

        result =
            result.replace(
                Regex("^aurix[,:]?\\s*"),
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
            .lowercase(Locale.getDefault())
            .replace(
                Regex("[^a-z0-9]"),
                ""
            )
            .trim()
    }

    private fun openInstalledApp(
        appName: String,
        speakResult: Boolean = true
    ): Boolean {

        val requestedName =
            appName.trim()

        if (requestedName.isBlank()) {

            if (speakResult) {
                speak(
                    "Which app should I open?"
                )
            }

            return true
        }

        val requested =
            normalizeAppName(requestedName)

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

        if (packages != null) {

            for (packageName in packages) {

                try {

                    val launchIntent =
                        packageManager
                            .getLaunchIntentForPackage(
                                packageName
                            )

                    if (launchIntent != null) {

                        launchIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )

                        launchIntent.addFlags(
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        )

                        context.startActivity(
                            launchIntent
                        )

                        if (speakResult) {
                            speak(
                                "Opening $requestedName."
                            )
                        }

                        return true
                    }

                } catch (_: Exception) {
                }
            }
        }

        // Generic launcher discovery.
        try {

            val launcherIntent =
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }

            val apps =
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.MATCH_ALL
                )

            var bestActivity:
                android.content.pm.ActivityInfo? = null

            var bestScore = 0

            for (resolveInfo in apps) {

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

                if (label.isBlank()) {
                    continue
                }

                val normalizedLabel =
                    normalizeAppName(label)

                val packagePart =
                    normalizeAppName(
                        activity
                            .packageName
                            .substringAfterLast(".")
                    )

                if (normalizedLabel == requested) {

                    bestActivity = activity
                    bestScore = 100
                    break
                }

                if (packagePart == requested) {

                    bestActivity = activity
                    bestScore = 95
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
                    max(
                        labelScore,
                        packageScore
                    )

                if (score > bestScore) {
                    bestScore = score
                    bestActivity = activity
                }
            }

            if (
                bestActivity != null &&
                bestScore >= 60
            ) {

                val launchIntent =
                    Intent(Intent.ACTION_MAIN).apply {

                        addCategory(
                            Intent.CATEGORY_LAUNCHER
                        )

                        component =
                            ComponentName(
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

                context.startActivity(
                    launchIntent
                )

                if (speakResult) {
                    speak(
                        "Opening $requestedName."
                    )
                }

                return true
            }

        } catch (_: Exception) {
        }

        if (speakResult) {
            speak(
                "I couldn't find $requestedName on your phone."
            )
        }

        return true
    }

    // =========================================================
    // SIMILARITY
    // =========================================================

    private fun similarityScore(
        a: String,
        b: String
    ): Int {

        if (a.isBlank() || b.isBlank()) {
            return 0
        }

        if (a == b) {
            return 100
        }

        if (a.contains(b) || b.contains(a)) {
            return 85
        }

        val distance =
            levenshteinDistance(a, b)

        val maxLength =
            max(a.length, b.length)

        if (maxLength == 0) {
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
            Array(a.length + 1) {
                IntArray(b.length + 1)
            }

        for (i in 0..a.length) {
            dp[i][0] = i
        }

        for (j in 0..b.length) {
            dp[0][j] = j
        }

        for (i in 1..a.length) {

            for (j in 1..b.length) {

                val cost =
                    if (a[i - 1] == b[j - 1]) {
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
    // GOOGLE SEARCH
    // =========================================================

    private fun googleSearch(
        query: String
    ) {

        try {

            val url =
                "https://www.google.com/search?q=" +
                    Uri.encode(query)

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).apply {

                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

            context.startActivity(intent)

            speak(
                "Searching for $query."
            )

        } catch (_: Exception) {

            speak(
                "I could not search that."
            )
        }
    }

    // =========================================================
    // NORMALIZATION
    // =========================================================

    private fun normalizeBilingualCommand(rawCommand: String): String {
        var c = normalizeCommand(rawCommand)

        val aliases = listOf(
            "khol do" to " kholo",
            "khol de" to " kholo",
            "chalu karo" to " chalao",
            "chalu kar do" to " chalao",
            "baja do" to " bajao",
            "bajaa do" to " bajao",
            "laga do" to " chalao",
            "lagao" to " chalao",
            "dikha do" to " dikhao",
            "dikhao" to " dikhao"
        )

        aliases.forEach { (from, to) ->
            c = c.replace(Regex("\\b" + Regex.escape(from) + "\\b", RegexOption.IGNORE_CASE), to.trim())
        }

        return c.replace(Regex("\\s+"), " ").trim()
    }

    private fun normalizeCommand(
        value: String
    ): String {

        return value
            .lowercase(Locale.getDefault())
            .trim()
            .replace(
                Regex("\\s+"),
                " "
            )
    }
}
