package com.prog7314.locallens.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prog7314.locallens.data.location.LocationHelper
import com.prog7314.locallens.data.location.UserLocationResult
import com.prog7314.locallens.data.model.Country
import com.prog7314.locallens.data.model.CountryData
import com.prog7314.locallens.data.model.NewsArticle
import com.prog7314.locallens.data.repository.InvalidApiKeyException
import com.prog7314.locallens.data.repository.MissingApiKeyException
import com.prog7314.locallens.data.repository.NewsRepository
import com.prog7314.locallens.data.repository.QuotaExceededException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing News UI state, location updates, pagination, and category filtering.
 */
class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NewsRepository()
    private val locationHelper = LocationHelper(application)

    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    // Selected country (Default: South Africa / "za")
    private val _selectedCountry = MutableStateFlow(CountryData.defaultCountry)
    val selectedCountry: StateFlow<Country> = _selectedCountry.asStateFlow()

    // Location coordinates display
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude.asStateFlow()

    private val _locationStatus = MutableStateFlow("Tap 'Use My Location' or pick a country.")
    val locationStatus: StateFlow<String> = _locationStatus.asStateFlow()

    // Selected Category ("All", "Business", "Technology", etc.)
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Tracks whether the user has denied location permission.
    // When true, HomeScreen shows a dedicated "permission denied" card.
    private val _locationPermissionDenied = MutableStateFlow(false)
    val locationPermissionDenied: StateFlow<Boolean> = _locationPermissionDenied.asStateFlow()

    // Internal list for pagination accumulation
    private val loadedArticles = mutableListOf<NewsArticle>()
    private var currentOffset = 0
    private val pageSize = 20

    init {
        // Load initial news for default country
        loadNews(resetPagination = true)
    }

    /**
     * Loads news from the repository for current selected country and category.
     */
    fun loadNews(resetPagination: Boolean = true) {
        viewModelScope.launch {
            if (resetPagination) {
                currentOffset = 0
                loadedArticles.clear()
                _uiState.value = NewsUiState.Loading
            } else {
                // Keep showing existing articles while appending new items
                val currentState = _uiState.value
                if (currentState is NewsUiState.Success) {
                    _uiState.value = currentState.copy(isAppending = true)
                }
            }

            val countryCode = _selectedCountry.value.code
            val category = _selectedCategory.value

            val result = repository.getNews(
                countryCode = countryCode,
                category = category,
                offset = currentOffset,
                number = pageSize
            )

            result.onSuccess { response ->
                val newArticles = response.news
                loadedArticles.addAll(newArticles)

                if (loadedArticles.isEmpty()) {
                    _uiState.value = NewsUiState.Empty
                } else {
                    val availableCount = response.available
                    val hasMore = loadedArticles.size < availableCount && newArticles.isNotEmpty()
                    
                    _uiState.value = NewsUiState.Success(
                        articles = loadedArticles.toList(),
                        totalAvailable = availableCount,
                        hasMore = hasMore,
                        isAppending = false
                    )
                }
            }.onFailure { exception ->
                when (exception) {
                    is MissingApiKeyException, is InvalidApiKeyException -> {
                        _uiState.value = NewsUiState.MissingApiKey
                    }
                    is QuotaExceededException -> {
                        _uiState.value = NewsUiState.Error("World News API daily quota reached. Try again tomorrow or use another key.")
                    }
                    else -> {
                        _uiState.value = NewsUiState.Error(exception.message ?: "Failed to load news articles.")
                    }
                }
            }
        }
    }

    /**
     * Appends the next page of articles to the current list.
     */
    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState is NewsUiState.Success && currentState.hasMore && !currentState.isAppending) {
            currentOffset += pageSize
            loadNews(resetPagination = false)
        }
    }

    /**
     * Triggered when user selects a category chip (e.g. "Technology", "Sports").
     */
    fun onCategorySelected(category: String) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
            loadNews(resetPagination = true)
        }
    }

    /**
     * Triggered when user manually selects a country from the country dialog.
     */
    fun onCountrySelected(country: Country) {
        _selectedCountry.value = country
        _locationStatus.value = "Manually selected ${country.name} (${country.code.uppercase()})"
        loadNews(resetPagination = true)
    }

    /**
     * Request fresh device location and reverse-geocode to detect country.
     */
    fun fetchDeviceLocation() {
        viewModelScope.launch {
            _locationStatus.value = "Fetching device location..."
            val locResult: UserLocationResult? = locationHelper.getFreshLocation()

            if (locResult != null) {
                _latitude.value = locResult.latitude
                _longitude.value = locResult.longitude

                val detectedCountry = CountryData.findByCode(locResult.countryCode)
                _selectedCountry.value = detectedCountry

                _locationStatus.value = "Detected: ${locResult.countryName} (${locResult.countryCode.uppercase()})"
                loadNews(resetPagination = true)
            } else {
                _locationStatus.value = "Location unavailable. Please check GPS settings."
            }
        }
    }

    /**
     * Called when the user denies the location permission dialog.
     * Sets the locationPermissionDenied flag so the UI shows a
     * dedicated card with a button to open device Settings.
     */
    fun onPermissionDenied() {
        _locationPermissionDenied.value = true
        _locationStatus.value = "Location permission denied. Select a country manually or tap to open Settings."
    }

    /**
     * Called after the user grants location permission (via the system dialog
     * OR after returning from Settings with permission enabled).
     * Clears the denied flag
     */
    fun onPermissionGranted() {
        _locationPermissionDenied.value = false
        fetchDeviceLocation()
    }
}
