package com.example.movielist.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movielist.R
import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.databinding.FragmentMoviesListBinding
import com.example.movielist.ui.adapters.MoviesRecyclerViewAdapter
import com.example.movielist.ui.viewmodels.BaseListViewModel
import com.example.movielist.ui.viewmodels.ErrorType
import com.example.movielist.ui.viewmodels.NowPlayingFragmentViewModel
import com.example.movielist.ui.viewmodels.PopularFragmentViewModel
import com.example.movielist.ui.viewmodels.SearchFragmentViewModel
import com.example.movielist.ui.viewmodels.TopRatedFragmentViewModel
import com.example.movielist.ui.viewmodels.UpcomingFragmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private lateinit var viewModel: BaseListViewModel

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

    private fun updateList(list: List<Movie>) {
        if (list.isNotEmpty() && binding.loadingIndicator.visibility != View.GONE) {
            binding.loadingIndicator.visibility = View.GONE
        }

        if (binding.list.adapter == null) {
            with(binding.list) {
                val columnsCount = calculateColumnsCount()
                layoutManager = when {
                    columnsCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnsCount)
                }
                adapter = MoviesRecyclerViewAdapter(list).apply {
                    onReachedLastItem = {
                        viewModel.loadNextPage()
                    }
                }
            }
        } else {
            binding.list.adapter?.notifyDataSetChanged()
        }
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner, this::updateList)
        viewModel.isLoadingFirstPage.observe(viewLifecycleOwner) {
            if (it) {
                binding.loadingIndicator.visibility = View.VISIBLE
                binding.noResultsText.visibility = View.GONE
            }
        }
        // for search mode
        // we are sending two request, the first is to get a list of keywords based on the user input, The second is to search using those keywords.
        // The following code displays loading indicator for the first mentioned request
        if (category == MovieCategory.UNCATEGORIZED) {
            (viewModel as? SearchFragmentViewModel)?.let {
                it.isRequestingNewSearch.observe(viewLifecycleOwner) { state ->
                    if (state)
                        binding.loadingIndicator.visibility = View.VISIBLE
                }
            }
        }

        viewModel.noResults.observe(viewLifecycleOwner) {
            binding.noResultsText.visibility = if (it) View.VISIBLE else View.GONE
            if (it)
                binding.loadingIndicator.visibility = View.GONE
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
                            MaterialAlertDialogBuilder(v.context)
                                .setTitle(R.string.error)
                                .setMessage(errorMsg)
                                .show()
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
        if (viewModel.items.value?.isEmpty() == true) {
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