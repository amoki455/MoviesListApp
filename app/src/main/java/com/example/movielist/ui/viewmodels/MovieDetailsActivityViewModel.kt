package com.example.movielist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movielist.ThisApp
import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieDetails
import com.example.movielist.data.models.MovieImage
import com.example.movielist.data.models.ProductionCompany
import kotlinx.coroutines.async

class MovieDetailsActivityViewModel : BaseViewModel() {

    private val mIsLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = mIsLoading

    private val mImages = MutableLiveData<List<MovieImage>>(emptyList())
    val images: LiveData<List<MovieImage>> = mImages

    private val mDetails = MutableLiveData<MovieDetails?>(null)
    val details: LiveData<MovieDetails?> = mDetails

    private val mProductionCompaniesString = MutableLiveData("")
    val productionCompaniesString: LiveData<String> = mProductionCompaniesString

    fun load(movie: Movie) {
        if (mIsLoading.value == true || movie.id == 0)
            return

        runOnScope(
            onFailure = {
                mIsLoading.postValue(false)
            }
        ) {
            mIsLoading.postValue(true)
            val detailsJob =
                viewModelScope.async { ThisApp.moviesRepository.getMovieDetails(movie.id) }
            val imagesJob =
                viewModelScope.async { ThisApp.moviesRepository.getMovieImages(movie.id) }

            val details = detailsJob.await()
            mDetails.postValue(details)
            createProductionCompaniesString(details.productionCompanies)

            val images = mutableListOf<MovieImage>().apply {
                add(MovieImage(filePath = movie.posterPath))
            }
            images.addAll(imagesJob.await())
            mImages.postValue(images)

            mIsLoading.postValue(false)
        }
    }

    private fun createProductionCompaniesString(productionCompanies: List<ProductionCompany>?) {
        if (productionCompanies == null)
            return

        mProductionCompaniesString.postValue(
            productionCompanies.fold("") { acc, prod ->
                if (prod.name != null) {
                    "- ${prod.name}\n$acc"
                } else acc
            }.trim('\n')
        )
    }
}