package com.example.qrcode_scan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage

@Composable
fun ScannerScreen() {
    var scannedText by remember { mutableStateOf("No code scanned yet") }
    val context = LocalContext.current

    val cameraScanner = remember { GmsBarcodeScanning.getClient(context) }
    val imageScanner = remember { BarcodeScanning.getClient() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val image = InputImage.fromFilePath(context, uri)
                    imageScanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                scannedText = barcodes[0].rawValue ?: "Empty code"
                            } else {
                                scannedText = "No QR code found in this image."
                            }
                        }
                        .addOnFailureListener { e ->
                            scannedText = "Error scanning image: ${e.message}"
                        }
                } catch (e: Exception) {
                    scannedText = "Failed to load image: ${e.message}"
                }
            }
        }
    )

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("QR Scanner", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                cameraScanner.startScan()
                    .addOnSuccessListener { barcode ->
                        scannedText = barcode.rawValue ?: "Empty code"
                    }
                    .addOnFailureListener { e ->
                        scannedText = "Camera scan failed: ${e.message}"
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text("Scan with Camera")
        }

        Spacer(modifier = Modifier.height(16.dp))

        FilledTonalButton(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text("Select from Album")
        }

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Result:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(scannedText, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}