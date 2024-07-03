package com.example.movielist.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MoviesListFragment : Fragment() {

    private var _binding: FragmentMoviesListBinding? = null
    private val binding: FragmentMoviesListBinding
        get() = _binding!!

    private var columnCount = 2
    private var category: MovieCategory = MovieCategory.UNCATEGORIZED
    private lateinit var viewModel: BaseListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
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
        collectErrors()
        return binding.root
    }

    private fun updateList(list: List<Movie>) {
        if (list.isNotEmpty() && binding.loadingIndicator.visibility != View.GONE) {
            binding.loadingIndicator.visibility = View.GONE
        }

        if (binding.list.adapter == null) {
            with(binding.list) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = MoviesRecyclerViewAdapter(list).apply {
                    onReachedLastItem = {
                        viewModel.loadNextPage()
                    }
                }
            }
        } else {
            (binding.list.adapter as? MoviesRecyclerViewAdapter)?.notifyDataSetChanged()
        }
    }

    private fun observeViewModel() {
        viewModel.items.observe(viewLifecycleOwner, this::updateList)
    }

    private fun collectErrors() {
        CoroutineScope(Dispatchers.Default).launch {
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

    override fun onStart() {
        super.onStart()
        if (viewModel.items.value?.isEmpty() == true) {
            viewModel.loadNextPage()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"
        private const val ARG_CATEGORY = "category"

        @JvmStatic
        fun newInstance(columnCount: Int, category: MovieCategory) =
            MoviesListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    putInt(ARG_CATEGORY, category.ordinal)
                }
            }
    }
}