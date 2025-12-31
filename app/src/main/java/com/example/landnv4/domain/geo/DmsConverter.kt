package com.example.landnv4.domain.geo

import kotlin.math.abs

object DmsConverter {
    // "31°46'44.1\"N" or "31 46 44.1 N" etc. (simple parser)
    fun dmsToDecimal(deg: Int, min: Int, sec: Double, isNegative: Boolean): Double {
        val sign = if (isNegative) -1 else 1
        return sign * (abs(deg).toDouble() + min / 60.0 + sec / 3600.0)
    }

    data class Dms(val deg: Int, val min: Int, val sec: Double, val hemi: Char)

    fun decimalToDms(value: Double, isLat: Boolean): Dms {
        val hemi = when {
            isLat && value >= 0 -> 'N'
            isLat && value < 0 -> 'S'
            !isLat && value >= 0 -> 'E'
            else -> 'W'
        }

        val v = abs(value)
        val d = v.toInt()
        val mFull = (v - d) * 60.0
        val m = mFull.toInt()
        val s = (mFull - m) * 60.0

        return Dms(d, m, s, hemi)
    }
}
