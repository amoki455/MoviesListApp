package com.example.movielist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movielist.ThisApp
import com.example.movielist.data.models.MovieCategory
import com.example.movielist.data.models.Keyword

class SearchFragmentViewModel : BaseListViewModel(MovieCategory.UNCATEGORIZED) {

    private val mIsRequestingNewSearch = MutableLiveData(false)
    val isRequestingNewSearch: LiveData<Boolean> = mIsRequestingNewSearch

    fun submitSearch(text: String) {
        if (mIsRequestingNewSearch.value == true || text.isEmpty())
            return

        runOnScope(
            onFailure = {
                mIsRequestingNewSearch.postValue(false)
            }
        ) {
            mIsRequestingNewSearch.postValue(true)
            ThisApp.moviesRepository.findKeywords(text).let {
                reload(it)
            }
            mIsRequestingNewSearch.postValue(false)
        }
    }

    private fun createSearchParam(keywords: List<Keyword>) {
        mSearchParam = keywords.fold("") { acc, keyword ->
            "${keyword.id}|$acc"
        }
    }

    private fun emptyItemsList() {
        mItemsList.clear()
        mItems.postValue(mItemsList)
    }

    private fun reload(keywords: List<Keyword>) {
        emptyItemsList()
        createSearchParam(keywords)
        mCurrentPage = 0
        if (keywords.isNotEmpty()) {
            mLoadedAllData = false
            loadNextPage()
        } else {
            mLoadedAllData = true
        }
    }
}