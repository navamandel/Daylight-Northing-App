package com.example.landnv4.databank

enum class BankType {
    ANCHORING, NORTHING, VALIDATING, TARGETS;

    fun title(): String = when (this) {
        ANCHORING -> "Anchoring Points"
        NORTHING -> "Northing Points"
        VALIDATING -> "Validating Points"
        TARGETS -> "Targets"
    }
}
