package com.example.featherquest


data class BirdObservations(
    val birdName: String = "",
    val date: String = "",
    val time: String = "",
    val location: Map<String, Double> = emptyMap(),
    val numberOfBirds: Int = 0,
    val notes: String = ""
)