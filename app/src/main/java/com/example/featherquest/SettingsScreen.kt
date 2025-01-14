package com.example.featherquest

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun SettingsScreen(navController: NavHostController, userName: String) {
    val database = Firebase.database
    val userRef: DatabaseReference = database.getReference("users/$userName")

    var unitSystem by remember { mutableStateOf("Kilometers") }  // Default to Kilometers
    var maxDistance by remember { mutableStateOf("50") }  // Keep as string to allow empty state

    val context = LocalContext.current

    // Load user settings from Firebase
    LaunchedEffect(true) {
        userRef.child("settings").get().addOnSuccessListener { snapshot ->
            snapshot.child("unitSystem").getValue(String::class.java)?.let { unitSystem = it }
            snapshot.child("maxDistance").getValue(Int::class.java)?.let { maxDistance = it.toString() }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Settings", fontSize = 45.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Distance Unit", fontSize = 20.sp)
        Row {
            RadioButton(
                selected = unitSystem == "Kilometers",
                onClick = { unitSystem = "Kilometers" }
            )
            Text(text = "Kilometers")

            RadioButton(
                selected = unitSystem == "Miles",
                onClick = { unitSystem = "Miles" }
            )
            Text(text = "Miles")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = maxDistance,
            onValueChange = { maxDistance = it },  // No conversion, keep as string
            label = { Text(text = "Max Distance ($unitSystem)") },  // Show the unit in the label
            isError = maxDistance.isEmpty()  // Display error if the field is empty
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val distanceToSave = maxDistance.toIntOrNull() ?: 50  // Use 50 if invalid
            // Save settings to Firebase
            val settings = mapOf(
                "unitSystem" to unitSystem,
                "maxDistance" to distanceToSave
            )
            userRef.child("settings").setValue(settings).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to save settings", Toast.LENGTH_SHORT).show()
                }
            }
        }) {
            Text(text = "Save Settings")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            navController.popBackStack()
        }) {
            Text(text = "Go Back")
        }
    }
}
