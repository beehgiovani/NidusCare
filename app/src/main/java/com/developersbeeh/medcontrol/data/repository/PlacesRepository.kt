// src/main/java/com/developersbeeh/medcontrol/data/repository/PlacesRepository.kt
package com.developersbeeh.medcontrol.data.repository

import android.content.Context
import android.util.Log
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.remote.GooglePlacesApiService
import com.developersbeeh.medcontrol.data.remote.PlaceDetails
import com.developersbeeh.medcontrol.data.remote.PlacesResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacesRepository @Inject constructor(
    private val apiService: GooglePlacesApiService,
    @ApplicationContext private val context: Context
) {

    suspend fun findNearbyPharmacies(lat: Double, lng: Double, radius: Int): Result<PlacesResponse> {
        return try {
            val apiKey = context.getString(R.string.google_places_api_key)

            if (apiKey.isBlank() || apiKey.startsWith("COLOQUE")) {
                return Result.failure(Exception("A chave da API do Google Places não está configurada corretamente."))
            }

            val response = apiService.findNearbyPharmacies(
                location = "$lat,$lng",
                radius = radius,
                apiKey = apiKey
            )

            if (response.status == "REQUEST_DENIED" || response.status == "INVALID_REQUEST") {
                Log.e("PLACES_API", "Erro API: ${response.errorMessage}")
                return Result.failure(Exception(response.errorMessage ?: "Erro desconhecido na API."))
            }

            Result.success(response)
        } catch (e: Exception) {
            Log.e("PLACES_API", "Falha na requisição", e)
            Result.failure(e)
        }
    }

    suspend fun getPlaceDetails(placeId: String): Result<PlaceDetails> {
        return try {
            val apiKey = context.getString(R.string.google_places_api_key)

            if (apiKey.isBlank() || apiKey.startsWith("COLOQUE")) {
                return Result.failure(Exception("A chave da API do Google Places não está configurada corretamente."))
            }

            val response = apiService.getPlaceDetails(
                placeId = placeId,
                apiKey = apiKey
            )

            if (response.status == "REQUEST_DENIED") {
                Log.e("PLACES_API", "Erro detalhes: ${response.errorMessage}")
                return Result.failure(Exception(response.errorMessage))
            }

            Result.success(response.result)
        } catch (e: Exception) {
            Log.e("PLACES_API", "Erro ao buscar detalhes", e)
            Result.failure(e)
        }
    }
}
