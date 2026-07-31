package com.prog7314.locallens.ui.viewmodel

import com.prog7314.locallens.data.model.NewsArticle

/**
 * Sealed UI State representing the state of the Home News screen.
 */
sealed interface NewsUiState {
    /** Initial or full refreshing state */
    object Loading : NewsUiState

    /** Active list of loaded news articles */
    data class Success(
        val articles: List<NewsArticle>,
        val totalAvailable: Int,
        val hasMore: Boolean,
        val isAppending: Boolean = false
    ) : NewsUiState

    /** No articles available for current search filters */
    object Empty : NewsUiState

    /** Error state with user-facing message */
    data class Error(val message: String) : NewsUiState

    /** API Key missing diagnostic state */
    object MissingApiKey : NewsUiState
}
