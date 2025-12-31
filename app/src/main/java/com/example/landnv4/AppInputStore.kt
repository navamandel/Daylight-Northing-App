package com.example.landnv4

import android.content.Context

object AppInputsStore {

    private const val PREFS_NAME = "required_inputs"

    private const val KEY_DATE = "date_iso"
    private const val KEY_TIME = "time_hundredth"
    private const val KEY_UTM  = "utm_14"

    fun save(context: Context, dateIso: String, timeHundredth: String, utm14: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putString(KEY_DATE, dateIso)
            .putString(KEY_TIME, timeHundredth)
            .putString(KEY_UTM, utm14)
            .apply()   // async + safe
    }

    fun load(context: Context): HomeInputs? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val date = prefs.getString(KEY_DATE, null) ?: return null
        val time = prefs.getString(KEY_TIME, null) ?: return null
        val utm  = prefs.getString(KEY_UTM, null) ?: return null

        return HomeInputs(
            dateIso = date,
            timeHundredth = time,
            utm14 = utm
        )
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
