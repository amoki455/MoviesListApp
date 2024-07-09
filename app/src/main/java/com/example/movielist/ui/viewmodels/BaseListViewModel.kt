package com.example.movielist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

abstract class BaseListViewModel<TEntity> : BaseViewModel() {
    private var currentPage = 0
    private var loadedAllData = false

    private val mItems = MutableLiveData<List<TEntity>>(emptyList())
    val items: LiveData<List<TEntity>> = mItems

    private val mIsLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = mIsLoading

    protected abstract suspend fun requestData(page: Int): List<TEntity>

    private fun consumeNewItems(newItems: List<TEntity>) {
        val currentItems = mItems.value ?: emptyList()
        mItems.postValue(currentItems + newItems)
        if (newItems.isNotEmpty()) {
            currentPage += 1
        } else {
            loadedAllData = true
        }
    }

    fun loadNextPage() {
        if (mIsLoading.value == true || loadedAllData) {
            return
        }

        runOnScope(
            onFailure = {
                mIsLoading.postValue(false)
            }
        ) {
            mIsLoading.postValue(true)
            val newItems = requestData(currentPage + 1)
            consumeNewItems(newItems)
            mIsLoading.postValue(false)
        }
    }

    fun reload() {
        clearItemsList()
        currentPage = 0
        loadedAllData = false
        loadNextPage()
    }

    fun clearItemsList() {
        val count = mItems.value?.size ?: 0
        if (count > 0) {
            mItems.postValue(emptyList())
        }
    }
}