package com.example.epay.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.epay.auth.Session
import com.example.epay.data.PaymentRepository
import com.example.epay.data.PaymentResponse
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

// UI state
sealed interface Outcome {
    data object None : Outcome
    data class Success(val id: String) : Outcome
    data class Failure(val reason: String) : Outcome
}

/** One-shot events that should not re-fire on recomposition/navigation. */
sealed interface PaymentEvent {
    data object ShowSuccessToast : PaymentEvent
    data class ShowFailureToast(val reason: String) : PaymentEvent
}

data class PaymentUi(
    val amountText: String = "",
    val currency: String = "usd",
    val note: String = "",
    val submitting: Boolean = false, // server call in-flight (before PaymentSheet)
    val finalizing: Boolean = false, // after PaymentSheet, waiting for Sheet/Room log
    val amountError: String? = null,
    val currencyError: String? = null,
    val pendingIntent: PaymentResponse? = null,
    val currentIntentId: String? = null,
    val outcome: Outcome = Outcome.None,
    val fatalError: String? = null
) {
    val busy get() = submitting || finalizing
    val canSubmit get() =
        !busy &&
                amountText.isNotBlank() &&
                amountError == null &&
                currencyError == null
}

class PaymentViewModel(private val repo: PaymentRepository) : ViewModel() {
    private val _ui = MutableStateFlow(PaymentUi())
    val ui: StateFlow<PaymentUi> = _ui.asStateFlow()

    private val _events = Channel<PaymentEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // form handlers
    fun onAmount(raw: String) {
        val clean = raw.filter { it.isDigit() || it == '.' }
            .replace(Regex("""(\..*)\."""), "$1")
        val err = when {
            clean.isBlank() -> null
            clean.toBigDecimalOrNull() == null -> "Invalid number"
            clean.toBigDecimal() < BigDecimal("0.50") -> "Must be at least 0.50"
            clean.toBigDecimal() > BigDecimal("999999.99") -> "Too large"
            else -> null
        }
        _ui.update { it.copy(amountText = clean, amountError = err, outcome = Outcome.None) }
    }

    fun onCurrency(raw: String) {
        val v = raw.lowercase().take(3)
        val err = when {
            v.isEmpty() -> "Currency required"
            v.length != 3 -> "Must be 3 letters (e.g. usd, eur)"
            v !in SUPPORTED -> "Unsupported currency"
            else -> null
        }
        _ui.update { it.copy(currency = v, currencyError = err, outcome = Outcome.None) }
    }

    fun onNote(v: String) {
        _ui.update { it.copy(note = v.take(140), outcome = Outcome.None) }
    }

    // actions
    fun submit() {
        val s = _ui.value
        if (!s.canSubmit) return
        val minor = s.amountText.toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .movePointRight(2)
            .toLong()

        _ui.update { it.copy(submitting = true, pendingIntent = null,
            outcome = Outcome.None, fatalError = null) }
        viewModelScope.launch {
            try {
                val response = repo.createPayment(
                    amount = minor,
                    currency = s.currency,
                    note = s.note,
                    customerName = CardSetupStore.name
                        .ifBlank { Session.user?.displayName ?: "Anonymous" },
                    customerEmail = CardSetupStore.email,
                    googleEmail = CardSetupStore.email
                        .ifBlank { Session.user?.email }
                )
                _ui.update { it.copy(
                    submitting = false,
                    pendingIntent = response,
                    currentIntentId = response.paymentIntentId
                ) }
            } catch (t: Throwable) {
                _ui.update { it.copy(submitting = false,
                    fatalError = t.localizedMessage ?: "Network error") }
            }
        }
    }

    // called after PaymentSheet returns
    // set `finalizing = true` during the trailing Room + Sheets update, then emit a one-shot toast event
    fun onPaymentSheetResult(result: PaymentSheetResult) {
        val id = _ui.value.currentIntentId ?: return

        _ui.update { it.copy(finalizing = true, pendingIntent = null) }

        viewModelScope.launch {
            when (result) {
                is PaymentSheetResult.Completed -> {
                    repo.updateStatus(id, "succeeded")
                    // clear form on success
                    _ui.value = PaymentUi(outcome = Outcome.Success(id))
                    _events.send(PaymentEvent.ShowSuccessToast)
                }
                is PaymentSheetResult.Failed -> {
                    val reason = result.error.localizedMessage ?: "Payment failed"
                    repo.updateStatus(id, "failed", reason)
                    _ui.update { it.copy(
                        finalizing = false,
                        currentIntentId = null,
                        outcome = Outcome.Failure(reason)
                    ) }
                    _events.send(PaymentEvent.ShowFailureToast(reason))
                }
                is PaymentSheetResult.Canceled -> {
                    repo.updateStatus(id, "canceled")
                    _ui.update { it.copy(
                        finalizing = false,
                        currentIntentId = null,
                        outcome = Outcome.Failure("Payment canceled")
                    ) }
                    _events.send(PaymentEvent.ShowFailureToast("Payment canceled"))
                }
            }
        }
    }

    fun consumePendingIntent() {
        _ui.update { it.copy(pendingIntent = null) }
    }

    companion object {
        private val SUPPORTED = setOf("usd", "eur", "gbp", "cad", "aud", "jpy", "cny", "hkd")
    }
}

class PaymentVmFactory(private val repo: PaymentRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PaymentViewModel(repo) as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    repository: PaymentRepository,
    onBack: () -> Unit,
    onHistory: () -> Unit
) {
    val vm: PaymentViewModel = viewModel(factory = PaymentVmFactory(repository))
    val state by vm.ui.collectAsState()
    val context = LocalContext.current

    val paymentSheet = rememberPaymentSheet { result ->
        vm.onPaymentSheetResult(result)
    }

    // launch PaymentSheet when client_secret arrives
    LaunchedEffect(state.pendingIntent) {
        val cs = state.pendingIntent?.clientSecret
        if (state.pendingIntent?.success == true && !cs.isNullOrBlank()) {
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = cs,
                configuration = PaymentSheet.Configuration("ePay Demo")
            )
            vm.consumePendingIntent()
        }
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                PaymentEvent.ShowSuccessToast ->
                    Toast.makeText(context, "Payment successful", Toast.LENGTH_SHORT).show()
                is PaymentEvent.ShowFailureToast ->
                    Toast.makeText(context, event.reason, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("New payment") },
            navigationIcon = {
                IconButton(onClick = onBack, enabled = !state.busy) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(onClick = onHistory, enabled = !state.busy) {
                    Icon(Icons.Default.History, "History")
                }
            }
        )
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.amountText, onValueChange = vm::onAmount,
                    label = { Text("Amount") }, placeholder = { Text("0.50") },
                    isError = state.amountError != null,
                    supportingText = state.amountError?.let { { Text(it) } },
                    singleLine = true,
                    enabled = !state.busy,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.currency, onValueChange = vm::onCurrency,
                    label = { Text("Currency (usd, eur, gbp, …)") },
                    isError = state.currencyError != null,
                    supportingText = state.currencyError?.let { { Text(it) } },
                    singleLine = true,
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.note, onValueChange = vm::onNote,
                    label = { Text("Message / note") }, minLines = 3,
                    enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth()
                )

                when (val o = state.outcome) {
                    is Outcome.Success -> OutcomeBanner(ok = true, text = "Payment successful")
                    is Outcome.Failure -> OutcomeBanner(ok = false, text = o.reason)
                    Outcome.None -> {}
                }

                state.fatalError?.let {
                    Text("Error: $it", color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = vm::submit,
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (state.submitting) CircularProgressIndicator(
                        strokeWidth = 2.dp, modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    ) else Text("Submit payment")
                }
            }

            // full-screen overlay while we finalize the result with the backend
            if (state.finalizing) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        Row(
                            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Finalizing payment…",
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutcomeBanner(ok: Boolean, text: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (ok) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (ok) Icons.Default.CheckCircle else Icons.Default.Error, null,
                tint = if (ok) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}