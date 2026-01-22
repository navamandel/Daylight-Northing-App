package com.example.landnv4.ui.inputs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.landnv4.R
import com.example.landnv4.data.inputs.AppInputsStore
import com.example.landnv4.domain.geo.UtmParser.utmToString

class RequiredInputsViewDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_required_inputs_view, null)

        val tvDate = view.findViewById<TextView>(R.id.tv_date)
        val tvTime = view.findViewById<TextView>(R.id.tv_time)
        val tvUtm  = view.findViewById<TextView>(R.id.tv_utm)

        val stored = AppInputsStore.load(requireContext())
        tvDate.text = stored?.dateIso ?: "—"
        tvTime.text = stored?.timeHundredth ?: "—"
        tvUtm.text  = if (stored != null) "E: ${stored.utm13.easting}, N: ${stored.utm13.northing}"
            else "—"

        return AlertDialog.Builder(requireContext())
            .setTitle("Required Inputs")
            .setView(view)
            .setPositiveButton("OK") { _, _ -> dismiss() }
            .create()
    }
}