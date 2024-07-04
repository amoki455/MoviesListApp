package com.example.movielist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movielist.ThisApp
import com.example.movielist.data.models.MovieDetails
import com.example.movielist.data.models.MovieImage
import kotlinx.coroutines.async

class MovieDetailsActivityViewModel : BaseViewModel() {

    private val mIsLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = mIsLoading

    private val mImages = MutableLiveData<List<MovieImage>>(emptyList())
    val images: LiveData<List<MovieImage>> = mImages

    private val mDetails = MutableLiveData<MovieDetails?>(null)
    val details: LiveData<MovieDetails?> = mDetails

    fun load(movieId: Int) {
        if (mIsLoading.value == true || movieId == 0)
            return

        runOnScope(
            onFailure = {
                mIsLoading.postValue(false)
            }
        ) {
            mIsLoading.postValue(true)
            val detailsJob =
                viewModelScope.async { ThisApp.moviesRepository.getMovieDetails(movieId) }
            val imagesJob =
                viewModelScope.async { ThisApp.moviesRepository.getMovieImages(movieId) }

            mDetails.postValue(detailsJob.await())

            val images = mutableListOf<MovieImage>()
            mDetails.value?.posterPath?.let {
                images.add(MovieImage(filePath = it))
            }
            images.addAll(imagesJob.await())
            mImages.postValue(images)

            mIsLoading.postValue(false)
        }
    }
}