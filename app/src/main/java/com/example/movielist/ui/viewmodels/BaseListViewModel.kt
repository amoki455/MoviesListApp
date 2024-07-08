package com.example.movielist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

abstract class BaseListViewModel<TEntity> : BaseViewModel() {
    private var isRequestingData = false
    private var loadedAllData = false
    private var currentPage = 0

    private val mItems = mutableListOf<TEntity>()
    val items: List<TEntity> = mItems

    private val mNewItems = MutableLiveData<List<TEntity>?>(null)
    val newItems: LiveData<List<TEntity>?> = mNewItems

    // for the view to re-display the loading indicator when reloading
    private val mIsLoadingFirstPage = MutableLiveData(false)
    val isLoadingFirstPage: LiveData<Boolean> = mIsLoadingFirstPage

    private val mNoResults = MutableLiveData(false)
    val noResults: LiveData<Boolean> = mNoResults

    protected abstract fun addLoadingIndicatorItem(items: MutableList<TEntity>)
    protected abstract fun isLoadingIndicatorItem(item: TEntity): Boolean
    protected abstract suspend fun requestData(page: Int): List<TEntity>

    fun consumeNewItems() {
        mNewItems.value?.let {
            mItems.addAll(it)
            if (it.isNotEmpty()) {
                currentPage += 1
                addLoadingIndicatorItem(mItems)
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
        mItems.lastOrNull()?.let {
            if (isLoadingIndicatorItem(it)) {
                mItems.removeLast()
                return i
            }
        }
        return -1
    }

    fun loadNextPage() {
        if (isRequestingData || loadedAllData || !isNewItemsConsumed()) {
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
            mNewItems.postValue(requestData(currentPage + 1))
            isRequestingData = false
            mIsLoadingFirstPage.postValue(false)
        }
    }

    fun reload() {
        currentPage = 0
        loadedAllData = false
        loadNextPage()
    }

    fun clearItemsList() {
        mItems.clear()
    }

    private fun isNewItemsConsumed(): Boolean = mNewItems.value == null
}