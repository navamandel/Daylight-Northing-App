package com.example.landnv4.domain.astro

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

object TimeUtil {
    @RequiresApi(Build.VERSION_CODES.O)
    fun nowUtc(zone: ZoneId = ZoneOffset.UTC): ZonedDateTime = ZonedDateTime.ofInstant(Instant.now(), zone)
}
