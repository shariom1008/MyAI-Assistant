package com.example.myaiassistant

/**
 * AURIX 2.0 Core
 *
 * Central command-processing layer.
 *
 * Architecture:
 *
 * Voice
 *   ↓
 * AurixCore
 *   ↓
 * Skill Engine
 *   ↓
 * Action
 *   ↓
 * Response
 */
object AurixCore {

    interface Skill {
        fun canHandle(command: String): Boolean
        fun execute(command: String): String?
    }

    private val skills = mutableListOf<Skill>()

    fun registerSkill(skill: Skill) {
        if (!skills.contains(skill)) {
            skills.add(skill)
        }
    }

    fun registerSkills(vararg newSkills: Skill) {
        newSkills.forEach { registerSkill(it) }
    }

    fun process(command: String): String {
        val cleanedCommand = command.trim()

        if (cleanedCommand.isEmpty()) {
            return "I didn't hear anything."
        }

        for (skill in skills) {
            try {
                if (skill.canHandle(cleanedCommand)) {
                    val result = skill.execute(cleanedCommand)

                    if (!result.isNullOrBlank()) {
                        return result
                    }
                }
            } catch (e: Exception) {
                return "Something went wrong while processing that command."
            }
        }

        return fallback(cleanedCommand)
    }

    private fun fallback(command: String): String {
        return "I understood: $command"
    }

    fun clearSkills() {
        skills.clear()
    }

    fun getSkillCount(): Int {
        return skills.size
    }
}
