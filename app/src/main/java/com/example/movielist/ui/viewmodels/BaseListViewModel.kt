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

    protected val mItemsList = mutableListOf<Movie>()
    protected val mItems = MutableLiveData<List<Movie>>(mItemsList)
    val items: LiveData<List<Movie>> = mItems

    // for te view to re-display the loading indicator when submitting new search query
    private val mIsLoadingFirstPage = MutableLiveData(false)
    val isLoadingFirstPage: LiveData<Boolean> = mIsLoadingFirstPage

    protected val mNoResults = MutableLiveData(false)
    val noResults: LiveData<Boolean> = mNoResults

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
                        processNewList(it)
                    }
                } else {
                    handleEmptyResponse()
                }
            } else {
                ThisApp.moviesRepository.getMoviesList(currentPage + 1, category)
                    .let {
                        processNewList(it)
                    }
            }
            isRequestingData = false
            mIsLoadingFirstPage.postValue(false)
        }
    }

    private fun processNewList(list: List<Movie>) {
        if (list.isNotEmpty()) {
            currentPage += 1
            mItemsList.apply {
                // remove previous empty item that indicate loading
                removeIf { item -> item.id == 0 }
                // add new items to the list
                addAll(list)
                // add empty item to indicate loading in RecyclerView
                add(Movie())
            }
            mItems.postValue(mItemsList)
            if (mNoResults.value == true) {
                mNoResults.postValue(false)
            }
        } else {
            handleEmptyResponse()
        }
    }

    private fun handleEmptyResponse() {
        // remove previous empty item that indicate loading
        mItemsList.removeIf { item -> item.id == 0 }
        mItems.postValue(mItemsList)
        loadedAllData = true
        if (currentPage == 0) {
            mNoResults.postValue(true)
        }
    }
}