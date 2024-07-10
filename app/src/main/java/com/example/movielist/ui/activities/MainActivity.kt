package com.example.movielist.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
    private var ignoreSearchViewTextChangeCallback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        searchViewModel = ViewModelProvider(this)[SearchFragmentViewModel::class.java]
        setSupportActionBar(binding.searchbar)

        if (!checkApiKey()) {
            return
        }

        ignoreSearchViewTextChangeCallback = savedInstanceState != null
        setupSearchView()
        setBackHandler()
        observeSearchViewModel()
        prepareFragments()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        backHandler.isEnabled = binding.searchView.isShowing
        ignoreSearchViewTextChangeCallback = false
    }

    private fun checkApiKey(): Boolean {
        @Suppress("KotlinConstantConditions")
        if (BuildConfig.MOVIES_API_KEY.isEmpty() || BuildConfig.MOVIES_API_KEY == "null") {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.api_key_error)
                .show()
            return false
        }
        return true
    }

    private fun setupSearchView() {
        val searchBarListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString() ?: ""

                if (ignoreSearchViewTextChangeCallback) {
                    searchViewModel.findKeywords(text)
                    return
                }

                with(searchViewModel) {
                    hideResults()
                    showSuggestions()
                    findKeywords(text)
                }
            }
        }
        with(binding.searchView) {
            setupWithSearchBar(binding.searchbar)
            with(editText) {
                layoutDirection = View.LAYOUT_DIRECTION_LTR
                textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                addTextChangedListener(searchBarListener)
                setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_SEARCH,
                        EditorInfo.IME_NULL -> {
                            onSearchQuerySubmit()
                            return@setOnEditorActionListener true
                        }
                    }
                    false
                }
                setOnClickListener {
                    with(searchViewModel) {
                        clearItemsList()
                        hideResults()
                        showSuggestions()
                    }
                }
            }
            addTransitionListener { _, _, newState ->
                if (newState === TransitionState.SHOWING) {
                    backHandler.isEnabled = true
                } else if (newState === TransitionState.HIDING) {
                    backHandler.isEnabled = false
                    // clear last suggestions list
                    searchViewModel.findKeywords("")
                }
            }
        }

        val keywords = searchViewModel.matchedKeywords.value?.toMutableList()
            ?: emptyList<Keyword>().toMutableList()
        binding.keywordsList.adapter =
            KeywordsRecyclerViewAdapter(keywords).apply {
                onItemClick = { k -> onSearchSuggestionClick(k) }
            }

        with(searchViewModel) {
            binding.searchSuggestionsView.isVisible = isSuggestionsVisible.value ?: false
            binding.searchResultsView.isVisible = isResultsVisible.value ?: false
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
            if (searchViewModel.isSuggestionsVisible.value == false) {
                with(searchViewModel) {
                    clearItemsList()
                    hideResults()
                    showSuggestions()
                }
            } else {
                binding.searchView.hide()
                backHandler.isEnabled = false
            }
        }
    }

    private fun observeSearchViewModel() {
        searchViewModel.matchedKeywords.observe(this, this::onSearchSuggestionsChanged)
        searchViewModel.selectedKeywordsNames.observe(this) { names ->
            binding.searchKeywordsInfo.text = getString(R.string.search_keywords_info, names)
        }
        searchViewModel.isSuggestionsVisible.observe(this) {
            binding.searchSuggestionsView.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
        searchViewModel.isResultsVisible.observe(this) {
            binding.searchResultsView.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
        searchViewModel.isLoadingMatchedKeywords.observe(this) {
            binding.suggestionsLoadingBar.isVisible = it
        }
    }

    private fun onSearchSuggestionsChanged(newList: List<Keyword>?) {
        if (newList == null)
            return

        val adapter = binding.keywordsList.adapter as? KeywordsRecyclerViewAdapter
        adapter?.apply {
            val previousCount = keywords.size
            val newCount = newList.size
            if (newList.isNotEmpty()) {
                keywords = newList.toMutableList()
                if (previousCount > newCount) {
                    // in this case changeCount and removeStartPosition both equals the value of newCount
                    notifyItemRangeChanged(0, /*changeCount*/ newCount)
                    val removedCount = previousCount - newCount
                    notifyItemRangeRemoved(/*removeStartPosition*/ newCount, removedCount)
                } else if (previousCount < newCount) {
                    // in this case changeCount and insertStartPosition both equals the value of previousCount
                    notifyItemRangeChanged(0, /*changeCount*/ previousCount)
                    val insertedCount = newCount - previousCount
                    notifyItemRangeInserted(/*insertStartPosition*/ previousCount, insertedCount)
                } else {
                    notifyItemRangeChanged(0, previousCount)
                }
            } else {
                keywords.clear()
                notifyItemRangeRemoved(0, previousCount)
            }
        }
    }

    private fun onSearchSuggestionClick(keyword: Keyword): Boolean {
        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        with(searchViewModel) {
            clearItemsList()
            hideSuggestions()
            showResults()
            selectKeyword(keyword)
        }
        return true
    }

    private fun onSearchQuerySubmit() {
        val keywords = searchViewModel.matchedKeywords.value
        if (searchViewModel.isLoadingMatchedKeywords.value == true || keywords.isNullOrEmpty())
            return

        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        with(searchViewModel) {
            clearItemsList()
            hideSuggestions()
            showResults()
            selectKeyword(*keywords.toTypedArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}