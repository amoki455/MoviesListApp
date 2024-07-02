package com.example.movielist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movielist.ThisApp
import com.example.movielist.data.models.Movie
import com.example.movielist.data.models.MovieCategory

abstract class BaseListViewModel(private val category: MovieCategory) : BaseViewModel() {
    private var mIsRequestingData = false
    protected var mLoadedAllData = false
    protected var mCurrentPage = 0

    protected val mItemsList = mutableListOf<Movie>()
    protected var mSearchParam: String = ""
    protected val mItems = MutableLiveData<List<Movie>>(mItemsList)

    val items: LiveData<List<Movie>> = mItems

    fun loadNextPage() {
        if (mIsRequestingData == true || mLoadedAllData == true) {
            return
        }

        runOnScope(
            onFailure = {
                mIsRequestingData = false
            }
        ) {
            mIsRequestingData = true
            if (category == MovieCategory.UNCATEGORIZED) {
                if (mSearchParam.isNotEmpty()) {
                    ThisApp.moviesRepository.searchByKeywords(
                        mSearchParam,
                        mCurrentPage + 1
                    ).let {
                        processNewList(it)
                    }
                }
            } else {
                ThisApp.moviesRepository.getMoviesList(mCurrentPage + 1, category)
                    .let {
                        processNewList(it)
                    }
            }
            mIsRequestingData = false
        }
    }

    private fun processNewList(list: List<Movie>) {
        if (list.isNotEmpty()) {
            mCurrentPage += 1
            mItemsList.apply {
                removeIf { item -> item.id == 0 }
                addAll(list)
                // add empty item to indicate loading in RecyclerView
                add(Movie())
            }
            mItems.postValue(mItemsList)
        } else {
            mItemsList.removeIf { item -> item.id == 0 }
            mItems.postValue(mItemsList)
            mLoadedAllData = true
        }
    }
}