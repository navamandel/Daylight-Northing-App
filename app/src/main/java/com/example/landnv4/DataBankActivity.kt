package com.example.landnv4

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.landnv4.databank.BankType
import com.example.landnv4.databinding.ActivityConverterBinding
import com.example.landnv4.databinding.ActivityDataBankBinding

class DataBankActivity : BaseActivity() {
    private lateinit var binding: ActivityDataBankBinding
    override fun getLayoutResId() = R.layout.activity_data_bank

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_data_bank)

        val root = contentContainer.getChildAt(0)
        binding = ActivityDataBankBinding.bind(root)

        setupToolbar("Data Bank")

        /*
        findViewById<View>(R.id.btnAnchoring).setOnClickListener {
            openList(BankType.ANCHORING)
        }
        findViewById<View>(R.id.btnNorthing).setOnClickListener {
            openList(BankType.NORTHING)
        }
        findViewById<View>(R.id.btnValidating).setOnClickListener {
            openList(BankType.VALIDATING)
        }
        findViewById<View>(R.id.btnTargets).setOnClickListener {
            openList(BankType.TARGETS)
        }*/

        binding.cardAnchoring.setOnClickListener { openList(BankType.ANCHORING) }
        binding.cardNorthing.setOnClickListener { openList(BankType.NORTHING) }
        binding.cardValidating.setOnClickListener { openList(BankType.VALIDATING) }
        binding.cardTargets.setOnClickListener { openList(BankType.TARGETS) }

    }

    private fun openList(type: BankType) {
        startActivity(
            Intent(this, PointsListActivity::class.java)
                .putExtra(PointsListActivity.EXTRA_BANK_TYPE, type.name)
        )
    }


}
