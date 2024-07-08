package com.example.movielist.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielist.R
import com.example.movielist.data.models.Keyword
import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.databinding.FragmentMoviesListBinding
import com.example.movielist.ui.activities.MovieDetailsActivity
import com.example.movielist.ui.adapters.MoviesRecyclerViewAdapter
import com.example.movielist.ui.viewmodels.BaseListViewModel
import com.example.movielist.ui.viewmodels.ErrorType
import com.example.movielist.ui.viewmodels.NowPlayingFragmentViewModel
import com.example.movielist.ui.viewmodels.PopularFragmentViewModel
import com.example.movielist.ui.viewmodels.SearchFragmentViewModel
import com.example.movielist.ui.viewmodels.TopRatedFragmentViewModel
import com.example.movielist.ui.viewmodels.UpcomingFragmentViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MoviesListFragment : Fragment() {

    private var _binding: FragmentMoviesListBinding? = null
    private val binding: FragmentMoviesListBinding
        get() = _binding!!

    private var category: MovieCategory = MovieCategory.UNCATEGORIZED
    private lateinit var viewModel: BaseListViewModel<Movie>

    private var errorCollectingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            category = MovieCategory.entries[it.getInt(ARG_CATEGORY)]
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoviesListBinding.inflate(inflater, container, false)
        viewModel = when (category) {
            MovieCategory.UNCATEGORIZED -> ViewModelProvider(requireActivity())[SearchFragmentViewModel::class.java]
            MovieCategory.NOW_PLAYING -> ViewModelProvider(this)[NowPlayingFragmentViewModel::class.java]
            MovieCategory.POPULAR -> ViewModelProvider(this)[PopularFragmentViewModel::class.java]
            MovieCategory.TOP_RATED -> ViewModelProvider(this)[TopRatedFragmentViewModel::class.java]
            MovieCategory.UPCOMING -> ViewModelProvider(this)[UpcomingFragmentViewModel::class.java]
        }
        observeViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (binding.list.adapter == null) {
            with(binding.list) {
                val columnsCount = calculateColumnsCount()
                layoutManager = when {
                    columnsCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnsCount)
                }
                adapter = MoviesRecyclerViewAdapter(viewModel.items).apply {
                    onReachedLastItem = {
                        viewModel.loadNextPage()
                    }
                    onItemClick = { item ->
                        Intent(requireActivity(), MovieDetailsActivity::class.java)
                            .putExtra(MovieDetailsActivity.MOVIE_ID, item.id)
                            .let { startActivity(it) }
                    }
                }
            }
        }

        if (viewModel.items.isNotEmpty()) {
            binding.loadingIndicator.visibility = View.GONE
        }
    }

    private fun onReceiveNewItems(items: List<Movie>?) {
        if (items == null)
            return

        binding.loadingIndicator.visibility = View.GONE
        viewModel.removeLoadingItem().let { index ->
            if (index != -1)
                binding.list.adapter?.notifyItemRemoved(index)
        }
        viewModel.items.size.let { startPosition ->
            viewModel.consumeNewItems()
            if (items.isNotEmpty()) {
                val count = items.size + 1 // +1 for the loading item
                binding.list.adapter?.notifyItemRangeInserted(startPosition, count)
            }
        }
    }

    private fun onLoadingFirstPage(state: Boolean) {
        if (state) {
            binding.loadingIndicator.visibility = View.VISIBLE
            binding.noResultsText.visibility = View.GONE
        }
    }

    private fun onNoResults(state: Boolean) {
        binding.noResultsText.visibility = if (state) View.VISIBLE else View.GONE
        if (state)
            binding.loadingIndicator.visibility = View.GONE
    }

    private fun onSelectedKeywordsChanged(keywords: List<Keyword>) {
        if(viewModel !is SearchFragmentViewModel || keywords.isEmpty())
            return

        val itemsCount = viewModel.items.size
        if (itemsCount > 0) {
            viewModel.clearItemsList()
            binding.list.adapter?.notifyItemRangeRemoved(0, itemsCount)
        }
        viewModel.reload()
    }

    private fun observeViewModel() {
        viewModel.newItems.observe(viewLifecycleOwner, this::onReceiveNewItems)
        viewModel.isLoadingFirstPage.observe(viewLifecycleOwner, this::onLoadingFirstPage)
        viewModel.noResults.observe(viewLifecycleOwner, this::onNoResults)

        if (category == MovieCategory.UNCATEGORIZED) {
            (viewModel as? SearchFragmentViewModel)?.apply {
                selectedKeywords.observe(
                    viewLifecycleOwner,
                    this@MoviesListFragment::onSelectedKeywordsChanged
                )
            }
        }
    }

    private fun collectErrors() {
        if (errorCollectingJob == null) {
            errorCollectingJob = CoroutineScope(Dispatchers.Default).launch {
                viewModel.errorEvents.collect {
                    withContext(Dispatchers.Main) {
                        this@MoviesListFragment.view?.let { v ->
                            val errorMsg = when (it) {
                                ErrorType.IO -> getString(R.string.error_io)
                                ErrorType.JSON_PARSE -> getString(R.string.error_parsing_data)
                                ErrorType.UNKNOWN -> getString(R.string.error_unknwon)
                            }
                            Snackbar.make(v, errorMsg, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun calculateColumnsCount(): Int {
        resources.displayMetrics?.let {
            it.widthPixels.div(it.density).let { widthDp ->
                val calculatedCount = widthDp.toInt().div(159) // item width in dp
                return if (calculatedCount > 1) calculatedCount else 1
            }
        }
        return 1
    }

    override fun onStart() {
        super.onStart()
        // exclude UNCATEGORIZED to avoid displaying unintended NoResults message
        if (viewModel.items.isEmpty() && category != MovieCategory.UNCATEGORIZED) {
            viewModel.loadNextPage()
        }
        collectErrors()
    }

    override fun onStop() {
        super.onStop()
        errorCollectingJob?.cancel()
        errorCollectingJob = null
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        // dynamically changing columns count based on available space (for example in landscape mode)
        val columnsCount = calculateColumnsCount()
        if (columnsCount > 1) {
            val layoutManager = (binding.list.layoutManager as? GridLayoutManager)
            if (layoutManager == null) {
                binding.list.layoutManager = GridLayoutManager(context, columnsCount)
            } else {
                layoutManager.spanCount = columnsCount
            }
        } else {
            val layoutManager = (binding.list.layoutManager as? LinearLayoutManager)
            if (layoutManager == null) {
                binding.list.layoutManager = LinearLayoutManager(context)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {

        private const val ARG_CATEGORY = "category"

        @JvmStatic
        fun newInstance(category: MovieCategory) =
            MoviesListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CATEGORY, category.ordinal)
                }
            }
    }
}