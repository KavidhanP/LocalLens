package com.prog7314.locallens.data.network

import com.prog7314.locallens.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the World News API.
 * Documentation: https://worldnewsapi.com/docs/search-news/
 */
interface WorldNewsApi {

    @GET("search-news")
    suspend fun searchNews(
        @Query("source-countries") sourceCountries: String,
        @Query("language") language: String = "en",
        @Query("earliest-publish-date") earliestPublishDate: String? = null,
        @Query("categories") categories: String? = null,
        @Query("number") number: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("api-key") apiKey: String
    ): NewsResponse

}
