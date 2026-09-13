package com.example.myaiassistant

import android.app.Activity
import android.content.ClipboardManager
import android.content.ClipData
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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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

    private lateinit var diagnosticText: TextView

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
    // GET APK SHA-1
    // -------------------------------------------------

    private fun getApkSha1(): String {

        return try {

            val packageNameValue =
                packageName

            val sha1List =
                mutableListOf<String>()

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

                for (
                    signature in signatures
                ) {

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

                    sha1List.add(
                        sha1
                    )
                }

            } else {

                @Suppress("DEPRECATION")
                val packageInfo =
                    packageManager.getPackageInfo(
                        packageNameValue,
                        PackageManager.GET_SIGNATURES
                    )

                @Suppress("DEPRECATION")
                val signatures =
                    packageInfo.signatures

                for (
                    signature in signatures
                ) {

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

                    sha1List.add(
                        sha1
                    )
                }
            }

            if (
                sha1List.isEmpty()
            ) {

                "SHA-1 NOT FOUND"

            } else {

                sha1List.joinToString(
                    separator = "\n"
                )
            }

        } catch (
            e: Exception
        ) {

            "SHA-1 ERROR:\n${e.javaClass.name}\n${e.message}"
        }
    }

    // -------------------------------------------------
    // GOOGLE CONFIG
    // -------------------------------------------------

    private fun getGoogleConfigText(): String {

        val packageNameValue =
            packageName

        val webClientId =
            try {

                getString(
                    R.string.default_web_client_id
                )

            } catch (
                e: Exception
            ) {

                "WEB CLIENT ID NOT FOUND"
            }

        val apkSha1 =
            getApkSha1()

        return """
AURIX GOOGLE CONFIG

--------------------------------

PACKAGE

$packageNameValue

--------------------------------

APK SHA-1

$apkSha1

--------------------------------

WEB CLIENT ID

$webClientId

--------------------------------

EXPECTED CI DEBUG SHA-1

F7:1D:CD:61:9E:22:23:95:B6:97:96:B5:B2:CF:B1:3C:86:C1:58:2E

--------------------------------
        """.trimIndent()
    }

    // -------------------------------------------------
    // LOG CONFIG
    // -------------------------------------------------

    private fun logAurixGoogleConfig() {

        try {

            val config =
                getGoogleConfigText()

            Log.d(
                "AURIX_AUTH",
                "========================================"
            )

            Log.d(
                "AURIX_AUTH",
                config
            )

            Log.d(
                "AURIX_AUTH",
                "========================================"
            )

        } catch (
            e: Exception
        ) {

            Log.e(
                "AURIX_AUTH",
                "Could not read Google config",
                e
            )
        }
    }

    // -------------------------------------------------
    // ACTIVITY CREATE
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

        logAurixGoogleConfig()

        // -----------------------------------------
        // ALREADY LOGGED IN
        // -----------------------------------------

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
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    40,
                    40,
                    40,
                    40
                )

                setBackgroundColor(
                    Color.BLACK
                )
            }

        // -----------------------------------------
        // TITLE
        // -----------------------------------------

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

        // -----------------------------------------
        // SUBTITLE
        // -----------------------------------------

        val subtitle =
            TextView(this).apply {

                text =
                    "GOOGLE LOGIN DIAGNOSTIC"

                textSize =
                    14f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.LTGRAY
                )
            }

        // -----------------------------------------
        // CONFIG BUTTON
        // -----------------------------------------

        val configButton =
            Button(this).apply {

                text =
                    "GOOGLE CONFIG CHECK"

                setOnClickListener {

                    showGoogleConfig()
                }
            }

        // -----------------------------------------
        // COPY CONFIG BUTTON
        // -----------------------------------------

        val copyButton =
            Button(this).apply {

                text =
                    "COPY CONFIG"

                setOnClickListener {

                    copyGoogleConfig()
                }
            }

        // -----------------------------------------
        // GOOGLE BUTTON
        // -----------------------------------------

        val googleButton =
            Button(this).apply {

                text =
                    "Continue with Google"

                setOnClickListener {

                    signInWithGoogle()
                }
            }

        // -----------------------------------------
        // EMAIL BUTTON
        // -----------------------------------------

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

        // -----------------------------------------
        // DIAGNOSTIC OUTPUT
        // -----------------------------------------

        diagnosticText =
            TextView(this).apply {

                text =
                    "Diagnostic ready."

                textSize =
                    12f

                setTextColor(
                    Color.LTGRAY
                )

                setPadding(
                    16,
                    16,
                    16,
                    16
                )
            }

        // -----------------------------------------
        // ADD VIEWS
        // -----------------------------------------

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
                    24
            }
        )

        root.addView(
            configButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                bottomMargin =
                    8
            }
        )

        root.addView(
            copyButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                bottomMargin =
                    8
            }
        )

        root.addView(
            googleButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                bottomMargin =
                    8
            }
        )

        root.addView(
            emailButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {

                bottomMargin =
                    16
            }
        )

        val scrollView =
            ScrollView(this).apply {

                addView(
                    diagnosticText,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0
            ).apply {

                weight =
                    1f
            }
        )

        setContentView(
            root
        )
    }

    // -------------------------------------------------
    // SHOW GOOGLE CONFIG
    // -------------------------------------------------

    private fun showGoogleConfig() {

        val config =
            getGoogleConfigText()

        diagnosticText.text =
            config

        android.app.AlertDialog.Builder(
            this
        )
            .setTitle(
                "AURIX GOOGLE CONFIG"
            )
            .setMessage(
                config
            )
            .setPositiveButton(
                "OK",
                null
            )
            .show()
    }

    // -------------------------------------------------
    // COPY GOOGLE CONFIG
    // -------------------------------------------------

    private fun copyGoogleConfig() {

        val config =
            getGoogleConfigText()

        val clipboard =
            getSystemService(
                CLIPBOARD_SERVICE
            ) as ClipboardManager

        val clip =
            ClipData.newPlainText(
                "AURIX Google Config",
                config
            )

        clipboard.setPrimaryClip(
            clip
        )

        Toast.makeText(
            this,
            "Google config copied.",
            Toast.LENGTH_SHORT
        ).show()
    }

    // -------------------------------------------------
    // GOOGLE SIGN-IN
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

                diagnosticText.text =
                    """
Starting Google login...

PACKAGE:
$packageName

WEB CLIENT:
$webClientId

APK SHA-1:
${getApkSha1()}

Calling CredentialManager...
                    """.trimIndent()

                Log.d(
                    "AURIX_AUTH",
                    "Starting GetGoogleIdOption flow"
                )

                // -------------------------------------
                // GOOGLE ID OPTION
                // -------------------------------------

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

                // -------------------------------------
                // REQUEST
                // -------------------------------------

                val request =
                    GetCredentialRequest.Builder()
                        .addCredentialOption(
                            googleIdOption
                        )
                        .build()

                // -------------------------------------
                // ACTIVITY CONTEXT
                //
                // MutableContextWrapper is intentionally
                // used here according to Android guidance.
                // -------------------------------------

                val mutableContext =
                    MutableContextWrapper(
                        this@AuthActivity
                    )

                Log.d(
                    "AURIX_AUTH",
                    "Calling CredentialManager..."
                )

                // -------------------------------------
                // GET CREDENTIAL
                // -------------------------------------

                val result =
                    credentialManager.getCredential(
                        context =
                            mutableContext,
                        request =
                            request
                    )

                Log.d(
                    "AURIX_AUTH",
                    "Credential received successfully"
                )

                diagnosticText.text =
                    diagnosticText.text.toString() +
                        "\n\nCredential received successfully."

                // -------------------------------------
                // CREDENTIAL
                // -------------------------------------

                val credential =
                    result.credential

                Log.d(
                    "AURIX_AUTH",
                    "Credential type = ${credential.type}"
                )

                diagnosticText.text =
                    diagnosticText.text.toString() +
                        "\nCredential type = ${credential.type}"

                // -------------------------------------
                // GOOGLE ID TOKEN
                // -------------------------------------

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

                diagnosticText.text =
                    diagnosticText.text.toString() +
                        "\n\nGoogle ID token received."

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

                val errorText =
                    """
GOOGLE LOGIN FAILED

Exception:
${e.javaClass.name}

Message:
${e.message ?: "No error message"}

--------------------------------

PACKAGE:
$packageName

APK SHA-1:
${getApkSha1()}

WEB CLIENT:
${try {
                        getString(
                            R.string.default_web_client_id
                        )
                    } catch (
                        _: Exception
                    ) {
                        "NOT FOUND"
                    }}
                    """.trimIndent()

                diagnosticText.text =
                    errorText

                showError(
                    "GOOGLE LOGIN FAILED",
                    "${e.javaClass.name}\n\n" +
                        "${e.message ?: "No error message"}"
                )

            } catch (
                e: Exception
            ) {

                Log.e(
                    "AURIX_AUTH",
                    "Unexpected Google error",
                    e
                )

                val errorText =
                    """
GOOGLE LOGIN ERROR

Exception:
${e.javaClass.name}

Message:
${e.message ?: "No error message"}

--------------------------------

PACKAGE:
$packageName

APK SHA-1:
${getApkSha1()}

WEB CLIENT:
${try {
                        getString(
                            R.string.default_web_client_id
                        )
                    } catch (
                        _: Exception
                    ) {
                        "NOT FOUND"
                    }}
                    """.trimIndent()

                diagnosticText.text =
                    errorText

                showError(
                    "GOOGLE LOGIN ERROR",
                    "${e.javaClass.name}\n\n" +
                        "${e.message ?: "No error message"}"
                )
            }
        }
    }

    // -------------------------------------------------
    // ERROR DIALOG
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
    // FIREBASE GOOGLE AUTH
    // -------------------------------------------------

    private fun firebaseAuthWithGoogle(
        idToken: String
    ) {

        diagnosticText.text =
            diagnosticText.text.toString() +
                "\n\nSending Google token to Firebase..."

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

                    Log.d(
                        "AURIX_AUTH",
                        "Firebase Google login successful"
                    )

                    diagnosticText.text =
                        diagnosticText.text.toString() +
                            "\nFirebase login SUCCESS."

                    if (
                        user != null
                    ) {

                        diagnosticText.text =
                            diagnosticText.text.toString() +
                                "\nUser = ${user.email}"

                        openAurix()
                    }

                } else {

                    Log.e(
                        "AURIX_AUTH",
                        "Firebase Google login failed",
                        task.exception
                    )

                    val message =
                        task.exception?.message
                            ?: "Firebase Google Login failed."

                    diagnosticText.text =
                        diagnosticText.text.toString() +
                            "\n\nFIREBASE LOGIN FAILED\n$message"

                    showError(
                        "FIREBASE LOGIN FAILED",
                        message
                    )
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
