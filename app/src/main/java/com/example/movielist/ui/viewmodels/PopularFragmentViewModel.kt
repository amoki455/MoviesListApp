package com.example.movielist.ui.viewmodels

import com.example.movielist.ThisApp
import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieCategory

class PopularFragmentViewModel : BaseListViewModel<Movie>() {
    override suspend fun requestData(page: Int): List<Movie> =
        ThisApp.moviesRepository.getMoviesList(page, MovieCategory.POPULAR)
}