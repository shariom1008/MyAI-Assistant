package com.example.myaiassistant

object AurixAgentEngine {

    data class AgentTask(
        val id: String,
        val command: String,
        val createdAt: Long = System.currentTimeMillis()
    )

    data class AgentStep(
        val id: Int,
        val action: String,
        val command: String,
        val description: String
    )

    data class AgentPlan(
        val task: AgentTask,
        val steps: List<AgentStep>
    )

    data class AgentResult(
        val success: Boolean,
        val message: String,
        val completedSteps: Int = 0,
        val totalSteps: Int = 0
    )

    fun createTask(command: String): AgentTask {
        return AgentTask(
            id = "task_${System.currentTimeMillis()}",
            command = command.trim()
        )
    }

    fun createPlan(task: AgentTask): AgentPlan {

        val command = task.command.lowercase().trim()

        val steps = mutableListOf<AgentStep>()

        /*
         * YOUTUBE + PHONE
         */
        if (
            command.contains("youtube") &&
            command.contains("phone")
        ) {

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "OPEN_YOUTUBE",
                    command = "open youtube",
                    description = "Open YouTube."
                )
            )

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "OPEN_PHONE",
                    command = "open phone",
                    description = "Open the phone app."
                )
            )
        }

        /*
         * YOUTUBE + SETTINGS
         */
        else if (
            command.contains("youtube") &&
            command.contains("settings")
        ) {

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "OPEN_YOUTUBE",
                    command = "open youtube",
                    description = "Open YouTube."
                )
            )

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "OPEN_SETTINGS",
                    command = "open settings",
                    description = "Open Android settings."
                )
            )
        }

        /*
         * PHONE + SETTINGS
         */
        else if (
            command.contains("phone") &&
            command.contains("settings")
        ) {

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "OPEN_PHONE",
                    command = "open phone",
                    description = "Open the phone app."
                )
            )

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "OPEN_SETTINGS",
                    command = "open settings",
                    description = "Open Android settings."
                )
            )
        }

        /*
         * SINGLE YOUTUBE
         */
        else if (command.contains("youtube")) {

            steps.add(
                AgentStep(
                    id = 1,
                    action = "OPEN_YOUTUBE",
                    command = "open youtube",
                    description = "Open YouTube."
                )
            )
        }

        /*
         * SINGLE PHONE
         */
        else if (
            command.contains("open phone") ||
            command.contains("open dialer")
        ) {

            steps.add(
                AgentStep(
                    id = 1,
                    action = "OPEN_PHONE",
                    command = "open phone",
                    description = "Open the phone app."
                )
            )
        }

        /*
         * SINGLE SETTINGS
         */
        else if (command.contains("open settings")) {

            steps.add(
                AgentStep(
                    id = 1,
                    action = "OPEN_SETTINGS",
                    command = "open settings",
                    description = "Open Android settings."
                )
            )
        }

        /*
         * BLUETOOTH
         */
        else if (
            command.contains("bluetooth") &&
            (
                command.contains("paired") ||
                command.contains("devices")
            )
        ) {

            steps.add(
                AgentStep(
                    id = 1,
                    action = "BLUETOOTH",
                    command = "show my paired bluetooth devices",
                    description = "Show paired Bluetooth devices."
                )
            )
        }

        /*
         * UNKNOWN COMMAND
         */
        else {

            steps.add(
                AgentStep(
                    id = 1,
                    action = "GENERAL",
                    command = task.command,
                    description = task.command
                )
            )
        }

        return AgentPlan(
            task = task,
            steps = steps
        )
    }

    fun executeStep(step: AgentStep): String {

        return try {

            AurixCommandRouter.route(
                step.command
            )

        } catch (_: Exception) {

            "I couldn't execute this step."
        }
    }

    fun run(command: String): AgentResult {

        if (command.isBlank()) {

            return AgentResult(
                success = false,
                message = "No command provided."
            )
        }

        val task =
            createTask(command)

        val plan =
            createPlan(task)

        var completed = 0

        for (step in plan.steps) {

            val response =
                executeStep(step)

            if (
                response.isNotBlank() &&
                !response.startsWith("I understood:")
            ) {
                completed++
            }
        }

        return AgentResult(
            success =
                completed == plan.steps.size,

            message =
                if (completed == plan.steps.size) {
                    "Agent completed all planned steps."
                } else {
                    "Agent completed $completed of ${plan.steps.size} steps."
                },

            completedSteps = completed,
            totalSteps = plan.steps.size
        )
    }
}
