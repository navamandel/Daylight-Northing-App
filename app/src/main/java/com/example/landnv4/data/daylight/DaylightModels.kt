package com.example.landnv4.data.daylight

data class DaylightRow(
    val utm: String,
    val date: String,      // YYYY-MM-DD
    val sunrise: String,   // HH:mm
    val sunset: String,    // HH:mm
    val twilight: String   // HH:mm (astronomical twilight begin per your spec)
)

data class DaylightFile(
    val data: List<DaylightRow>
)
