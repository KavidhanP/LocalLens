package com.prog7314.locallens.data.model

/**
 * Represents a country available for selection in the app.
 */
data class Country(
    val code: String,      // 2-letter ISO country code used by World News API (e.g. "za")
    val name: String,      // country name (e.g. "South Africa")
    val flagEmoji: String  // Visual flag emoji for  display
)

object CountryData {
    val defaultCountry = Country("za", "South Africa", "🇿🇦")

    val popularCountries = listOf(
        Country("za", "South Africa", "🇿🇦"),
        Country("us", "United States", "🇺🇸"),
        Country("gb", "United Kingdom", "🇬🇧"),
        Country("ca", "Canada", "🇨🇦"),
        Country("au", "Australia", "🇦🇺"),
        Country("in", "India", "🇮🇳"),
        Country("de", "Germany", "🇩🇪"),
        Country("fr", "France", "🇫🇷"),
        Country("jp", "Japan", "🇯🇵"),
        Country("br", "Brazil", "🇧🇷"),
        Country("nz", "New Zealand", "🇳🇿"),
        Country("ng", "Nigeria", "🇳🇬"),
        Country("ke", "Kenya", "🇰🇪"),
        Country("ie", "Ireland", "🇮🇪"),
        Country("sg", "Singapore", "🇸🇬")
    )

    fun findByCode(code: String): Country {
        val cleanCode = code.lowercase().trim()
        return popularCountries.find { it.code == cleanCode }
            ?: Country(cleanCode, cleanCode.uppercase(), "🌐")
    }
}
