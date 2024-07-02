package com.example.movielist.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.movielist.R
import com.example.movielist.databinding.FragmentMainBinding
import com.example.movielist.ui.adapters.MainActivityPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        MainActivityPagerAdapter(childFragmentManager, lifecycle).let {
            binding.pager.adapter = it
        }
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, i ->
            when (i) {
                0 -> tab.text = getString(R.string.now_playing)
                1 -> tab.text = getString(R.string.popular)
                2 -> tab.text = getString(R.string.top_rated)
                3 -> tab.text = getString(R.string.Upcoming)
            }
        }.attach()

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}