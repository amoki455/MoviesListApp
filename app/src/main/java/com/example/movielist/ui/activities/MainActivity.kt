package com.example.movielist.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.movielist.R
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.databinding.ActivityMainBinding
import com.example.movielist.ui.adapters.MainActivityPagerAdapter
import com.example.movielist.ui.fragments.MainFragment
import com.example.movielist.ui.fragments.MoviesListFragment
import com.example.movielist.ui.viewmodels.SearchFragmentViewModel
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private lateinit var searchFragmentViewModel: SearchFragmentViewModel
    private val SEARCH_FRAGMENT_TAG = "search-fragment"
    private val MAIN_FRAGMENT_TAG = "main-fragment"

    private lateinit var backHandler: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.rgb(34, 34, 43)),
            SystemBarStyle.dark(Color.rgb(42, 43, 54))
        )

        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        searchFragmentViewModel = ViewModelProvider(this)[SearchFragmentViewModel::class.java]
        setSupportActionBar(binding.toolbar)

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame, MainFragment::class.java, null, MAIN_FRAGMENT_TAG)
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .commit()

        backHandler = onBackPressedDispatcher.addCallback(this, false) {
            Handler(mainLooper).post {
                supportFragmentManager.popBackStack()
            }
            backHandler.isEnabled = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        menu?.findItem(R.id.menu_search_item)?.let {
            (it.actionView as SearchView).apply {
                setOnQueryTextListener(this@MainActivity)
                isSubmitButtonEnabled = true
                maxWidth = 1900
                (findViewById(androidx.appcompat.R.id.search_src_text) as? EditText)?.let { editText ->
                    editText.setTextColor(Color.WHITE)
                    editText.setHintTextColor(Color.WHITE)
                }
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (supportFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG) == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.main_frame,
                    MoviesListFragment.newInstance(2, MovieCategory.UNCATEGORIZED),
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
        }
        searchFragmentViewModel.submitSearch(query ?: "")
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
}