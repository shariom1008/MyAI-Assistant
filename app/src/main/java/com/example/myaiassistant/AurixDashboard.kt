package com.example.myaiassistant

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

/**
 * AURIX 2.0
 *
 * Futuristic Quick Action Dashboard
 *
 * UI-only module.
 * Existing AURIX service and skills are not modified here.
 */
object AurixDashboard {

    interface ActionListener {
        fun onAction(action: String)
    }

    fun create(
        context: Context,
        listener: ActionListener
    ): View {

        val container =
            LinearLayout(context).apply {

                orientation =
                    LinearLayout.VERTICAL

                setPadding(
                    dp(context, 8),
                    dp(context, 8),
                    dp(context, 8),
                    dp(context, 8)
                )
            }

        // =====================================================
        // DASHBOARD TITLE
        // =====================================================

        val title =
            TextView(context).apply {

                text =
                    "AURIX CONTROL"

                textSize = 12f

                setTextColor(
                    Color.rgb(
                        130,
                        205,
                        240
                    )
                )

                typeface =
                    Typeface.DEFAULT_BOLD

                gravity =
                    Gravity.CENTER

                letterSpacing =
                    0.18f
            }

        container.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 35)
            )
        )

        // =====================================================
        // ROW 1
        // =====================================================

        val row1 =
            createRow(context)

        row1.addView(
            createAction(
                context,
                "ASK",
                "Voice",
                "ASK",
                listener
            )
        )

        row1.addView(
            createAction(
                context,
                "PHONE",
                "Control",
                "PHONE",
                listener
            )
        )

        container.addView(
            row1,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 72)
            )
        )

        // =====================================================
        // ROW 2
        // =====================================================

        val row2 =
            createRow(context)

        row2.addView(
            createAction(
                context,
                "MUSIC",
                "Audio",
                "MUSIC",
                listener
            )
        )

        row2.addView(
            createAction(
                context,
                "BT",
                "Devices",
                "BLUETOOTH",
                listener
            )
        )

        container.addView(
            row2,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 72)
            )
        )

        // =====================================================
        // ROW 3
        // =====================================================

        val row3 =
            createRow(context)

        row3.addView(
            createAction(
                context,
                "VISION",
                "Camera",
                "VISION",
                listener
            )
        )

        row3.addView(
            createAction(
                context,
                "WEB",
                "Internet",
                "WEB",
                listener
            )
        )

        container.addView(
            row3,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 72)
            )
        )

        // =====================================================
        // ROW 4
        // =====================================================

        val row4 =
            createRow(context)

        row4.addView(
            createAction(
                context,
                "WATCH",
                "Wearables",
                "WEARABLES",
                listener
            )
        )

        row4.addView(
            createAction(
                context,
                "SYSTEM",
                "Settings",
                "SYSTEM",
                listener
            )
        )

        container.addView(
            row4,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 72)
            )
        )

        return container
    }

    // =========================================================
    // ROW
    // =========================================================

    private fun createRow(
        context: Context
    ): LinearLayout {

        return LinearLayout(context).apply {

            orientation =
                LinearLayout.HORIZONTAL

            gravity =
                Gravity.CENTER

            setPadding(
                0,
                dp(context, 4),
                0,
                dp(context, 4)
            )
        }
    }

    // =========================================================
    // ACTION CARD
    // =========================================================

    private fun createAction(
        context: Context,
        mainText: String,
        subText: String,
        action: String,
        listener: ActionListener
    ): View {

        val card =
            LinearLayout(context).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                background =
                    createCardBackground()

                isClickable = true

                isFocusable = true

                setPadding(
                    dp(context, 6),
                    dp(context, 4),
                    dp(context, 6),
                    dp(context, 4)
                )

                setOnClickListener {

                    listener.onAction(action)

                    animate()
                        .scaleX(0.94f)
                        .scaleY(0.94f)
                        .setDuration(80)
                        .withEndAction {

                            animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                        }
                        .start()
                }
            }

        val main =
            TextView(context).apply {

                text =
                    mainText

                textSize = 13f

                setTextColor(
                    Color.WHITE
                )

                typeface =
                    Typeface.DEFAULT_BOLD

                gravity =
                    Gravity.CENTER

                letterSpacing =
                    0.08f
            }

        val sub =
            TextView(context).apply {

                text =
                    subText

                textSize = 8f

                setTextColor(
                    Color.rgb(
                        105,
                        165,
                        195
                    )
                )

                gravity =
                    Gravity.CENTER

                letterSpacing =
                    0.05f
            }

        card.addView(
            main,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 27)
            )
        )

        card.addView(
            sub,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(context, 20)
            )
        )

        val params =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1f
            )

        params.setMargins(
            dp(context, 5),
            0,
            dp(context, 5),
            0
        )

        card.layoutParams =
            params

        return card
    }

    // =========================================================
    // CARD BACKGROUND
    // =========================================================

    private fun createCardBackground():
        GradientDrawable {

        val drawable =
            GradientDrawable()

        drawable.setColor(
            Color.argb(
                125,
                12,
                35,
                70
            )
        )

        drawable.cornerRadius =
            18f

        drawable.setStroke(
            1,
            Color.argb(
                120,
                70,
                170,
                215
            )
        )

        return drawable
    }

    // =========================================================
    // DP
    // =========================================================

    private fun dp(
        context: Context,
        value: Int
    ): Int {

        return (
            value *
                context.resources
                    .displayMetrics
                    .density
            ).toInt()
    }
}
