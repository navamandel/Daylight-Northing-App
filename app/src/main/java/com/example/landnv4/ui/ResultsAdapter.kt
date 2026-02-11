package com.example.landnv4.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.landnv4.databinding.RowResultBinding

class ResultsAdapter(
    private val onClick: (() -> Unit)? = null
) : ListAdapter<ResultItem, ResultsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ResultItem>() {
        override fun areItemsTheSame(oldItem: ResultItem, newItem: ResultItem) =
            oldItem.label == newItem.label

        override fun areContentsTheSame(oldItem: ResultItem, newItem: ResultItem) =
            oldItem == newItem
    }

    class VH(val binding: RowResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.tvLabel.text = item.label
        holder.binding.tvValue.text = item.value

        holder.binding.root.setOnClickListener { onClick?.invoke() }

    }

    fun clearResults() = submitList(emptyList())


}
