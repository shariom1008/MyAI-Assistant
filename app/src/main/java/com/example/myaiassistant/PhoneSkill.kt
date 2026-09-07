package com.example.myaiassistant

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * AURIX 2.0
 * Basic Phone Control Skill
 */
class PhoneSkill(
    private val context: Context
) : AurixCore.Skill {

    override fun canHandle(command: String): Boolean {
        val cmd = command.lowercase().trim()

        return cmd.contains("open") ||
                cmd.contains("launch") ||
                cmd.contains("start")
    }

    override fun execute(command: String): String? {
        val cmd = command.lowercase().trim()

        return when {
            cmd.contains("youtube") -> {
                openAppOrWebsite(
                    packageName = "com.google.android.youtube",
                    url = "https://www.youtube.com",
                    appName = "YouTube"
                )
            }

            cmd.contains("chrome") -> {
                openAppOrWebsite(
                    packageName = "com.android.chrome",
                    url = "https://www.google.com",
                    appName = "Chrome"
                )
            }

            cmd.contains("settings") -> {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    context.startActivity(intent)
                    "Opening settings."
                } catch (e: Exception) {
                    "I couldn't open settings."
                }
            }

            cmd.contains("phone") ||
                    cmd.contains("dialer") ||
                    cmd.contains("call") -> {
                try {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    context.startActivity(intent)
                    "Opening phone."
                } catch (e: Exception) {
                    "I couldn't open the phone app."
                }
            }

            else -> null
        }
    }

    private fun openAppOrWebsite(
        packageName: String,
        url: String,
        appName: String
    ): String {

        return try {

            val launchIntent =
                context.packageManager.getLaunchIntentForPackage(packageName)

            if (launchIntent != null) {

                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)

                "Opening $appName."

            } else {

                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(browserIntent)

                "Opening $appName in browser."
            }

        } catch (e: Exception) {
            "I couldn't open $appName."
        }
    }
}
