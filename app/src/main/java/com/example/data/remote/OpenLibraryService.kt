package com.example.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OpenLibraryService {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 15
    ): OpenLibrarySearchResponse

    @GET("search.json")
    suspend fun searchByIsbn(
        @Query("isbn") isbn: String,
        @Query("limit") limit: Int = 5
    ): OpenLibrarySearchResponse

    companion object {
        private const val BASE_URL = "https://openlibrary.org/"
        // Replace this placeholder before production release with an email you monitor.
        private const val CONTACT_EMAIL = "YOUR_EMAIL@example.com"

        fun create(): OpenLibraryService {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "ChapterLedger/1.0 ($CONTACT_EMAIL)")
                        .build()
                    chain.proceed(request)
                }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OpenLibraryService::class.java)
        }
    }
}
