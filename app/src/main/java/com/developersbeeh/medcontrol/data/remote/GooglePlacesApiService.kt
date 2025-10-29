// src/main/java/com/developersbeeh/medcontrol/data/remote/GooglePlacesApiService.kt
package com.developersbeeh.medcontrol.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// --- Respostas da API ---
data class PlacesResponse(
    val results: List<Place>,
    @SerializedName("status") val status: String? = null,
    @SerializedName("error_message") val errorMessage: String? = null
)

data class PlaceDetailsResponse(
    val result: PlaceDetails,
    @SerializedName("status") val status: String? = null,
    @SerializedName("error_message") val errorMessage: String? = null
)

// --- Modelos de Dados ---
data class Place(
    @SerializedName("place_id") val placeId: String,
    val name: String,
    val vicinity: String?,
    val geometry: Geometry,
    @SerializedName("opening_hours") val openingHours: OpeningHours? = null
)

data class PlaceDetails(
    @SerializedName("place_id") val placeId: String,
    val name: String,
    @SerializedName("formatted_phone_number") val phoneNumber: String?,
    val vicinity: String?
)

data class Geometry(val location: Location)
data class Location(val lat: Double, val lng: Double)
data class OpeningHours(@SerializedName("open_now") val openNow: Boolean? = null)

// --- Interface do Serviço ---
interface GooglePlacesApiService {

    @GET("maps/api/place/nearbysearch/json")
    suspend fun findNearbyPharmacies(
        @Query("location") location: String, // "lat,lng"
        @Query("radius") radius: Int, // em metros
        @Query("key") apiKey: String,
        @Query("type") type: String = "pharmacy",
        @Query("language") language: String = "pt-BR"
    ): PlacesResponse

    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "name,vicinity,formatted_phone_number,place_id",
        @Query("key") apiKey: String,
        @Query("language") language: String = "pt-BR"
    ): PlaceDetailsResponse
}
