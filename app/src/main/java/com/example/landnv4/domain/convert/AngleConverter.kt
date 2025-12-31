package com.example.landnv4.domain.geo

import kotlin.math.PI

object AngleConverter {
    private const val NATO_MILS = 6400.0
    private const val SWEDISH_MILS = 6300.0

    // Artillery mil (mil-radian): 1 mil = 0.001 rad
    fun milsArtilleryToRadians(mil: Double) = mil / 1000.0
    fun radiansToMilsArtillery(rad: Double) = rad * 1000.0

    fun milsNatoToRadians(mil: Double) = mil * (2.0 * PI) / NATO_MILS
    fun radiansToMilsNato(rad: Double) = rad * NATO_MILS / (2.0 * PI)

    fun milsSwedishToRadians(mil: Double) = mil * (2.0 * PI) / SWEDISH_MILS
    fun radiansToMilsSwedish(rad: Double) = rad * SWEDISH_MILS / (2.0 * PI)

    fun degreesToRadians(deg: Double) = deg * PI / 180.0
    fun radiansToDegrees(rad: Double) = rad * 180.0 / PI

    fun radiansToGradians(rad: Double) = radiansToDegrees(rad) * (10.0 / 9.0)
    fun gradiansToRadians(gon: Double) = degreesToRadians(gon * (9.0 / 10.0))

    fun radiansToTurns(rad: Double) = rad / (2.0 * PI)
    fun turnsToRadians(turns: Double) = turns * (2.0 * PI)
}
