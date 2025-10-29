package com.developersbeeh.medcontrol.ui.pharmacy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.remote.Place
import com.developersbeeh.medcontrol.databinding.ItemPharmacyBinding
import kotlin.math.roundToInt

class PharmacyAdapter(
    private val onPharmacyClick: (Place) -> Unit,
    private val onOptionsClick: (Place, View) -> Unit,
    private val userLat: Double? = null, // ✅ NOVO: Localização do usuário
    private val userLng: Double? = null  // ✅ NOVO: Localização do usuário
) : ListAdapter<Place, PharmacyAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPharmacyBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPharmacyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onPharmacyClick(getItem(bindingAdapterPosition))
                }
            }

            binding.buttonMenu.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onOptionsClick(getItem(bindingAdapterPosition), it)
                }
            }
        }

        fun bind(place: Place) {
            val context = binding.root.context
            binding.textViewPharmacyName.text = place.name
            binding.textViewPharmacyAddress.text = place.vicinity

            // ✅ NOVO: Calcular e exibir distância
            if (userLat != null && userLng != null) {
                val distance = calculateDistance(
                    userLat, userLng,
                    place.geometry.location.lat,
                    place.geometry.location.lng
                )
                
                binding.textViewDistance.visibility = View.VISIBLE
                binding.textViewDistance.text = formatDistance(distance)
                
                // Código de cor baseado na distância
                val distanceColor = when {
                    distance < 0.5 -> R.color.success_green
                    distance < 2.0 -> R.color.md_theme_primary
                    else -> R.color.warning_orange
                }
                binding.textViewDistance.setTextColor(
                    ContextCompat.getColor(context, distanceColor)
                )
            } else {
                binding.textViewDistance.visibility = View.GONE
            }

            // Status de abertura
            if (place.openingHours?.openNow == true) {
                binding.textViewOpenStatus.text = "Aberta agora"
                binding.textViewOpenStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.success_green)
                )
            } else {
                binding.textViewOpenStatus.text = "Fechada"
                binding.textViewOpenStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.error_red)
                )
            }
        }

        /**
         * Calcula distância entre dois pontos usando fórmula de Haversine
         * @return Distância em quilômetros
         */
        private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val R = 6371.0 // Raio da Terra em km
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return R * c
        }

        /**
         * Formata distância para exibição amigável
         */
        private fun formatDistance(distanceKm: Double): String {
            return if (distanceKm < 1.0) {
                "${(distanceKm * 1000).roundToInt()} m"
            } else {
                "${String.format("%.1f", distanceKm)} km"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Place>() {
        override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem.placeId == newItem.placeId
        }

        override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
            return oldItem == newItem
        }
    }
}

