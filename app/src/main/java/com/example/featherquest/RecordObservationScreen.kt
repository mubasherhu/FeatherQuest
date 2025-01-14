package com.example.featherquest

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import com.example.featherquest.api.EBirdApiService
import com.example.featherquest.api.BirdSpecies
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.android.gms.maps.model.LatLng

@Composable
fun RecordObservationScreen(navController: NavHostController, userName: String, userLocation: LatLng) {
    val context = LocalContext.current
    val database = Firebase.database.reference

    var birdName by remember { mutableStateOf("") }
    var numberOfBirds by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<BirdSpecies>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var useCurrentLocation by remember { mutableStateOf(true) }  // Toggle for location input
    var customLocation by remember { mutableStateOf("") } // Holds custom location input

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.ebird.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val eBirdApiService = retrofit.create(EBirdApiService::class.java)

    fun searchBirds(query: String) {
        if (query.length < 2) return

        isSearching = true
        eBirdApiService.searchBirdSpecies(
            query = query,
            apiKey = BuildConfig.EBIRD_API_KEY
        ).enqueue(object : Callback<List<BirdSpecies>> {
            override fun onResponse(call: Call<List<BirdSpecies>>, response: Response<List<BirdSpecies>>) {
                isSearching = false
                if (response.isSuccessful) {
                    searchResults = response.body() ?: emptyList()
                }
            }

            override fun onFailure(call: Call<List<BirdSpecies>>, t: Throwable) {
                isSearching = false
                Toast.makeText(context, "Failed to search birds", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bird Species Search Field with Autocomplete Dropdown
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                searchBirds(it)
            },
            label = { Text("Search Bird Species") },
            modifier = Modifier.fillMaxWidth()
        )

        // Display suggestions as dropdown items
        if (searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp) // Limit dropdown height
            ) {
                items(searchResults) { species ->
                    DropdownMenuItem(
                        onClick = {
                            birdName = species.comName
                            searchResults = emptyList()
                            searchQuery = ""
                        }
                    ) {
                        Text(species.comName)
                    }
                }
            }
        }

        // Bird Name Field
        OutlinedTextField(
            value = birdName,
            onValueChange = { birdName = it },
            label = { Text("Bird Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Number of Birds Field
        OutlinedTextField(
            value = numberOfBirds,
            onValueChange = { numberOfBirds = it },
            label = { Text("Number of Birds") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Notes Field
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        // Toggle for Location Selection
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Use Current Location")
            Switch(
                checked = useCurrentLocation,
                onCheckedChange = { useCurrentLocation = it }
            )
        }

        // If using custom location, show input field
        if (!useCurrentLocation) {
            OutlinedTextField(
                value = customLocation,
                onValueChange = { customLocation = it },
                label = { Text("Enter Location (Latitude, Longitude)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = {
                val currentTime = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                // Validate and parse custom location input if not using current location
                val observationLocation = if (useCurrentLocation) {
                    // Use user's current location
                    mapOf("latitude" to userLocation.latitude, "longitude" to userLocation.longitude)
                } else {
                    // Split and parse custom location (latitude, longitude)
                    val coordinates = customLocation.split(",").map { it.trim().toDoubleOrNull() }

                    if (coordinates.size == 2 && coordinates[0] != null && coordinates[1] != null) {
                        // Valid custom location
                        mapOf("latitude" to coordinates[0]!!, "longitude" to coordinates[1]!!)
                    } else {
                        // Invalid custom location input, show error message
                        Toast.makeText(context, "Invalid custom location, using current location.", Toast.LENGTH_SHORT).show()
                        mapOf("latitude" to userLocation.latitude, "longitude" to userLocation.longitude)
                    }
                }

                // Create observation object
                val observation = BirdObservations(
                    birdName = birdName,
                    date = dateFormat.format(currentTime),
                    time = timeFormat.format(currentTime),
                    location = observationLocation,
                    numberOfBirds = numberOfBirds.toIntOrNull() ?: 0,
                    notes = notes
                )

                // Save observation to Firebase
                database.child("users").child(userName).child("observations")
                    .push().setValue(observation)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Observation recorded successfully", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to record observation", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = birdName.isNotBlank() && numberOfBirds.isNotBlank()
        ) {
            Text("Record Observation")
        }

    }
}