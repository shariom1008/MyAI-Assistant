package com.example.myaiassistant

/**
 * AURIX 2.0 Command Router
 *
 * Routes recognized voice/text commands
 * through the new AURIX Skill Engine.
 */
object AurixCommandRouter {

    fun route(command: String): String {
        if (command.isBlank()) {
            return "I didn't hear a command."
        }

        return try {
            AurixSkillEngine.process(command)
        } catch (e: Exception) {
            "Sorry, I couldn't process that command."
        }
    }
}
