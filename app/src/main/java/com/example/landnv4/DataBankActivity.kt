package com.example.landnv4

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.landnv4.databank.BankType

class DataBankActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_bank)

        setupToolbar("Data Bank")

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
        }
    }

    private fun openList(type: BankType) {
        startActivity(
            Intent(this, PointsListActivity::class.java)
                .putExtra(PointsListActivity.EXTRA_BANK_TYPE, type.name)
        )
    }


}
