package com.example.myaiassistant

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.content.MutableContextWrapper

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException

import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import java.security.SecureRandom

class AuthActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

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
                signInWithGoogle()
            }
        }

        val emailButton = Button(this).apply {
            text = "Login / Sign Up with Email"

            setOnClickListener {
                Toast.makeText(
                    this@AuthActivity,
                    "Email Login next step mein add karenge.",
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

    private fun signInWithGoogle() {

        CoroutineScope(Dispatchers.Main).launch {

            try {

                val nonce =
                    generateSecureRandomNonce()

                val googleSignInOption =
                    GetSignInWithGoogleOption.Builder(
                        getString(R.string.default_web_client_id)
                    )
                        .setNonce(nonce)
                        .build()

                val request =
                    GetCredentialRequest.Builder()
                        .addCredentialOption(
                            googleSignInOption
                        )
                        .build()

                val mutableContext =
                    MutableContextWrapper(this@AuthActivity)

                val result =
                    credentialManager.getCredential(
                        context = mutableContext,
                        request = request
                    )

                val credential =
                    result.credential

                val googleCredential =
                    GoogleIdTokenCredential
                        .createFrom(
                            credential.data
                        )

                val idToken =
                    googleCredential.idToken

                firebaseAuthWithGoogle(idToken)
} catch (e: GetCredentialException) {

    Log.e(
        "AURIX_AUTH",
        "Google CredentialManager error",
        e
    )

    android.app.AlertDialog.Builder(this@AuthActivity)
        .setTitle("Google Sign-In Error")
        .setMessage(
            "${e.javaClass.name}\n\n${e.message ?: "No error message"}"
        )
        .setPositiveButton("OK", null)
        .show()
        
            } catch (e: Exception) {

                Toast.makeText(
                    this@AuthActivity,
                    "Google error: ${e.javaClass.simpleName}\n${e.message ?: "No error message"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun generateSecureRandomNonce(
        byteLength: Int = 32
    ): String {

        val randomBytes =
            ByteArray(byteLength)

        SecureRandom().nextBytes(randomBytes)

        return Base64.encodeToString(
            randomBytes,
            Base64.NO_WRAP or
                    Base64.URL_SAFE or
                    Base64.NO_PADDING
        )
    }

    private fun firebaseAuthWithGoogle(
        idToken: String
    ) {

        val credential =
            GoogleAuthProvider.getCredential(
                idToken,
                null
            )

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val user: FirebaseUser? =
                        auth.currentUser

                    if (user != null) {
                        openAurix()
                    }

                } else {

                    Toast.makeText(
                        this,
                        "Firebase Google Login failed.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun openAurix() {

        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
        )

        finish()
    }
}
