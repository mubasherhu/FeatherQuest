package com.example.featherquest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable


@Composable
fun ViewObservationsScreen(navController: NavHostController, userName: String) {
    val context = LocalContext.current
    var observations by remember { mutableStateOf<List<Pair<String, BirdObservations>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedObservation by remember { mutableStateOf<Pair<String, BirdObservations>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val database = Firebase.database.reference
    val userObservationsRef = database.child("users").child(userName).child("observations")

    LaunchedEffect(Unit) {
        try {
            userObservationsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val observationsList = mutableListOf<Pair<String, BirdObservations>>()
                        for (childSnapshot in snapshot.children) {
                            try {
                                val observationId = childSnapshot.key ?: continue
                                val observation = childSnapshot.getValue(BirdObservations::class.java)
                                if (observation != null) {
                                    observationsList.add(Pair(observationId, observation))
                                }
                            } catch (e: Exception) {
                                Log.e("ViewObservations", "Error processing observation: ${e.message}")
                            }
                        }
                        observations = observationsList.sortedByDescending { it.second.date + it.second.time }
                        isLoading = false
                        errorMessage = null
                    } catch (e: Exception) {
                        Log.e("ViewObservations", "Error processing observations: ${e.message}")
                        errorMessage = "Error loading observations"
                        isLoading = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ViewObservations", "Database error: ${error.message}")
                    errorMessage = "Failed to load observations"
                    isLoading = false
                }
            })
        } catch (e: Exception) {
            Log.e("ViewObservations", "Error setting up database listener: ${e.message}")
            errorMessage = "Error connecting to database"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Observations") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Unknown error",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                observations.isEmpty() -> {
                    Text(
                        text = "No observations recorded yet",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn {
                        items(observations) { (id, observation) ->
                            ObservationCard(
                                observation = observation,
                                onDelete = {
                                    userObservationsRef.child(id).removeValue()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Observation deleted", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to delete observation", Toast.LENGTH_SHORT).show()
                                        }
                                },
                                onClick = {
                                    selectedObservation = if (selectedObservation?.first == id) null else Pair(id, observation)
                                },
                                isExpanded = selectedObservation?.first == id
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ObservationCard(
    observation: BirdObservations,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    isExpanded: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = observation.birdName,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${observation.date} at ${observation.time}",
                        style = MaterialTheme.typography.body2
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete observation")
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Number of birds: ${observation.numberOfBirds}")
                observation.location["latitude"]?.let { lat ->
                    observation.location["longitude"]?.let { lng ->
                        Text("Location: ($lat, $lng)")
                    }
                }
                if (observation.notes.isNotBlank()) {
                    Text("Notes: ${observation.notes}")
                }
            }
        }
    }
}
