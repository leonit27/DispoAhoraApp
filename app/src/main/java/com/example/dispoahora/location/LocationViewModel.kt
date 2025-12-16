package com.example.dispoahora.location


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val locationService = LocationService(application)

    private val _currentAddress = MutableStateFlow("Seleccionar ubicación")
    val currentAddress = _currentAddress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun detectLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = locationService.getCurrentLocation()
            if (result != null) {
                _currentAddress.value = result
            } else {
                _currentAddress.value = "No se pudo detectar"
            }
            _isLoading.value = false
        }
    }

    fun setManualLocation(location: String) {
        _currentAddress.value = location
    }
}