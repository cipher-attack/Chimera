package com.chimera.zpqmxr

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.chimera.zpqmxr.data.AppDatabase
import com.chimera.zpqmxr.data.PayloadRepository
import com.chimera.zpqmxr.ui.MainScreen
import com.chimera.zpqmxr.ui.MainViewModel
import com.chimera.zpqmxr.ui.theme.MyApplicationTheme

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.unit.dp
import android.content.Context
import com.chimera.zpqmxr.ui.components.OnboardingScreen

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN
            )
        )
    } else {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        )
    }

    val database = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java,
        "chimera_database"
    ).build()
    val repository = PayloadRepository(database.payloadDao())
    
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, applicationContext) as T
        }
    }
    val viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

    setContent {
      MyApplicationTheme {
        val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
        var isAuthenticated by remember { mutableStateOf(false) }
        var hasSeenOnboarding by remember { mutableStateOf(prefs.getBoolean("has_seen_onboarding", false)) }

        if (!hasSeenOnboarding) {
            OnboardingScreen(onComplete = {
                prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                hasSeenOnboarding = true
            })
        } else if (isBiometricEnabled && !isAuthenticated) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Lock, 
                                    contentDescription = "Secured", 
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Authentication Required",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Identity verification required to decrypt toolset",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        Button(
                            onClick = {
                                val executor = ContextCompat.getMainExecutor(this@MainActivity)
                                val biometricPrompt = BiometricPrompt(this@MainActivity, executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            super.onAuthenticationSucceeded(result)
                                            isAuthenticated = true
                                        }
                                    })
                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("Access Restricted")
                                    .setSubtitle("Authenticate to unlock engine")
                                    .setDeviceCredentialAllowed(true)
                                    .build()
                                biometricPrompt.authenticate(promptInfo)
                            },
                        ) {
                            Text("Verify Identity")
                        }
                    }
                }
            }
            LaunchedEffect(Unit) {
                val executor = ContextCompat.getMainExecutor(this@MainActivity)
                val biometricPrompt = BiometricPrompt(this@MainActivity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            isAuthenticated = true
                        }
                    })
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Access Restricted")
                    .setSubtitle("Authenticate to unlock engine")
                    .setDeviceCredentialAllowed(true)
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            MainScreen(viewModel = viewModel)
        }
      }
    }
  }
}
