package com.example.myaiassistant

import android.app.Activity
import android.content.Intent
import android.content.MutableContextWrapper
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager


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
    // GOOGLE CONFIG DIAGNOSTIC
    // -------------------------------------------------

    private fun logAurixGoogleConfig() {

        try {

            val packageNameValue =
                packageName

            val webClientId =
                getString(
                    R.string.default_web_client_id
                )


            Log.d(
                "AURIX_AUTH",
                "========================================"
            )

            Log.d(
                "AURIX_AUTH",
                "AURIX GOOGLE CONFIG"
            )

            Log.d(
                "AURIX_AUTH",
                "PACKAGE = $packageNameValue"
            )

            Log.d(
                "AURIX_AUTH",
                "WEB CLIENT = $webClientId"
            )


            // -----------------------------------------
            // ANDROID 9+
            // -----------------------------------------

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.P
            ) {

                val packageInfo =
                    packageManager.getPackageInfo(
                        packageNameValue,
                        PackageManager.GET_SIGNING_CERTIFICATES
                    )

                val signingInfo =
                    packageInfo.signingInfo

                val signatures =
                    if (
                        signingInfo.hasMultipleSigners()
                    ) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }

                for (signature in signatures) {

                    val sha1Bytes =
                        MessageDigest
                            .getInstance("SHA-1")
                            .digest(
                                signature.toByteArray()
                            )

                    val sha1 =
                        sha1Bytes.joinToString(":") {
                            "%02X".format(it)
                        }

                    Log.d(
                        "AURIX_AUTH",
                        "APK SHA1 = $sha1"
                    )
                }

            } else {

                // -------------------------------------
                // ANDROID 7 / 8
                // -------------------------------------

                @Suppress("DEPRECATION")
                val packageInfo =
                    packageManager.getPackageInfo(
                        packageNameValue,
                        PackageManager.GET_SIGNATURES
                    )

                @Suppress("DEPRECATION")
                val signatures =
                    packageInfo.signatures

                for (signature in signatures) {

                    val sha1Bytes =
                        MessageDigest
                            .getInstance("SHA-1")
                            .digest(
                                signature.toByteArray()
                            )

                    val sha1 =
                        sha1Bytes.joinToString(":") {
                            "%02X".format(it)
                        }

                    Log.d(
                        "AURIX_AUTH",
                        "APK SHA1 = $sha1"
                    )
                }
            }


            Log.d(
                "AURIX_AUTH",
                "========================================"
            )

        } catch (e: Exception) {

            Log.e(
                "AURIX_AUTH",
                "Could not read Google config",
                e
            )
        }
    }


    // -------------------------------------------------
    // ON CREATE
    // -------------------------------------------------

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        auth =
            FirebaseAuth.getInstance()


        credentialManager =
            CredentialManager.create(
                this
            )


        // ---------------------------------------------
        // PRINT ACTUAL APK CONFIG
        // ---------------------------------------------

        logAurixGoogleConfig()


        // ---------------------------------------------
        // ALREADY LOGGED IN
        // ---------------------------------------------

        if (
            auth.currentUser != null
        ) {

            openAurix()

            return
        }


        createAuthInterface()
    }


    // -------------------------------------------------
    // LOGIN UI
    // -------------------------------------------------

    private fun createAuthInterface() {

        val root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    48,
                    48,
                    48,
                    48
                )

                setBackgroundColor(
                    Color.BLACK
                )
            }


        val title =
            TextView(this).apply {

                text =
                    "A U R I X"

                textSize =
                    36f

                typeface =
                    Typeface.DEFAULT_BOLD

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.WHITE
                )
            }


        val subtitle =
            TextView(this).apply {

                text =
                    "YOUR PERSONAL AI ASSISTANT"

                textSize =
                    13f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.LTGRAY
                )
            }


        val loginTitle =
            TextView(this).apply {

                text =
                    "LOGIN TO CONTINUE"

                textSize =
                    18f

                typeface =
                    Typeface.DEFAULT_BOLD

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.WHITE
                )
            }


        val googleButton =
            Button(this).apply {

                text =
                    "Continue with Google"

                setOnClickListener {

                    signInWithGoogle()
                }
            }


        val emailButton =
            Button(this).apply {

                text =
                    "Login / Sign Up with Email"

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

                topMargin =
                    8

                bottomMargin =
                    60
            }
        )


        root.addView(
            loginTitle,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                bottomMargin =
                    24
            }
        )


        root.addView(
            googleButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                bottomMargin =
                    16
            }
        )


        root.addView(
            emailButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )


        setContentView(
            root
        )
    }


    // -------------------------------------------------
    // GOOGLE SIGN IN
    // -------------------------------------------------

    private fun signInWithGoogle() {

        CoroutineScope(
            Dispatchers.Main
        ).launch {

            try {

                val webClientId =
                    getString(
                        R.string.default_web_client_id
                    )


                Log.d(
                    "AURIX_AUTH",
                    "Starting Sign in with Google button flow"
                )


                Log.d(
                    "AURIX_AUTH",
                    "Web client ID: $webClientId"
                )


                // -------------------------------------
                // GOOGLE BUTTON FLOW
                // -------------------------------------

                val googleOption =
                    GetSignInWithGoogleOption.Builder(
                        serverClientId = webClientId
                    )
                        .build()


                val request =
                    GetCredentialRequest.Builder()
                        .addCredentialOption(
                            googleOption
                        )
                        .build()


                // -------------------------------------
                // FOREGROUND ACTIVITY CONTEXT
                // -------------------------------------

                val mutableContext =
                    MutableContextWrapper(
                        this@AuthActivity
                    )


                val result =
                    credentialManager.getCredential(
                        context = mutableContext,
                        request = request
                    )


                // -------------------------------------
                // GOOGLE CREDENTIAL
                // -------------------------------------

                val credential =
                    result.credential


                val googleCredential =
                    GoogleIdTokenCredential.createFrom(
                        credential.data
                    )


                val idToken =
                    googleCredential.idToken


                Log.d(
                    "AURIX_AUTH",
                    "Google ID token received successfully"
                )


                // -------------------------------------
                // FIREBASE
                // -------------------------------------

                firebaseAuthWithGoogle(
                    idToken
                )

            } catch (
                e: GetCredentialException
            ) {

                Log.e(
                    "AURIX_AUTH",
                    "CredentialManager failed",
                    e
                )


                android.app.AlertDialog.Builder(
                    this@AuthActivity
                )
                    .setTitle(
                        "GOOGLE LOGIN FAILED"
                    )
                    .setMessage(
                        "${e.javaClass.name}\n\n" +
                            "${e.message ?: "No error message"}"
                    )
                    .setPositiveButton(
                        "OK",
                        null
                    )
                    .show()

            } catch (
                e: Exception
            ) {

                Log.e(
                    "AURIX_AUTH",
                    "Unexpected Google error",
                    e
                )


                android.app.AlertDialog.Builder(
                    this@AuthActivity
                )
                    .setTitle(
                        "GOOGLE LOGIN ERROR"
                    )
                    .setMessage(
                        "${e.javaClass.name}\n\n" +
                            "${e.message ?: "No error message"}"
                    )
                    .setPositiveButton(
                        "OK",
                        null
                    )
                    .show()
            }
        }
    }


    // -------------------------------------------------
    // FIREBASE GOOGLE AUTH
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

                    val user:
                        FirebaseUser? =
                        auth.currentUser


                    if (
                        user != null
                    ) {

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
}
