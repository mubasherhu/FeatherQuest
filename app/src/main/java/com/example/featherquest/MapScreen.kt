package com.example.featherquest

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.featherquest.api.EBirdApiService
import com.example.featherquest.api.HotspotLocation
import com.example.featherquest.api.BirdSighting
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory

@Composable
fun MyMapScreen(navController: NavHostController, userName: String, userLatLng: LatLng) {
    val context = LocalContext.current
    val database = Firebase.database
    val userRef = database.getReference("users/$userName")

    var hotspots by remember { mutableStateOf<List<HotspotLocation>>(emptyList()) }
    var birdSightings by remember { mutableStateOf<List<BirdSighting>>(emptyList()) }
    var maxDistance by remember { mutableStateOf(50) }
    var unitSystem by remember { mutableStateOf("kilometers") } // "kilometers" or "miles"

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLatLng, 10f)
    }

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.ebird.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val eBirdApiService = retrofit.create(EBirdApiService::class.java)

    // Helper function to convert miles to kilometers or vice versa
    fun convertDistance(distance: Int, fromUnit: String, toUnit: String): Int {
        return if (fromUnit != toUnit) {
            if (fromUnit == "miles") (distance * 1.60934).toInt() else (distance / 1.60934).toInt()
        } else distance
    }

    // Fetch bird data
    fun fetchBirdData(lat: Double, lng: Double, distance: Int, unit: String) {
        val convertedDistance =
            if (unit == "miles") convertDistance(distance, "miles", "kilometers") else distance

        eBirdApiService.getNearbyHotspots(
            lat,
            lng,
            convertedDistance,
            apiKey = BuildConfig.EBIRD_API_KEY
        ).enqueue(object : Callback<List<HotspotLocation>> {
            override fun onResponse(
                call: Call<List<HotspotLocation>>,
                response: Response<List<HotspotLocation>>
            ) {
                if (response.isSuccessful) {
                    hotspots = response.body() ?: emptyList()
                } else {
                    Log.e("eBirdAPI", "Failed to fetch hotspots: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<HotspotLocation>>, t: Throwable) {
                Log.e("eBirdAPI", "Hotspots API call failed: ${t.message}")
            }
        })

        eBirdApiService.getRecentBirdSightings(
            lat,
            lng,
            convertedDistance,
            apiKey = BuildConfig.EBIRD_API_KEY
        ).enqueue(object : Callback<List<BirdSighting>> {
            override fun onResponse(
                call: Call<List<BirdSighting>>,
                response: Response<List<BirdSighting>>
            ) {
                if (response.isSuccessful) {
                    birdSightings = response.body() ?: emptyList()
                    Toast.makeText(
                        context,
                        "Found ${birdSightings.size} bird sightings",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e(
                        "eBirdAPI",
                        "Failed to fetch bird sightings: ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<List<BirdSighting>>, t: Throwable) {
                Log.e("eBirdAPI", "Bird sightings API call failed: ${t.message}")
            }
        })
    }

    // Load user settings and fetch data
    LaunchedEffect(Unit) {
        userRef.child("settings").get().addOnSuccessListener { snapshot ->
            maxDistance = snapshot.child("maxDistance").getValue(Int::class.java) ?: 50
            unitSystem = snapshot.child("unitSystem").getValue(String::class.java) ?: "kilometers"

            fetchBirdData(userLatLng.latitude, userLatLng.longitude, maxDistance, unitSystem)
        }.addOnFailureListener { exception ->
            Log.e("Settings", "Error loading settings", exception)
            fetchBirdData(userLatLng.latitude, userLatLng.longitude, 50, "kilometers")
        }
    }

    // Layout for Map and Buttons
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp) // Prevents the map from overlapping the notification bar
    ) {
        // Map takes 3/4 of the screen
        Box(
            modifier = Modifier
                .weight(0.75f)
                .fillMaxWidth()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                // User location marker
                Marker(
                    state = MarkerState(position = userLatLng),
                    title = "Your Location",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )

                // User's search radius circle (converted to meters)
                val radiusInMeters =
                    if (unitSystem == "miles") maxDistance * 1609.34 else maxDistance * 1000.0
                Circle(
                    center = userLatLng,
                    radius = radiusInMeters,
                    fillColor = Color(0x220000FF), // Semi-transparent blue
                    strokeColor = Color.Blue,
                    strokeWidth = 2f
                )

                // Hotspot circles
                hotspots.forEach { hotspot ->
                    val hotspotLatLng = LatLng(hotspot.latitude, hotspot.longitude)
                    Circle(
                        center = hotspotLatLng,
                        radius = 1000.0,
                        fillColor = Color(0x22FF0000), // Semi-transparent red
                        strokeColor = Color.Red,
                        strokeWidth = 2f
                    )
                }

                // Bird sighting markers
                birdSightings.forEach { sighting ->
                    Marker(
                        state = MarkerState(position = LatLng(sighting.lat, sighting.lng)),
                        title = sighting.comName,
                        snippet = "Recent sighting",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
            }

            // Add the "Return to My Location" floating action button
            FloatingActionButton(
                onClick = {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(userLatLng, 10f))
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Return to my location"
                )
            }
        }

        // Buttons take 1/4 of the screen height with a nice background color
        Surface(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxWidth(),
            color = Color(0xFFE0F7FA) // Light blue background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Top row of buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            navController.navigate("recordObservation/$userName")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            "Record Observation",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp
                            )
                        )
                    }

                    Button(
                        onClick = {
                            navController.navigate("viewObservations/$userName")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            "View Observations",
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                // Bottom row - Achievements button
                Button(
                    onClick = {
                        navController.navigate("achievements/$userName")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        "Achievements & Badges",
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }

    }
}
