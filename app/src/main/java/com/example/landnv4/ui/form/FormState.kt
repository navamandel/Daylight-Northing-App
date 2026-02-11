package com.example.landnv4.ui.form

import com.example.landnv4.data.db.infobank.HeightType
import com.example.landnv4.domain.geo.Utm
import kotlin.collections.toMutableMap
import kotlin.math.round

/**
 * Holds current form values & errors.
 * Keys match FormItem.key (e.g. "utm", "date", "A_utm", "name", etc.)
 */
class FormState (
    private val onChange: (String) -> Unit
) {

    private val listeners = mutableSetOf<(String) -> Unit>()

    private val _values = mutableMapOf<String, Any?>()
    val values: Map<String, Any?> get() = _values

    fun set(key: String, value: Any?) {
        _values[key] = value
        onChange(key)
        listeners.forEach { it(key) }
    }

    private val enabledMap = mutableMapOf<String, Boolean>()

    fun setEnabled(key: String, enabled: Boolean) {
        enabledMap[key] = enabled
        listeners.forEach { it("__enabled__$key") }
    }

    fun isEnabled(key: String): Boolean = enabledMap[key] ?: true

    private val errorFields = listOf("Title", "Message", "Key")
    var error = errorFields.associateWith { "" }.toMutableMap()
    fun setStateError(err: List<String>) {
        error = errorFields.zip(err).toMap() as MutableMap<String, String>
    }
    //fun getError(): MutableMap<String, String> = error

    fun getString(key: String): String? = _values[key] as? String
    fun getBoolean(key: String): Boolean? = _values[key] as? Boolean

    fun clearValue(key: String) { _values[key] = "" }
    fun clearAllValues(vals: List<String>? = null) {
        if (vals == null) {
            _values.clear()
        } else {
            vals.forEach { _values[it] = "" }
        }



        //listeners.clear()
    }

    fun addListener(l: (String) -> Unit) { listeners.add(l) }
    fun removeListener(listener: (key: String) -> Unit) { listeners.remove(listener) }

}






/*class FormState {

    private val values: MutableMap<String, Any?> = mutableMapOf()
    private val errors: MutableMap<String, String?> = mutableMapOf()

    private val listeners = mutableSetOf<(key: String) -> Unit>()

    fun addListener(listener: (key: String) -> Unit) { listeners.add(listener) }
    fun removeListener(listener: (key: String) -> Unit) { listeners.remove(listener) }
    fun hasValue(key: String): Boolean = values.containsKey(key)

    /**
     * Use this for seeding defaults WITHOUT notifying listeners.
     * (Keeps dependent-spinner logic from firing during initial submit.)
     */
    fun putIfAbsent(key: String, defaultValue: Any?) {
        if (!values.containsKey(key)) values[key] = defaultValue
    }

    fun setValue(key: String, value: Any?) {
        values[key] = value
        clearError(key)
        notifyChanged(key)
    }

    fun getValue(key: String): Any? = values[key]

    fun setError(key: String, message: String?) {
        errors[key] = message
        notifyChanged(key)
    }

    fun getError(key: String): String? = errors[key]

    fun clearError(key: String) {
        if (errors.containsKey(key)) {
            errors.remove(key)
            notifyChanged(key)
        }
    }

    fun clearAllErrors() {
        errors.clear()
        // No per-key notify; caller can call adapter.notifyDataSetChanged() if needed.
    }

    private fun notifyChanged(key: String) {
        listeners.forEach { it(key) }
    }

    // ---- Typed helpers ----

    fun getString(key: String): String? = when (val v = values[key]) {
        null -> null
        is String -> v
        else -> v.toString()
    }

    fun getBoolean(key: String): Boolean? = when (val v = values[key]) {
        is Boolean -> v
        is String -> v.toBooleanStrictOrNull()
        else -> null
    }

    fun getInt(key: String): Int? = when (val v = values[key]) {
        is Int -> v
        is Long -> v.toInt()
        is Double -> v.toInt()
        is Float -> v.toInt()
        is String -> v.toIntOrNull()
        else -> null
    }

    fun getDouble(key: String): Double? = when (val v = values[key]) {
        is Double -> v
        is Float -> v.toDouble()
        is Int -> v.toDouble()
        is Long -> v.toDouble()
        is String -> v.toDoubleOrNull()
        else -> null
    }

    fun getUtm(key: String): FormItem.UtmItem.UtmItemValue? =
        values[key] as? FormItem.UtmItem.UtmItemValue

    fun getHeight(key: String): FormItem.Height.HeightValue? =
        values[key] as? FormItem.Height.HeightValue?


    companion object {
        fun roundToDecimals(value: Double, decimals: Int): Double {
            val factor = Math.pow(10.0, decimals.toDouble())
            return round(value * factor) / factor
        }
    }
}
*/