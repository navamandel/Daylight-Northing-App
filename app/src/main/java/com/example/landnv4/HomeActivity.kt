package com.example.landnv4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        findViewById<Button>(R.id.btn_daylight).setOnClickListener {
            startActivity(Intent(this, DaylightActivity::class.java))
        }
        findViewById<Button>(R.id.btn_northing).setOnClickListener {
            startActivity(Intent(this, NorthingStarMapActivity::class.java))
        }
    }
}