package com.example.landnv4.ui.databank

import android.R
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class PointSpinnerAdapter(
    context: Context,
    private val items: List<PointItem>
) : ArrayAdapter<PointItem>(context, R.layout.simple_spinner_item, items) {

    init {
        setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = super.getView(position, convertView, parent)
        (v as TextView).text = display(items[position])
        return v
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = super.getDropDownView(position, convertView, parent)
        (v as TextView).text = display(items[position])
        return v
    }

    private fun display(p: PointItem): String {
        // Shows nicely in spinner
        return "${p.name} (UTM: ${p.utm})"
    }
}