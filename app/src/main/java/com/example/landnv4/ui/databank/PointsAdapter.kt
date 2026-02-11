package com.example.landnv4.ui.databank


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.landnv4.data.db.infobank.HeightConverters.toPrettyString
import com.example.landnv4.databinding.RowPointItemBinding

class PointsAdapter(
    private val onClick: (PointItem) -> Unit,
    private val onDelete: (PointItem) -> Unit
) : ListAdapter<PointItem, PointsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<PointItem>() {
        override fun areItemsTheSame(oldItem: PointItem, newItem: PointItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PointItem, newItem: PointItem) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowPointItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val binding: RowPointItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PointItem) {
            binding.tvName.text = item.name
            binding.tvUtm.text = "UTM: E=${item.utm.easting}, N=${item.utm.northing}"
            binding.tvLocation.text = "Location: ${item.location}"
            binding.tvHeight.text = "Height: ${item.height} ${item.heightType.toPrettyString()}"

            binding.root.setOnClickListener { onClick(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }
}
