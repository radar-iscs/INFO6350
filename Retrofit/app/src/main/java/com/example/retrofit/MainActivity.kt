package com.example.retrofit
/*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.retrofit.ui.theme.MamounClass3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MamounClass3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MamounClass3Theme {
        Greeting("Android")
    }
}



// ---- new code
// ================================
// MainActivity.kt (Jetpack Compose UI)
// ================================


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

            Button(onClick = {
                val sum = (a.toIntOrNull() ?: 0) + (b.toIntOrNull() ?: 0)
                result = "Result: $sum"
            }) {
                Text("Add")
            }

            Text(text = result)

            Button(onClick = {
                RetrofitClient.api.getPost().enqueue(object : Callback<Post> {
                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        result = "GET: ${response.body()?.title}"
                    }

                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        result = "GET error"
                    }
                })
            }) {
                Text("Call GET")
            }

            Button(onClick = {
                val post = Post(1, title = "Hello", body = "From Compose")
                RetrofitClient.api.createPost(post).enqueue(object : Callback<Post> {
                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        result = "POST id: ${response.body()?.id}"
                    }

                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        result = "POST error"
                    }
                })
            }) {
                Text("Call POST")
            }
        }
    }
}
*/import android.os.Bundle
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
