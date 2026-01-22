package com.example.landnv4.ui.form

import com.example.landnv4.data.db.infobank.Converters
import com.example.landnv4.data.db.infobank.HeightConverters
import com.example.landnv4.data.db.infobank.HeightType
import com.example.landnv4.domain.geo.Utm
import mil.nga.grid.Hemisphere

/**
 * Generic form model for a RecyclerView-based input form.
 * Each item has a stable [key] so you can read/write values from a FormState map.
 */
sealed interface FormItem {

    val key: String
    val label: String
    val required: Boolean

    /** Plain text input (e.g., name, location). */
    data class Text(
        override val key: String,
        override val label: String,
        override val required: Boolean = false,
        val hint: String? = null,
        val initial: String? = null
    ) : FormItem

    /** Numeric input (int/decimal). */
    data class Number(
        override val key: String,
        override val label: String,
        override val required: Boolean = false,
        val hint: String? = null,
        val initial: String? = null,
        val allowDecimal: Boolean = true
    ) : FormItem

    /** Generic spinner/select. */
    data class Spinner(
        override val key: String,
        override val label: String,
        override val required: Boolean = false,
        val dependsOnKey: String? = null,
        val optionsProvider: ((parentValue: String?) -> List<Pair<String, String>>)? = null,

        // fallback
        val staticOptions: List<Pair<String, String>> = emptyList()
    ) : FormItem

    /** Boolean switch (e.g., hemisphere). */
    data class Switch(
        override val key: String,
        override val label: String,
        override val required: Boolean = false,
        val onText: String? = null,
        val offText: String? = null,
        val updateKey: String? = null
    ) : FormItem

    /**
     * Date field stored as a formatted string (e.g., "2026-01-15").
     * [showNowButton] lets UI show "Now/Today" to set current date.
     */
    data class Date(
        override val key: String,
        override val label: String = "Date",
        override val required: Boolean = false,
        val hint: String = "yyyy-MM-dd",
        val initial: String? = null,
        val showTodayButton: Boolean = true,
        val onClick: (() -> Unit)? = null,
        val currentDate: String? = null
    ) : FormItem

    /**
     * Time field stored as a formatted string (e.g., "14:37").
     * [showNowButton] lets UI show "Now" to set current time.
     */
    data class Time(
        override val key: String,
        override val label: String = "Time",
        override val required: Boolean = false,
        val hint: String = "HH:mm",
        val initial: String? = null,
        val showNowButton: Boolean = true,
        val onClick: (() -> Unit)? = null,
        val currentTime: String? = null
    ) : FormItem

    /**
     * UTM composite input: Easting/Northing/Zone/Hemisphere.
     *
     * Values can be stored either as:
     * - separate keys (utm_easting, utm_northing, ...) OR
     * - a single key with a UtmValue object (recommended).
     *
     * This model assumes single key storing a UtmValue.
     */
    data class UtmItem(
        override val key: String,
        override val label: String,        // e.g. "UTM"
        override val required: Boolean = true,
        val eastingKey: String = "utm_easting",
        val northingKey: String = "utm_northing",
        val zoneKey: String = "utm_zone",
        val hemisphereKey: String = "utm_hemisphere", // true=N, false=S
        var showCurr: String = "show_current",
        val currentUtm: Utm? = null
    ) : FormItem

    data class Coords(
        override val key: String,
        override val label: String,
        override val required: Boolean = true,
        val eastingXKey: String = "ex_coord",
        var showNYCoord: Boolean = true,
        val northingYKey: String = "ny_coord",
        var showZCoord: Boolean = false,
        val zKey: String = "z_coord",
        var showZoneHemi: Boolean = false,
        val zoneKey: String = "utm_zone",
        val hemisphereKey: String = "utm_hemisphere",
        val hintsLabels: MutableMap<String, String>? = null
    ) : FormItem

    /**
     * Height composite input: value + unit.
     * This lets you reuse it in DataBank and Yeilut.
     */
    data class Height(
        override val key: String,          // e.g. "height"
        override val label: String,        // e.g. "Height"
        override val required: Boolean = false,
        val valueKey: String = "height_value",
        val unitKey: String = "height_unit", // "METERS"/"FEET"
        val units: List<String> = listOf("METERS", "FEET")
    ) : FormItem

}