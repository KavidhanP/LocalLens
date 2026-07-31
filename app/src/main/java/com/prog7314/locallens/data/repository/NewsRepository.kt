package com.prog7314.locallens.data.repository

import com.prog7314.locallens.BuildConfig
import com.prog7314.locallens.data.model.NewsResponse
import com.prog7314.locallens.data.network.NetworkModule
import com.prog7314.locallens.data.network.WorldNewsApi
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Custom exceptions to provide specific error messages.
 */
class MissingApiKeyException : Exception("World News API Key is missing. Please add WORLD_NEWS_API_KEY to your local.properties file.")
class QuotaExceededException : Exception("World News API request quota limit reached. Please try again later.")
class InvalidApiKeyException : Exception("Invalid World News API Key. Please verify your key in local.properties.")

/**
 * Repository responsible for fetching news data from World News API.
 */
class NewsRepository(
    private val api: WorldNewsApi = NetworkModule.api
) {

    /**
     * Fetches news articles for a given country code, optional category, and pagination offset.
     */
    suspend fun getNews(
        countryCode: String,
        category: String? = null,
        offset: Int = 0,
        number: Int = 20
    ): Result<NewsResponse> {
        val apiKey = BuildConfig.WORLD_NEWS_API_KEY.trim()

        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY") {
            return Result.failure(MissingApiKeyException())
        }

        val earliestPublishDate = getEarliestPublishDateString()
        val categoryQuery = if (category.isNull_or_all()) null else category?.lowercase()


        return try {
            val response = api.searchNews(
                sourceCountries = countryCode.lowercase(),
                language = "en",
                earliestPublishDate = earliestPublishDate,
                categories = categoryQuery,
                number = number,
                offset = offset,
                apiKey = apiKey
            )
            Result.success(response)
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> Result.failure(InvalidApiKeyException())
                402, 429 -> Result.failure(QuotaExceededException())
                else -> Result.failure(Exception("API Request failed with HTTP code ${e.code()}: ${e.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection and try again."))
        } catch (e: Exception) {
            Result.failure(Exception("An unexpected error occurred: ${e.localizedMessage}"))
        }
    }

    /**
     * Dynamically calculates earliest publish date
     */
    private fun getEarliestPublishDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -14)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(calendar.time)
    }

    private fun String?.isNull_or_all(): Boolean {
        return this.isNull_or_blank() || this.equals("all", ignoreCase = true)
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.isBlank()
    }
}
