package com.example.myaiassistant

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AurixAI {

    private const val API_KEY = "AQ.Ab8RN6IdPKvJXJsU0vwFEm6wGMirVFQI4cF-w01al8GPH6_kNA"

    suspend fun ask(
        question: String
    ): String {

        return withContext(
            Dispatchers.IO
        ) {

            try {

                val url =
                    URL(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$API_KEY"
                    )

                val connection =
                    url.openConnection()
                        as HttpURLConnection

                connection.requestMethod =
                    "POST"

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.doOutput = true

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
                                                    """
                                                    You are AURIX, a helpful personal voice assistant.

                                                    Answer the user's question accurately and naturally.

                                                    User question:
                                                    $question

                                                    Reply in simple Hindi/Hinglish.
                                                    Keep the answer concise enough for voice.
                                                    Do not mention that you are Gemini.
                                                    Do not tell the user to open another app.
                                                    """.trimIndent()
                                                )
                                            }
                                        )
                                    )
                                }
                            )
                        )

                        put(
                            "tools",
                            JSONArray().put(
                                JSONObject().apply {
                                    put(
                                        "google_search",
                                        JSONObject()
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

                    return@withContext(
                        "Sorry boss, abhi mujhe iska answer nahi mil paaya."
                    )
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

                if (
                    candidates == null ||
                    candidates.length() == 0
                ) {

                    return@withContext(
                        "Sorry boss, mujhe koi answer nahi mila."
                    )
                }

                val candidate =
                    candidates.getJSONObject(0)

                val content =
                    candidate.optJSONObject(
                        "content"
                    )

                val parts =
                    content?.optJSONArray(
                        "parts"
                    )

                if (
                    parts == null ||
                    parts.length() == 0
                ) {

                    return@withContext(
                        "Sorry boss, answer read nahi ho paaya."
                    )
                }

                val answer =
                    parts.getJSONObject(0)
                        .optString("text")
                        .trim()

                if (
                    answer.isBlank()
                ) {

                    "Sorry boss, abhi answer nahi mil paaya."

                } else {

                    answer
                }

            } catch (
                e: Exception
            ) {

                "Sorry boss, internet ya AI service mein problem aa rahi hai."
            }
        }
    }
}
