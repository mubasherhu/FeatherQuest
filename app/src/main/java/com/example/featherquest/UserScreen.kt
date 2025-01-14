package com.example.featherquest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun UserScreen(navController: NavHostController, userName: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFE0F7FA) // Light cyan background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome $userName",
                fontSize = 30.sp,
                color = Color(0xFF00796B), // Teal color for text
                modifier = Modifier.padding(bottom = 32.dp) // Add space below the text
            )

            // Button to view hotspots
            Button(
                onClick = {
                    navController.navigate("mapScreen/$userName")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp) // Rounded corners for buttons
            ) {
                Text(text = "View Birding Hotspots on Map", color = Color.White)
            }

            // Button for settings
            Button(
                onClick = {
                    navController.navigate("settings/$userName")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp) // Rounded corners for buttons
            ) {
                Text(text = "Settings", color = Color.White)
            }
        }
    }
}
