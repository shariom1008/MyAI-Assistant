package com.example.myaiassistant

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth

class AuthActivity : Activity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            openAurix()
            return
        }

        createAuthInterface()
    }

    private fun createAuthInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.BLACK)
        }

        val title = TextView(this).apply {
            text = "A U R I X"
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }

        val subtitle = TextView(this).apply {
            text = "YOUR PERSONAL AI ASSISTANT"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
        }

        val loginTitle = TextView(this).apply {
            text = "LOGIN TO CONTINUE"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }

        val googleButton = Button(this).apply {
            text = "Continue with Google"
            setOnClickListener {
                Toast.makeText(
                    this@AuthActivity,
                    "Google Login coming next.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val emailButton = Button(this).apply {
            text = "Login / Sign Up with Email"
            setOnClickListener {
                Toast.makeText(
                    this@AuthActivity,
                    "Email Login coming next.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
                bottomMargin = 60
            }
        )

        root.addView(
            loginTitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 24
            }
        )

        root.addView(
            googleButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        )

        root.addView(
            emailButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)
    }

    private fun openAurix() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
        finish()
    }
}
