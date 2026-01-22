package com.example.landnv4.data.inputs

import android.content.Context
import com.example.landnv4.domain.geo.Utm
import com.example.landnv4.domain.geo.UtmParser.toUtm
import com.example.landnv4.domain.geo.UtmParser.utmToString

object AppInputsStore {

    private const val PREFS_NAME = "required_inputs"

    private const val KEY_DATE = "date_iso"
    private const val KEY_TIME = "time_hundredth"
    private const val KEY_UTM  = "utm_13"
    private const val KEY_CLOSED_AT = "closed_at"

    fun save(context: Context, dateIso: String, timeHundredth: String, utm13: Utm) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putString(KEY_DATE, dateIso)
            .putString(KEY_TIME, timeHundredth)
            .putString(KEY_UTM, utm13.utmToString())
            .apply()   // async + safe
    }

    fun load(context: Context): HomeInputs? {
        clearIfExpired(context)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val date = prefs.getString(KEY_DATE, null) ?: return null
        val time = prefs.getString(KEY_TIME, null) ?: return null
        val utm  = prefs.getString(KEY_UTM, null) ?: return null

        return HomeInputs(
            dateIso = date,
            timeHundredth = time,
            utm13 = utm.toUtm()
        )
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun setClosedAt(context: Context, millis: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_CLOSED_AT, millis)
            .apply()
    }

    fun clearIfExpired(context: Context, ttlMinutes: Long = 30) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val closedAt = prefs.getLong(KEY_CLOSED_AT, -1L)
        if (closedAt <= 0L) return

        val ttlMillis = ttlMinutes * 60_000L
        val expired = System.currentTimeMillis() - closedAt > ttlMillis
        if (expired) {
            prefs.edit().clear().apply()
        }
    }
}
