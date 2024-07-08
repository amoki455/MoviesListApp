package com.example.movielist.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.movielist.BuildConfig
import com.example.movielist.R
import com.example.movielist.data.models.Keyword
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.databinding.ActivityMainBinding
import com.example.movielist.ui.adapters.KeywordsRecyclerViewAdapter
import com.example.movielist.ui.fragments.MainFragment
import com.example.movielist.ui.fragments.MoviesListFragment
import com.example.movielist.ui.viewmodels.SearchFragmentViewModel
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.search.SearchView.TransitionState


class MainActivity : AppCompatActivity() {

    companion object {
        private const val SEARCH_FRAGMENT_TAG = "search-fragment"
        private const val MAIN_FRAGMENT_TAG = "main-fragment"
    }

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private lateinit var backHandler: OnBackPressedCallback
    private lateinit var searchViewModel: SearchFragmentViewModel

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
        searchViewModel = ViewModelProvider(this)[SearchFragmentViewModel::class.java]

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.searchbar)

        if (!checkApiKey()) {
            return
        }

        setupSearchView()
        setBackHandler()
        observeSearchSuggestions()
        prepareFragments()
    }

    private fun checkApiKey(): Boolean {
        if (BuildConfig.MOVIES_API_KEY.isEmpty() || BuildConfig.MOVIES_API_KEY == "null") {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.api_key_error)
                .show()
            return false
        }
        return true
    }

    private fun setupSearchView() {
        with(binding.searchView) {
            setupWithSearchBar(binding.searchbar)
            with(editText) {
                layoutDirection = View.LAYOUT_DIRECTION_LTR
                textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                addTextChangedListener(searchBarListener)
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
                        onSearchQuerySubmit()
                        return@setOnEditorActionListener true
                    }
                    false
                }
                setOnClickListener {
                    binding.searchSuggestionsView.visibility = View.VISIBLE
                }
            }
            addTransitionListener { _, _, newState ->
                if (newState === TransitionState.SHOWING) {
                    backHandler.isEnabled = true
                } else if (newState === TransitionState.HIDING) {
                    backHandler.isEnabled = false
                }
            }
        }
        binding.keywordsList.adapter =
            KeywordsRecyclerViewAdapter(emptyList<Keyword>().toMutableList()).apply {
                onItemClick = { k -> onSearchSuggestionClick(k) }
            }
    }

    private fun prepareFragments() {
        if (supportFragmentManager.findFragmentByTag(MAIN_FRAGMENT_TAG) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, MainFragment::class.java, null, MAIN_FRAGMENT_TAG)
                .commit()
        }

        if (supportFragmentManager.findFragmentByTag(SEARCH_FRAGMENT_TAG) == null) {
            val fragment = MoviesListFragment.newInstance(MovieCategory.UNCATEGORIZED)
            supportFragmentManager.beginTransaction()
                .replace(R.id.search_fragment_frame, fragment, SEARCH_FRAGMENT_TAG)
                .commit()
        }
    }

    private fun setBackHandler() {
        val isEnabled = binding.searchView.isShowing
        backHandler = onBackPressedDispatcher.addCallback(this, isEnabled) {
            binding.searchView.hide()
            backHandler.isEnabled = false
        }
    }

    private fun observeSearchSuggestions() {
        searchViewModel.matchedKeywords.observe(this) { newList ->
            if (newList == null)
                return@observe

            val adapter = binding.keywordsList.adapter as? KeywordsRecyclerViewAdapter
            adapter?.apply {
                val previousCount = keywords.size
                if (newList.isNotEmpty()) {
                    keywords.addAll(newList)
                    notifyItemRangeInserted(previousCount, newList.size)
                    keywords.removeAll(keywords.subList(0, previousCount))
                    notifyItemRangeRemoved(0, previousCount)
                } else {
                    keywords.clear()
                    notifyItemRangeRemoved(0, previousCount)
                }
            }
        }

        searchViewModel.isLoadingMatchedKeywords.observe(this) { state ->
            binding.suggestionsLoadingBar.visibility = if (state) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private val searchBarListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.searchSuggestionsView.visibility = View.VISIBLE
            val text = s?.toString()
            if (text?.isNotEmpty() == true) {
                searchViewModel.findKeywords(text)
            } else {
                searchViewModel.clearMatchedKeywords()
            }
        }
    }

    private fun onSearchSuggestionClick(keyword: Keyword): Boolean {
        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        binding.searchSuggestionsView.visibility = View.GONE
        searchViewModel.selectKeyword(keyword)
        displayCurrentSearchInfo(listOf(keyword))
        return true
    }

    private fun onSearchQuerySubmit() {
        val keywords = searchViewModel.matchedKeywords.value
        if (searchViewModel.isLoadingMatchedKeywords.value == true || keywords.isNullOrEmpty())
            return

        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        binding.searchSuggestionsView.visibility = View.GONE
        searchViewModel.selectKeyword(*keywords.toTypedArray())
        displayCurrentSearchInfo(keywords)
    }

    private fun displayCurrentSearchInfo(keywords: List<Keyword>) {
        val keywordsText = keywords.fold("") { acc, keyword ->
            "${keyword.name}, $acc"
        }
        binding.searchKeywordsInfo.text = getString(R.string.search_keywords_info, keywordsText)
    }
}