package com.example.myaiassistant

/**
 * AURIX 2.0
 *
 * Multi-Step Agent Planner
 *
 * Converts a user goal into a sequence of
 * smaller actions that can later be executed
 * by AURIX skills.
 */
object AurixAgentEngine {

    data class AgentTask(
        val id: String,
        val command: String,
        val createdAt: Long = System.currentTimeMillis()
    )

    data class AgentStep(
        val id: Int,
        val action: String,
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

    /**
     * Creates an agent task.
     */
    fun createTask(command: String): AgentTask {

        return AgentTask(
            id = "task_${System.currentTimeMillis()}",
            command = command.trim()
        )
    }

    /**
     * Converts one user command into multiple steps.
     */
    fun createPlan(task: AgentTask): AgentPlan {

        val command = task.command.lowercase()

        val steps = mutableListOf<AgentStep>()

        /*
         * Bluetooth + music workflow
         */
        if (
            command.contains("bluetooth") &&
            (
                command.contains("music") ||
                command.contains("play")
            )
        ) {

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "BLUETOOTH",
                    description = "Connect or prepare the Bluetooth audio device."
                )
            )

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "PLAY",
                    description = "Start media playback."
                )
            )
        }

        /*
         * Open application + music workflow
         */
        if (
            command.contains("youtube") &&
            (
                command.contains("music") ||
                command.contains("play")
            )
        ) {

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "OPEN_YOUTUBE",
                    description = "Open YouTube."
                )
            )

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "PLAY",
                    description = "Start media playback."
                )
            )
        }

        /*
         * Phone workflow
         */
        if (
            command.contains("open phone") ||
            command.contains("open dialer")
        ) {

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "OPEN_PHONE",
                    description = "Open the phone dialer."
                )
            )
        }

        /*
         * Settings workflow
         */
        if (command.contains("open settings")) {

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "OPEN_SETTINGS",
                    description = "Open Android settings."
                )
            )
        }

        /*
         * If no specialized workflow was detected,
         * keep the original command as one step.
         */
        if (steps.isEmpty()) {

            steps.add(
                AgentStep(
                    id = 1,
                    action = "GENERAL",
                    description = task.command
                )
            )
        }

        return AgentPlan(
            task = task,
            steps = steps
        )
    }

    /**
     * Executes the current plan.
     *
     * Actual skill execution will be connected
     * in the next stage.
     */
    fun execute(plan: AgentPlan): AgentResult {

        if (plan.steps.isEmpty()) {

            return AgentResult(
                success = false,
                message = "No execution steps were created."
            )
        }

        return AgentResult(
            success = true,
            message = "Agent plan created with ${plan.steps.size} step(s).",
            completedSteps = 0,
            totalSteps = plan.steps.size
        )
    }

    /**
     * Complete Agent pipeline.
     */
    fun run(command: String): AgentResult {

        if (command.isBlank()) {

            return AgentResult(
                success = false,
                message = "No command provided."
            )
        }

        val task = createTask(command)

        val plan = createPlan(task)

        return execute(plan)
    }
}
