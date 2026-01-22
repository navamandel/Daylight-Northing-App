package com.example.landnv4.ui.form

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.landnv4.ConverterActivity
import com.example.landnv4.R
import com.example.landnv4.data.db.infobank.HeightType
import com.example.landnv4.data.inputs.AppInputsStore
import com.example.landnv4.databinding.RowFormCoordBinding
import com.example.landnv4.databinding.RowFormDateBinding
import com.example.landnv4.databinding.RowFormHeightBinding
import com.example.landnv4.databinding.RowFormSpinnerBinding
import com.example.landnv4.databinding.RowFormSwitchBinding
import com.example.landnv4.databinding.RowFormTextBinding
import com.example.landnv4.databinding.RowFormTimeBinding
import com.example.landnv4.databinding.RowFormUtmBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class FormAdapter(
    private val items: List<FormItem>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val state = FormState { changedKey ->
        onValueChanged(changedKey)
    }
    enum class Payload { ERROR, SPINNER_OPTIONS, COORDS_INPUTS, ENABLED, EXTERNAL_TEXT }

    private val errors = mutableMapOf<String, String?>()

    private var rv: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        rv = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        rv = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    private fun safeNotifyItemChanged(position: Int, payload: Any? = null) {
        if (position == RecyclerView.NO_POSITION) return
        val recyclerView = rv ?: run {
            if (payload == null) notifyItemChanged(position) else notifyItemChanged(position, payload)
            return
        }

        // If RV is in layout/scroll, post to next frame
        if (recyclerView.isComputingLayout) {
            recyclerView.post {
                if (payload == null) notifyItemChanged(position) else notifyItemChanged(position, payload)
            }
        } else {
            if (payload == null) notifyItemChanged(position) else notifyItemChanged(position, payload)
        }
    }



    fun setError(key: String, message: String?) {
        errors[key] = message
        safeNotifyItemChanged(items.indexOfFirst { it.key == key }.coerceAtLeast(0))
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is FormItem.Text -> 1
        is FormItem.Spinner -> 2
        is FormItem.Switch -> 3
        is FormItem.Date -> 4
        is FormItem.Time -> 5
        is FormItem.UtmItem -> 6
        is FormItem.Coords -> 7
        is FormItem.Height -> 8
        else -> error("unknown viewType")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            1 -> TextVH(RowFormTextBinding.inflate(inflater, parent, false))
            2 -> SpinnerVH(RowFormSpinnerBinding.inflate(inflater, parent, false))
            3 -> SwitchVH(RowFormSwitchBinding.inflate(inflater, parent, false))
            4 -> DateVH(RowFormDateBinding.inflate(inflater, parent, false))
            5 -> TimeVH(RowFormTimeBinding.inflate(inflater, parent, false))
            6 -> UtmVH(RowFormUtmBinding.inflate(inflater, parent, false))
            7 -> CoordsVH(RowFormCoordBinding.inflate(inflater, parent, false))
            8 -> HeightVH(RowFormHeightBinding.inflate(inflater, parent, false))
            else -> error("unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is TextVH -> holder.bind(item as FormItem.Text)
            is SpinnerVH -> holder.bind(item as FormItem.Spinner)
            is SwitchVH -> holder.bind(item as FormItem.Switch)
            is DateVH -> holder.bind(item as FormItem.Date)
            is TimeVH -> holder.bind(item as FormItem.Time)
            is UtmVH -> holder.bind(item as FormItem.UtmItem)
            is CoordsVH -> holder.bind(item as FormItem.Coords)
            is HeightVH -> holder.bind(item as FormItem.Height)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position) // full bind
            return
        }

        val item = items[position]

        // If multiple payloads arrive, we can handle them all
        payloads.forEach { p ->
            when (p) {
                Payload.ERROR -> {
                    when (holder) {
                        is TextVH -> holder.bindErrorOnly(item as FormItem.Text)
                        is DateVH -> holder.bindErrorOnly(item as FormItem.Date)
                        is TimeVH -> holder.bindErrorOnly(item as FormItem.Time)
                        is UtmVH -> holder.bindErrorOnly(item as FormItem.UtmItem)
                        is CoordsVH -> holder.bindErrorOnly(item as FormItem.Coords)
                        is HeightVH -> holder.bindErrorOnly(item as FormItem.Height)
                    }
                }

                Payload.SPINNER_OPTIONS -> {
                    if (holder is SpinnerVH) holder.bindOptionsOnly(item as FormItem.Spinner)
                    else onBindViewHolder(holder, position) // fallback
                }

                Payload.COORDS_INPUTS -> {
                    if (holder is CoordsVH) holder.bindVisibilityOnly(item as FormItem.Coords)
                    else onBindViewHolder(holder, position)
                }

                Payload.ENABLED -> {
                    when (holder) {
                        is UtmVH -> holder.bindEnabledOnly(item as FormItem.UtmItem)
                        // add others if you have them
                        else -> onBindViewHolder(holder, position)
                    }
                }

                Payload.EXTERNAL_TEXT -> {
                    when (holder) {
                        is DateVH -> holder.bindExternalText(item as FormItem.Date)
                        is TimeVH -> holder.bindExternalText(item as FormItem.Time)
                        else -> onBindViewHolder(holder, position)
                    }
                }

                else -> onBindViewHolder(holder, position)
            }
        }
    }

    // ---- ViewHolders below ----
    inner class TextVH(private val b: RowFormTextBinding) :
        RecyclerView.ViewHolder(b.root) {

        private var isBinding = false
        private var currentKey: String? = null

        init {
            b.etValue.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = currentKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key)?.toString().orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }
        }


        fun bind(item: FormItem.Text) {
            b.tvLabel.text = item.label
            b.etValue.hint = item.hint ?: ""
            currentKey = item.key

            val existingValue = state.getString(item.key)
            if (existingValue != null && b.etValue.text?.toString()?.trim() != existingValue) {
                isBinding = true
                b.etValue.setTextSafely(existingValue)
                isBinding = false
            }

            val current = state.getString(item.key) ?: item.initial ?: ""
            //b.etValue.setText(current.trim())
            isBinding = true
            b.etValue.setTextSafely(current)
            isBinding = false

            // show error
            b.tvError.text = errors[item.key]
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE

        }

        fun bindErrorOnly(item: FormItem.Text) {
            b.tvError.text = errors[item.key].orEmpty()
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE
        }

    }


    inner class SpinnerVH(private val b: RowFormSpinnerBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(item: FormItem.Spinner) {
            b.tvLabel.text = item.label

            val parentValue = item.dependsOnKey?.let { state.getString(it) }

            val options = item.optionsProvider?.invoke(parentValue)
                ?: item.staticOptions
            val optValues = options.map { it.first }
            val optLabels = options.map { it.second }

            val adapter = ArrayAdapter(
                b.root.context,
                android.R.layout.simple_spinner_item,
                optLabels
            ).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            b.spinner.adapter = adapter

            val current = state.getString(item.key)
            val idx = optValues.indexOf(current)
            if (idx >= 0) {
                b.spinner.setSelection(idx, false)
            } else {
                state.set(item.key, null)
                if (options.isNotEmpty()) {
                    b.spinner.setSelection(0, false)
                    state.set(item.key, optValues[0])
                }
            }

            b.spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        state.set(item.key, optValues[position])
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
        }

        fun bindOptionsOnly(item: FormItem.Spinner) {
            // update spinner adapter/options based on dependsOnKey
            // DO NOT reset selection unless required

        }



    }


    inner class SwitchVH(private val b: RowFormSwitchBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: FormItem.Switch) {
            b.tvLabel.text = item.label
            b.sw.textOn = item.onText
            b.sw.textOff = item.offText

            val current = state.getBoolean(item.key) ?: false
            b.sw.setOnCheckedChangeListener(null)
            b.sw.isChecked = current
            b.sw.setOnCheckedChangeListener { _, isChecked -> state.set(item.key, isChecked) }
        }
    }


    private inner class DateVH(private val b: RowFormDateBinding) :
        RecyclerView.ViewHolder(b.root) {

        private var isBinding = false
        private var currentKey: String? = null

        init {
            b.etDate.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = currentKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key).orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }
        }

        fun bind(item: FormItem.Date) {
            currentKey = item.key
            b.etDate.hint = item.hint

            b.tvError.text = errors[item.key]
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE

            val desired = state.getString(item.key)
            if (b.etDate.text?.toString() != desired && desired != null){
                isBinding = true
                b.etDate.setTextSafely(desired)
                b.etDate.setSelection(b.etDate.text?.length ?: 0)
                isBinding = false
            } else if (item.currentDate != null) {
                isBinding = true
                b.etDate.setTextSafely(item.currentDate)
                b.etDate.setSelection(b.etDate.text?.length ?: 0)
                isBinding = false
            }

            b.btnToday.visibility = if (item.showTodayButton) View.VISIBLE else View.GONE
            b.btnToday.setOnClickListener { item.onClick?.invoke() }

            /*b.btnToday.setOnClickListener {
                val v = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                state.set(item.key, v.trim())
                b.etDate.setText(v.trim())
                b.etDate.setSelection(v.length)
            }

            b.etDate.doAfterTextChanged {
                state.set(item.key, b.etDate.text?.toString()?.trim().orEmpty())
            }*/
        }

        fun bindErrorOnly(item: FormItem.Date) {
            b.tvError.text = errors[item.key].orEmpty()
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE
        }

        fun bindExternalText(item: FormItem.Date) {
            isBinding = true
            b.etDate.setTextSafely(state.getString(item.key).orEmpty())
            isBinding = false
        }

    }


    inner class TimeVH(private val b: RowFormTimeBinding) :
        RecyclerView.ViewHolder(b.root) {
        private var isBinding = false
        private var currentKey: String? = null

        init {
            b.etTime.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = currentKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key).orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }
        }

        fun bind(item: FormItem.Time) {
            currentKey = item.key

            b.etTime.hint = item.hint

            b.tvError.text = errors[item.key] ?: ""
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE

            val desired = state.getString(item.key)
            if (b.etTime.text?.toString() != desired && desired != null) {
                isBinding = true
                b.etTime.setTextSafely(desired)
                b.etTime.setSelection(b.etTime.text?.length ?: 0)
                isBinding = false
            } else if (item.currentTime != null) {
                isBinding = true
                b.etTime.setTextSafely(item.currentTime)
                b.etTime.setSelection(b.etTime.text?.length ?: 0)
                isBinding = false
            }

            b.btnNow.visibility = if (item.showNowButton) View.VISIBLE else View.GONE
            b.btnNow.setOnClickListener { item.onClick?.invoke() }
            /*b.btnNow.setOnClickListener {
                val v = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                state.set(item.key, v.trim())
                b.etTime.setText(v.trim())
                b.etTime.setSelection(v.length)
            }

            b.etTime.doAfterTextChanged {
                state.set(item.key, b.etTime.text?.toString()?.trim().orEmpty())
            }*/
        }

        fun bindErrorOnly(item: FormItem.Time) {
            b.tvError.text = errors[item.key].orEmpty()
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE
        }

        fun bindExternalText(item: FormItem.Time) {
            isBinding = true
            b.etTime.setTextSafely(state.getString(item.key).orEmpty())
            isBinding = false
        }

    }


    inner class UtmVH(private val b: RowFormUtmBinding) :
        RecyclerView.ViewHolder(b.root) {

        private var isBinding = false
        private var eastingKey: String? = null
        private var northingKey: String? = null
        private var zoneKey: String? = null
        private var hemiKey: String? = null

        init {
            b.etEasting.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = eastingKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key).orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }

            b.etNorthing.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = northingKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key).orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }

            b.etZone.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = zoneKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key).orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }
        }

        fun bind(item: FormItem.UtmItem) {
            eastingKey = item.eastingKey
            northingKey = item.northingKey
            zoneKey = item.zoneKey
            hemiKey = item.hemisphereKey

            b.tvLabel.text = item.label

            b.tvError.text = errors[item.key].orEmpty()
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE

            b.etEasting.hint = "UTM Easting"
            b.etNorthing.hint = "UTM Northing"
            b.etZone.hint = "UTM Zone"

            val currentE = state.getString(item.eastingKey) ?: item.currentUtm?.easting?.toString() ?: ""
            val currentN = state.getString(item.northingKey) ?: item.currentUtm?.northing?.toString() ?: ""
            val currentZ = state.getString(item.zoneKey) ?: item.currentUtm?.zone?.toString() ?: ""
            val currentH = state.getBoolean(item.hemisphereKey) ?: item.currentUtm?.hemisphereNorth ?: true
            if (state.getBoolean(item.hemisphereKey) == null) state.set(item.hemisphereKey, true)

            // prevent switch listener loops if you have a listener elsewhere
            b.swHemi.setOnCheckedChangeListener(null)

            // Update text safely WITHOUT forcing cursor selection every bind
            isBinding = true
            b.etEasting.setTextSafely(currentE)
            b.etNorthing.setTextSafely(currentN)
            b.etZone.setTextSafely(currentZ)
            b.swHemi.isChecked = currentH
            isBinding = false

            b.swHemi.setOnCheckedChangeListener { _, checked ->
                if (isBinding) return@setOnCheckedChangeListener
                val hk = hemiKey ?: return@setOnCheckedChangeListener
                if (state.getBoolean(hk) != checked) state.set(hk, checked)
            }

            // Set UI from current
            /*if (b.etEasting.text?.toString() != current_e) {
                isBinding = true
                b.etEasting.setTextSafely(current_e.orEmpty())
                b.etEasting.setSelection(b.etEasting.text?.length ?: 0)
                isBinding = false
            } else b.etEasting.setHint("UTM Easting")
            if (b.etNorthing.text?.toString() != current_n) {
                isBinding = true
                b.etNorthing.setTextSafely(current_n.orEmpty())
                b.etNorthing.setSelection(b.etNorthing.text?.length ?: 0)
                isBinding = false
            } else b.etNorthing.setHint("UTM Northing")
            if (b.etZone.text?.toString() != current_z) {
                isBinding = true
                b.etZone.setTextSafely(current_z.orEmpty())
                b.etZone.setSelection(b.etZone.text?.length ?: 0)
                isBinding = false
            } else b.etZone.setHint("UTM Zone")
            b.swHemi.isChecked = current_h ?: true


            b.swHemi.setOnCheckedChangeListener { _, _ ->
                state.set(item.hemisphereKey, b.swHemi.isChecked)
            }*/

        }

        fun bindEnabledOnly(item: FormItem.UtmItem) {
            val enabled = state.getBoolean(item.hemisphereKey) ?: true // or whatever your logic is
            b.etEasting.isEnabled = enabled
            b.etNorthing.isEnabled = enabled
            b.etZone.isEnabled = enabled
        }

        fun bindErrorOnly(item: FormItem.UtmItem) {
            b.tvError.text = errors[item.key].orEmpty()
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE
        }

    }


    inner class CoordsVH(private val b: RowFormCoordBinding) :
        RecyclerView.ViewHolder(b.root) {

        private var isBinding = false
        private var eastingXKey: String? = null
        private var northingYKey: String? = null
        private var zKey: String? = null
        private var zoneKey: String? = null
        private var hemiKey: String? = null

        init {
            b.etExCoord.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = eastingXKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key).orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }

            b.etNyCoord.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = northingYKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key).orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }

            b.etZCoord.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = zKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key).orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }

            b.etZone.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = zoneKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key).orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }
        }
        fun bind(item: FormItem.Coords) {
            eastingXKey = item.eastingXKey
            northingYKey = item.northingYKey
            zKey = item.zKey
            zoneKey = item.zoneKey
            hemiKey = item.hemisphereKey

            b.nyCoord.visibility = if (item.showNYCoord) View.VISIBLE else View.GONE
            b.zCoord.visibility = if (item.showZCoord) View.VISIBLE else View.GONE
            b.zoneHemi.visibility = if (item.showZoneHemi) View.VISIBLE else View.GONE

            b.tvError.text = state.getError(item.key).orEmpty()
            b.tvError.visibility = if (state.getError(item.key).isNullOrBlank()) View.GONE else View.VISIBLE

            /*b.etExCoord.hint = "UTM Easting"
            b.etNyCoord.hint = "UTM Northing"
            b.etZCoord.hint = ""
            b.etZone.hint = "UTM Zone"*/

            // Read current value from state
            val currentE = state.getString(item.eastingXKey).orEmpty()
            val currentN = state.getString(item.northingYKey).orEmpty()
            val currentZ = state.getString(item.zKey).orEmpty()
            val currentZone = state.getString(item.zoneKey).orEmpty()
            val currentH = state.getBoolean(item.hemisphereKey) ?: true

            b.swHemi.setOnCheckedChangeListener(null)

            // Update text safely WITHOUT forcing cursor selection every bind
            isBinding = true
            b.etExCoord.setTextSafely(currentE)
            b.etNyCoord.setTextSafely(currentN)
            b.etZCoord.setTextSafely(currentZ)
            b.etZone.setTextSafely(currentZone)
            b.swHemi.isChecked = currentH
            isBinding = false

            b.swHemi.setOnCheckedChangeListener { _, checked ->
                if (isBinding) return@setOnCheckedChangeListener
                val hk = hemiKey ?: return@setOnCheckedChangeListener
                if (state.getBoolean(hk) != checked) state.set(hk, checked)
            }

            // Set UI from current
            /*if (b.etExCoord.text?.toString() != current_ex) {
                isBinding = true
                currentKey = item.eastingXKey
                b.etExCoord.setTextSafely(current_ex.orEmpty())
                b.etExCoord.setSelection(b.etExCoord.text?.length ?: 0)
                isBinding = false
            }
            if (item.showNYCoord && b.etNyCoord.text?.toString() != current_ny) {
                isBinding = true
                currentKey = item.northingYKey
                b.etNyCoord.setTextSafely(current_ny.orEmpty())
                b.etNyCoord.setSelection(b.etNyCoord.text?.length ?: 0)
                isBinding = false
            }
            if (item.showZCoord && b.etZCoord.text?.toString() != current_z) {
                isBinding = true
                currentKey = item.zKey
                b.etZCoord.setTextSafely(current_z.orEmpty())
                b.etZCoord.setSelection(b.etZCoord.text?.length ?: 0)
                isBinding = false
            }
            if (item.showZone && b.etZone.text?.toString() != current_zone) {
                isBinding = true
                currentKey = item.zoneKey
                b.etZone.setTextSafely(current_zone.orEmpty())
                b.etZone.setSelection(b.etZone.text?.length ?: 0)
                isBinding = false
            }
            b.swHemi.isChecked = current_h ?: true

            b.swHemi.setOnCheckedChangeListener { _, _ ->
                state.set(item.hemisphereKey, b.swHemi.isChecked)
            }*/
        }

        fun bindVisibilityOnly(item: FormItem.Coords) {
            // show/hide views only; no setText
            b.nyCoord.visibility = if (item.showNYCoord) View.VISIBLE else View.GONE
            b.zCoord.visibility = if (item.showZCoord) View.VISIBLE else View.GONE
            b.zoneHemi.visibility = if (item.showZoneHemi) View.VISIBLE else View.GONE

            b.etExCoord.hint = state.getString("crdsHint1")
            b.tvInputExLabel.visibility = if (item.showNYCoord) View.VISIBLE else View.GONE
            b.tvInputExLabel.text = state.getString("crdsLabel1")
            b.etNyCoord.hint = state.getString("crdsHint2")
            b.tvNyLabel.text = state.getString("crdsLabel2")
        }

        fun bindErrorOnly(item: FormItem.Coords) {
            b.tvError.text = errors[item.key].orEmpty()
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE
        }

    }


    inner class HeightVH(
        private val b: RowFormHeightBinding
    ) : RecyclerView.ViewHolder(b.root) {

        private var isBinding = false
        private var currentKey: String? = null

        init {
            b.etHeight.doAfterTextChanged { editable ->
                if (isBinding) return@doAfterTextChanged

                val key = currentKey ?: return@doAfterTextChanged
                val newValue = editable?.toString().orEmpty()

                // Prevent redundant state writes (which can trigger adapter updates)
                if (state.getString(key).orEmpty() != newValue) {
                    state.set(key, newValue)
                }
            }
        }

        fun bind(item: FormItem.Height) {
            b.tvLabel.text = item.label

            // Units spinner
            val adapter = ArrayAdapter(b.root.context, android.R.layout.simple_spinner_item, item.units)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            b.spUnit.adapter = adapter

            // restore existing values if any
            val existingValue = state.getString(item.valueKey)?.trim()
            if (existingValue != null && b.etHeight.text?.toString()?.trim() != existingValue) {
                isBinding = true
                b.etHeight.setTextSafely(existingValue)
                isBinding = false
            }

            val existingUnit = state.getString(item.unitKey)?.trim()
            if (existingUnit != null) {
                val idx = item.units.indexOf(existingUnit)
                if (idx >= 0) b.spUnit.setSelection(idx, false)
            } else {
                // default
                state.set(item.unitKey, item.units.first())
            }

            // listeners (careful: don’t add multiple watchers)
            /*b.etHeight.doAfterTextChanged {
                state.set(item.valueKey, it?.toString()?.trim())
            }*/

            b.spUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    state.set(item.unitKey, item.units[position])
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            // show error
            b.tvError.text = errors[item.key]
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE

        }

        fun bindErrorOnly(item: FormItem.Height) {
            b.tvError.text = errors[item.key].orEmpty()
            b.tvError.visibility = if (errors[item.key].isNullOrBlank()) View.GONE else View.VISIBLE
        }
    }


    private fun EditText.setTextSafely(newText: String) {
        val old = text?.toString() ?: ""
        if (old == newText) return

        // Save cursor position relative to end (so typing feels stable)
        val oldSelection = selectionStart.coerceAtLeast(0)
        val fromEnd = old.length - oldSelection

        setText(newText)

        // Restore cursor as best as possible
        val newPos = (newText.length - fromEnd).coerceIn(0, newText.length)
        setSelection(newPos)
    }


    fun onValueChanged(key: String) {
        // find all spinners that depend on this key
        items.forEachIndexed { index, spec ->
            if (spec is FormItem.Spinner && spec.dependsOnKey == key) {
                safeNotifyItemChanged(index)
                return@forEachIndexed
            }
            /*if (key == "category") {
                safeNotifyItemChanged(index)
                return@forEachIndexed
            }*/
            if (//state.getString("category") == ConverterActivity.Mode.GEO.name &&
                key == "unit") {

                val indexx = items.indexOfFirst { it is FormItem.Coords && it.key == "value" }
                if (index != -1) {
                    val specc = items[indexx] as FormItem.Coords

                    when(state.getString(key)) {
                        ConverterActivity.GeoInputType.LATLON.name,
                        ConverterActivity.GeoInputType.ITM.name,
                        ConverterActivity.GeoInputType.WEB_MERCATOR.name -> {
                            specc.showNYCoord = true
                            specc.showZCoord = false
                            specc.showZoneHemi = false
                        }
                        ConverterActivity.GeoInputType.UTM.name -> {
                            specc.showNYCoord = true
                            specc.showZCoord = false
                            specc.showZoneHemi = true
                        }
                        ConverterActivity.GeoInputType.ECEF.name -> {
                            specc.showNYCoord = true
                            specc.showZCoord = true
                            specc.showZoneHemi = false
                        }
                        else -> {
                            specc.showNYCoord = false
                            specc.showZCoord = false
                            specc.showZoneHemi = false
                        }
                    }

                    when(state.getString(key)) {
                        ConverterActivity.GeoInputType.LATLON.name,
                        ConverterActivity.GeoInputType.UTM.name,
                        ConverterActivity.GeoInputType.ITM.name -> {
                            state.set("crdsHint1", specc.hintsLabels?.getOrDefault("hint_easting", ""))
                            state.set("crdsLabel1", specc.hintsLabels?.getOrDefault("label_easting", ""))

                            state.set("crdsHint2", specc.hintsLabels?.getOrDefault("hint_northing", ""))
                            state.set("crdsLabel2", specc.hintsLabels?.getOrDefault("label_northing", ""))
                        }

                        ConverterActivity.GeoInputType.ECEF.name,
                        ConverterActivity.GeoInputType.WEB_MERCATOR.name -> {
                            state.set("crdsHint1", specc.hintsLabels?.getOrDefault("hint_x_coord", ""))
                            state.set("crdsLabel1", specc.hintsLabels?.getOrDefault("label_x_coord", ""))

                            state.set("crdsHint2", specc.hintsLabels?.getOrDefault("hint_y_coord", ""))
                            state.set("crdsLabel2", specc.hintsLabels?.getOrDefault("label_y_coord", ""))
                        }

                        else -> {
                            state.set("crdsHint1", "Enter ${state.getString(spec.key) ?: "Value"}")
                            state.set("crdsLabel1", "")
                        }

                    }
                    /*state.clearValue("ex_coord")
                    state.clearValue("ny_coord")
                    state.clearValue("z_coord")
                    state.clearValue("utm_zone")
                    state.clearValue("utm_hemisphere")*/
                    safeNotifyItemChanged(indexx, Payload.COORDS_INPUTS)
                }
                return@forEachIndexed
            }

            if ((spec.key == "date" && key == "date") ||
                (spec.key == "time" && key == "time")
            ) {
                safeNotifyItemChanged(index, Payload.EXTERNAL_TEXT)
                return@forEachIndexed
            }
            if (spec is FormItem.Switch && spec.updateKey != null && spec.key == key) {
                val targetIndex = indexOfKey(spec.updateKey)
                if (targetIndex != RecyclerView.NO_POSITION && targetIndex >= 0) {
                    safeNotifyItemChanged(targetIndex, Payload.ENABLED)
                }
                return@forEachIndexed
            }
        }
    }

    fun indexOfKey(key: String): Int = items.indexOfFirst { it.key == key }


}




/*
class FormAdapter(
    private var items: List<FormItem>,
    private val state: FormState,
    private val onAction: ((key: String) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun submit(newItems: List<FormItem>) {
        items = newItems
        seedInitialValues()
        notifyDataSetChanged()
    }

    /** Convenience: set from Activity/Edit mode. */
    fun setValue(key: String, value: Any?) {
        state.setValue(key, value)
        val idx = items.indexOfFirst { it.key == key }
        if (idx >= 0) notifyItemChanged(idx)
    }

    fun updateItem(key: String, transform: (FormItem) -> FormItem) {
        val idx = items.indexOfFirst { it.key == key }
        if (idx < 0) return
        items = items.toMutableList().also { it[idx] = transform(it[idx]) }
        notifyItemChanged(idx)
    }

    fun indexOfKey(key: String): Int = items.indexOfFirst { it.key == key }

    private fun seedInitialValues() {
        for (it in items) {
            when (it) {
                is FormItem.Text -> state.putIfAbsent(it.key, it.initial ?: "")
                is FormItem.Number -> state.putIfAbsent(it.key, it.initial ?: "")
                is FormItem.Spinner -> state.putIfAbsent(it.key, it.initialValue ?: it.options.firstOrNull()?.value)
                is FormItem.Switch -> state.putIfAbsent(it.key, it.initial)
                is FormItem.Date -> state.putIfAbsent(it.key, it.initial ?: "")
                is FormItem.Time -> state.putIfAbsent(it.key, it.initial ?: "")
                is FormItem.UtmItem -> state.putIfAbsent(it.key, it.initial ?: FormItem.UtmItem.UtmItemValue(
                    easting = "",
                    northing = "",
                    zone = it.zoneMin,
                    hemisphere = true
                ))
                is FormItem.Height -> state.putIfAbsent(it.key, it.initial ?: FormItem.Height.HeightValue(
                    heightRaw = "",
                    unit = HeightType.METERS
                ))
                else -> {}
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FormItem.Header -> R.layout.include_form
            is FormItem.Text -> R.layout.row_form_text
            is FormItem.Number -> R.layout.row_form_text
            is FormItem.Spinner -> R.layout.row_form_spinner
            is FormItem.Switch -> R.layout.row_form_switch
            is FormItem.Date -> R.layout.row_form_date
            is FormItem.Time -> R.layout.row_form_time
            is FormItem.UtmItem -> R.layout.row_form_utm
            is FormItem.Height -> R.layout.row_form_height
            is FormItem.Action -> R.layout.row_form_action
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.include_form -> HeaderVH(v)
            R.layout.row_form_text -> TextVH(v)
            R.layout.row_form_spinner -> SpinnerVH(v)
            R.layout.row_form_switch -> SwitchVH(v)
            R.layout.row_form_date -> DateVH(v)
            R.layout.row_form_time -> TimeVH(v)
            R.layout.row_form_utm -> UtmVH(v)
            R.layout.row_form_height -> HeightVH(v)
            R.layout.row_form_action -> ActionVH(v)
            else -> error("Unknown viewType $viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is FormItem.Header -> (holder as HeaderVH).bind(item)
            is FormItem.Text -> (holder as TextVH).bindText(item)
            is FormItem.Number -> (holder as TextVH).bindNumber(item)
            is FormItem.Spinner -> (holder as SpinnerVH).bind(item)
            is FormItem.Switch -> (holder as SwitchVH).bind(item)
            is FormItem.Date -> (holder as DateVH).bind(item)
            is FormItem.Time -> (holder as TimeVH).bind(item)
            is FormItem.UtmItem -> (holder as UtmVH).bind(item)
            is FormItem.Height -> (holder as HeightVH).bind(item)
            is FormItem.Action -> (holder as ActionVH).bind(item)
        }
    }

    // ------------------------
    // ViewHolders
    // ------------------------

    private inner class HeaderVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.form_title)
        fun bind(item: FormItem.Header) { tv.text = item.title }
    }

    /**
     * Used for both Text and Number by configuring inputType/filters.
     * Expects row_form_text.xml ids: tv_label, et_value, (optional) error
     */
    private inner class TextVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvLabel: TextView = v.findViewById(R.id.tv_label)
        private val et: EditText = v.findViewById(R.id.et_value)
        private val tvError: TextView? = v.findViewById(R.id.error)

        private var watcher: TextWatcher? = null

        fun bindText(item: FormItem.Text) {
            tvLabel.text = item.label
            et.hint = item.hint ?: ""

            bindCommon(
                key = item.key,
                desired = state.getString(item.key) ?: (item.initial ?: ""),
                inputType = android.text.InputType.TYPE_CLASS_TEXT,
                filters = emptyArray()
            )
        }

        fun bindNumber(item: FormItem.Number) {
            tvLabel.text = item.label
            et.hint = item.hint ?: ""

            val inputType =
                if (item.allowDecimal)
                    android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                else
                    android.text.InputType.TYPE_CLASS_NUMBER

            bindCommon(
                key = item.key,
                desired = state.getString(item.key) ?: (item.initial ?: ""),
                inputType = inputType,
                filters = buildNumberFilters(item.allowDecimal)
            )
        }

        private fun bindCommon(
            key: String,
            desired: String,
            inputType: Int,
            filters: Array<InputFilter>
        ) {
            tvError?.text = state.getError(key).orEmpty()
            tvError?.visibility = if (state.getError(key).isNullOrBlank()) View.GONE else View.VISIBLE

            // Remove old watcher
            watcher?.let { et.removeTextChangedListener(it) }

            et.inputType = inputType
            et.filters = filters

            if (et.text?.toString() != desired) {
                et.setText(desired)
                et.setSelection(et.text?.length ?: 0)
            }

            watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    state.setValue(key, s?.toString().orEmpty())
                }
            }
            et.addTextChangedListener(watcher)
        }
    }

    /**
     * row_form_spinner.xml ids: tv_label, spinner, (optional) error
     */
    private inner class SpinnerVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvLabel: TextView = v.findViewById(R.id.tv_label)
        private val sp: Spinner = v.findViewById(R.id.spinner)
        private val tvError: TextView? = v.findViewById(R.id.error)

        fun bind(item: FormItem.Spinner) {
            tvLabel.text = item.label

            tvError?.text = state.getError(item.key).orEmpty()
            tvError?.visibility = if (state.getError(item.key).isNullOrBlank()) View.GONE else View.VISIBLE

            val ctx = itemView.context
            val adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, item.options.map { it.display })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sp.adapter = adapter

            val currentValue = state.getString(item.key) ?: item.initialValue ?: item.options.firstOrNull()?.value
            val idx = item.options.indexOfFirst { it.value == currentValue }.coerceAtLeast(0)

            sp.onItemSelectedListener = null
            sp.setSelection(idx, false)
            sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    state.setValue(item.key, item.options[position].value)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    /**
     * row_form_switch.xml ids: tv_label, sw, (optional) error
     */
    private inner class SwitchVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvLabel: TextView = v.findViewById(R.id.tv_label)
        private val sw: Switch = v.findViewById(R.id.sw)
        private val tvError: TextView? = v.findViewById(R.id.error)

        fun bind(item: FormItem.Switch) {
            tvLabel.text = item.label
            sw.textOn = item.onText
            sw.textOff = item.offText

            tvError?.text = state.getError(item.key).orEmpty()
            tvError?.visibility = if (state.getError(item.key).isNullOrBlank()) View.GONE else View.VISIBLE

            val current = state.getBoolean(item.key) ?: item.initial
            sw.setOnCheckedChangeListener(null)
            sw.isChecked = current
            sw.setOnCheckedChangeListener { _, isChecked -> state.setValue(item.key, isChecked) }
        }
    }

    /**
     * row_form_date.xml ids: et_value, btn_today(optional), (optional) error
     */
    private inner class DateVH(v: View) : RecyclerView.ViewHolder(v) {
        private val et: EditText = v.findViewById(R.id.et_value)
        private val btnToday: View? = v.findViewById(R.id.btn_today)
        private val tvError: TextView? = v.findViewById(R.id.error)

        private var watcher: TextWatcher? = null

        fun bind(item: FormItem.Date) {
            et.hint = item.hint

            tvError?.text = state.getError(item.key).orEmpty()
            tvError?.visibility = if (state.getError(item.key).isNullOrBlank()) View.GONE else View.VISIBLE

            watcher?.let { et.removeTextChangedListener(it) }

            val desired = state.getString(item.key) ?: (item.initial ?: "")
            if (et.text?.toString() != desired) {
                et.setText(desired)
                et.setSelection(et.text?.length ?: 0)
            }

            watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    state.setValue(item.key, s?.toString().orEmpty())
                }
            }
            et.addTextChangedListener(watcher)

            btnToday?.visibility = if (item.showTodayButton) View.VISIBLE else View.GONE
            btnToday?.setOnClickListener {
                val v = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                state.setValue(item.key, v)
                et.setText(v)
                et.setSelection(v.length)
            }
        }
    }

    /**
     * row_form_time.xml ids: et_value, btn_now(optional), (optional) error
     */
    private inner class TimeVH(v: View) : RecyclerView.ViewHolder(v) {
        private val et: EditText = v.findViewById(R.id.et_value)
        private val btnNow: View? = v.findViewById(R.id.btn_now)
        private val tvError: TextView? = v.findViewById(R.id.error)

        private var watcher: TextWatcher? = null

        fun bind(item: FormItem.Time) {
            et.hint = item.hint

            tvError?.text = state.getError(item.key).orEmpty()
            tvError?.visibility = if (state.getError(item.key).isNullOrBlank()) View.GONE else View.VISIBLE

            watcher?.let { et.removeTextChangedListener(it) }

            val desired = state.getString(item.key) ?: (item.initial ?: "")
            if (et.text?.toString() != desired) {
                et.setText(desired)
                et.setSelection(et.text?.length ?: 0)
            }

            watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    state.setValue(item.key, s?.toString().orEmpty())
                }
            }
            et.addTextChangedListener(watcher)

            btnNow?.visibility = if (item.showNowButton) View.VISIBLE else View.GONE
            btnNow?.setOnClickListener {
                val v = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                state.setValue(item.key, v)
                et.setText(v)
                et.setSelection(v.length)
            }
        }
    }

    /**
     * row_form_utm.xml ids: et_easting, et_northing, sp_zone, sw_hemi, (optional) error
     */
    private inner class UtmVH(v: View) : RecyclerView.ViewHolder(v) {
        private val etE: EditText = v.findViewById(R.id.et_easting)
        private val etN: EditText = v.findViewById(R.id.et_northing)
        private val spZone: Spinner = v.findViewById(R.id.sp_zone)
        private val swHemi: Switch = v.findViewById(R.id.sw_hemi)
        private val tvError: TextView? = v.findViewById(R.id.error)

        private var eWatcher: TextWatcher? = null
        private var nWatcher: TextWatcher? = null

        fun bind(item: FormItem.UtmItem) {
            tvError?.text = state.getError(item.key).orEmpty()
            tvError?.visibility = if (state.getError(item.key).isNullOrBlank()) View.GONE else View.VISIBLE

            // Zone spinner setup
            val ctx = itemView.context
            val zones = (item.zoneMin..item.zoneMax).map { it.toString() }
            val zoneAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, zones)
            zoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spZone.adapter = zoneAdapter

            // Filters to enforce digits + decimals (raw)
            etE.filters = arrayOf(buildFixedDecimalFilter(item.eastingDigits, item.maxDecimals))
            etN.filters = arrayOf(buildFixedDecimalFilter(item.northingDigits, item.maxDecimals))

            // Read current value from state
            val current = state.getUtm(item.key) ?: item.initial ?: FormItem.UtmItem.UtmItemValue(
                easting = "",
                northing = "",
                zone = item.zoneMin,
                hemisphere = true
            )

            // Detach listeners before setting UI
            eWatcher?.let { etE.removeTextChangedListener(it) }
            nWatcher?.let { etN.removeTextChangedListener(it) }
            spZone.onItemSelectedListener = null
            swHemi.setOnCheckedChangeListener(null)

            // Set UI from current
            if (etE.text?.toString() != current.easting) {
                etE.setText(current.easting)
                etE.setSelection(etE.text?.length ?: 0)
            }
            if (etN.text?.toString() != current.northing) {
                etN.setText(current.northing)
                etN.setSelection(etN.text?.length ?: 0)
            }
            spZone.setSelection((current.zone - item.zoneMin).coerceAtLeast(0), false)
            swHemi.isChecked = current.hemisphere

            // Attach listeners -> write back to state
            eWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) { pushUtm(item) }
            }
            nWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) { pushUtm(item) }
            }
            etE.addTextChangedListener(eWatcher)
            etN.addTextChangedListener(nWatcher)

            spZone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { pushUtm(item) }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            swHemi.setOnCheckedChangeListener { _, _ -> pushUtm(item) }

            // Ensure state is at least initialized
            state.putIfAbsent(item.key, current)
        }

        private fun pushUtm(item: FormItem.UtmItem) {
            val zone = item.zoneMin + spZone.selectedItemPosition.coerceAtLeast(0)
            val v = FormItem.UtmItem.UtmItemValue(
                easting = etE.text?.toString().orEmpty(),
                northing = etN.text?.toString().orEmpty(),
                zone = zone,
                hemisphere = swHemi.isChecked
            )
            state.setValue(item.key, v)
        }
    }

    /**
     * row_form_height.xml ids: et_height, sp_unit, (optional) error
     */
    private inner class HeightVH(v: View) : RecyclerView.ViewHolder(v) {
        private val etHeight: EditText = v.findViewById(R.id.et_height)
        private val spUnit: Spinner = v.findViewById(R.id.sp_unit)
        private val tvError: TextView? = v.findViewById(R.id.error)

        private var watcher: TextWatcher? = null

        fun bind(item: FormItem.Height) {
            tvError?.text = state.getError(item.key).orEmpty()
            tvError?.visibility = if (state.getError(item.key).isNullOrBlank()) View.GONE else View.VISIBLE

            val ctx = itemView.context
            val adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, item.units.map { it.display })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spUnit.adapter = adapter

            val current = state.getHeight(item.key) ?: item.initial ?: FormItem.Height.HeightValue(
                heightRaw = "",
                unit = HeightType.METERS
            )

            // detach
            watcher?.let { etHeight.removeTextChangedListener(it) }
            spUnit.onItemSelectedListener = null

            // set UI
            if (etHeight.text?.toString() != current.heightRaw) {
                etHeight.setText(current.heightRaw)
                etHeight.setSelection(etHeight.text?.length ?: 0)
            }

            val unitIdx = item.units.indexOfFirst { it.value == current.unit.name }.coerceAtLeast(0)
            spUnit.setSelection(unitIdx, false)

            // attach
            watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) { pushHeight(item) }
            }
            etHeight.addTextChangedListener(watcher)

            spUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { pushHeight(item) }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            state.putIfAbsent(item.key, current)
        }

        private fun pushHeight(item: FormItem.Height) {
            val idx = spUnit.selectedItemPosition.coerceAtLeast(0)
            val unitName = item.units.getOrNull(idx)?.value ?: HeightType.METERS.name
            val unit = runCatching { HeightType.valueOf(unitName) }.getOrDefault(HeightType.METERS)

            val v = FormItem.Height.HeightValue(
                heightRaw = etHeight.text?.toString().orEmpty(),
                unit = unit
            )
            state.setValue(item.key, v)
        }
    }

    /**
     * row_form_action.xml ids: action_title, action_subtitle(optional), action_container
     */
    private inner class ActionVH(v: View) : RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.action_title)
        private val subtitle: TextView? = v.findViewById(R.id.action_subtitle)
        private val container: View = v.findViewById(R.id.action_container)

        fun bind(item: FormItem.Action) {
            title.text = item.title
            subtitle?.text = item.subtitle ?: ""
            subtitle?.visibility = if (item.subtitle.isNullOrBlank()) View.GONE else View.VISIBLE

            container.setOnClickListener { onAction?.invoke(item.key) }
        }
    }

    // ------------------------
    // Helpers
    // ------------------------

    /**
     * Allows digits, optional single '.', up to [maxDecimals] after dot,
     * and up to [intDigits] before dot.
     */
    private fun buildFixedDecimalFilter(intDigits: Int, maxDecimals: Int): InputFilter {
        return InputFilter { source, _, _, dest, dstart, dend ->
            val newText = dest.substring(0, dstart) + source + dest.substring(dend)

            if (newText.isEmpty()) return@InputFilter null

            val parts = newText.split('.')
            if (parts.size > 2) return@InputFilter ""

            val intPart = parts[0]
            if (intPart.isNotEmpty() && !intPart.all { it.isDigit() }) return@InputFilter ""
            if (intPart.length > intDigits) return@InputFilter ""

            if (parts.size == 2) {
                val frac = parts[1]
                if (frac.isNotEmpty() && !frac.all { it.isDigit() }) return@InputFilter ""
                if (maxDecimals >= 0 && frac.length > maxDecimals) return@InputFilter ""
            }

            null
        }
    }

    private fun buildNumberFilters(allowDecimal: Boolean, maxDecimals: Int = 10): Array<InputFilter> {
        val decimalFilter = InputFilter { source, _, _, dest, dstart, dend ->
            val newText = dest.substring(0, dstart) + source + dest.substring(dend)

            if (!allowDecimal) {
                return@InputFilter if (newText.all { it.isDigit() } || newText.isEmpty()) null else ""
            }

            val parts = newText.split('.')
            if (parts.size > 2) return@InputFilter ""
            if (parts[0].isNotEmpty() && !parts[0].all { it.isDigit() }) return@InputFilter ""
            if (parts.size == 2) {
                if (!parts[1].all { it.isDigit() }) return@InputFilter ""
                if (maxDecimals > 0 && parts[1].length > maxDecimals) return@InputFilter ""
            }
            null
        }

        return arrayOf(decimalFilter)
    }
}


class FormAdapter(
    private var items: List<FormItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val values: MutableMap<String, Any?> = mutableMapOf()

    fun submit(newItems: List<FormItem>) {
        items = newItems
        seedInitialValues()
        notifyDataSetChanged()
    }

    fun getValues(): Map<String, Any?> = values.toMap()

    fun setValue(key: String, value: Any?) {
        values[key] = value
        val idx = items.indexOfFirst { it.key == key }
        if (idx >= 0) notifyItemChanged(idx)
    }

    private fun seedInitialValues() {
        for (it in items) {
            when (it) {
                is FormItem.Text -> values.putIfAbsent(it.key, it.initial ?: "")
                is FormItem.Number -> values.putIfAbsent(it.key, it.initial ?: "")
                is FormItem.Spinner -> values.putIfAbsent(it.key, it.initialValue ?: it.options.firstOrNull()?.value)
                is FormItem.Switch -> values.putIfAbsent(it.key, it.initial)
                is FormItem.UtmItem -> values.putIfAbsent(it.key, it.initial)
                is FormItem.Height -> values.putIfAbsent(it.key, Pair(it.initialValue ?: "", it.initialUnit))
                is FormItem.Date -> values.putIfAbsent(it.key, it.initial ?: "")
                is FormItem.Time -> values.putIfAbsent(it.key, it.initial ?: "")
                else -> {}
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FormItem.Header -> R.layout.include_form
            is FormItem.Text -> R.layout.row_form_text
            is FormItem.Number -> R.layout.row_form_text // reuse; Number config differs
            is FormItem.Spinner -> R.layout.row_form_spinner
            is FormItem.Switch -> R.layout.row_form_switch
            is FormItem.Date -> R.layout.row_form_date
            is FormItem.Time -> R.layout.row_form_time
            is FormItem.UtmItem -> R.layout.row_form_utm
            is FormItem.Height -> R.layout.row_form_height
            is FormItem.Action -> R.layout.row_form_action
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.include_form -> HeaderVH(v)
            R.layout.row_form_text -> TextVH(v)
            R.layout.row_form_spinner -> SpinnerVH(v)
            R.layout.row_form_switch -> SwitchVH(v)
            R.layout.row_form_date -> DateVH(v)
            R.layout.row_form_time -> TimeVH(v)
            R.layout.row_form_utm -> UtmVH(v)
            R.layout.row_form_height -> HeightVH(v)
            R.layout.row_form_action -> ActionVH(v)
            else -> error("Unknown viewType $viewType")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is FormItem.Header -> (holder as HeaderVH).bind(item)
            is FormItem.Text -> (holder as TextVH).bindText(item)
            is FormItem.Number -> (holder as TextVH).bindNumber(item)
            is FormItem.Spinner -> (holder as SpinnerVH).bind(item)
            is FormItem.Switch -> (holder as SwitchVH).bind(item)
            is FormItem.Date -> (holder as DateVH).bind(item)
            is FormItem.Time -> (holder as TimeVH).bind(item)
            is FormItem.UtmItem -> (holder as UtmVH).bind(item)
            is FormItem.Height -> (holder as HeightVH).bind(item)
            is FormItem.Action -> (holder as ActionVH).bind(item)
        }
    }

    // ------------------------
    // ViewHolders
    // ------------------------

    private inner class HeaderVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tv: TextView = v.findViewById(R.id.form_title)
        fun bind(item: FormItem.Header) { tv.text = item.title }
    }

    /**
     * Used for both Text and Number by configuring inputType/filters.
     * Expects row_form_text.xml ids: label, input, error
     */
    private inner class TextVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvLabel: TextView = v.findViewById(R.id.tv_label)
        private val et: EditText = v.findViewById(R.id.et_value)

        fun bindText(item: FormItem.Text) {
            tvLabel.text = item.label
            et.hint = item.hint ?: ""

            val current = values[item.key] as? String ?: item.initial ?: ""
            setTextSafely(et, current) { values[item.key] = it }
        }

        fun bindNumber(item: FormItem.Number) {
            tvLabel.text = item.label
            et.hint = item.hint ?: ""

            et.inputType =
                if (item.allowDecimal)
                    android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                else
                    android.text.InputType.TYPE_CLASS_NUMBER

            val current = values[item.key] as? String ?: item.initial ?: ""
            setTextSafely(et, current) { values[item.key] = it }
        }
    }

    /**
     * row_form_spinner.xml ids: label, spinner, error
     */
    private inner class SpinnerVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvLabel: TextView = v.findViewById(R.id.tv_label)
        private val sp: Spinner = v.findViewById(R.id.spinner)

        fun bind(item: FormItem.Spinner) {
            tvLabel.text = item.label
            val ctx = itemView.context

            val adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, item.options.map { it.display })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sp.adapter = adapter

            val currentValue = values[item.key] as? String ?: item.initialValue
            val idx = if (currentValue == null) 0 else item.options.indexOfFirst { it.value == currentValue }.coerceAtLeast(0)

            sp.onItemSelectedListener = null
            sp.setSelection(idx, false)
            sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    values[item.key] = item.options[position].value
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    /**
     * row_form_switch.xml ids: label, sw, error
     */
    private inner class SwitchVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvLabel: TextView = v.findViewById(R.id.tv_label)
        private val sw: Switch = v.findViewById(R.id.sw)

        fun bind(item: FormItem.Switch) {
            tvLabel.text = item.label
            sw.textOn = item.onText
            sw.textOff = item.offText

            val current = values[item.key] as? Boolean ?: item.initial
            sw.setOnCheckedChangeListener(null)
            sw.isChecked = current
            sw.setOnCheckedChangeListener { _, isChecked -> values[item.key] = isChecked }
        }
    }

    /**
     * row_form_date.xml ids: label, input, btn_now, btn_pick, error
     * (btn_pick can be optional; if you don’t have it, remove those lines)
     */
    private inner class DateVH(v: View) : RecyclerView.ViewHolder(v) {
        private val et: EditText = v.findViewById(R.id.et_value)
        private val btnToday: View? = v.findViewById(R.id.btn_today)

        fun bind(item: FormItem.Date) {
            et.hint = item.hint

            val current = values[item.key] as? String ?: item.initial ?: ""
            setTextSafely(et, current) { values[item.key] = it }

            btnToday!!.visibility = if (item.showTodayButton) View.VISIBLE else View.GONE
            btnToday.setOnClickListener {
                values[item.key] = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                // notifyItemChanged(bindingAdapterPosition)
            }
        }
    }

    /**
     * row_form_time.xml ids: label, input, btn_now, btn_pick, error
     */
    private inner class TimeVH(v: View) : RecyclerView.ViewHolder(v) {
        private val et: EditText = v.findViewById(R.id.et_value)
        private val btnNow: View? = v.findViewById(R.id.btn_now)

        fun bind(item: FormItem.Time) {
            et.hint = item.hint

            val current = values[item.key] as? String ?: item.initial ?: ""
            setTextSafely(et, current) { values[item.key] = it }

            btnNow!!.visibility = if (item.showNowButton) View.VISIBLE else View.GONE
            btnNow.setOnClickListener {
                values[item.key] = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                // notifyItemChanged(bindingAdapterPosition)
            }
        }
    }

    /**
     * row_form_utm.xml ids: label, et_easting, et_northing, sp_zone, sw_hemisphere, error
     */
    private inner class UtmVH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvLabel: TextView = v.findViewById(R.id.tv_label)
        private val etE: EditText = v.findViewById(R.id.et_easting)
        private val etN: EditText = v.findViewById(R.id.et_northing)
        private val spZone: Spinner = v.findViewById(R.id.sp_zone)
        private val swHemi: Switch = v.findViewById(R.id.sw_hemi)

        fun bind(item: FormItem.UtmItem) {
            // tvLabel.text = item.label
            val ctx = itemView.context

            // Zone options
            val zones = (item.zoneMin..item.zoneMax).map { it.toString() }
            val zoneAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, zones)
            zoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spZone.adapter = zoneAdapter

            val current = (values[item.key] as? FormItem.UtmItem.UtmItemValue) ?: item.initial

            val eTxt = current?.easting ?: ""
            val nTxt = current?.northing ?: ""
            val zone = current?.zone ?: item.zoneMin
            val isNorthern = current?.hemisphere ?: true

            updateUtm(
                item,
                etE.text?.toString()!!.toDoubleOrNull(),
                etN.text?.toString()!!.toDoubleOrNull(),
                zoneFromSpinner(item),
                swHemi.isChecked
            )

            spZone.onItemSelectedListener = null
            spZone.setSelection((zone - item.zoneMin).coerceAtLeast(0), false)
            spZone.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    updateUtm(
                        item,
                        etE.text?.toString()!!.toDoubleOrNull(),
                        etN.text?.toString()!!.toDoubleOrNull(),
                        zoneFromSpinner(item),
                        swHemi.isChecked
                    )
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            swHemi.setOnCheckedChangeListener(null)
            swHemi.isChecked = isNorthern
            swHemi.setOnCheckedChangeListener { _, _ ->
                updateUtm(
                    item,
                    etE.text?.toString()!!.toDoubleOrNull(),
                    etN.text?.toString()!!.toDoubleOrNull(),
                    zoneFromSpinner(item),
                    swHemi.isChecked
                )
            }
        }

        private fun zoneFromSpinner(item: FormItem.UtmItem): Int {
            return item.zoneMin + spZone.selectedItemPosition
        }


        private fun updateUtm(item: FormItem.UtmItem, e: Double?, n: Double?, zone: Int, isNorthern: Boolean) {
            values[item.key] = FormItem.UtmItem.UtmItemValue(
                easting = e,
                northing = n,
                zone = zone,
                hemisphere = isNorthern
            )
        }
    }

    /**
     * row_form_height.xml ids: et_height, sp_unit, error (label optional)
     */
    private inner class HeightVH(v: View) : RecyclerView.ViewHolder(v) {
        private val etHeight: EditText = v.findViewById(R.id.et_height)
        private val spUnit: Spinner = v.findViewById(R.id.sp_unit)

        fun bind(item: FormItem.Height) {
            val ctx = itemView.context

            val adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, item.units.map { it.display })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spUnit.adapter = adapter

            val current = values[item.key] as? Pair<*, *>
            val heightStr = (current?.first as? String) ?: item.initialValue ?: ""
            val unitValue = (current?.second as? String) ?: item.initialUnit
            val hText = etHeight.text?.toString()
            values[item.key] = Pair(hText!!.toDouble(), selectedUnitValue(item))

            val unitIdx = item.units.indexOfFirst { it.value == unitValue }.coerceAtLeast(0)
            spUnit.onItemSelectedListener = null
            spUnit.setSelection(unitIdx, false)
            spUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    values[item.key] = Pair(etHeight.text?.toString().orEmpty(), selectedUnitValue(item))
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        private fun selectedUnitValue(item: FormItem.Height): HeightType {
            val idx = spUnit.selectedItemPosition.coerceAtLeast(0)
            return HeightConverters.toHeightType(item.units.getOrNull(idx)?.value ?: item.units.first().value)
            //item.units.getOrNull(idx)?.value ?: item.units.first().value
        }
    }

    /**
     * row_form_action.xml ids: title, subtitle(optional), container
     */
    private inner class ActionVH(v: View) : RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.action_title)
        private val subtitle: TextView? = v.findViewById(R.id.action_subtitle)
        private val container: View = v.findViewById(R.id.action_container)

        fun bind(item: FormItem.Action) {
            title.text = item.title
            subtitle?.text = item.subtitle ?: ""
            subtitle?.visibility = if (item.subtitle.isNullOrBlank()) View.GONE else View.VISIBLE

            // container.isEnabled = item.enabled
            // container.alpha = if (item.enabled) 1f else 0.5f

            // container.setOnClickListener {
            //     if (item.enabled) onAction?.invoke(item.key)
            // }
        }
    }

    // ------------------------
    // Helpers
    // ------------------------

    fun updateItem(key: String, transform: (FormItem) -> FormItem) {
        val idx = items.indexOfFirst { it.key == key }
        if (idx < 0) return
        items = items.toMutableList().also { it[idx] = transform(it[idx]) }
        notifyItemChanged(idx)
    }

    fun indexOfKey(key: String): Int = items.indexOfFirst { it.key == key }


    private fun setTextSafely(editText: EditText, newValue: String, onChanged: (String) -> Unit) {
        // Remove previous watcher if attached
        val oldWatcher = editText.getTag(R.id.form_text_watcher_tag) as? TextWatcher
        if (oldWatcher != null) editText.removeTextChangedListener(oldWatcher)

        if (editText.text?.toString() != newValue) {
            editText.setText(newValue)
            // move cursor to end
            editText.setSelection(editText.text?.length ?: 0)
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onChanged(s?.toString().orEmpty())
            }
        }
        editText.addTextChangedListener(watcher)
        editText.setTag(R.id.form_text_watcher_tag, watcher)
    }

    /**
     * Simple decimal filter:
     * - Allows digits
     * - Allows at most one '.'
     * - Allows up to [maxDecimals] after '.'
     */
    private fun buildNumberFilters(allowDecimal: Boolean, maxDecimals: Int): Array<InputFilter> {
        val decimalFilter = InputFilter { source, _, _, dest, dstart, dend ->
            val newText = dest.substring(0, dstart) + source + dest.substring(dend)

            if (!allowDecimal) {
                // only digits
                return@InputFilter if (newText.all { it.isDigit() } || newText.isEmpty()) null else ""
            }

            // allow empty / partial input like "." or "12."
            val parts = newText.split('.')
            if (parts.size > 2) return@InputFilter ""
            if (parts[0].isNotEmpty() && !parts[0].all { it.isDigit() }) return@InputFilter ""
            if (parts.size == 2) {
                if (!parts[1].all { it.isDigit() }) return@InputFilter ""
                if (maxDecimals > 0 && parts[1].length > maxDecimals) return@InputFilter ""
            }
            null
        }

        return arrayOf(decimalFilter)
    }
}
*/