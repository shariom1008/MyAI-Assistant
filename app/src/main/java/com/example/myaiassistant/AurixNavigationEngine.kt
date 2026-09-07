package com.example.myaiassistant

import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * AURIX 2.0
 *
 * Navigation Engine
 *
 * Handles:
 * - Home screen
 * - Back
 * - App launching
 * - Android Settings
 */
object AurixNavigationEngine {

    fun goHome(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            true

        } catch (_: Exception) {
            false
        }
    }

    fun goBack(context: Context): Boolean {
        return try {
            val intent = Intent(
                "android.intent.action.VIEW"
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            false

        } catch (_: Exception) {
            false
        }
    }

    fun openSettings(context: Context): Boolean {
        return try {
            val intent = Intent(
                Settings.ACTION_SETTINGS
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            true

        } catch (_: Exception) {
            false
        }
    }

    fun openApp(
        context: Context,
        packageName: String
    ): Boolean {

        return try {

            val intent =
                context.packageManager
                    .getLaunchIntentForPackage(packageName)

            if (intent != null) {

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                )

                context.startActivity(intent)

                true

            } else {
                false
            }

        } catch (_: Exception) {
            false
        }
    }

    fun openAppByName(
        context: Context,
        appName: String
    ): Boolean {

        val packageManager =
            context.packageManager

        val installedApps =
            packageManager
                .getInstalledApplications(0)

        val target =
            installedApps.firstOrNull {

                val label =
                    packageManager
                        .getApplicationLabel(it)
                        .toString()

                label.equals(
                    appName,
                    ignoreCase = true
                )
            }

        return if (target != null) {

            openApp(
                context,
                target.packageName
            )

        } else {
            false
        }
    }
}
