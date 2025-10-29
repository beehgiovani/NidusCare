// src/main/java/com/developersbeeh/medcontrol/ui/documents/HealthDocumentsAdapter.kt
package com.developersbeeh.medcontrol.ui.documents

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.data.model.DocumentoSaude
import com.developersbeeh.medcontrol.databinding.ItemHealthDocumentBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HealthDocumentsAdapter(
    private val onItemClick: (DocumentoSaude) -> Unit,
    private val onMenuClick: (documento: DocumentoSaude, anchorView: View) -> Unit
) : ListAdapter<DocumentoSaude, HealthDocumentsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHealthDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHealthDocumentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(documento: DocumentoSaude) {
            binding.imageViewDocIcon.setImageResource(documento.tipo.iconRes)
            binding.textViewDocTitle.text = documento.titulo

            // ✅ LÓGICA DE EXIBIÇÃO DE DETALHES APRIMORADA
            val dataFormatada = try {
                LocalDate.parse(documento.dataDocumento, DateTimeFormatter.ISO_LOCAL_DATE)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR")))
            } catch (e: Exception) {
                documento.dataDocumento // Fallback para o texto original se a data não for no formato esperado
            }

            binding.textViewDocDetails.text = "$dataFormatada • ${documento.tipo.displayName}"

            val providerInfo = listOfNotNull(documento.medicoSolicitante, documento.laboratorio)
                .filter { it.isNotBlank() }
                .joinToString(" / ")

            if (providerInfo.isNotEmpty()) {
                binding.textViewDocProvider.visibility = View.VISIBLE
                binding.textViewDocProvider.text = providerInfo
            } else {
                binding.textViewDocProvider.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onItemClick(documento)
            }
            binding.buttonMenu.setOnClickListener {
                onMenuClick(documento, it)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DocumentoSaude>() {
        override fun areItemsTheSame(oldItem: DocumentoSaude, newItem: DocumentoSaude): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: DocumentoSaude, newItem: DocumentoSaude): Boolean {
            return oldItem == newItem
        }
    }
}