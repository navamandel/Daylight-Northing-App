package com.example.landnv4.data.inputs

import androidx.lifecycle.ViewModel
import com.example.landnv4.domain.geo.Utm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class HomeInputs(
    val dateIso: String,
    val timeHundredth: String,
    val utm13: Utm
)

class HomeInputsViewModel : ViewModel() {
    private val _inputs = MutableStateFlow<HomeInputs?>(null)
    val inputs: StateFlow<HomeInputs?> = _inputs

    fun setInputs(dateIso: String, timeHundredth: String, utm13: Utm) {
        _inputs.value = HomeInputs(dateIso, timeHundredth, utm13)
    }

    fun clear() { _inputs.value = null }
}
