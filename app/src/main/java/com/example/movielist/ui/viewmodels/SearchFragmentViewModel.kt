package com.example.movielist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movielist.ThisApp
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.data.models.Keyword

class SearchFragmentViewModel : BaseListViewModel(MovieCategory.UNCATEGORIZED) {

    private val mIsRequestingNewSearch = MutableLiveData(false)
    val isRequestingNewSearch: LiveData<Boolean> = mIsRequestingNewSearch

    /**
     * should call clearItemsList() and notify remove changes to recycler view before calling this
      */
    fun submitSearch(text: String) {
        if (mIsRequestingNewSearch.value == true || text.isEmpty())
            return

        runOnScope(
            onFailure = {
                mIsRequestingNewSearch.postValue(false)
            }
        ) {
            mIsRequestingNewSearch.postValue(true)
            // remove noResults state if it was true to indicate we are trying to get results
            mNoResults.postValue(false)
            ThisApp.moviesRepository.findKeywords(text).let {
                reload(it)
            }
            mIsRequestingNewSearch.postValue(false)
        }
    }

    private fun createSearchParam(keywords: List<Keyword>) {
        searchParam = keywords.fold("") { acc, keyword ->
            "${keyword.id}|$acc"
        }
    }

    fun clearItemsList() {
        mItems.clear()
    }

    private fun reload(keywords: List<Keyword>) {
        createSearchParam(keywords)
        currentPage = 0
        loadedAllData = false
        loadNextPage()
    }
}