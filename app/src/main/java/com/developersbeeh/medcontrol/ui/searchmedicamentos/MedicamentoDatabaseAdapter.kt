package com.developersbeeh.medcontrol.ui.addmedicamento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.MedicamentoDatabase

fun String.capitalizeWords(): String = lowercase().split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }


class MedicamentoDatabaseAdapter(
    private val onMedicamentoSelected: (MedicamentoDatabase) -> Unit
) : RecyclerView.Adapter<MedicamentoDatabaseAdapter.MedicamentoViewHolder>() {

    private var medicamentos: List<MedicamentoDatabase> = emptyList()

    fun submitList(newList: List<MedicamentoDatabase>) {
        medicamentos = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicamento_database_item, parent, false)
        return MedicamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicamentoViewHolder, position: Int) {
        val medicamento = medicamentos[position]
        holder.bind(medicamento)
        holder.itemView.setOnClickListener { onMedicamentoSelected(medicamento) }
    }

    override fun getItemCount(): Int = medicamentos.size

    class MedicamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomeTextView: TextView = itemView.findViewById(R.id.textViewMedicamentoNome)
        private val principioAtivoTextView: TextView = itemView.findViewById(R.id.textViewPrincipioAtivo)
        private val classeTerapeuticaTextView: TextView = itemView.findViewById(R.id.textViewClasseTerapeutica)

        fun bind(medicamento: MedicamentoDatabase) {
            nomeTextView.text = medicamento.NOME_PRODUTO.capitalizeWords()
            principioAtivoTextView.text = medicamento.PRINCIPIO_ATIVO.capitalizeWords()
            classeTerapeuticaTextView.text = medicamento.CLASSE_TERAPEUTICA.capitalizeWords()

        }
    }
}

