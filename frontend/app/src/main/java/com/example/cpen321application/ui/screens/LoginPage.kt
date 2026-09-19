package com.example.cpen321application.ui.screens

import android.content.Context
import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import java.security.SecureRandom

// TODO: To remove later
private const val WEB_CLIENT_ID = "509299733928-e5tgi112qh427v925r1msqeiahv6kjj4.apps.googleusercontent.com"

private fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom().nextBytes(randomBytes)

    return Base64.encodeToString(
        randomBytes,
        Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
    )
}

@Composable
fun LoginPage(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val credentialManager = remember {
        CredentialManager.create(context)
    }

    val coroutineScope = rememberCoroutineScope()

    var userName by remember {
        mutableStateOf<String?>(null)
    }

    var userEmail by remember {
        mutableStateOf<String?>(null)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login Page")

        Spacer(modifier = Modifier.height(24.dp))

        // Not logged in
        if (userName == null) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null

                        signInWithGoogle(
                            context = context,
                            credentialManager = credentialManager,

                            onSuccess = { name, email, idToken ->
                                userName = name
                                userEmail = email
                                isLoading = false

                                println("Google ID token received")
                                println("Token length: ${idToken.length}")
                            },

                            onError = { error ->
                                errorMessage = error
                                isLoading = false
                            }
                        )
                    }
                }
            ) {
                Text(
                    if (isLoading) {
                        "Signing in..."
                    } else {
                        "Sign in with Google"
                    }
                )
            }
        } else {
            Text("Signed in successfully")

            Spacer(modifier = Modifier.height(16.dp))

            Text("Name: $userName")
            Text("Email: $userEmail")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Error: $it")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBackClick) {
            Text("Back")
        }
    }
}

private suspend fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    onSuccess: (String?, String?, String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .setNonce(generateSecureRandomNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            context = context,
            request = request
        )

        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential =
                GoogleIdTokenCredential.createFrom(credential.data)

            onSuccess(
                googleCredential.displayName,
                googleCredential.id,
                googleCredential.idToken
            )
        } else {
            onError("Unexpected credential type")
        }
    } catch (error: GoogleIdTokenParsingException) {
        onError("Could not read Google sign-in response")
    } catch (error: GetCredentialException) {
        onError("Google sign-in failed: ${error.message}")
    }
}