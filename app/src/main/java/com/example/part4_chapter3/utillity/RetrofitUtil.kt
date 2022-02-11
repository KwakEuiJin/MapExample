package com.example.part4_chapter3.utillity

import com.example.part4_chapter3.BuildConfig
import com.example.part4_chapter3.Url
import com.example.part4_chapter3.Url.TMAP_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitUtil {
    val apiService: ApiService by lazy { getRetrofit().create(ApiService::class.java) }

    private fun getRetrofit(): Retrofit {

        return Retrofit.Builder()
            .baseUrl(Url.TMAP_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}