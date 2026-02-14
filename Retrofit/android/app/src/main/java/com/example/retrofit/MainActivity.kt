package com.example.retrofit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Result") }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = a,
                onValueChange = { a = it },
                label = { Text("Number A") }
            )

            OutlinedTextField(
                value = b,
                onValueChange = { b = it },
                label = { Text("Number B") }
            )

            Text(text = result)

            // -------- GET /add?a=&b=
            Button(
                onClick = {
                    val da = a.toDoubleOrNull() ?: 0.0
                    val db = b.toDoubleOrNull() ?: 0.0

                    RetrofitClient.api.addGet(da, db)
                        .enqueue(object : Callback<AddResponse> {

                            override fun onResponse(
                                call: Call<AddResponse>,
                                response: Response<AddResponse>
                            ) {
                                result = "GET Result: ${response.body()?.result}"
                            }

                            override fun onFailure(
                                call: Call<AddResponse>,
                                t: Throwable
                            ) {
                                result = "GET error"
                            }
                        })
                }
            ) {
                Text("Call GET")
            }


            // -------- POST /add  { a, b }
            Button(
                onClick = {
                    val da = a.toDoubleOrNull() ?: 0.0
                    val db = b.toDoubleOrNull() ?: 0.0

                    // Create the request body
                    val request = AddRequest(da, db)

                    // Call POST /add
                    RetrofitClient.api.addPost(request)
                        .enqueue(object : Callback<AddResponse> {

                            override fun onResponse(
                                call: Call<AddResponse>,
                                response: Response<AddResponse>
                            ) {
                                result = "POST Result: ${response.body()?.result}"
                            }

                            override fun onFailure(
                                call: Call<AddResponse>,
                                t: Throwable
                            ) {
                                result = "POST error"
                            }
                        })
                }
            ) {
                Text("Call POST")
            }

        }
    }
}
