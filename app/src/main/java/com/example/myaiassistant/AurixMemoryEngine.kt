package com.example.myaiassistant

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * AURIX Memory Engine
 *
 * Local memory system.
 *
 * Supports:
 * - Remember information
 * - Recall saved memories
 * - Forget specific memory
 * - Forget all memories
 * - Show saved memories
 *
 * Data is stored locally on the device.
 */
object AurixMemoryEngine {

    private const val PREF_NAME = "aurix_memory"
    private const val MEMORY_KEY = "memories"

    /**
     * Main entry point.
     */
    fun answer(context: Context, command: String): String? {

        val c = command.trim().lowercase()

        if (c.isBlank()) {
            return null
        }

        // =========================================================
        // REMEMBER
        // =========================================================

        if (
            c.startsWith("remember that ") ||
            c.startsWith("remember ") ||
            c.startsWith("yaad rakh ") ||
            c.startsWith("yaad rakhna ") ||
            c.startsWith("isko yaad rakh") ||
            c.startsWith("ye yaad rakh")
        ) {

            var memory = command.trim()

            memory = memory
                .replaceFirst(
                    Regex(
                        "^remember that\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^remember\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^yaad rakhna\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^yaad rakh\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^isko yaad rakh\\s*",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^ye yaad rakh\\s*",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .trim()

            if (memory.isBlank()) {
                return "Kya yaad rakhna hai, Boss?"
            }

            saveMemory(context, memory)

            return "Theek hai Boss, maine ye baat yaad rakh li hai."
        }

        // =========================================================
        // SHOW MEMORY
        // =========================================================

        if (
            c.contains("what do you remember") ||
            c.contains("what you remember") ||
            c.contains("what do you know about me") ||
            c.contains("show my memories") ||
            c.contains("show memories") ||
            c.contains("meri memory dikhao") ||
            c.contains("mujhe kya yaad hai") ||
            c.contains("mere baare mein kya yaad hai") ||
            c.contains("kya yaad hai")
        ) {

            val memories = getMemories(context)

            if (memories.isEmpty()) {
                return "Boss, abhi meri memory mein kuch bhi saved nahi hai."
            }

            val builder = StringBuilder()

            builder.append(
                "Boss, mujhe ye baatein yaad hain: "
            )

            memories.forEachIndexed { index, memory ->

                builder.append(
                    "${index + 1}. $memory"
                )

                if (index != memories.lastIndex) {
                    builder.append(". ")
                }
            }

            return builder.toString()
        }

        // =========================================================
        // MEMORY COUNT
        // =========================================================

        if (
            c.contains("how many memories") ||
            c.contains("memory count") ||
            c.contains("kitni memories") ||
            c.contains("kitni memory")
        ) {

            val count = getMemories(context).size

            return if (count == 0) {
                "Boss, memory mein kuch bhi saved nahi hai."
            } else {
                "Boss, meri memory mein $count baatein saved hain."
            }
        }

        // =========================================================
        // FORGET ALL
        // =========================================================

        if (
            c.contains("forget everything") ||
            c.contains("forget all memories") ||
            c.contains("delete all memories") ||
            c.contains("clear all memories") ||
            c.contains("sab kuch bhool jao") ||
            c.contains("sab yaadein bhool jao") ||
            c.contains("memory clear karo") ||
            c.contains("memory delete karo")
        ) {

            clearMemories(context)

            return "Theek hai Boss, maine saari saved memories delete kar di hain."
        }

        // =========================================================
        // FORGET LAST MEMORY
        // =========================================================

        if (
            c.contains("forget last memory") ||
            c.contains("last memory bhool jao") ||
            c.contains("last wali memory bhool jao")
        ) {

            val removed = removeLastMemory(context)

            return if (removed) {
                "Theek hai Boss, last memory bhool gaya."
            } else {
                "Boss, memory mein kuch bhi saved nahi hai."
            }
        }

        // =========================================================
        // FORGET SPECIFIC MEMORY
        // =========================================================

        if (
            c.startsWith("forget ") ||
            c.startsWith("delete memory ") ||
            c.startsWith("remove memory ") ||
            c.startsWith("ye bhool jao ") ||
            c.startsWith("isko bhool jao ")
        ) {

            var target = command.trim()

            target = target
                .replaceFirst(
                    Regex(
                        "^forget\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^delete memory\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^remove memory\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^ye bhool jao\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^isko bhool jao\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .trim()

            if (target.isBlank()) {
                return "Boss, kaunsi memory bhoolni hai?"
            }

            val removed = removeMemory(
                context,
                target
            )

            return if (removed) {
                "Theek hai Boss, maine woh memory delete kar di."
            } else {
                "Boss, mujhe aisi koi saved memory nahi mili."
            }
        }

        // =========================================================
        // SEARCH MEMORY
        // =========================================================

        if (
            c.startsWith("do you remember ") ||
            c.startsWith("kya tumhe yaad hai ") ||
            c.startsWith("yaad hai ")
        ) {

            var target = command.trim()

            target = target
                .replaceFirst(
                    Regex(
                        "^do you remember\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^kya tumhe yaad hai\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .replaceFirst(
                    Regex(
                        "^yaad hai\\s+",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .trim()

            if (target.isBlank()) {
                return "Boss, kya yaad hai ye batao."
            }

            val found = searchMemory(
                context,
                target
            )

            return if (found != null) {
                "Haan Boss, mujhe yaad hai: $found"
            } else {
                "Nahi Boss, ye baat meri memory mein saved nahi hai."
            }
        }

        return null
    }

    // =============================================================
    // SAVE MEMORY
    // =============================================================

    private fun saveMemory(
        context: Context,
        memory: String
    ) {

        val memories = getMemories(context).toMutableList()

        val cleanMemory = memory
            .trim()
            .replace(
                Regex("\\s+"),
                " "
            )

        if (cleanMemory.isBlank()) {
            return
        }

        // Avoid exact duplicates.
        val alreadyExists = memories.any {
            it.equals(
                cleanMemory,
                ignoreCase = true
            )
        }

        if (alreadyExists) {
            return
        }

        memories.add(cleanMemory)

        saveMemories(
            context,
            memories
        )
    }

    // =============================================================
    // GET MEMORIES
    // =============================================================

    private fun getMemories(
        context: Context
    ): List<String> {

        return try {

            val prefs = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

            val raw = prefs.getString(
                MEMORY_KEY,
                null
            )

            if (raw.isNullOrBlank()) {
                return emptyList()
            }

            val array = JSONArray(raw)

            val result = mutableListOf<String>()

            for (i in 0 until array.length()) {

                val item = array.optString(i)

                if (item.isNotBlank()) {
                    result.add(item)
                }
            }

            result

        } catch (e: Exception) {

            emptyList()
        }
    }

    // =============================================================
    // SAVE MEMORY LIST
    // =============================================================

    private fun saveMemories(
        context: Context,
        memories: List<String>
    ) {

        try {

            val array = JSONArray()

            memories.forEach {
                array.put(it)
            }

            context
                .getSharedPreferences(
                    PREF_NAME,
                    Context.MODE_PRIVATE
                )
                .edit()
                .putString(
                    MEMORY_KEY,
                    array.toString()
                )
                .apply()

        } catch (e: Exception) {
            // Ignore storage errors safely.
        }
    }

    // =============================================================
    // REMOVE SPECIFIC MEMORY
    // =============================================================

    private fun removeMemory(
        context: Context,
        target: String
    ): Boolean {

        val memories =
            getMemories(context).toMutableList()

        val index = memories.indexOfFirst {

            it.equals(
                target.trim(),
                ignoreCase = true
            ) ||
                    it.contains(
                        target.trim(),
                        ignoreCase = true
                    )
        }

        if (index == -1) {
            return false
        }

        memories.removeAt(index)

        saveMemories(
            context,
            memories
        )

        return true
    }

    // =============================================================
    // REMOVE LAST MEMORY
    // =============================================================

    private fun removeLastMemory(
        context: Context
    ): Boolean {

        val memories =
            getMemories(context).toMutableList()

        if (memories.isEmpty()) {
            return false
        }

        memories.removeAt(
            memories.lastIndex
        )

        saveMemories(
            context,
            memories
        )

        return true
    }

    // =============================================================
    // CLEAR ALL MEMORIES
    // =============================================================

    private fun clearMemories(
        context: Context
    ) {

        context
            .getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(MEMORY_KEY)
            .apply()
    }

    // =============================================================
    // SEARCH MEMORY
    // =============================================================

    private fun searchMemory(
        context: Context,
        target: String
    ): String? {

        val cleanTarget =
            target.trim()

        if (cleanTarget.isBlank()) {
            return null
        }

        val memories =
            getMemories(context)

        return memories.firstOrNull {

            it.contains(
                cleanTarget,
                ignoreCase = true
            )
        }
    }
}
