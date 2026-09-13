package com.example.myaiassistant

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.security.SecureRandom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    private lateinit var statusText: TextView
    private lateinit var googleButton: TextView
    private lateinit var root: LinearLayout

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        credentialManager =
            CredentialManager.create(this)

        if (auth.currentUser != null) {
            openAurix()
            return
        }

        createInterface()
    }

    // -------------------------------------------------
    // UI
    // -------------------------------------------------

    private fun createInterface() {

        root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    dp(24),
                    dp(30),
                    dp(24),
                    dp(24)
                )

                background =
                    GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        intArrayOf(
                            Color.rgb(3, 6, 22),
                            Color.rgb(9, 7, 35),
                            Color.rgb(4, 14, 34)
                        )
                    )
            }

        setContentView(root)

        addTopSpacer()

        addAurixLogo()

        addSubtitle()

        addWelcome()

        addGoogleButton()

        addSecurityInfo()

        addStatus()

        addFooter()
    }

    private fun addTopSpacer() {

        root.addView(
            View(this),
            LinearLayout.LayoutParams(
                1,
                dp(18)
            )
        )
    }

    private fun addAurixLogo() {

        val logo =
            TextView(this).apply {

                text =
                    "A U R I X"

                textSize =
                    38f

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.WHITE
                )

                letterSpacing =
                    0.12f
            }

        root.addView(
            logo,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val line =
            TextView(this).apply {

                text =
                    "━━━━━━━━━━━━"

                textSize =
                    10f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        70,
                        190,
                        255
                    )
                )
            }

        root.addView(
            line,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )
    }

    private fun addSubtitle() {

        val subtitle =
            TextView(this).apply {

                text =
                    "INTELLIGENCE CORE"

                textSize =
                    10f

                gravity =
                    Gravity.CENTER

                letterSpacing =
                    0.28f

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.rgb(
                        90,
                        205,
                        255
                    )
                )
            }

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )
    }

    private fun addWelcome() {

        val welcome =
            TextView(this).apply {

                text =
                    "Welcome, Boss"

                textSize =
                    25f

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.WHITE
                )

                setPadding(
                    0,
                    dp(28),
                    0,
                    dp(8)
                )
            }

        root.addView(
            welcome,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val description =
            TextView(this).apply {

                text =
                    "Sign in to activate your personal AURIX assistant."

                textSize =
                    12f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        150,
                        160,
                        190
                    )
                )

                setPadding(
                    dp(10),
                    0,
                    dp(10),
                    dp(24)
                )
            }

        root.addView(
            description,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addGoogleButton() {

        googleButton =
            TextView(this).apply {

                text =
                    "Continue with Google"

                textSize =
                    14f

                gravity =
                    Gravity.CENTER

                typeface =
                    Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.WHITE
                )

                background =
                    createGoogleButtonBackground()

                elevation =
                    dp(8).toFloat()

                setPadding(
                    dp(12),
                    0,
                    dp(12),
                    0
                )

                setOnClickListener {

                    signInWithGoogle()
                }
            }

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(56)
            )

        params.setMargins(
            dp(4),
            dp(4),
            dp(4),
            dp(12)
        )

        root.addView(
            googleButton,
            params
        )
    }

    private fun addSecurityInfo() {

        val card =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(18),
                    dp(14),
                    dp(18),
                    dp(14)
                )

                background =
                    GradientDrawable().apply {

                        cornerRadius =
                            dp(18).toFloat()

                        setColor(
                            Color.argb(
                                55,
                                30,
                                45,
                                85
                            )
                        )

                        setStroke(
                            dp(1),
                            Color.argb(
                                90,
                                70,
                                180,
                                255
                            )
                        )
                    }
            }

        val secure =
            TextView(this).apply {

                text =
                    "●  SECURE AURIX CORE"

                textSize =
                    10f

                typeface =
                    Typeface.DEFAULT_BOLD

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        80,
                        220,
                        180
                    )
                )
            }

        card.addView(
            secure
        )

        val info =
            TextView(this).apply {

                text =
                    "Google authentication • Firebase protected"

                textSize =
                    9f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        135,
                        150,
                        185
                    )
                )

                setPadding(
                    0,
                    dp(7),
                    0,
                    0
                )
            }

        card.addView(
            info
        )

        val params =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(
            dp(4),
            dp(8),
            dp(4),
            dp(12)
        )

        root.addView(
            card,
            params
        )
    }

    private fun addStatus() {

        statusText =
            TextView(this).apply {

                text =
                    "AURIX authentication ready."

                textSize =
                    10f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        120,
                        135,
                        170
                    )
                )

                setPadding(
                    dp(8),
                    dp(8),
                    dp(8),
                    dp(8)
                )
            }

        root.addView(
            statusText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(50)
            )
        )
    }

    private fun addFooter() {

        val footerSpace =
            View(this)

        root.addView(
            footerSpace,
            LinearLayout.LayoutParams(
                1,
                0,
                1f
            )
        )

        val footer =
            TextView(this).apply {

                text =
                    "AURIX  •  PERSONAL AI ASSISTANT"

                textSize =
                    8f

                gravity =
                    Gravity.CENTER

                letterSpacing =
                    0.14f

                setTextColor(
                    Color.rgb(
                        80,
                        100,
                        140
                    )
                )
            }

        root.addView(
            footer,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )
    }

    // -------------------------------------------------
    // GOOGLE BUTTON BACKGROUND
    // -------------------------------------------------

    private fun createGoogleButtonBackground():
        GradientDrawable {

        return GradientDrawable().apply {

            cornerRadius =
                dp(18).toFloat()

            setColor(
                Color.rgb(
                    25,
                    35,
                    70
                )
            )

            setStroke(
                dp(1),
                Color.rgb(
                    85,
                    175,
                    255
                )
            )
        }
    }

    // -------------------------------------------------
    // SECURE NONCE
    // -------------------------------------------------

    private fun generateSecureRandomNonce(
        byteLength: Int = 32
    ): String {

        val randomBytes =
            ByteArray(byteLength)

        SecureRandom().nextBytes(
            randomBytes
        )

        return Base64.encodeToString(
            randomBytes,
            Base64.NO_WRAP or
                Base64.URL_SAFE or
                Base64.NO_PADDING
        )
    }

    // -------------------------------------------------
    // GOOGLE LOGIN
    // -------------------------------------------------

    private fun signInWithGoogle() {

        googleButton.isEnabled =
            false

        googleButton.text =
            "Connecting to Google..."

        statusText.text =
            "Secure Google authentication in progress..."

        CoroutineScope(
            Dispatchers.Main
        ).launch {

            try {

                val webClientId =
                    getString(
                        R.string.default_web_client_id
                    )

                val googleIdOption =
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(
                            false
                        )
                        .setServerClientId(
                            webClientId
                        )
                        .setNonce(
                            generateSecureRandomNonce()
                        )
                        .build()

                val request =
                    GetCredentialRequest.Builder()
                        .addCredentialOption(
                            googleIdOption
                        )
                        .build()

                statusText.text =
                    "Choose your Google account..."

                val result =
                    credentialManager.getCredential(
                        this@AuthActivity,
                        request
                    )

                val credential =
                    result.credential

                val googleCredential =
                    GoogleIdTokenCredential.createFrom(
                        credential.data
                    )

                val idToken =
                    googleCredential.idToken

                statusText.text =
                    "Authenticating with AURIX..."

                firebaseAuthWithGoogle(
                    idToken
                )

            } catch (
                e: GetCredentialException
            ) {

                resetGoogleButton()

                statusText.text =
                    "Google login cancelled or unavailable."

                showError(
                    "Google Login",
                    e.message
                        ?: "Google authentication failed."
                )

            } catch (
                e: Exception
            ) {

                resetGoogleButton()

                statusText.text =
                    "Authentication failed."

                showError(
                    "Authentication Error",
                    e.message
                        ?: "Unable to sign in."
                )
            }
        }
    }

    // -------------------------------------------------
    // FIREBASE
    // -------------------------------------------------

    private fun firebaseAuthWithGoogle(
        idToken: String
    ) {

        val credential =
            GoogleAuthProvider.getCredential(
                idToken,
                null
            )

        auth.signInWithCredential(
            credential
        )
            .addOnCompleteListener(
                this
            ) { task ->

                if (
                    task.isSuccessful
                ) {

                    statusText.text =
                        "Authentication successful. Starting AURIX..."

                    openAurix()

                } else {

                    resetGoogleButton()

                    statusText.text =
                        "Firebase authentication failed."

                    showError(
                        "Firebase Login",
                        task.exception?.message
                            ?: "Firebase authentication failed."
                    )
                }
            }
    }

    // -------------------------------------------------
    // RESET BUTTON
    // -------------------------------------------------

    private fun resetGoogleButton() {

        googleButton.isEnabled =
            true

        googleButton.text =
            "Continue with Google"
    }

    // -------------------------------------------------
    // ERROR
    // -------------------------------------------------

    private fun showError(
        title: String,
        message: String
    ) {

        android.app.AlertDialog.Builder(
            this
        )
            .setTitle(
                title
            )
            .setMessage(
                message
            )
            .setPositiveButton(
                "OK",
                null
            )
            .show()
    }

    // -------------------------------------------------
    // OPEN AURIX
    // -------------------------------------------------

    private fun openAurix() {

        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
        )

        finish()
    }

    // -------------------------------------------------
    // DP
    // -------------------------------------------------

    private fun dp(
        value: Int
    ): Int {

        return (
            value *
                resources.displayMetrics.density
            ).toInt()
    }
}
