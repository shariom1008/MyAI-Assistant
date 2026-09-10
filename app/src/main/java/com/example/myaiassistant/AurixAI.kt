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
        BuildConfig.MY_API_KEY


    // =========================================================
    // AURIX CONVERSATION MEMORY
    // =========================================================

    private val conversationHistory =
        mutableListOf<Pair<String, String>>()

    private const val MAX_HISTORY = 8


    // =========================================================
    // ASK AI
    // =========================================================

    fun ask(
        question: String,
        callback: (String) -> Unit
    ) {

        val cleanQuestion =
            question.trim()

        if (cleanQuestion.isBlank()) {

            postResult(
                "Boss, mujhe kuch sunai nahi diya.",
                callback
            )

            return
        }


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

                    // =================================================
                    // GEMINI CONNECTION
                    // =================================================

                    val url =
                        URL(
                            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$API_KEY"
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


                    // =================================================
                    // BUILD CONVERSATION CONTEXT
                    // =================================================

                    val historyText =
                        synchronized(
                            conversationHistory
                        ) {

                            conversationHistory
                                .joinToString(
                                    "\n"
                                ) { item ->

                                    """
                                    User: ${item.first}
                                    AURIX: ${item.second}
                                    """.trimIndent()
                                }
                        }


                    // =================================================
                    // AURIX PERSONALITY + CONTEXT
                    // =================================================

                    val prompt =
                        """
                        You are AURIX, a personal AI voice assistant.

                        Your personality:
                        - Friendly
                        - Intelligent
                        - Calm
                        - Helpful
                        - Respectful
                        - Slightly futuristic
                        - Treat the user as "Boss" naturally
                        - Never sound robotic
                        - Never mention Gemini

                        IMPORTANT LANGUAGE RULES:

                        Reply in the same language style
                        the user is using.

                        If the user speaks English,
                        reply in natural English.

                        If the user speaks Hindi,
                        reply in natural Roman Hindi.

                        If the user speaks Hinglish,
                        reply in natural Hinglish.

                        If the user speaks Haryanvi,
                        reply naturally in Haryanvi.

                        NEVER use Devanagari Hindi script.

                        IMPORTANT VOICE RULES:

                        Keep answers conversational
                        and suitable for text-to-speech.

                        Avoid unnecessary lists,
                        symbols, markdown,
                        and very long explanations
                        unless the user specifically asks
                        for detailed information.

                        CONVERSATION CONTEXT:

                        $historyText

                        CURRENT USER MESSAGE:

                        $cleanQuestion

                        Answer the current user message
                        using the conversation context when useful.

                        Do not repeat the entire previous conversation.

                        Do not tell the user to open another app.

                        Do not say that you are Gemini.

                        You are AURIX.
                        """.trimIndent()


                    // =================================================
                    // REQUEST BODY
                    // =================================================

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


                    // =================================================
                    // SEND REQUEST
                    // =================================================

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


                    // =================================================
                    // SUCCESS
                    // =================================================

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

                            // =========================================
                            // SAVE CONVERSATION
                            // =========================================

                            synchronized(
                                conversationHistory
                            ) {

                                conversationHistory.add(
                                    Pair(
                                        cleanQuestion,
                                        answer
                                    )
                                )


                                // Keep only latest conversations
                                while (
                                    conversationHistory.size >
                                    MAX_HISTORY
                                ) {

                                    conversationHistory.removeAt(
                                        0
                                    )
                                }
                            }


                            postResult(
                                answer,
                                callback
                            )
                        }


                        return@Thread
                    }


                    // =================================================
                    // RATE LIMIT / 429
                    // =================================================

                    if (
                        responseCode == 429
                    ) {

                        connection.disconnect()


                        attempt++


                        if (
                            attempt <= MAX_RETRIES
                        ) {

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


                    // =================================================
                    // OTHER HTTP ERRORS
                    // =================================================

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
    // CLEAR CONVERSATION
    // =========================================================

    fun clearConversation() {

        synchronized(
            conversationHistory
        ) {

            conversationHistory.clear()
        }
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
