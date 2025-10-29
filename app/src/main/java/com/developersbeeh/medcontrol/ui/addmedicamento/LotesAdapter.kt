package com.developersbeeh.medcontrol.ui.addmedicamento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.EstoqueLote
import java.time.format.DateTimeFormatter

// CONSTRUTOR CORRIGIDO: Não recebe mais a lista.
class LotesAdapter(
    private val unidadesDosagem: String,
    private val onDelete: (EstoqueLote) -> Unit
) : ListAdapter<EstoqueLote, LotesAdapter.LoteViewHolder>(LoteDiffCallback()) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    inner class LoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewLoteInfo)
        val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDeleteLote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_medicamento_lote_item, parent, false)
        return LoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoteViewHolder, position: Int) {
        val lote = getItem(position)
        val unidade = unidadesDosagem.ifBlank { "unidades" }
        holder.textView.text = "${lote.quantidade} $unidade, Val: ${lote.dataValidade.format(dateFormatter)}"
        holder.deleteButton.setOnClickListener {
            onDelete(lote)
        }
    }
}

class LoteDiffCallback : DiffUtil.ItemCallback<EstoqueLote>() {
    override fun areItemsTheSame(oldItem: EstoqueLote, newItem: EstoqueLote): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: EstoqueLote, newItem: EstoqueLote): Boolean {
        return oldItem == newItem
    }
}