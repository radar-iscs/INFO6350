package com.example.realtime_location

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // initiate the view model via factory
            val vm: LocationViewModel = viewModel(
                factory = LocationViewModelFactory(applicationContext)
            )

            LocationScreen(vm)
        }
    }
}