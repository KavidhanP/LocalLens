package com.prog7314.locallens.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model representing an individual news article from the World News API.
 *
 * The API returns two author fields:
 *  - "author"  → a single nullable String (often null)
 *  - "authors" → a nullable List<String> (may or may not be present)
 *
 * Both are mapped here. displayAuthors prefers the list, then falls back to the
 * singular field, then to "Unknown Author".
 *
 * All optional fields have nullable types with null defaults so the JSON parser
 * never crashes on missing or unexpected values.
 */
@Serializable
data class NewsArticle(
    @SerialName("id")
    val id: Long = 0,

    @SerialName("title")
    val title: String? = null,

    @SerialName("summary")
    val summary: String? = null,

    @SerialName("text")
    val text: String? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("image")
    val image: String? = null,

    @SerialName("video")
    val video: String? = null,

    @SerialName("publish_date")
    val publishDate: String? = null,

    // Singular author field (often null in API responses)
    @SerialName("author")
    val author: String? = null,

    // Author list field (present only in some articles)
    @SerialName("authors")
    val authors: List<String>? = null,

    @SerialName("category")
    val category: String? = null,

    @SerialName("language")
    val language: String? = null,

    @SerialName("source_country")
    val sourceCountry: String? = null,

    @SerialName("sentiment")
    val sentiment: Double? = null
) {
    /** Safe displayable title — never blank */
    val displayTitle: String
        get() = title?.takeIf { it.isNotBlank() } ?: "Untitled Article"

    /**
     * Safe displayable summary.
     * Priority: summary field → first 200 chars of full text → fallback message.
     */
    val displaySummary: String
        get() = summary?.takeIf { it.isNotBlank() }
            ?: text?.takeIf { it.isNotBlank() }?.take(200)?.let { "$it…" }
            ?: "No summary available for this article."

    /**
     * Safe displayable author(s).
     * Priority: authors list → singular author field → "Unknown Author".
     */
    val displayAuthors: String
        get() {
            val fromList = authors?.filter { it.isNotBlank() }
            if (!fromList.isNullOrEmpty()) return fromList.joinToString(", ")
            val fromSingle = author?.takeIf { it.isNotBlank() }
            if (fromSingle != null) return fromSingle
            return "Unknown Author"
        }

    /** Safe displayable category with first letter uppercased */
    val displayCategory: String
        get() = category?.replaceFirstChar { it.uppercase() } ?: "General"

    /**
     * Returns a valid absolute image URL, or null if the URL is relative or blank
     */
    val safeImageUrl: String?
        get() = image?.takeIf { it.startsWith("http://") || it.startsWith("https://") }

    /** Returns true only when the article has a proper https URL to open in browser */
    val hasValidUrl: Boolean
        get() = !url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))
}
