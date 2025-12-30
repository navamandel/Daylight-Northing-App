package com.example.landnv4.domain.model

data class HorizontalCoords(
    val azimuthDeg: Double,   // 0..360 from North
    val altitudeDeg: Double   // -90..+90
)
