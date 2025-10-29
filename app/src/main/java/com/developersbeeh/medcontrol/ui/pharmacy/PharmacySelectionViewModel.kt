package com.developersbeeh.medcontrol.ui.pharmacy

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developersbeeh.medcontrol.data.remote.Place
import com.developersbeeh.medcontrol.data.remote.PlaceDetails
import com.developersbeeh.medcontrol.data.repository.PlacesRepository
import com.developersbeeh.medcontrol.util.Event
import com.developersbeeh.medcontrol.util.UiState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.math.*

sealed class NavigationEvent {
    data class ToMedicationSelection(val pharmacyDetails: PlaceDetails) : NavigationEvent()
}

@HiltViewModel
class PharmacySelectionViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val locationProvider: FusedLocationProviderClient
) : ViewModel() {

    private val _uiState = MutableLiveData<UiState<List<Place>>>()
    val uiState: LiveData<UiState<List<Place>>> = _uiState

    private val _navigationEvent = MutableLiveData<Event<NavigationEvent>>()
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent

    private val _placeDetailsForOptions = MutableLiveData<Event<UiState<Pair<PlaceDetails, Place>>>>()
    val placeDetailsForOptions: LiveData<Event<UiState<Pair<PlaceDetails, Place>>>> = _placeDetailsForOptions

    // ✅ NOVO: Expor localização do usuário para o adapter
    private val _userLocation = MutableLiveData<Pair<Double, Double>?>()
    val userLocation: LiveData<Pair<Double, Double>?> = _userLocation

    private var currentRadius = 1000 // Padrão: 1km
    private var currentLocation: android.location.Location? = null
    private var allPharmacies: List<Place> = emptyList() // ✅ NOVO: Cache de farmácias

    fun onPermissionResult(context: Context, granted: Boolean) {
        if (granted) {
            fetchLocationAndPharmacies(context)
        } else {
            _uiState.value = UiState.Error("A permissão de localização é necessária para encontrar farmácias próximas.")
        }
    }

    fun setDistanceFilter(radiusInMeters: Int) {
        if (radiusInMeters != currentRadius) {
            currentRadius = radiusInMeters
            fetchPharmacies()
        }
    }

    fun onPharmacySelected(place: Place) {
        viewModelScope.launch {
            _placeDetailsForOptions.postValue(Event(UiState.Loading))
            val detailsResult = placesRepository.getPlaceDetails(place.placeId)
            detailsResult.onSuccess { details ->
                if (details.phoneNumber.isNullOrBlank()) {
                    _uiState.postValue(UiState.Error("Esta farmácia não possui um número de telefone cadastrado para contato."))
                    fetchPharmacies()
                } else {
                    _navigationEvent.postValue(Event(NavigationEvent.ToMedicationSelection(details)))
                }
            }.onFailure {
                _placeDetailsForOptions.postValue(Event(UiState.Error("Não foi possível obter os detalhes da farmácia.")))
            }
        }
    }

    fun fetchDetailsForOptions(place: Place) {
        _placeDetailsForOptions.value = Event(UiState.Loading)
        viewModelScope.launch {
            val detailsResult = placesRepository.getPlaceDetails(place.placeId)
            detailsResult.onSuccess { details ->
                _placeDetailsForOptions.postValue(Event(UiState.Success(Pair(details, place))))
            }.onFailure {
                _placeDetailsForOptions.postValue(Event(UiState.Error(it.message ?: "Erro desconhecido")))
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLocationAndPharmacies(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            _uiState.value = UiState.Error("Permissão de localização não concedida.")
            return
        }

        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                currentLocation = withTimeoutOrNull(10000) {
                    locationProvider.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).await()
                }

                if (currentLocation != null) {
                    // ✅ NOVO: Atualizar localização do usuário
                    _userLocation.postValue(Pair(currentLocation!!.latitude, currentLocation!!.longitude))
                    fetchPharmacies()
                } else {
                    _uiState.postValue(UiState.Error("Não foi possível obter sua localização. Verifique se o GPS está ativado e tente novamente."))
                }
            } catch (e: Exception) {
                _uiState.postValue(UiState.Error("Ocorreu um erro ao obter localização: ${e.message}"))
            }
        }
    }

    private fun fetchPharmacies() {
        val location = currentLocation ?: return
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = placesRepository.findNearbyPharmacies(location.latitude, location.longitude, currentRadius)
            result.onSuccess { response ->
                allPharmacies = response.results
                
                // ✅ NOVO: Ordenar farmácias por distância (mais próximas primeiro)
                val sortedPharmacies = sortPharmaciesByDistance(
                    allPharmacies,
                    location.latitude,
                    location.longitude
                )
                
                _uiState.postValue(UiState.Success(sortedPharmacies))
            }.onFailure {
                _uiState.postValue(UiState.Error("Erro da API: ${it.message}"))
            }
        }
    }

    /**
     * ✅ NOVO: Ordena farmácias por distância do usuário
     */
    private fun sortPharmaciesByDistance(
        pharmacies: List<Place>,
        userLat: Double,
        userLng: Double
    ): List<Place> {
        return pharmacies.sortedBy { place ->
            calculateDistance(
                userLat,
                userLng,
                place.geometry.location.lat,
                place.geometry.location.lng
            )
        }
    }

    /**
     * ✅ NOVO: Calcula distância usando fórmula de Haversine
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Raio da Terra em km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}

