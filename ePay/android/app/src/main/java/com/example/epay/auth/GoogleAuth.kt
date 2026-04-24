package com.example.epay.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.epay.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

data class GoogleUser(val email: String, val displayName: String?, val idToken: String)

// in-memory session: survives for the app process lifetime
object Session {
    var user: GoogleUser? = null
}

suspend fun signInWithGoogle(context: Context): Result<GoogleUser> = runCatching {
    val option = GetGoogleIdOption.Builder()
        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .setFilterByAuthorizedAccounts(false)
        .build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
    val response = CredentialManager.create(context).getCredential(context, request)
    val cred = response.credential
    require(cred is CustomCredential &&
            cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        "Unexpected credential type"
    }
    val g = GoogleIdTokenCredential.createFrom(cred.data)
    GoogleUser(email = g.id, displayName = g.displayName, idToken = g.idToken)
        .also { Session.user = it }
}