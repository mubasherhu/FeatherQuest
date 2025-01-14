package com.example.featherquest

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SettingsViewModel : ViewModel() {
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId/settings")

    fun saveSettings(unit: String, maxDistance: Int) {
        val settings = mapOf(
            "unit" to unit,
            "distancePreference" to maxDistance
        )
        dbRef.setValue(settings)
    }

    fun loadSettings(onSettingsLoaded: (String, Int) -> Unit) {
        dbRef.get().addOnSuccessListener {
            val unit = it.child("unit").getValue(String::class.java) ?: "km"
            val distance = it.child("distancePreference").getValue(Int::class.java) ?: 50
            onSettingsLoaded(unit, distance)
        }
    }
}
