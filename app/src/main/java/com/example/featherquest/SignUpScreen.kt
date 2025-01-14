package com.example.featherquest

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun SignUpScreen(navController: NavHostController) {
    val database = Firebase.database
    val usersRef = database.getReference("users")

    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Validation states
    var isEmailValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var isUserNameValid by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sign Up", fontSize = 45.sp)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                isEmailValid = Patterns.EMAIL_ADDRESS.matcher(it).matches()
            },
            label = { Text(text = "Email") },
            isError = !isEmailValid
        )
        if (!isEmailValid) {
            Text("Invalid email format", color = androidx.compose.ui.graphics.Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = {
                userName = it
                isUserNameValid = userName.isNotBlank() // Ensure username is not empty
            },
            label = { Text(text = "Username") },
            isError = !isUserNameValid
        )
        if (!isUserNameValid) {
            Text("Username cannot be empty", color = androidx.compose.ui.graphics.Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                isPasswordValid = password.length >= 6 // Ensure password is at least 6 characters
            },
            label = { Text(text = "Password") },
            isError = !isPasswordValid
        )
        if (!isPasswordValid) {
            Text("Password must be at least 6 characters", color = androidx.compose.ui.graphics.Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        Button(onClick = {
            if (email.isBlank() || userName.isBlank() || password.isBlank()) {
                Toast.makeText(context, "All fields must be filled out", Toast.LENGTH_SHORT).show()
            } else if (!isEmailValid) {
                Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            } else if (!isPasswordValid) {
                Toast.makeText(context, "Password too short", Toast.LENGTH_SHORT).show()
            } else if (!isUserNameValid) {
                Toast.makeText(context, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                // Check if username already exists in the database
                usersRef.child(userName).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        Toast.makeText(context, "Username already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        val user = User(email, password)
                        usersRef.child(userName).setValue(user)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                                    navController.navigate("login")
                                } else {
                                    Toast.makeText(context, "Sign Up Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            }
        }) {
            Text(text = "Sign Up")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Already have an account? Login",
            modifier = Modifier.clickable {
                navController.navigate("login")
            }
        )
    }
}
