package com.example.realtime_location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// View Model
class LocationViewModel(private val repo: LocationRepository) : ViewModel() {

    // internal mutable state representing the current location
    private val _location = MutableStateFlow<LocationModel?>(null)

    // public immutable state exposed to the UI layer
    val location: StateFlow<LocationModel?> = _location.asStateFlow()

    // start collecting real-time location updates from the repository every 3000ms
    fun startTracking() {
        viewModelScope.launch {
            repo.getLocationUpdates(3000L).collect { updatedLocation ->
                _location.value = updatedLocation
            }
        }
    }

    // check permission state from UI
    fun checkPermission(): Boolean {
        return repo.hasLocationPermission()
    }
}