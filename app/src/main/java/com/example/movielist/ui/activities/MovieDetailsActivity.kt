package com.example.movielist.ui.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.movielist.R
import com.example.movielist.databinding.ActivityMovieDetailsBinding
import com.example.movielist.ui.adapters.MovieImagesPagerAdapter
import com.example.movielist.ui.viewmodels.ErrorType
import com.example.movielist.ui.viewmodels.MovieDetailsActivityViewModel
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieDetailsActivity : AppCompatActivity() {
    companion object {
        const val MOVIE_ID = "com.example.movielist.movie-id"
    }

    private var _binding: ActivityMovieDetailsBinding? = null
    private val binding: ActivityMovieDetailsBinding
        get() = _binding!!

    private lateinit var viewModel: MovieDetailsActivityViewModel
    private var errorCollectingJob: Job? = null

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
        _binding = ActivityMovieDetailsBinding.inflate(LayoutInflater.from(this))
        viewModel = ViewModelProvider(this)[MovieDetailsActivityViewModel::class]
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.imagesViewPager.adapter =
            MovieImagesPagerAdapter(viewModel.images.value ?: emptyList())

        TabLayoutMediator(binding.tabLayout, binding.imagesViewPager) { tab, _ ->
            tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_circle, null)
        }.attach()

        observeViewmodel()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeViewmodel() {
        viewModel.isLoading.observe(this) {
            binding.loadingIndicator.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.details.observe(this) {
            if (it == null)
                return@observe

            binding.title.text = it.title
            binding.averageVote.text = it.voteAverage.toString()
            binding.releaseDate.text = it.releaseData
            binding.overview.text = it.overview
        }

        viewModel.images.observe(this) {
            (binding.imagesViewPager.adapter as? MovieImagesPagerAdapter)?.apply {
                images = it
                notifyDataSetChanged()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.images.value?.isEmpty() == true || viewModel.details.value == null) {
            viewModel.load(intent?.extras?.getInt(MOVIE_ID) ?: 0)
        }
        collectErrors()
    }

    override fun onStop() {
        super.onStop()
        errorCollectingJob?.cancel()
        errorCollectingJob = null
    }
}