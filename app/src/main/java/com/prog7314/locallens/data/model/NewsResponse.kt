package com.prog7314.locallens.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level response wrapper for the World News API /search-news endpoint.
 */
@Serializable
data class NewsResponse(
    @SerialName("offset")
    val offset: Int = 0,

    @SerialName("number")
    val number: Int = 0,

    @SerialName("available")
    val available: Int = 0,

    @SerialName("news")
    val news: List<NewsArticle> = emptyList()
)
