package com.example.landnv4.domain.model

data class Star(
    val id: Int,
    val name: String,     // "proper" or fallback
    val raDeg: Double,    // degrees
    val decDeg: Double,   // degrees
    val mag: Double? = null
)
