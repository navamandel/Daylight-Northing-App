package com.example.landnv4


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landnv4.databank.BankType

import com.example.landnv4.databinding.ActivityPointsListBinding
import com.example.landnv4.ui.databank.PointItem
import com.example.landnv4.ui.databank.PointsAdapter
import com.example.landnv4.ui.databank.PointsListViewModel
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener


class PointsListActivity : BaseActivity() {

    companion object {
        const val EXTRA_BANK_TYPE = "bank_type"
    }

    private lateinit var binding: ActivityPointsListBinding
    private val vm: PointsListViewModel by viewModels()
    private lateinit var type: BankType
    private var fullList: List<PointItem> = emptyList()
    private var currentQuery: String = ""


    private val adapter = PointsAdapter(
        onClick = { item -> openEdit(item) },
        onDelete = { item -> vm.delete(type, item) }
    )

    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // No need to manually refresh because Flow updates automatically.
            // This is here if you later want snackbars, etc.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPointsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        type = BankType.valueOf(intent.getStringExtra(EXTRA_BANK_TYPE) ?: BankType.ANCHORING.name)

        setupToolbar(type.title())

        binding.rvPoints.layoutManager = LinearLayoutManager(this)
        binding.rvPoints.adapter = adapter

        binding.etSearch.addTextChangedListener { text ->
            currentQuery = text?.toString().orEmpty()
            applyFilter()
        }



        val raw = intent.getStringExtra(EXTRA_BANK_TYPE)
        type = runCatching { BankType.valueOf(raw ?: BankType.ANCHORING.name) }
            .getOrElse { BankType.ANCHORING }


        binding.fabAdd.setOnClickListener {
            openEdit(null)
        }



        /*lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.pointsFlow(type).collect { list ->
                    adapter.submitList(list)
                }
            }
        }*/

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.pointsFlow(type).collect { list ->
                    fullList = list
                    applyFilter()
                }
            }
        }


    }



    private fun openEdit(existing: PointItem?) {
        val i = Intent(this, PointEditActivity::class.java)
            .putExtra(PointEditActivity.EXTRA_BANK_TYPE, type.name)

        if (existing != null) {
            i.putExtra(PointEditActivity.EXTRA_ID, existing.id)
            i.putExtra(PointEditActivity.EXTRA_UTM_E, existing.utm.easting)
            i.putExtra(PointEditActivity.EXTRA_UTM_N, existing.utm.northing)
            i.putExtra(PointEditActivity.EXTRA_UTM_Z, existing.utm.zone)
            i.putExtra(PointEditActivity.EXTRA_UTM_H, existing.utm.hemisphereNorth)
            i.putExtra(PointEditActivity.EXTRA_LOCATION, existing.location)
            i.putExtra(PointEditActivity.EXTRA_HEIGHT, existing.height)
            i.putExtra(PointEditActivity.EXTRA_HEIGHT_TYPE, existing.heightType.name)
            i.putExtra(PointEditActivity.EXTRA_NAME, existing.name)
        }

        editLauncher.launch(i)
    }

    private fun applyFilter() {
        if (currentQuery.isBlank()) {
            adapter.submitList(fullList)
            return
        }

        val q = currentQuery.trim().lowercase()

        val filtered = fullList.sortedWith(
            compareByDescending<PointItem> {
                it.name.lowercase().startsWith(q)
            }.thenBy {
                it.name.lowercase()
            }
        ).filter { item ->
            item.name.lowercase().startsWith(q) ||
                    item.name.contains(q, ignoreCase = true) ||
                    item.utm.toString().contains(q, ignoreCase = true) ||
                    item.location.contains(q, ignoreCase = true) ||
                    item.height.toString().contains(q)
        }

        adapter.submitList(filtered)
    }

}
