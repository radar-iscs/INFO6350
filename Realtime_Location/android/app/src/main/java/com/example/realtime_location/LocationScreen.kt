package com.example.realtime_location

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// View
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(vm: LocationViewModel) {
    // observe the location state from the view model
    val location by vm.location.collectAsState()

    var hasPermission by remember { mutableStateOf(vm.checkPermission()) }

    // setup the permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
            if (isGranted) {
                vm.startTracking()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (hasPermission) {
            vm.startTracking()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Live Tracking") }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (hasPermission) {
                if (location != null) {
                    RealTimeMap(location = location!!)
                } else {
                    CircularProgressIndicator() // show an indicator while fetching first GPS lock
                }
            } else {
                PermissionDeniedView {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }
}

// convert the LocationModel to Google Maps latitude & longitude
@Composable
fun RealTimeMap(location: LocationModel) {
    val currentLatLng = LatLng(location.latitude, location.longitude)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 16f)
    }

    // smoothly animates the camera to follow the user (similar to Uber) every time the location changes
    LaunchedEffect(location) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLng(currentLatLng),
            durationMs = 1000
        )
    }

    // render Google Map
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true)
    ) {
        Marker(
            state = MarkerState(position = currentLatLng),
            title = "Your Location",
            snippet = "Lat: ${location.latitude}, Lng: ${location.longitude}"
        )
    }
}

// show the view when permission denied
@Composable
fun PermissionDeniedView(onRequestPermission: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Location permission is required for live tracking.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}