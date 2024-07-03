package com.example.movielist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movielist.ThisApp
import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieCategory

abstract class BaseListViewModel(private val category: MovieCategory) : BaseViewModel() {
    private var isRequestingData = false
    protected var loadedAllData = false
    protected var currentPage = 0
    protected var searchParam: String = ""

    protected val mItems = mutableListOf<Movie>()
    val items: List<Movie> = mItems

    private val mNewItems = MutableLiveData<List<Movie>?>(null)
    val newItems: LiveData<List<Movie>?> = mNewItems

    // for the view to re-display the loading indicator when submitting new search query
    private val mIsLoadingFirstPage = MutableLiveData(false)
    val isLoadingFirstPage: LiveData<Boolean> = mIsLoadingFirstPage

    protected val mNoResults = MutableLiveData(false)
    val noResults: LiveData<Boolean> = mNoResults

    fun consumeNewItems() {
        mNewItems.value?.let {
            mItems.addAll(it)
            if (it.isNotEmpty()) {
                currentPage += 1
                // add empty item as loading indicator
                mItems.add(Movie())
                if (mNoResults.value == true) {
                    mNoResults.postValue(false)
                }
            } else {
                loadedAllData = true
                if (currentPage == 0) {
                    mNoResults.postValue(true)
                }
            }
            mNewItems.postValue(null)
        }
    }

    fun removeLoadingItem(): Int {
        val i = mItems.size - 1
        if (mItems.lastOrNull()?.id == 0) {
            mItems.removeLast()
            return i
        }
        return -1
    }

    fun loadNextPage() {
        if (isRequestingData || loadedAllData) {
            return
        }

        runOnScope(
            onFailure = {
                isRequestingData = false
                mIsLoadingFirstPage.postValue(false)
            }
        ) {
            isRequestingData = true
            mIsLoadingFirstPage.postValue(currentPage == 0)
            // remove noResults state if it was true to indicate we are trying to get results
            mNoResults.postValue(false)
            if (category == MovieCategory.UNCATEGORIZED) {
                if (searchParam.isNotEmpty()) {
                    ThisApp.moviesRepository.searchByKeywords(
                        searchParam,
                        currentPage + 1
                    ).let {
                        mNewItems.postValue(it)
                    }
                } else {
                    mNewItems.postValue(emptyList())
                }
            } else {
                ThisApp.moviesRepository.getMoviesList(currentPage + 1, category)
                    .let {
                        mNewItems.postValue(it)
                    }
            }
            isRequestingData = false
            mIsLoadingFirstPage.postValue(false)
        }
    }
}