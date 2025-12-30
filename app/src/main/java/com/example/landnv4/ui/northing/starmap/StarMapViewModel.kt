package com.example.landnv4.ui.northing.starmap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.landnv4.data.repo.StarQueryRepository
import com.example.landnv4.data.repo.StarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StarMapViewModel(app: Application) : AndroidViewModel(app) {

    private val preloadRepo = StarRepository(app.applicationContext)
    private val queryRepo = StarQueryRepository()

    private val _ui = MutableStateFlow(StarMapUiState())
    val ui: StateFlow<StarMapUiState> = _ui

    init {
        viewModelScope.launch {
            try {
                // simplest: just call ensurePreloaded (your current method always inserts).
                // Better: only preload if empty (recommended to add DAO countStars()).
                preloadRepo.ensurePreloadedFromAssets()

                val stars = queryRepo.getAllStars()


                _ui.value = StarMapUiState(loading = false, stars = stars)
            } catch (e: Exception) {
                _ui.value = StarMapUiState(loading = false, error = e.message ?: "Failed to load stars")
            }
        }
    }
}
