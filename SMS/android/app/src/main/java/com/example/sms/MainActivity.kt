package com.example.sms

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.sms.ui.ReceiveSmsActivity
import com.example.sms.ui.SendSmsActivity

/**
 * Main launcher activity — requests permissions and shows navigation buttons
 */
class MainActivity : ComponentActivity() {

    // Modern permission launcher (replaces onRequestPermissionsResult)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "SMS permissions are required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions on launch if not already granted
        if (!checkSmsPermissions()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
                )
            )
        }

        setContent {
            MaterialTheme {
                MainScreen(
                    onSendClick = {
                        if (checkSmsPermissions()) {
                            startActivity(Intent(this, SendSmsActivity::class.java))
                        } else {
                            Toast.makeText(this, "Grant permissions first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onReceiveClick = {
                        startActivity(Intent(this, ReceiveSmsActivity::class.java))
                    }
                )
            }
        }
    }

    private fun checkSmsPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun MainScreen(onSendClick: () -> Unit, onReceiveClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SMS App",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onSendClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send SMS")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onReceiveClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Received SMS")
            }
        }
    }
}