package com.example.myaiassistant

import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AurixAI {

    // =========================================================
    // AURIX AI REQUEST CONTROL
    // =========================================================

    @Volatile
    private var lastRequestTime = 0L

    @Volatile
    private var requestInProgress = false

    private const val REQUEST_COOLDOWN = 3000L

    private const val MAX_RETRIES = 2

    private val API_KEY =
    BuildConfig.GEMINI_API_KEY

    // =========================================================
    // ASK AI
    // =========================================================

    fun ask(
        question: String,
        callback: (String) -> Unit
    ) {

        val now =
            System.currentTimeMillis()

        // -----------------------------------------------------
        // BLOCK DUPLICATE REQUESTS
        // -----------------------------------------------------

        if (requestInProgress) {

            postResult(
                "Boss, pehle wali request complete hone do.",
                callback
            )

            return
        }

        // -----------------------------------------------------
        // COOLDOWN
        // -----------------------------------------------------

        if (
            now - lastRequestTime <
            REQUEST_COOLDOWN
        ) {

            postResult(
                "Boss, thoda wait karo.",
                callback
            )

            return
        }

        requestInProgress = true
        lastRequestTime = now

        Thread {

            var attempt = 0

            while (
                attempt <= MAX_RETRIES
            ) {

                try {

                    val url =
                        URL(
                            ""https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$API_KEY""
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

                    connection.doOutput =
                        true

                    // -------------------------------------------------
                    // AURIX PERSONALITY PROMPT
                    // -------------------------------------------------

                    val prompt =
                        """
                        You are AURIX, a helpful personal voice assistant.

                        Answer the user's question accurately and naturally.

                        User question:
                        $question

                        If current or up-to-date information is required,
                        answer based on available information.

                        Reply in natural Roman Hindi, Hinglish,
                        or Haryanvi depending on the user's language
                        and speaking style.

                        Never use Devanagari Hindi script.

                        If the user speaks Haryanvi,
                        reply naturally in Haryanvi.

                        If the user speaks Hindi,
                        reply naturally in Hindi or Hinglish.

                        If the user mixes Hindi, English and Haryanvi,
                        naturally mix them too.

                        Keep the response conversational,
                        concise and suitable for voice.

                        Do not mention Gemini.

                        Do not tell the user to open another app.
                        """.trimIndent()

                    // -------------------------------------------------
                    // REQUEST BODY
                    // -------------------------------------------------

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

                    // -------------------------------------------------
                    // SUCCESS
                    // -------------------------------------------------

                    if (
                        responseCode in 200..299
                    ) {

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
                                ?.optJSONObject(
                                    "content"
                                )
                                ?.optJSONArray(
                                    "parts"
                                )
                                ?.optJSONObject(0)
                                ?.optString(
                                    "text"
                                )
                                ?.trim()

                        requestInProgress =
                            false

                        connection.disconnect()

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

                        return@Thread
                    }

                    // -------------------------------------------------
                    // RATE LIMIT / 429
                    // -------------------------------------------------

                    if (
                        responseCode == 429
                    ) {

                        connection.disconnect()

                        attempt++

                        if (
                            attempt <= MAX_RETRIES
                        ) {

                            // Exponential backoff:
                            // 2 sec → 4 sec

                            val waitTime =
                                2000L *
                                    attempt

                            Thread.sleep(
                                waitTime
                            )

                            continue

                        } else {

                            requestInProgress =
                                false

                            postResult(
                                "Boss, AI ki request limit abhi full hai. Thoda baad dobara try karte hain.",
                                callback
                            )

                            return@Thread
                        }
                    }

                    // -------------------------------------------------
                    // OTHER HTTP ERRORS
                    // -------------------------------------------------

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

                    requestInProgress =
                        false

                    connection.disconnect()

                    postResult(
                    "AI ERROR $responseCode: $errorText",
                    callback
                        )

                    return@Thread

                } catch (
                    e: Exception
                ) {

                    attempt++

                    if (
                        attempt <= MAX_RETRIES
                    ) {

                        Thread.sleep(
                            1500L
                        )

                        continue
                    }

                    requestInProgress =
                        false

                    postResult(
                        "AI ERROR: ${e.javaClass.simpleName}",
                        callback
                    )

                    return@Thread
                }
            }

            requestInProgress =
                false

            postResult(
                "Boss, AI abhi available nahi hai.",
                callback
            )

        }.start()
    }

    // =========================================================
    // MAIN THREAD RESPONSE
    // =========================================================

    private fun postResult(
        result: String,
        callback: (String) -> Unit
    ) {

        Handler(
            Looper.getMainLooper()
        ).post {

            callback(
                result
            )
        }
    }
}
