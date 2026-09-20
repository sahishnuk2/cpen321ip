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
import com.example.cpen321application.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.net.NetworkInterface
import java.util.Collections
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

    var serverIp by remember {
        mutableStateOf<String?>(null)
    }

    var clientIp by remember {
        mutableStateOf<String?>(null)
    }

    var serverTime by remember {
        mutableStateOf<String?>(null)
    }

    var clientTime by remember {
        mutableStateOf<String?>(null)
    }

    var studentName by remember {
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
                                coroutineScope.launch {
                                    val result = auth(
                                        BuildConfig.API_BASE_URL,
                                        idToken
                                    )

                                    if (result.successful) {
                                        userName = name
                                        userEmail = email

                                        serverIp = fetchServerIP(BuildConfig.API_BASE_URL)
                                        clientIp = getLocalIpAddress(useIPv4 = true)
                                        serverTime = fetchServerLocalTime(BuildConfig.API_BASE_URL)
                                        clientTime = getClientLocalTime()
                                        studentName = fetchStudentName(BuildConfig.API_BASE_URL)

                                    } else {
                                        errorMessage = result.message
                                    }
                                    isLoading = false
                                }

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

//            Text("Name: $userName")
//            Text("Email: $userEmail")

            Text("Server IP: $serverIp")
            Text("Client IP: $clientIp")
            Text("Server Local Time: $serverTime")
            Text("Client Local Time: $clientTime")
            Text("My Name: $studentName")
            Text("User's Name: $userName")
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
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
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

private data class AuthResult(
    val successful: Boolean,
    val message: String
)

private suspend fun auth(apiBaseUrl: String, token: String): AuthResult = withContext(Dispatchers.IO) {
    val authUrl = "${apiBaseUrl.trimEnd('/')}/auth/google"
    var connection: HttpURLConnection? = null
    try {
        connection = (URL(authUrl).openConnection() as HttpURLConnection)
        connection.apply {
            requestMethod = "POST"
            connectTimeout = 5_000
            readTimeout = 5_000
            doOutput = true

            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        val requestBody = JSONObject().put("token", token).toString()

        connection.outputStream
            .bufferedWriter(Charsets.UTF_8)
            .use { writer ->
                writer.write(requestBody)
            }

        when (val code = connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                AuthResult(successful = true, message = body)
            }
            else -> {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                AuthResult(
                    successful = false,
                    message = "Login failed: HTTP $code" +
                        (errorBody?.let { " — $it" } ?: "")
                )
            }
        }
    } catch (e: Exception) {
        AuthResult(
            successful = false,
            message = "Backend unreachable ($authUrl): ${e.message ?: e.javaClass.simpleName}"
        )
    } finally {
        connection?.disconnect()
    }
}

private suspend fun fetchServerIP(apiBaseUrl: String): String = withContext(Dispatchers.IO) {
    val url = "${apiBaseUrl.trimEnd('/')}/server/ip"
    try {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 5_000
        }

        when (val code = connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                JSONObject(body).getString("ip")
            }
            else -> {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                "Backend error ($url): HTTP $code${errorBody?.let { " — $it" } ?: ""}"
            }
        }
    } catch (e: Exception) {
        "Backend unreachable ($url): ${e.message ?: e.javaClass.simpleName}"
    }
}

private suspend fun fetchServerLocalTime(apiBaseUrl: String): String = withContext(Dispatchers.IO) {
    val url = "${apiBaseUrl.trimEnd('/')}/server/time"
    try {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 5_000
        }

        when (val code = connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                JSONObject(body).getString("time")
            }
            else -> {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                "Backend error ($url): HTTP $code${errorBody?.let { " — $it" } ?: ""}"
            }
        }
    } catch (e: Exception) {
        "Backend unreachable ($url): ${e.message ?: e.javaClass.simpleName}"
    }
}

private suspend fun fetchStudentName(apiBaseUrl: String): String = withContext(Dispatchers.IO) {
    val url = "${apiBaseUrl.trimEnd('/')}/student/name"
    try {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 5_000
        }

        when (val code = connection.responseCode) {
            HttpURLConnection.HTTP_OK -> {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                "${json.getString("firstName")} ${json.getString("lastName")}"

            }
            else -> {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                "Backend error ($url): HTTP $code${errorBody?.let { " — $it" } ?: ""}"
            }
        }
    } catch (e: Exception) {
        "Backend unreachable ($url): ${e.message ?: e.javaClass.simpleName}"
    }
}

fun getClientLocalTime(): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss 'GMT'XXX")

    return ZonedDateTime
        .now(ZoneId.systemDefault())
        .format(formatter)
}

// Adapted from stack overflow: https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
fun getLocalIpAddress(useIPv4: Boolean): String {
    try {
        val interfaces = Collections.list(
            NetworkInterface.getNetworkInterfaces()
        )

        for (networkInterface in interfaces) {
            val addresses = Collections.list(
                networkInterface.inetAddresses
            )

            for (address in addresses) {
                if (address.isLoopbackAddress) {
                    continue
                }

                val hostAddress = address.hostAddress
                    ?: continue

                val isIPv4 = !hostAddress.contains(":")

                if (useIPv4 && isIPv4) {
                    return hostAddress
                }

                if (!useIPv4 && !isIPv4) {
                    val zoneIndex = hostAddress.indexOf("%")

                    if (zoneIndex < 0) {
                        return hostAddress.uppercase()
                    } else {
                        return hostAddress
                            .substring(0, zoneIndex)
                            .uppercase()
                    }
                }
            }
        }
    } catch (error: Exception) {
        return ""
    }

    return ""
}
