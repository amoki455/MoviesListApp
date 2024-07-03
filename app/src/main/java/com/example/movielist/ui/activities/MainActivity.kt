package com.example.movielist.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.movielist.R
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.databinding.ActivityMainBinding
import com.example.movielist.ui.fragments.MainFragment
import com.example.movielist.ui.fragments.MoviesListFragment
import com.google.android.material.color.MaterialColors

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    companion object {
        private const val SEARCH_FRAGMENT_TAG = "search-fragment"
        private const val MAIN_FRAGMENT_TAG = "main-fragment"
        private const val STATE_SUBTITLE = "state-subtitle"
    }

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private lateinit var backHandler: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            SystemBarStyle.dark(
                MaterialColors.getColor(
                    this,
                    R.attr.toolbar_color,
                    Color.rgb(34, 34, 43)
                )
            ),
            SystemBarStyle.dark(
                MaterialColors.getColor(
                    this,
                    R.attr.background_color,
                    Color.rgb(42, 43, 54)
                )
            )
        )

        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.main_title)

        val isBackHandlerEnabled =
            supportFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG) != null
        backHandler = onBackPressedDispatcher.addCallback(this, isBackHandlerEnabled) {
            supportActionBar?.title = getString(R.string.main_title)
            supportActionBar?.subtitle = ""
            supportFragmentManager.popBackStack()
            backHandler.isEnabled = false
        }
        prepareFragments()
    }

    private fun prepareFragments() {
        // add main fragment if there are not any. usually in first start of the activity.
        if (supportFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG) == null
            && supportFragmentManager.findFragmentByTag(MAIN_FRAGMENT_TAG) == null
        ) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, MainFragment::class.java, null, MAIN_FRAGMENT_TAG)
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        menu?.findItem(R.id.menu_search_item)?.let {
            (it.actionView as SearchView).apply {
                setOnQueryTextListener(this@MainActivity)
                isSubmitButtonEnabled = true
                maxWidth = 1900
                layoutDirection = View.LAYOUT_DIRECTION_LTR
                (findViewById(androidx.appcompat.R.id.search_src_text) as? EditText)?.apply {
                    setTextColor(Color.WHITE)
                    setHintTextColor(Color.WHITE)
                }
            }
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_SUBTITLE, supportActionBar?.subtitle?.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (supportFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG) != null) {
            supportActionBar?.title = getString(R.string.search_results)
            supportActionBar?.subtitle = savedInstanceState.getString(STATE_SUBTITLE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query == null)
            return true

        var fragment: MoviesListFragment? =
            supportFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG) as? MoviesListFragment

        if (fragment == null) {
            fragment = MoviesListFragment.newInstance(MovieCategory.UNCATEGORIZED)
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.main_frame,
                    fragment,
                    SEARCH_FRAGMENT_TAG
                )
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .addToBackStack(null)
                .commit()
            backHandler.isEnabled = true
            supportActionBar?.title = getString(R.string.search_results)
        }
        fragment.search(query)
        supportActionBar?.subtitle = query
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
}