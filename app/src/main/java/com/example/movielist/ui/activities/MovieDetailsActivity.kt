package com.example.movielist.ui.activities

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.movielist.R
import com.example.movielist.ThisApp
import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieDetails
import com.example.movielist.data.models.MovieImage
import com.example.movielist.databinding.ActivityMovieDetailsBinding
import com.example.movielist.databinding.GenreChipBinding
import com.example.movielist.ui.adapters.MovieImagesPagerAdapter
import com.example.movielist.ui.viewmodels.ErrorType
import com.example.movielist.ui.viewmodels.MovieDetailsActivityViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieDetailsActivity : AppCompatActivity() {
    companion object {
        const val MOVIE = "com.example.movielist.movie"
    }

    private var _binding: ActivityMovieDetailsBinding? = null
    private val binding: ActivityMovieDetailsBinding
        get() = _binding!!

    private lateinit var viewModel: MovieDetailsActivityViewModel
    private var errorCollectingJob: Job? = null
    private var movie: Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMovieDetailsBinding.inflate(LayoutInflater.from(this))
        viewModel = ViewModelProvider(this)[MovieDetailsActivityViewModel::class]
        setContentView(binding.root)

        movie = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.extras?.getParcelable(MOVIE, Movie::class.java)
        } else {
            intent?.extras?.getParcelable(MOVIE) as? Movie
        }

        Glide.with(binding.initialImage)
            .load(ThisApp.createImageUrl(movie?.posterPath ?: ""))
            .placeholder(R.drawable.image_placeholder)
            .into(binding.initialImage)

        binding.imagesViewPager.adapter = MovieImagesPagerAdapter().apply {
            onLoadedFirstImage = {
                _binding?.let {
                    it.initialImage.isVisible = false
                }
            }
        }
    }

    override fun onEnterAnimationComplete() {
        startUiDrawing()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        startUiDrawing()
    }

    private fun startUiDrawing() {
        fillViewData()
        observeViewmodel()

        // delay TabLayoutMediator because it causes animation lag when there is large number of images
        CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            _binding?.let {
                TabLayoutMediator(it.tabLayout, it.imagesViewPager) { tab, _ ->
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_circle, null)
                }.attach()
            }
        }
    }

    private fun fillViewData() {
        if (viewModel.isLoading.value == true || viewModel.details.value == null) {
            movie?.let {
                _binding?.let { it2 ->
                    with(it2) {
                        title.text = it.title
                        overview.text = it.overview
                        releaseDate.text = it.releaseData
                        averageVote.text = it.voteAverage.toString()
                    }
                }
            }
        }
    }

    private fun observeViewmodel() {
        viewModel.details.observe(this, this::onDetailsLoaded)
        viewModel.images.observe(this, this::onImagesLoaded)

        viewModel.isLoading.observe(this) {
            binding.loadingIndicator.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.productionCompaniesString.observe(this) {
            binding.prodCompanies.text = it
        }
    }

    private fun onDetailsLoaded(details: MovieDetails?) {
        if (details == null)
            return

        binding.title.text = details.title
        binding.averageVote.text = details.voteAverage.toString()
        binding.releaseDate.text = details.releaseData
        binding.overview.text = details.overview
        binding.budget.text = details.budget.toString()
        binding.revenue.text = details.revenue.toString()
        addGenresToView(details)
    }

    private fun onImagesLoaded(newList: List<MovieImage>) {
        (binding.imagesViewPager.adapter as? MovieImagesPagerAdapter)
            ?.submitList(newList)
    }

    private fun addGenresToView(details: MovieDetails) {
        _binding?.let {
            details.genres?.forEach { genre ->
                val chip = GenreChipBinding.inflate(LayoutInflater.from(this)).root
                with(chip) {
                    text = genre.name
                    id = View.generateViewId()
                }
                it.genresLayout.addView(chip)
                it.genresFlow.addView(chip)
            }
        }
    }

    private fun collectErrors() {
        if (errorCollectingJob == null) {
            errorCollectingJob = CoroutineScope(Dispatchers.Default).launch {
                viewModel.errorEvents.collect {
                    withContext(Dispatchers.Main) {
                        _binding?.root?.let { v ->
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        movie?.let {
            if (viewModel.images.value?.isEmpty() == true || viewModel.details.value == null) {
                viewModel.load(it)
            }
        }

        collectErrors()
    }

    override fun onStop() {
        super.onStop()
        errorCollectingJob?.cancel()
        errorCollectingJob = null
    }
}