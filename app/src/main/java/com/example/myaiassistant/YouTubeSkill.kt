package com.example.myaiassistant.skills

import com.example.myaiassistant.BuildConfig

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import java.net.URL
import java.util.Locale
import org.json.JSONObject

/**
 * AURIX YouTube skill.
 *
 * Owns YouTube-specific command detection, query extraction, app launching
 * and YouTube Data API search. This is a normal Kotlin class, so it does
 * not need an AndroidManifest entry.
 */
class YouTubeSkill(
    private val context: Context,
    private val speak: (String) -> Unit,
    private val status: (String) -> Unit = {}
) {

    fun handle(rawCommand: String): Boolean {
        val command = normalize(rawCommand)
        if (!isYouTubeCommand(command)) return false

        status("EXECUTING")
        handleYouTube(command)
        return true
    }

    fun executeForAgent(rawCommand: String): String? {
        val command = normalize(rawCommand)
        if (!isYouTubeCommand(command)) return null

        return try {
            if (command == "youtube" || command == "open youtube") {
                openYouTube(speakResult = false)
                "YouTube opened."
            } else {
                handleYouTube(command, speakResult = false)
                "YouTube command executed."
            }
        } catch (_: Exception) {
            "I couldn't execute this step."
        }
    }

    private fun isYouTubeCommand(command: String): Boolean {
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
        command: String,
        speakResult: Boolean = true
    ) {
        var youtubeQuery: String? = null

        when {
            command.contains("youtube kholo") && command.contains("search karo") -> {
                youtubeQuery = command.substringAfter("youtube kholo").substringBefore("search karo").trim()
            }
            command.contains("youtube kholo") && command.contains("chalao") -> {
                youtubeQuery = command.substringAfter("youtube kholo").substringBeforeLast("chalao").trim()
            }
            command.contains("youtube par") && command.contains("search karo") -> {
                youtubeQuery = command.substringAfter("youtube par").substringBefore("search karo").trim()
            }
            command.contains("youtube pe") && command.contains("search karo") -> {
                youtubeQuery = command.substringAfter("youtube pe").substringBefore("search karo").trim()
            }
            command.startsWith("search youtube ") -> {
                youtubeQuery = command.removePrefix("search youtube ").trim()
            }
            command.startsWith("youtube search ") -> {
                youtubeQuery = command.removePrefix("youtube search ").trim()
            }
            command.startsWith("youtube par ") -> {
                youtubeQuery = command.removePrefix("youtube par ").trim()
            }
            command.startsWith("youtube pe ") -> {
                youtubeQuery = command.removePrefix("youtube pe ").trim()
            }
            command.startsWith("play ") -> {
                youtubeQuery = command.removePrefix("play ").trim()
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
                youtubeQuery = command.replace("song chalao", "", ignoreCase = true).trim()
            }
            command.contains("gana chalao") -> {
                youtubeQuery = command.replace("gana chalao", "", ignoreCase = true).trim()
            }
            command.contains("gaana chalao") -> {
                youtubeQuery = command.replace("gaana chalao", "", ignoreCase = true).trim()
            }
        }

        if (!youtubeQuery.isNullOrBlank()) {
            var cleanQuery = youtubeQuery.trim()
                .removeSuffix("search")
                .removeSuffix("search karo")
                .removeSuffix("chalao")
                .removeSuffix("play karo")
                .removeSuffix("gana chalao")
                .trim()

            cleanQuery = cleanQuery
                .removePrefix("ka ")
                .removePrefix("ki ")
                .removePrefix("ko ")
                .trim()

            if (cleanQuery.isNotBlank()) {
                searchYouTube(cleanQuery, speakResult)
            } else {
                openYouTube(speakResult)
            }
            return
        }

        openYouTube(speakResult)
    }

    private fun openYouTube(speakResult: Boolean = true) {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com")
            ).apply {
                setPackage("com.google.android.youtube")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            if (speakResult) speak("Opening YouTube.")
        } catch (_: Exception) {
            try {
                val fallback = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
                if (speakResult) speak("Opening YouTube.")
            } catch (_: Exception) {
                if (speakResult) speak("YouTube is not available.")
            }
        }
    }

    private fun searchYouTube(query: String, speakResult: Boolean = true) {
        if (query.isBlank()) {
            openYouTube(speakResult)
            return
        }

        val isLatest = query.lowercase(Locale.getDefault()).contains("latest")
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
                val response = URL(url).readText()
                val items = JSONObject(response).getJSONArray("items")
                var videoId: String? = null

                for (i in 0 until items.length()) {
                    val idObject = items.getJSONObject(i).getJSONObject("id")
                    if (idObject.optString("kind") == "youtube#video") {
                        videoId = idObject.optString("videoId")
                        if (!videoId.isNullOrBlank()) break
                    }
                }

                if (videoId.isNullOrBlank()) throw Exception("No video found")

                Handler(Looper.getMainLooper()).post {
                    val youtubeIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=$videoId")
                    ).apply {
                        setPackage("com.google.android.youtube")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    try {
                        context.startActivity(youtubeIntent)
                        if (speakResult) speak("Playing $query on YouTube.")
                    } catch (_: Exception) {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/watch?v=$videoId")
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(browserIntent)
                        if (speakResult) speak("Opening $query on YouTube.")
                    }
                }
            } catch (_: Exception) {
                Handler(Looper.getMainLooper()).post {
                    try {
                        val searchIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                "https://www.youtube.com/results?search_query=" +
                                    Uri.encode(cleanQuery)
                            )
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(searchIntent)
                        if (speakResult) speak("Playing $query on YouTube.")
                    } catch (_: Exception) {
                        if (speakResult) speak("YouTube is not available.")
                    }
                }
            }
        }.start()
    }

    private fun normalize(value: String): String {
        return value
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")
    }
}
