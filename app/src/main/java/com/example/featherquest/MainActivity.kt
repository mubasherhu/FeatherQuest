package com.example.featherquest

import AchievementsPage
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.featherquest.ui.theme.FeatherQuestTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLatLng by mutableStateOf(LatLng(0.0, 0.0))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check for location permissions
        checkLocationPermission()

        setContent {
            FeatherQuestTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("signup") { SignUpScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("mapScreen/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName")
                        userName?.let { MyMapScreen(navController, it, userLatLng) }
                    }
                    composable("user/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName")
                        userName?.let { UserScreen(navController, it) }
                    }
                    composable("settings/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName")
                        userName?.let { SettingsScreen(navController, it) }
                    }
                    composable("recordObservation/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        RecordObservationScreen(navController, userName, userLatLng)
                    }
                    composable("viewObservations/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        ViewObservationsScreen(navController, userName)
                    }
                    // Add the achievements route
                    composable("achievements/{userName}") { backStackEntry ->
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        AchievementsScreen(userName)
                    }
                }
            }
        }
    }

    // Check for location permission
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation()
        }
    }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission required.", Toast.LENGTH_LONG).show()
            }
        }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    userLatLng = LatLng(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@Composable
fun AchievementsScreen(userName: String) {
    AchievementsPage(userName)
}
