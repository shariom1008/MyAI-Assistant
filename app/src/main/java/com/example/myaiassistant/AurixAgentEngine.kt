package com.example.myaiassistant

/**
 * AURIX 2.0
 *
 * Multi-Step Agent Planner + Executor
 *
 * Converts a user goal into a sequence of
 * smaller actions and executes supported actions
 * through the existing AURIX command router.
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

    fun createTask(command: String): AgentTask {

        return AgentTask(
            id = "task_${System.currentTimeMillis()}",
            command = command.trim()
        )
    }

    fun createPlan(task: AgentTask): AgentPlan {

        val command = task.command.lowercase()

        val steps = mutableListOf<AgentStep>()

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

        if (command.contains("open settings")) {

            steps.add(
                AgentStep(
                    id = steps.size + 1,
                    action = "OPEN_SETTINGS",
                    description = "Open Android settings."
                )
            )
        }

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
     * Executes the current agent plan.
     */
    fun execute(plan: AgentPlan): AgentResult {

        if (plan.steps.isEmpty()) {

            return AgentResult(
                success = false,
                message = "No execution steps were created."
            )
        }

        var completedSteps = 0

        for (step in plan.steps) {

            val command = when (step.action) {

                "OPEN_YOUTUBE" ->
                    "open youtube"

                "OPEN_PHONE" ->
                    "open phone"

                "OPEN_SETTINGS" ->
                    "open settings"

                "BLUETOOTH" ->
                    "show my paired bluetooth devices"

                "PLAY" ->
                    "play music"

                "GENERAL" ->
                    step.description

                else ->
                    continue
            }

            val response = AurixCommandRouter.route(command)

            if (
                response.isNotBlank() &&
                !response.startsWith("I understood:")
            ) {
                completedSteps++
            }
        }

        return AgentResult(
            success = completedSteps == plan.steps.size,
            message =
                if (completedSteps == plan.steps.size) {
                    "Agent completed all planned steps."
                } else {
                    "Agent completed $completedSteps of ${plan.steps.size} steps."
                },
            completedSteps = completedSteps,
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
