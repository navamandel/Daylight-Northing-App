package com.example.landnv4.ui

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.landnv4.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ExpandableSection(
    private val root: ViewGroup,
    private val header: View,
    private val content: View,
    private val btnToggle: MaterialButton,
    private val btnClear: MaterialButton? = null,
    private val btnAction: MaterialButton? = null,
) {
    var isExpanded: Boolean = true
        private set

    var onExpandedChanged: ((Boolean) -> Unit)? = null

    val contentContainer = root.findViewById<View>(R.id.expandable_container)

    fun setTitle(text: CharSequence) {
        root.findViewById<TextView>(R.id.tvTitle).text = text

        /*val parentColor = (content.background as? android.graphics.drawable.ColorDrawable)?.color
        if (parentColor != null) {
            (root.findViewById<LinearLayout>(R.id.expandable_container)).apply {
                setBackgroundColor(parentColor)
                backgroundTintList = null
            }
        }*/

    }

    fun setClearVisible(visible: Boolean) {
        btnClear?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun setActionVisible(btnText: String) {
        btnAction?.visibility = View.VISIBLE
        btnAction?.text = btnText
    }

    fun setExpanded(expanded: Boolean, animate: Boolean = true) {
        btnToggle.setIconResource(if (expanded) R.drawable.ic_expand_less_24 else R.drawable.ic_expand_more_24)
        if (isExpanded == expanded) return
        isExpanded = expanded

        if (animate) {
            TransitionManager.beginDelayedTransition(root, AutoTransition())
        }
        contentContainer.visibility = if (expanded) View.VISIBLE else View.GONE
        //btnToggle.setIconResource(if (expanded) R.drawable.ic_expand_less_24 else R.drawable.ic_expand_more_24)
        btnToggle.contentDescription = if (expanded) "Collapse" else "Expand"

        onExpandedChanged?.invoke(expanded)
    }

    fun toggle(animate: Boolean = true) = setExpanded(!isExpanded, animate)

    fun wireClicks() {
        header.setOnClickListener { toggle() }
        btnToggle.setOnClickListener { toggle() }
    }
}
