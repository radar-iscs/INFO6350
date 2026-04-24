package com.example.epay.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.epay.auth.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ViewModel
data class CardSetupUi(val name: String = "", val email: String = "", val valid: Boolean = false)

class CardSetupViewModel : ViewModel() {
    private val _ui = MutableStateFlow(CardSetupUi(
        name  = Session.user?.displayName.orEmpty(),
        email = Session.user?.email.orEmpty()           // prefill from Google account
    ).let { it.copy(valid = validate(it.name, it.email)) })
    val ui: StateFlow<CardSetupUi> = _ui.asStateFlow()

    fun onName(v: String)  { _ui.update { it.copy(name = v, valid = validate(v, it.email)) } }
    fun onEmail(v: String) { _ui.update { it.copy(email = v, valid = validate(it.name, v)) } }

    private fun validate(n: String, e: String) =
        n.isNotBlank() && Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(e)
}

// hand-off between screens
object CardSetupStore { var name = ""; var email = "" }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSetupScreen(
    onContinue: () -> Unit,
    onHistory: () -> Unit,
    onSignOut: () -> Unit
) {
    val vm: CardSetupViewModel = viewModel()
    val state by vm.ui.collectAsState()
    val user = Session.user

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Set up payment") },
            actions = {
                IconButton(onClick = onHistory) {
                    Icon(Icons.Default.History, "History")
                }
            }
        )
    }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // signed-in user banner
            if (user != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null,
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                user.displayName ?: "Google user",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(user.email, style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = {
                            Session.user = null
                            onSignOut()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Sign out")
                        }
                    }
                }
            }

            Text("Customer details", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = state.name, onValueChange = vm::onName,
                label = { Text("Name") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.email, onValueChange = vm::onEmail,
                label = { Text("Email (you can change this)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Card details will be collected on the next screen using Stripe's secure PaymentSheet. No card data is stored in this app.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    CardSetupStore.name  = state.name
                    CardSetupStore.email = state.email
                    onContinue()
                },
                enabled = state.valid,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Continue") }
        }
    }
}