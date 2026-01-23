package com.example.neuexample2addcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AddTwoNumbersScreen()
            }
        }
    }
}

@Composable
fun AddTwoNumbersScreen() {

    var num1 by remember { mutableStateOf("") }
    var num2 by remember { mutableStateOf("") }
    var result by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 164.dp,
                bottom = 16.dp
            )
    ) {

        // Number 1
        Text(text = "Number 1")
        TextField(
            value = num1,
            onValueChange = { num1 = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Number 2
        Text(text = "Number 2")
        TextField(
            value = num2,
            onValueChange = { num2 = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add Button
        Button(
            onClick = {
                val n1 = num1.toIntOrNull() ?: 0
                val n2 = num2.toIntOrNull() ?: 0
                result = n1 + n2
            }
        ) {
            Text("Add")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Result
        Text(
            text = "Result: $result",
            fontSize = 18.sp
        )
    }
}
