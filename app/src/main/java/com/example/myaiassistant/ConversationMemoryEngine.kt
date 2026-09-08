package com.example.myaiassistant

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ConversationMemoryEngine {

    data class Turn(
        val user: String,
        val assistant: String,
        val time: Long = System.currentTimeMillis()
    )

    private val turns = ArrayDeque<Turn>()

    private const val MAX_TURNS = 12
    private const val PREFS_NAME = "aurix_memory"
    private const val KEY_TURNS = "conversation_turns"

    private var initialized = false

    fun initialize(context: Context) {

        if (initialized) return

        initialized = true

        load(context)
    }

    fun addTurn(
        context: Context,
        user: String,
        assistant: String
    ) {

        if (user.isBlank()) return

        turns.addLast(
            Turn(
                user = user.trim(),
                assistant = assistant.trim()
            )
        )

        while (turns.size > MAX_TURNS) {
            turns.removeFirst()
        }

        save(context)
    }

    fun getRecentTurns(): List<Turn> {
        return turns.toList()
    }

    fun getContext(): String {

        return turns.joinToString("\n") {
            "User: ${it.user}\nAURIX: ${it.assistant}"
        }
    }

    fun clear(context: Context) {

        turns.clear()

        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(KEY_TURNS)
            .apply()
    }

    private fun save(context: Context) {

        val array = JSONArray()

        turns.forEach { turn ->

            val obj = JSONObject()

            obj.put("user", turn.user)
            obj.put("assistant", turn.assistant)
            obj.put("time", turn.time)

            array.put(obj)
        }

        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_TURNS,
                array.toString()
            )
            .apply()
    }

    private fun load(context: Context) {

        turns.clear()

        val prefs =
            context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val data =
            prefs.getString(
                KEY_TURNS,
                null
            ) ?: return

        try {

            val array =
                JSONArray(data)

            for (i in 0 until array.length()) {

                val obj =
                    array.getJSONObject(i)

                turns.addLast(
                    Turn(
                        user =
                            obj.optString("user"),

                        assistant =
                            obj.optString("assistant"),

                        time =
                            obj.optLong(
                                "time",
                                System.currentTimeMillis()
                            )
                    )
                )
            }

            while (turns.size > MAX_TURNS) {
                turns.removeFirst()
            }

        } catch (_: Exception) {

            turns.clear()
        }
    }
}
