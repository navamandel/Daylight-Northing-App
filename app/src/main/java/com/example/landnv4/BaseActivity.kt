package com.example.landnv4

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.landnv4.ui.inputs.RequiredInputsViewDialog
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.view.updatePadding
import com.example.landnv4.databinding.ActivityBaseBinding

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var contentContainer: FrameLayout
    protected lateinit var contentRoot: View
    private lateinit var baseBinding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge: content can draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        baseBinding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(baseBinding.root)

        // Inflate child content into the container
        contentContainer = findViewById(R.id.content_container)
        contentRoot = layoutInflater.inflate(getLayoutResId(), contentContainer, true)
        setupToolbarInternal()
    }

    @LayoutRes
    protected abstract fun getLayoutResId(): Int

    protected fun setupToolbar(title: String, showBack: Boolean = true) {
        val toolbar = baseBinding.includeToolbar.toolbar
        toolbar.title = title

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(showBack)

        if (showBack) {
            toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        } else {
            toolbar.navigationIcon = null
        }
    }

    private fun setupToolbarInternal() {
        val toolbar = baseBinding.includeToolbar.toolbar

        // IMPORTANT:
        // Apply ONLY status bar inset to the toolbar (top padding),
        // so no overlap, no gap, and no cut-off.
        val appBar = findViewById<View>(R.id.appbar) // give your AppBarLayout an id
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = top)
            insets
        }
    }


    protected fun setupToolbarr(
        title: String,
        showBack: Boolean = true
    ) {
        //WindowCompat.setDecorFitsSystemWindows(window, true)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        /*ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.updatePadding(top = top)
            insets
        }*/


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
