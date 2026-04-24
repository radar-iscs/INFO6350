package com.example.epay.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.epay.data.PaymentRepository
import com.example.epay.data.TransactionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryViewModel(repo: PaymentRepository) : ViewModel() {
    val transactions: StateFlow<List<TransactionEntity>> =
        repo.observeTransactions().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}

class HistoryVmFactory(private val repo: PaymentRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = HistoryViewModel(repo) as T
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(repository: PaymentRepository, onBack: () -> Unit) {
    val vm: HistoryViewModel = viewModel(factory = HistoryVmFactory(repository))
    val items by vm.transactions.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("History") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        )
    }) { padding ->
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No transactions yet")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.paymentIntentId }) { tx -> Row(tx) }
            }
        }
    }
}

@Composable
private fun Row(tx: TransactionEntity) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.foundation.layout.Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${tx.currency.uppercase()} ${"%.2f".format(tx.amount / 100.0)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(tx.note.ifBlank { "(no message)" },
                    style = MaterialTheme.typography.bodyMedium)
                Text(
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(tx.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            StatusChip(tx.status)
        }
    }
}

// stripe raw status to label for users
private fun friendlyStatus(raw: String): String = when (raw.lowercase()) {
    "succeeded"              -> "Success"
    "failed"                 -> "Failed"
    "canceled"               -> "Canceled"
    "requires_payment_method"-> "Incomplete"
    "requires_action"        -> "Action needed"
    "processing"             -> "Processing"
    else                     -> raw
}

@Composable
private fun StatusChip(status: String) {
    val label = friendlyStatus(status)
    val (bg, fg) = when (label) {
        "Success"                  -> MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer
        "Failed", "Canceled"       -> MaterialTheme.colorScheme.errorContainer to
                MaterialTheme.colorScheme.onErrorContainer
        else                        -> MaterialTheme.colorScheme.surfaceVariant to
                MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = MaterialTheme.shapes.small, color = bg) {
        Text(label, color = fg, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}