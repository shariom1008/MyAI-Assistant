package com.example.myaiassistant

/**
 * AURIX 2.0 Skill Engine
 *
 * Manages all AURIX skills from one central place.
 */
object AurixSkillEngine {

    private val skills = mutableListOf<AurixCore.Skill>()

    /**
     * Add a skill to AURIX.
     */
    fun addSkill(skill: AurixCore.Skill) {
        if (!skills.contains(skill)) {
            skills.add(skill)
            AurixCore.registerSkill(skill)
        }
    }

    /**
     * Add multiple skills.
     */
    fun addSkills(vararg newSkills: AurixCore.Skill) {
        newSkills.forEach { addSkill(it) }
    }

    /**
     * Process a command through AURIX Core.
     */
    fun process(command: String): String {
        return AurixCore.process(command)
    }

    /**
     * Number of currently registered skills.
     */
    fun skillCount(): Int {
        return skills.size
    }

    /**
     * Check whether a skill is registered.
     */
    fun hasSkill(skill: AurixCore.Skill): Boolean {
        return skills.contains(skill)
    }

    /**
     * Remove all registered skills.
     */
    fun clear() {
        skills.clear()
        AurixCore.clearSkills()
    }
}
