package com.example.landnv4.ui.northing.starmap

import com.example.landnv4.domain.model.Star

data class StarMapUiState(
    val loading: Boolean = true,
    val stars: List<Star> = emptyList(),
    val error: String? = null
)
