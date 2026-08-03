package com.prog7314.locallens.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prog7314.locallens.ui.components.ArticleCard
import com.prog7314.locallens.ui.components.CategoryFilter
import com.prog7314.locallens.ui.components.CountryPickerDialog
import com.prog7314.locallens.ui.components.EmptyStateView
import com.prog7314.locallens.ui.components.ErrorStateView
import com.prog7314.locallens.ui.components.LocationHeader
import com.prog7314.locallens.ui.components.LoadingStateView
import com.prog7314.locallens.ui.components.MissingApiKeyView
import com.prog7314.locallens.ui.viewmodel.NewsUiState
import com.prog7314.locallens.ui.viewmodel.NewsViewModel

import com.prog7314.locallens.ui.components.PermissionDeniedCard

import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.prog7314.locallens.ui.components.SecurityDialog

/**
 * Main Home screen layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NewsViewModel,
    onRequestLocationPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    isBiometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
    onSignOutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val latitude by viewModel.latitude.collectAsState()
    val longitude by viewModel.longitude.collectAsState()
    val locationStatus by viewModel.locationStatus.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val locationPermissionDenied by viewModel.locationPermissionDenied.collectAsState()

    var showCountryDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    if (showCountryDialog) {
        CountryPickerDialog(
            currentCountry = selectedCountry,
            onCountrySelected = { country ->
                viewModel.onCountrySelected(country)
            },
            onDismissRequest = { showCountryDialog = false }
        )
    }

    if (showSecurityDialog) {
        SecurityDialog(
            isBiometricEnabled = isBiometricEnabled,
            onToggleBiometric = onToggleBiometric,
            onDismissRequest = { showSecurityDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LocalLens",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Location-Aware News Explorer",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadNews(resetPagination = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Security & Biometrics") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showMenu = false
                                showSecurityDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sign Out") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showMenu = false
                                onSignOutClick()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header 1: Location & Country controls
            LocationHeader(
                country = selectedCountry,
                latitude = latitude,
                longitude = longitude,
                locationStatus = locationStatus,
                onUseMyLocationClicked = onRequestLocationPermission,
                onChangeCountryClicked = { showCountryDialog = true }
            )

            // Location Permission Denied Card
            if (locationPermissionDenied) {
                PermissionDeniedCard(
                    onOpenSettingsClicked = onOpenAppSettings,
                    onChangeCountryClicked = { showCountryDialog = true }
                )
            }

            // Header 2: Category Chips

            CategoryFilter(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    viewModel.onCategorySelected(category)
                }
            )

            // Body Content based on UiState
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is NewsUiState.Loading -> {
                        LoadingStateView()
                    }

                    is NewsUiState.MissingApiKey -> {
                        MissingApiKeyView()
                    }

                    is NewsUiState.Empty -> {
                        EmptyStateView(
                            onRetryClicked = { viewModel.loadNews(resetPagination = true) }
                        )
                    }

                    is NewsUiState.Error -> {
                        ErrorStateView(
                            errorMessage = state.message,
                            onRetryClicked = { viewModel.loadNews(resetPagination = true) }
                        )
                    }

                    is NewsUiState.Success -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(
                                items = state.articles,
                                key = { article -> article.id.takeIf { it != 0L } ?: article.url.hashCode().toLong() }
                            ) { article ->
                                ArticleCard(article = article)
                            }

                            // Pagination / Load More section at bottom
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.isAppending) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else if (state.hasMore) {
                                        OutlinedButton(
                                            onClick = { viewModel.loadNextPage() },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ExpandMore,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "Load More Articles")
                                        }
                                    } else {
                                        Text(
                                            text = "End of available news articles",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
