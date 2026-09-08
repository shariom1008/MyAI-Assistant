package com.example.myaiassistant

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * AURIX Personal Memory 2.0
 *
 * Stores persistent user facts separately from
 * temporary conversation history.
 */
object PersonalMemoryEngine {

    private const val PREFS = "aurix_personal_memory"
    private const val KEY_FACTS = "facts"

    data class Fact(
        val key: String,
        val value: String,
        val updatedAt: Long = System.currentTimeMillis()
    )

    private val facts = LinkedHashMap<String, Fact>()
    private val freeFacts = ArrayList<String>()

    private var initialized = false

    // =========================================================
    // INITIALIZE
    // =========================================================

    fun initialize(context: Context) {

        if (initialized) return

        initialized = true

        load(context)
    }

    // =========================================================
    // SAVE FACT
    // =========================================================

    fun remember(
        context: Context,
        key: String,
        value: String
    ) {

        initialize(context)

        val cleanKey = normalizeKey(key)
        val cleanValue = clean(value)

        if (
            cleanKey.isBlank() ||
            cleanValue.isBlank()
        ) {
            return
        }

        facts[cleanKey] =
            Fact(
                key = cleanKey,
                value = cleanValue
            )

        save(context)
    }

    // =========================================================
    // FREE MEMORY
    // =========================================================

    fun rememberThat(
    context: Context,
    statement: String
) {

    initialize(context)

    val cleanStatement =
        clean(statement)

    if (cleanStatement.isBlank()) {
        return
    }

    // ---------------------------------------------------------
    // STRUCTURED PERSONAL MEMORY
    // ---------------------------------------------------------

    val lower =
        cleanStatement.lowercase()

    fun saveStructured(
        key: String,
        value: String
    ) {

        val cleanKey =
            normalizeKey(key)

        val cleanValue =
            clean(value)

        if (
            cleanKey.isNotBlank() &&
            cleanValue.isNotBlank()
        ) {

            facts[cleanKey] =
                Fact(
                    key = cleanKey,
                    value = cleanValue
                )
        }
    }

    // NAME
    if (
        lower.startsWith("my name is ")
    ) {

        val value =
            cleanStatement
                .substring(
                    "my name is ".length
                )
                .trim()

        saveStructured(
            "name",
            value
        )
    }

    // LOCATION
    else if (
        lower.startsWith("i live in ")
    ) {

        val value =
            cleanStatement
                .substring(
                    "i live in ".length
                )
                .trim()

        saveStructured(
            "location",
            value
        )
    }

    else if (
        lower.startsWith("i am from ")
    ) {

        val value =
            cleanStatement
                .substring(
                    "i am from ".length
                )
                .trim()

        saveStructured(
            "location",
            value
        )
    }

    // JOB
    else if (
        lower.startsWith("i work as ")
    ) {

        val value =
            cleanStatement
                .substring(
                    "i work as ".length
                )
                .trim()

        saveStructured(
            "job",
            value
        )
    }

    else if (
        lower.startsWith("my job is ")
    ) {

        val value =
            cleanStatement
                .substring(
                    "my job is ".length
                )
                .trim()

        saveStructured(
            "job",
            value
        )
    }

    // FAVOURITE / FAVORITE
    else if (
        lower.startsWith("my favourite ") ||
        lower.startsWith("my favorite ")
    ) {

        val prefixLength =
            if (
                lower.startsWith("my favourite ")
            ) {
                "my favourite ".length
            } else {
                "my favorite ".length
            }

        val remainder =
            cleanStatement
                .substring(
                    prefixLength
                )
                .trim()

        val separator =
            Regex(
                "\\s+is\\s+"
            )

        val match =
            separator.find(
                remainder
            )

        if (match != null) {

            val type =
                remainder
                    .substring(
                        0,
                        match.range.first
                    )
                    .trim()

            val value =
                remainder
                    .substring(
                        match.range.last + 1
                    )
                    .trim()

            val key =
                when (
                    normalizeKey(type)
                ) {

                    "color",
                    "colour" ->
                        "favorite_color"

                    "food" ->
                        "favorite_food"

                    "song" ->
                        "favorite_song"

                    "movie" ->
                        "favorite_movie"

                    else ->
                        "favorite_${normalizeKey(type)}"
                }

            saveStructured(
                key,
                value
            )
        }
    }

    // LIKES
    else if (
        lower.startsWith("i like ")
    ) {

        val value =
            cleanStatement
                .substring(
                    "i like ".length
                )
                .trim()

        saveStructured(
            "likes",
            value
        )
    }

    else if (
        lower.startsWith("i love ")
    ) {

        val value =
            cleanStatement
                .substring(
                    "i love ".length
                )
                .trim()

        saveStructured(
            "likes",
            value
        )
    }

    // DISLIKES
    else if (
        lower.startsWith("i don't like ")
    ) {

        val value =
            cleanStatement
                .substring(
                    "i don't like ".length
                )
                .trim()

        saveStructured(
            "dislikes",
            value
        )
    }

    else if (
        lower.startsWith("i hate ")
    ) {

        val value =
            cleanStatement
                .substring(
                    "i hate ".length
                )
                .trim()

        saveStructured(
            "dislikes",
            value
        )
    }

    // ---------------------------------------------------------
    // ALWAYS KEEP ORIGINAL FREE-FORM MEMORY TOO
    // ---------------------------------------------------------

    if (
        !freeFacts.any {
            it.equals(
                cleanStatement,
                ignoreCase = true
            )
        }
    ) {

        freeFacts.add(
            cleanStatement
        )
    }

    while (
        freeFacts.size > 50
    ) {
        freeFacts.removeAt(0)
    }

    save(context)
    }

    // =========================================================
    // GET
    // =========================================================

    fun get(
        context: Context,
        key: String
    ): String? {

        initialize(context)

        return facts[
            normalizeKey(key)
        ]?.value
    }

    fun has(
        context: Context,
        key: String
    ): Boolean {

        return !get(
            context,
            key
        ).isNullOrBlank()
    }

    fun getFacts(
        context: Context
    ): List<Fact> {

        initialize(context)

        return facts.values.toList()
    }

    fun getFreeFacts(
        context: Context
    ): List<String> {

        initialize(context)

        return freeFacts.toList()
    }

    // =========================================================
    // FORGET
    // =========================================================

    fun forget(
        context: Context,
        key: String
    ): Boolean {

        initialize(context)

        val removed =
            facts.remove(
                normalizeKey(key)
            )

        save(context)

        return removed != null
    }

    fun forgetAll(
        context: Context
    ) {

        initialize(context)

        facts.clear()
        freeFacts.clear()

        save(context)
    }

    // =========================================================
    // SUMMARY
    // =========================================================

    fun buildSummary(
        context: Context
    ): String {

        initialize(context)

        val parts = mutableListOf<String>()

        get(context, "name")?.let {
            parts.add("Your name is $it")
        }

        get(context, "location")?.let {
            parts.add("You live in $it")
        }

        get(context, "job")?.let {
            parts.add("You work as $it")
        }

        get(context, "favorite_color")?.let {
            parts.add("Your favourite colour is $it")
        }

        get(context, "favorite_food")?.let {
            parts.add("Your favourite food is $it")
        }

        get(context, "favorite_song")?.let {
            parts.add("Your favourite song is $it")
        }

        get(context, "favorite_movie")?.let {
            parts.add("Your favourite movie is $it")
        }

        get(context, "likes")?.let {
            parts.add("You like $it")
        }

        get(context, "dislikes")?.let {
            parts.add("You don't like $it")
        }

        if (freeFacts.isNotEmpty()) {

            freeFacts
                .takeLast(8)
                .forEach {
                    parts.add(it)
                }
        }

        return if (parts.isEmpty()) {
            "I don't know much about you yet."
        } else {
            parts.joinToString(". ") + "."
        }
    }

    // =========================================================
    // DEBUG / COUNT
    // =========================================================

    fun size(
        context: Context
    ): Int {

        initialize(context)

        return facts.size + freeFacts.size
    }

    // =========================================================
    // NORMALIZE
    // =========================================================

    private fun normalizeKey(
        value: String
    ): String {

        return value
            .lowercase()
            .trim()
            .replace(
                Regex("[^a-z0-9]+"),
                "_"
            )
            .trim('_')
    }

    private fun clean(
        value: String
    ): String {

        return value
            .trim()
            .removeSuffix(".")
            .removeSuffix("!")
            .removeSuffix("?")
            .trim()
    }

    // =========================================================
    // PERSISTENCE - SAVE
    // =========================================================

    private fun save(
        context: Context
    ) {

        val root = JSONObject()

        val factsArray = JSONArray()

        facts.values.forEach {

            val obj = JSONObject()

            obj.put(
                "key",
                it.key
            )

            obj.put(
                "value",
                it.value
            )

            obj.put(
                "updatedAt",
                it.updatedAt
            )

            factsArray.put(obj)
        }

        val freeArray = JSONArray()

        freeFacts.forEach {
            freeArray.put(it)
        }

        root.put(
            "facts",
            factsArray
        )

        root.put(
            "freeFacts",
            freeArray
        )

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                KEY_FACTS,
                root.toString()
            )
            .apply()
    }

    // =========================================================
    // PERSISTENCE - LOAD
    // =========================================================

    private fun load(
        context: Context
    ) {

        facts.clear()
        freeFacts.clear()

        val data =
            context
                .getSharedPreferences(
                    PREFS,
                    Context.MODE_PRIVATE
                )
                .getString(
                    KEY_FACTS,
                    null
                )
                ?: return

        try {

            val root =
                JSONObject(data)

            val factsArray =
                root.optJSONArray(
                    "facts"
                )

            if (factsArray != null) {

                for (
                    i in 0 until factsArray.length()
                ) {

                    val obj =
                        factsArray.getJSONObject(i)

                    val key =
                        obj.optString("key")

                    val value =
                        obj.optString("value")

                    if (
                        key.isNotBlank() &&
                        value.isNotBlank()
                    ) {

                        facts[key] =
                            Fact(
                                key = key,
                                value = value,
                                updatedAt =
                                    obj.optLong(
                                        "updatedAt",
                                        System.currentTimeMillis()
                                    )
                            )
                    }
                }
            }

            val freeArray =
                root.optJSONArray(
                    "freeFacts"
                )

            if (freeArray != null) {

                for (
                    i in 0 until freeArray.length()
                ) {

                    val value =
                        freeArray.optString(i)

                    if (value.isNotBlank()) {
                        freeFacts.add(value)
                    }
                }
            }

        } catch (_: Exception) {

            facts.clear()
            freeFacts.clear()
        }
    }
}
