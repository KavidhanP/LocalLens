package com.prog7314.locallens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prog7314.locallens.data.biometric.BiometricAuthManager
import com.prog7314.locallens.data.biometric.BiometricPreferencesRepository
import com.prog7314.locallens.ui.auth.AuthScreen
import com.prog7314.locallens.ui.auth.AuthViewModel
import com.prog7314.locallens.ui.biometric.BiometricLockScreen
import com.prog7314.locallens.ui.screens.HomeScreen
import com.prog7314.locallens.ui.theme.LocalLensTheme
import com.prog7314.locallens.ui.viewmodel.NewsViewModel
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val newsViewModel: NewsViewModel by viewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            newsViewModel.fetchDeviceLocation()
        } else {
            newsViewModel.onPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val biometricRepo = BiometricPreferencesRepository(applicationContext)
        val biometricAuthManager = BiometricAuthManager(applicationContext)

        setContent {
            LocalLensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel {
                        AuthViewModel(applicationContext)
                    }

                    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
                    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

                    val biometricEnabled by biometricRepo.biometricEnabled.collectAsState(initial = null)
                    var sessionUnlocked by rememberSaveable { mutableStateOf(false) }

                    val scope = rememberCoroutineScope()

                    when {
                        // 1. Preference loading -> show loading state
                        biometricEnabled == null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // 2. No Firebase user -> show login screen
                        currentUser == null -> {
                            AuthScreen(
                                uiState = authUiState,
                                onSignInClick = { authViewModel.signInWithGoogle() },
                                onClearError = { authViewModel.clearError() }
                            )
                        }

                        // 3. Signed in and locked -> show biometric lock screen
                        biometricEnabled == true && !sessionUnlocked -> {
                            BiometricLockScreen(
                                onUnlock = {
                                    if (biometricAuthManager.canAuthenticate()) {
                                        biometricAuthManager.showBiometricPrompt(
                                            activity = this@MainActivity,
                                            onSuccess = { sessionUnlocked = true },
                                            onError = { error ->
                                                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    } else {
                                        // If device has no hardware/enrolled biometrics, bypass lock
                                        sessionUnlocked = true
                                    }
                                },
                                onLogout = {
                                    sessionUnlocked = false
                                    authViewModel.signOut()
                                }
                            )
                        }

                        // 4. Signed in and unlocked -> show existing LocalLens app
                        else -> {
                            HomeScreen(
                                viewModel = newsViewModel,
                                onRequestLocationPermission = { requestLocationPermissions() },
                                onOpenAppSettings = { openAppSettings() },
                                isBiometricEnabled = biometricEnabled == true,
                                onToggleBiometric = { enable ->
                                    if (enable) {
                                        if (biometricAuthManager.canAuthenticate()) {
                                            biometricAuthManager.showBiometricPrompt(
                                                activity = this@MainActivity,
                                                title = "Confirm Biometrics",
                                                subtitle = "Authenticate to enable biometric lock",
                                                onSuccess = {
                                                    scope.launch {
                                                        biometricRepo.setBiometricEnabled(true)
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "Biometric App Lock enabled",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                },
                                                onError = { error ->
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Authentication failed: $error",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            )
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Biometrics not configured on device",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        scope.launch {
                                            biometricRepo.setBiometricEnabled(false)
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Biometric App Lock disabled",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                onSignOutClick = {
                                    sessionUnlocked = false
                                    authViewModel.signOut()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
}