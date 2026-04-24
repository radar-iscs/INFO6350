package com.example.sms.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sms.model.SmsModel
import com.example.sms.viewmodel.SmsViewModel

class SendSmsActivity : ComponentActivity() {

    private val viewModel: SmsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SendSmsScreen(
                    onSend = { phone, message ->
                        viewModel.sendSms(SmsModel(phone, message))
                        Toast.makeText(this, "SMS Sent!", Toast.LENGTH_SHORT).show()
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendSmsScreen(onSend: (phone: String, message: String) -> Unit, onBack: () -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send SMS") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = messageBody,
                onValueChange = { messageBody = it },
                label = { Text("Message") },
                minLines = 4,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSend(phoneNumber, messageBody) },
                enabled = phoneNumber.isNotBlank() && messageBody.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send")
            }
        }
    }
}