package com.example.landnv4

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.landnv4.ui.inputs.RequiredInputsViewDialog
import com.google.android.material.appbar.MaterialToolbar

abstract class BaseActivity : AppCompatActivity() {

    protected fun setupToolbar(
        title: String,
        showBack: Boolean = true
    ) {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(showBack)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            // Back arrow
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            // Home button
            R.id.action_home -> {
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                startActivity(intent)
                true
            }

            // Required Inputs
            R.id.action_required_inputs -> {
                openRequiredInputs()
                true
            }

            // Dark / Light mode toggle
            R.id.action_theme -> {
                toggleTheme()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    protected open fun openRequiredInputs() {
        // Default behavior: open activity
        RequiredInputsViewDialog().show(supportFragmentManager, "RequiredInputsView")
        // If you want a dialog instead, override this in specific activities

    }

    private fun toggleTheme() {
        val current = AppCompatDelegate.getDefaultNightMode()
        val next = if (current == AppCompatDelegate.MODE_NIGHT_YES)
            AppCompatDelegate.MODE_NIGHT_NO
        else
            AppCompatDelegate.MODE_NIGHT_YES

        AppCompatDelegate.setDefaultNightMode(next)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.action_theme)
        val night = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        item.title = if (night) "Light Mode" else "Dark Mode"
        return super.onPrepareOptionsMenu(menu)
    }


}
