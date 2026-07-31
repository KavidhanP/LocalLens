# LocalLens

> Discover the news happening around you, wherever you are.

## Description

LocalLens is a location-aware Android news app that fetches live articles from the World News API based on your current country. You can use your device location or manually select any country and filter by category.


## Screenshots

![App screenshot](Screenshots/Screenshot%202026-08-01%20001332.png)
![App screenshot](Screenshots/Screenshot%202026-08-01%20001347.png)

## Features

- Auto-detect country from device GPS
- Manual country selection
- Category filtering (Business, Technology, Sports, Health, Politics, etc.)
- Article cards with headline, summary, author, date, category, and image
- Open full article in device browser
- Load More pagination (20 articles per page)
- Graceful handling of errors, missing data, and denied permissions

## Architecture

MVVM with a repository layer. UI uses Jetpack Compose, state managed via StateFlow, networking via Retrofit and OkHttp, and location via FusedLocationProvider and Geocoder.

## World News API

This app uses the World News API (https://worldnewsapi.com/). A free API key is required.

## API Key Setup

1. Register for a free key at https://worldnewsapi.com/
2. Open `local.properties` in the project root
3. Add: `WORLD_NEWS_API_KEY= yourapikeyhere`
4. Rebuild the project

The key is never committed to Git. If missing, the app shows a setup prompt.

## Location Flow

User taps "Use My Location" -> app requests permission -> FusedLocationProvider gets GPS coordinates -> Geocoder converts to country code -> country code sent to API -> news displayed.

## Testing with Durban Location (Emulator)

1. Open emulator Extended Controls -> Location
2. Set Latitude: -29.8587, Longitude: 31.0218
3. Click Set Location, then tap "Use My Location" in the app
4. Expected: South Africa detected, country code "za" used

## How to Run

1. Clone the repository and open in Android Studio
2. Add your API key to `local.properties`
3. Run on an emulator or physical device (Android 8.0+)

## Known Limitations

- Free API plan has a daily request quota
- Some news sources return relative image URLs which show a placeholder instead
- Geocoding may fail on emulators without Google Play services
- The API key is compiled into the APK; do not share debug builds publicly
