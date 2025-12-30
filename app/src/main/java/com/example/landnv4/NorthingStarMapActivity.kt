package com.example.landnv4

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.viewmodel.compose.viewModel
// import com.example.landnv4.StarMapScreen
import com.example.landnv4.ui.northing.starmap.StarMapViewModel
import com.example.landnv4.ui.theme.LAndNV4Theme   // change if needed

class NorthingStarMapActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_northingstarmap)

        findViewById<ComposeView>(R.id.composeView).setContent {
            LAndNV4Theme {
                val vm: StarMapViewModel = viewModel()
                StarMapScreen(vm)
            }
        }
    }
}
