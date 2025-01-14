package com.example.featherquest.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import retrofit2.http.Path

data class HotspotLocation(
    val locId: String,
    val locName: String,
    val latitude: Double,
    val longitude: Double,
    val countryCode: String,
    val subnational1Code: String,
    val latestObsDt: String?,
    val numSpeciesAllTime: Int?
)

data class BirdSighting(
    val speciesCode: String,
    val comName: String,
    val sciName: String,
    val locId: String,
    val locName: String,
    val obsDt: String,
    val howMany: Int?,
    val lat: Double,
    val lng: Double
)
data class BirdSpecies(
    val comName: String,
    val sciName: String,
    val speciesCode: String
)
interface EBirdApiService {
    @GET("v2/ref/hotspot/geo")
    fun getNearbyHotspots(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("dist") dist: Int,
        @Query("fmt") fmt: String = "json",
        @Query("key") apiKey: String
    ): Call<List<HotspotLocation>>

    @GET("v2/data/obs/geo/recent")
    fun getRecentBirdSightings(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("dist") dist: Int,
        @Query("back") back: Int = 14, // Last 14 days
        @Query("key") apiKey: String
    ): Call<List<BirdSighting>>

    @GET("v2/ref/taxonomy/find/{query}")
    fun searchBirdSpecies(
        @Path("query") query: String,
        @Query("key") apiKey: String
    ): Call<List<BirdSpecies>>
}