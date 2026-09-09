package com.example.myaiassistant

import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AurixAI {
        // =========================================================
        // GEMINI REQUEST COOLDOWN
        // =========================================================

    @Volatile
    private var lastRequestTime = 0L
    @Volatile
    private var requestInProgress = false

    private const val REQUEST_COOLDOWN = 3000L

    private val API_KEY =
        BuildConfig.GEMINI_API_KEY

    fun ask(
        question: String,
        callback: (String) -> Unit
    ) {
               val now = System.currentTimeMillis()

if (requestInProgress) {

    postResult(
        "Boss, pehle wali request complete hone do.",
        callback
    )

    return
}

if (now - lastRequestTime < REQUEST_COOLDOWN) {

    postResult(
        "Boss, thoda sa wait karo.",
        callback
    )

    return
}

requestInProgress = true
lastRequestTime = now
        Thread {

            try {

                val url =
                    URL(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=$API_KEY"
                    )

                val connection =
                    url.openConnection()
                        as HttpURLConnection

                connection.requestMethod =
                    "POST"

                connection.connectTimeout =
                    15000

                connection.readTimeout =
                    30000

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.doOutput = true

                val prompt =
                """
                You are AURIX, a helpful personal voice assistant.

                 Answer the user's question accurately.

                 User question:
                 $question

                 Use Google Search when current or up-to-date information is needed.

                 Reply in natural Roman Hindi, Hinglish, or Haryanvi depending on the user's language and speaking style.

                 Do not use Devanagari Hindi script.

                 If the user speaks Haryanvi, reply naturally in Haryanvi.
                 If the user speaks Hindi, reply in natural Hindi or Hinglish.
                 If the user mixes Hindi, English and Haryanvi, naturally mix them too.

                 Keep the response conversational, natural and suitable for voice.
                 Do not mention Gemini.
                 Do not tell the user to open another app.
                 """.trimIndent()

                val requestBody =
                    JSONObject().apply {

                        put(
                            "contents",
                            JSONArray().put(
                                JSONObject().apply {

                                    put(
                                        "parts",
                                        JSONArray().put(
                                            JSONObject().apply {

                                                put(
                                                    "text",
                                                    prompt
                                                )
                                            }
                                        )
                                    )
                                }
                            )
                        )
                    }

                connection.outputStream.use {

                    it.write(
                        requestBody
                            .toString()
                            .toByteArray(
                                Charsets.UTF_8
                            )
                    )
                }

                val responseCode =
                    connection.responseCode

                if (
                    responseCode !in 200..299
                ) {

                    val errorText =
                        try {

                            connection.errorStream
                                ?.bufferedReader()
                                ?.use {
                                    it.readText()
                                }

                        } catch (_: Exception) {

                            ""
                        }
                        if (responseCode == 429) {

    postResult(
        "Boss, AI ki request limit abhi full hai. Thoda wait karke dobara try karo.",
        callback
    )

} else {

    postResult(
        "AI ERROR $responseCode",
        callback
    )
                        }

                    postResult(
                        "AI ERROR $responseCode $errorText",
                        callback
                    )
                    requestInProgress = false

                    connection.disconnect()

                    return@Thread
                }

                val responseText =
                    connection.inputStream
                        .bufferedReader()
                        .use {
                            it.readText()
                        }

                val response =
                    JSONObject(
                        responseText
                    )

                val candidates =
                    response.optJSONArray(
                        "candidates"
                    )

                val answer =
                    candidates
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")
                        ?.trim()

                if (
                    answer.isNullOrBlank()
                ) {

                    postResult(
                        "Sorry boss, mujhe iska answer nahi mil paaya.",
                        callback
                    )

                } else {

                    postResult(
                        answer,
                        callback
                    )
                }
                requestInProgress = false
                connection.disconnect()

            } catch (
                e: Exception
            ) {

                postResult(
                    "AI ERROR: ${e.javaClass.simpleName} ${e.message}",
                    callback
                )
                requestInProgress = false
            }

        }.start()
    }

    private fun postResult(
        result: String,
        callback: (String) -> Unit
    ) {

        Handler(
            Looper.getMainLooper()
        ).post {

            callback(result)
        }
    }
}
