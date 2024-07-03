package com.example.movielist.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.ui.fragments.MoviesListFragment

class MainActivityPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MoviesListFragment.newInstance(MovieCategory.NOW_PLAYING)
            1 -> MoviesListFragment.newInstance(MovieCategory.POPULAR)
            2 -> MoviesListFragment.newInstance(MovieCategory.TOP_RATED)
            3 -> MoviesListFragment.newInstance(MovieCategory.UPCOMING)
            else -> throw IndexOutOfBoundsException()
        }
    }
}