package com.example.movielist.network

import com.example.movielist.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiAdapter {
    private var adapter: Retrofit? = null
    val instance: Retrofit
        get() {
            adapter?.let { return it }
            adapter = Retrofit.Builder()
                .baseUrl(BuildConfig.MOVIES_API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return adapter!!
        }

    private val client = OkHttpClient.Builder()
        .addNetworkInterceptor {
            val req = it.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.MOVIES_API_KEY}")
                .build()
            it.proceed(req)
        }.build()
}