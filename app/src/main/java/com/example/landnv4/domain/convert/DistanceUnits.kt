package com.example.landnv4.domain.convert

object DistanceUnits {
    enum class Unit(val symbols: Set<String>, val metersPerUnit: Double) {
        M(setOf("m","meter","meters"), 1.0),
        KM(setOf("km","kilometer","kilometers"), 1000.0),
        MI(setOf("mi","mile","miles"), 1609.344),
        FT(setOf("ft","feet","foot"), 0.3048),
        NM(setOf("nm","nmi","nauticalmile","nauticalmiles"), 1852.0)
    }

    fun parseUnit(token: String): Unit =
        Unit.entries.firstOrNull { token.lowercase() in it.symbols }
            ?: throw IllegalArgumentException("Unknown unit: $token")

    fun toMeters(value: Double, unit: Unit): Double = value * unit.metersPerUnit
    fun fromMeters(meters: Double, unit: Unit): Double = meters / unit.metersPerUnit
}
