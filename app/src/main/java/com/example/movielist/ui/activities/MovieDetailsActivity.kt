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
import com.example.movielist.ui.viewmodels.MovieDetailsActivityViewModel
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayoutMediator

class MovieDetailsActivity : AppCompatActivity() {
    companion object {
        const val MOVIE_ID = "com.example.movielist.movie-id"
    }

    private var _binding: ActivityMovieDetailsBinding? = null
    private val binding: ActivityMovieDetailsBinding
        get() = _binding!!

    private lateinit var viewModel: MovieDetailsActivityViewModel

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
        setSupportActionBar(binding.toolbar)
        binding.imagesViewPager.adapter =
            MovieImagesPagerAdapter(viewModel.images.value ?: emptyList())

        TabLayoutMediator(binding.tabLayout, binding.imagesViewPager) { tab, _ ->
            tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_circle, null)
        }.attach()

        observeViewmodel()
        viewModel.load(intent?.extras?.getInt(MOVIE_ID) ?: 0)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeViewmodel() {
        viewModel.isLoading.observe(this) {
            binding.loadingIndicator.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.details.observe(this) {
            binding.collapsingLayout.title = it.title
            binding.averageVoteText.text = it.voteAverage.toString()
            binding.releaseDateText.text = it.releaseData
            binding.overviewText.text = it.overview
        }

        viewModel.images.observe(this) {
            (binding.imagesViewPager.adapter as? MovieImagesPagerAdapter)?.apply {
                images = it
                notifyDataSetChanged()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}