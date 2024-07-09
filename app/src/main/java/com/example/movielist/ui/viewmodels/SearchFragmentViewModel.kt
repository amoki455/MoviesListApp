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

    private var searchParam = ""
    private var matchedKeywordsQueryJob: Deferred<Unit>? = null

    private val mMatchedKeywords = MutableLiveData<List<Keyword>?>(null)
    val matchedKeywords: LiveData<List<Keyword>?> = mMatchedKeywords

    private val mIsLoadingMatchedKeywords = MutableLiveData(false)
    val isLoadingMatchedKeywords: LiveData<Boolean> = mIsLoadingMatchedKeywords

    private val mIsSuggestionsVisible = MutableLiveData(false)
    val isSuggestionsVisible: LiveData<Boolean> = mIsSuggestionsVisible

    private val mIsResultsVisible = MutableLiveData(false)
    val isResultsVisible: LiveData<Boolean> = mIsResultsVisible

    private val mSelectedKeywordsNames = MutableLiveData("")
    val selectedKeywordsNames: LiveData<String> = mSelectedKeywordsNames

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
                if (text.isNotEmpty()) {
                    val result = ThisApp.moviesRepository.findKeywords(text)
                    mMatchedKeywords.postValue(result)
                } else {
                    mMatchedKeywords.postValue(emptyList())
                }
                mIsLoadingMatchedKeywords.postValue(false)
            }

            try {
                matchedKeywordsQueryJob?.await()
            } catch (_: CancellationException) { /*ignored*/
            }
        }
    }

    fun selectKeyword(vararg keywords: Keyword) {
        createSearchParam(keywords.toList())

        if (searchParam.isNotEmpty()) {
            val keywordsNames = keywords.fold("") { acc, keyword ->
                "${keyword.name}, $acc"
            }
            mSelectedKeywordsNames.postValue(keywordsNames)
            reload()
        } else {
            mSelectedKeywordsNames.postValue("")
        }
    }

    override suspend fun requestData(page: Int): List<Movie> {
        return if (searchParam.isNotEmpty()) {
            ThisApp.moviesRepository.searchByKeywords(
                searchParam,
                page
            )
        } else {
            emptyList()
        }
    }

    private fun createSearchParam(keywords: List<Keyword>) {
        if (keywords.isEmpty())
            searchParam = ""

        searchParam = keywords.fold("") { acc, keyword ->
            "${keyword.id}|$acc"
        }
    }

    fun showSuggestions() = mIsSuggestionsVisible.postValue(true)
    fun hideSuggestions() = mIsSuggestionsVisible.postValue(false)
    fun showResults() = mIsResultsVisible.postValue(true)
    fun hideResults() = mIsResultsVisible.postValue(false)
}