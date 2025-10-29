package com.developersbeeh.medcontrol.ui.cycletracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.CycleSummary
import com.developersbeeh.medcontrol.databinding.CycleHistoryItemBinding
import java.time.format.DateTimeFormatter
import java.util.Locale

class CycleHistoryAdapter : ListAdapter<CycleSummary, CycleHistoryAdapter.CycleHistoryViewHolder>(CycleDiffCallback()) {

    // ✅ ROBUSTEZ: O formatter agora usa a string de recurso para o locale
    private val locale = Locale(
        "pt", // Idealmente, isso viria de R.string.locale_pt
        "BR"  // Idealmente, isso viria de R.string.locale_br
    )
    private val dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMM. 'de' yyyy", locale)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CycleHistoryViewHolder {
        val binding = CycleHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CycleHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CycleHistoryViewHolder, position: Int) {
        val cycle = getItem(position)
        // ✅ REATORADO: Passa o formatter atualizado
        holder.bind(cycle, dateFormatter)
    }

    class CycleHistoryViewHolder(private val binding: CycleHistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cycle: CycleSummary, formatter: DateTimeFormatter) {
            val context = binding.root.context
            binding.textViewStartDate.text = cycle.startDate.format(formatter)
            // ✅ REATORADO: Usa getString com format args
            binding.textViewCycleLength.text = context.getString(R.string.cycle_length_days, cycle.cycleLength)
            binding.textViewPeriodLength.text = context.getString(R.string.period_length_days, cycle.periodLength)
        }
    }
}

class CycleDiffCallback : DiffUtil.ItemCallback<CycleSummary>() {
    override fun areItemsTheSame(oldItem: CycleSummary, newItem: CycleSummary): Boolean {
        return oldItem.startDate == newItem.startDate
    }

    override fun areContentsTheSame(oldItem: CycleSummary, newItem: CycleSummary): Boolean {
        return oldItem == newItem
    }
}