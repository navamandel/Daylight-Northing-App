package com.example.landnv4

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

data class HomeInputs(
    val dateIso: String,
    val timeHundredth: String,
    val utm14: String
)

class HomeInputsViewModel : ViewModel() {
    private val _inputs = MutableStateFlow<HomeInputs?>(null)
    val inputs: StateFlow<HomeInputs?> = _inputs

    fun setInputs(dateIso: String, timeHundredth: String, utm14: String) {
        _inputs.value = HomeInputs(dateIso, timeHundredth, utm14)
    }

    fun clear() { _inputs.value = null }
}
