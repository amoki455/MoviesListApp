package com.example.movielist.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movielist.BuildConfig
import com.google.gson.JsonParseException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException

abstract class BaseViewModel : ViewModel() {
    private val mErrorEvents = MutableSharedFlow<ErrorType>()
    val errorEvents = mErrorEvents.asSharedFlow()

    protected fun runOnScope(
        onFailure: (suspend (Throwable) -> Unit)? = null,
        block: suspend () -> Unit
    ) =
        viewModelScope.launch {
            runCatching {
                block()
            }.onFailure {
                mErrorEvents.emit(
                    when (it) {
                        is IOException -> ErrorType.IO
                        is JsonParseException -> ErrorType.JSON_PARSE
                        else -> ErrorType.UNKNOWN
                    }
                )
                // BuildConfig is generated before each build so BUILD_TYPE may change.
                @Suppress("KotlinConstantConditions")
                if (BuildConfig.BUILD_TYPE == "debug") {
                    it.printStackTrace()
                }
                onFailure?.invoke(it)
            }
        }
}

enum class ErrorType {
    IO, JSON_PARSE, UNKNOWN
}