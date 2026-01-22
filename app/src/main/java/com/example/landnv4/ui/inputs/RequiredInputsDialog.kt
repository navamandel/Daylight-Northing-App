package com.example.landnv4.ui.inputs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landnv4.R
import com.example.landnv4.data.inputs.AppInputsStore
import com.example.landnv4.data.inputs.HomeInputValidation
import com.example.landnv4.data.inputs.HomeInputs
import com.example.landnv4.databinding.ActivityConverterBinding
import com.example.landnv4.domain.astro.TimeUtil.nowUtc
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.landnv4.databinding.IncludeFormBinding
import com.example.landnv4.databinding.DialogRequiredInputsBinding
import com.example.landnv4.domain.geo.UtmParser.parseUtm
import com.example.landnv4.ui.form.FormAdapter
import com.example.landnv4.ui.form.FormItem


class RequiredInputsDialog : DialogFragment() {
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var binding: DialogRequiredInputsBinding
    private lateinit var formAdapter: FormAdapter

    interface Listener {
        fun onInputsSaved(inputs: HomeInputs)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogRequiredInputsBinding.inflate(layoutInflater)


        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Required Inputs")
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton("Save", null) // we override to prevent auto-dismiss on invalid
            .setNegativeButton("Cancel") { _, _ -> dismiss() }
            .setNeutralButton("Clear", null)
            .create()

        dialog.setOnShowListener {
            val stored = AppInputsStore.load(requireContext())


            formAdapter = FormAdapter(listOf(
                FormItem.UtmItem(
                    key = "utm",
                    label = "Utm Coordinates",
                    currentUtm = stored?.utm13
                ),
                FormItem.Date(
                    key = "date",
                    label = "Date",
                    currentDate = stored?.dateIso,
                    onClick = {
                        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val d = nowUtc(ZoneId.of("Asia/Jerusalem"))
                        // etDate.setText(d.format(fmt))
                        formAdapter.state.set("date", d.format(fmt))
                    }
                ),
                FormItem.Time(
                    key = "time",
                    label = "Time",
                    currentTime = stored?.timeHundredth,
                    onClick = {
                        val fmt = DateTimeFormatter.ofPattern("HH:mm:ss.SS")
                        val t = nowUtc(ZoneId.of("Asia/Jerusalem"))
                        formAdapter.state.set("time", t.format(fmt))
                    }
                )
            ) )

            if (stored != null) {
                formAdapter.state.set("show_current", true)
                formAdapter.state.set("utm_easting", stored.utm13.easting.toString())
                formAdapter.state.set("utm_northing", stored.utm13.northing.toString())
                formAdapter.state.set("utm_zone", stored.utm13.zone.toString())
                formAdapter.state.set("utm_hemisphere", stored.utm13.hemisphereNorth)

                formAdapter.state.set("date", stored.dateIso)
                formAdapter.state.set("time", stored.timeHundredth)
            }

            binding.includeForm.formTitle.text = "User Information"
            binding.includeForm.formSubtitle.text = "Enter Your Location, Date and Time"
            binding.includeForm.rvInput.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = formAdapter
                setHasFixedSize(false)
            }


            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {

                val d = formAdapter.state.getString("date").toString().trim()
                val t = formAdapter.state.getString("time").toString().trim()

                val zoneStr = formAdapter.state.getString("utm_zone")?.trim()
                val zone = zoneStr?.toIntOrNull()
                val hemi = formAdapter.state.getBoolean("utm_hemisphere")

                if (zone == null) {
                    binding.tvError.text = "Please choose a UTM zone (1–60)"
                    return@setOnClickListener
                }
                if (hemi == null) {
                    binding.tvError.text = "Please choose hemisphere (North/South)"
                    return@setOnClickListener
                }
                val u = parseUtm(
                    formAdapter.state.getString("utm_easting").toString().trim(),
                    formAdapter.state.getString("utm_northing").toString().trim(),
                    zone,
                    hemi
                )

                val dateOk = HomeInputValidation.validateDateIso(d)
                val timeOk = HomeInputValidation.validateTimeHundredth(t)
                val utmOk = HomeInputValidation.validateUtm13(u)

                when {
                    dateOk != null -> binding.tvError.text = "Date must be YYYY-MM-DD"
                    timeOk != null -> binding.tvError.text = "Time must be HH:MM:SS.ss (hundredths)"
                    utmOk != null -> binding.tvError.text = utmOk
                    else -> {
                        binding.tvError.text = ""
                        (activity as? Listener)?.onInputsSaved(HomeInputs(d, t, u))

                        dismiss()
                    }
                }
            }

            val clearBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            clearBtn.setOnClickListener {
                // wipe stored values
                AppInputsStore.clear(requireContext())

                formAdapter.state.clearAllValues()
            }
        }

        return dialog
    }



}

/*
package com.example.landnv4.ui.inputs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landnv4.R
import com.example.landnv4.data.inputs.AppInputsStore
import com.example.landnv4.data.inputs.HomeInputValidation
import com.example.landnv4.data.inputs.HomeInputs
import com.example.landnv4.databinding.ActivityConverterBinding
import com.example.landnv4.domain.astro.TimeUtil.nowUtc
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class RequiredInputsDialog : DialogFragment() {
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText

    interface Listener {
        fun onInputsSaved(inputs: HomeInputs)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_required_inputs, null)


        etDate = view.findViewById(R.id.et_date)
        etTime = view.findViewById(R.id.et_time)
        val etUtm = view.findViewById<EditText>(R.id.et_utm)
        val tvError = view.findViewById<TextView>(R.id.tv_error)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Required Inputs")
            .setView(view)
            .setCancelable(false)
            .setPositiveButton("Save", null) // we override to prevent auto-dismiss on invalid
            .setNegativeButton("Cancel") { _, _ -> dismiss() }
            .setNeutralButton("Clear", null)
            .create()

        dialog.setOnShowListener {
            val stored = AppInputsStore.load(requireContext())

            etDate.hint = stored?.dateIso
            etTime.hint = stored?.timeHundredth
            etUtm.hint = stored?.utm13

            view.findViewById<Button>(R.id.btnDateToday).setOnClickListener {
                // val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                // sdf.timeZone = java.util.TimeZone.getDefault()
                // etDate.setText(sdf.format(Date()))

                // val instant = Instant.now()
                // val israel = instant.atZone(ZoneId.of("Asia/Jerusalem"))

                val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val d = nowUtc(ZoneId.of("Asia/Jerusalem"))
                etDate.setText(d.format(fmt))
            }


            view.findViewById<Button>(R.id.btnTimeNow).setOnClickListener {
                // val instant = Instant.now()
                //val local = instant.atZone(ZoneId.systemDefault())
                // val local = instant.atZone(ZoneId.of("Asia/Jerusalem"))

                val fmt = DateTimeFormatter.ofPattern("HH:mm:ss.SS")
                val t = nowUtc(ZoneId.of("Asia/Jerusalem"))
                etTime.setText(t.format(fmt))
            }


            val btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btn.setOnClickListener {
                val stored = AppInputsStore.load(requireContext())

                var d = etDate.text.toString().trim()
                var t = etTime.text.toString().trim()
                var u = etUtm.text.toString().trim()

                var dateOk = HomeInputValidation.validateDateIso(d)
                var timeOk = HomeInputValidation.validateTimeHundredth(t)
                var utmOk = HomeInputValidation.validateUtm13(u)

                if (stored != null) {
                    if (dateOk != null) {
                        dateOk = null
                        d = stored.dateIso
                    }
                    if (timeOk != null) {
                        timeOk = null
                        t = stored.timeHundredth
                    }
                    if (utmOk != null) {
                        utmOk = null
                        u = stored.utm13
                    }
                }

                when {
                    dateOk != null -> tvError.text = "Date must be YYYY-MM-DD"
                    timeOk != null -> tvError.text = "Time must be HH:MM:SS.ss (hundredths)"
                    utmOk != null -> tvError.text = "UTM must be exactly 14 digits"
                    else -> {
                        tvError.text = ""
                        (activity as? Listener)?.onInputsSaved(HomeInputs(d, t, u))
                        dismiss()
                    }
                }
            }

            val clearBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            clearBtn.setOnClickListener {
                // wipe stored values
                AppInputsStore.clear(requireContext())

                // wipe current UI
                etDate.text?.clear()
                etTime.text?.clear()
                etUtm.text?.clear()

                etDate.hint = ""
                etTime.hint = ""
                etUtm.hint = ""
                tvError.text = ""
            }
        }

        return dialog
    }



}
 */