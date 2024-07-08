package com.example.movielist.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.movielist.ThisApp
import com.example.movielist.data.models.Keyword
import com.example.movielist.data.models.Movie
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.concurrent.CancellationException

class SearchFragmentViewModel : BaseListViewModel<Movie>() {

    private val mMatchedKeywords = MutableLiveData<List<Keyword>?>(null)
    val matchedKeywords: LiveData<List<Keyword>?> = mMatchedKeywords

    private val mSelectedKeywords = MutableLiveData<List<Keyword>>(emptyList())

    /**
     * Observe this and call clearItemsList and notify remove changes to recycler view adapter then call reload
     * to reload with the newly selected keyword
     */
    val selectedKeywords: LiveData<List<Keyword>> = mSelectedKeywords

    private val mIsLoadingMatchedKeywords = MutableLiveData(false)
    val isLoadingMatchedKeywords: LiveData<Boolean> = mIsLoadingMatchedKeywords

    private var matchedKeywordsQueryJob: Deferred<Unit>? = null

    /**
     * Get available keywords from inputted query.
     * Call this method every time the text changes in SearchView
     */
    fun findKeywords(text: String) {
        runOnScope(
            onFailure = {
                mIsLoadingMatchedKeywords.postValue(false)
            }
        ) {
            // This method will be called every time when text changes in the search box so we should delay before each call
            // and cancel the previous call if there is a new request

            if (matchedKeywordsQueryJob?.isActive == true) {
                matchedKeywordsQueryJob?.cancel(CancellationException("Requested a new query"))
            }
            matchedKeywordsQueryJob = viewModelScope.async {
                delay(300)
                mIsLoadingMatchedKeywords.postValue(true)
                val result = ThisApp.moviesRepository.findKeywords(text)
                mMatchedKeywords.postValue(result)
                mIsLoadingMatchedKeywords.postValue(false)
            }

            try {
                matchedKeywordsQueryJob?.await()
            } catch (_: CancellationException) { /*ignored*/ }
        }
    }

    fun selectKeyword(vararg keywords: Keyword) {
        mSelectedKeywords.postValue(keywords.toList())
    }

    fun clearMatchedKeywords() {
        mMatchedKeywords.postValue(emptyList())
    }

    override fun addLoadingIndicatorItem(items: MutableList<Movie>) {
        items.add(Movie())
    }

    override fun isLoadingIndicatorItem(item: Movie): Boolean = item.id == 0

    override suspend fun requestData(page: Int): List<Movie> {
        val searchParam = createSearchParam()
        return if (searchParam.isNotEmpty()) {
            ThisApp.moviesRepository.searchByKeywords(
                searchParam,
                page
            )
        } else {
            emptyList()
        }
    }

    private fun createSearchParam(): String {
        val keywords = mSelectedKeywords.value ?: emptyList()
        if (keywords.isEmpty())
            return "";

        return keywords.fold("") { acc, keyword ->
            "${keyword.id}|$acc"
        }
    }
}