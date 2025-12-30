package com.example.landnv4.domain.astro

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.landnv4.domain.model.HorizontalCoords
import com.example.landnv4.domain.model.Observer
import com.example.landnv4.domain.model.Star
import java.time.ZonedDateTime
import kotlin.math.*

object AstroMath {

    @RequiresApi(Build.VERSION_CODES.O)
    fun starToHorizontal(star: Star, observer: Observer, utcTime: ZonedDateTime): HorizontalCoords {
        val lat = observer.latDeg.toRadians()
        val dec = star.decDeg.toRadians()
        val ra = star.raDeg.toRadians() // ✅ degrees -> radians

        val lst = localSiderealTimeRad(observer.lonDeg, utcTime)
        val ha = normalizePiRad(lst - ra)

        // val sinAlt = sin(dec) * sin(lat) + cos(dec) * cos(lat) * cos(ha)
        // val alt = asin(sinAlt)

        // Azimuth from North, eastward
        // altitude (keep your existing altitude calc or use this)
        val sinAlt = sin(dec) * sin(lat) + cos(dec) * cos(lat) * cos(ha)
        val alt = asin(sinAlt)

        // ✅ Stable azimuth formula
        val az = atan2(
            sin(ha),
            cos(ha) * sin(lat) - tan(dec) * cos(lat)
        )

        // Convert to degrees and normalize
        var azDeg = az.toDegrees()
        azDeg = (azDeg + 360.0) % 360.0

        // This formula gives azimuth measured from SOUTH in some conventions.
        // To get "from North, clockwise" (what you want), flip by 180°:
        azDeg = (azDeg + 180.0) % 360.0

        return HorizontalCoords(
            azimuthDeg = azDeg,
            altitudeDeg = alt.toDegrees()
        )


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun localSiderealTimeRad(lonDeg: Double, utc: ZonedDateTime): Double {
        val jd = julianDate(utc)
        val d = jd - 2451545.0
        var gmst = 280.46061837 + 360.98564736629 * d
        gmst = (gmst % 360.0 + 360.0) % 360.0
        val lstDeg = gmst + lonDeg
        return ((lstDeg % 360.0 + 360.0) % 360.0).toRadians()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun julianDate(t: ZonedDateTime): Double {
        val year = t.year
        val month = t.monthValue
        val day = t.dayOfMonth
        val hour = t.hour
        val minute = t.minute
        val second = t.second

        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)

        val dayFrac = (hour + (minute + second / 60.0) / 60.0) / 24.0

        return floor(365.25 * (y + 4716)) +
                floor(30.6001 * (m + 1)) +
                day + dayFrac + b - 1524.5
    }

    private fun normalizeAngleRad(x: Double): Double {
        var a = x % (2 * Math.PI)
        if (a < 0) a += 2 * Math.PI
        return a
    }

    private fun normalizePiRad(x: Double): Double {
        var a = x % (2 * Math.PI)
        if (a < -Math.PI) a += 2 * Math.PI
        if (a > Math.PI) a -= 2 * Math.PI
        return a
    }


    private fun Double.toRadians() = this * Math.PI / 180.0
    private fun Double.toDegrees() = this * 180.0 / Math.PI
}
