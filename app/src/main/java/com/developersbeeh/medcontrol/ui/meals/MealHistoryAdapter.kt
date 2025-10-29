package com.developersbeeh.medcontrol.ui.meals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.Refeicao
import com.developersbeeh.medcontrol.data.model.TipoRefeicao
import com.developersbeeh.medcontrol.databinding.ItemMealBinding
import com.developersbeeh.medcontrol.databinding.ItemTimelineDateHeaderBinding
import java.time.LocalDate

sealed class MealListItem {
    data class Header(val date: LocalDate) : MealListItem()
    data class Meal(val refeicao: Refeicao) : MealListItem()
}

class MealHistoryAdapter : ListAdapter<MealListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    private val VIEW_TYPE_HEADER = 1
    private val VIEW_TYPE_MEAL = 2

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MealListItem.Header -> VIEW_TYPE_HEADER
            is MealListItem.Meal -> VIEW_TYPE_MEAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(ItemTimelineDateHeaderBinding.inflate(inflater, parent, false))
            else -> MealViewHolder(ItemMealBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is MealListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is MealListItem.Meal -> (holder as MealViewHolder).bind(item)
        }
    }

    class HeaderViewHolder(private val binding: ItemTimelineDateHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: MealListItem.Header) {
            val context = binding.root.context
            binding.textViewDate.text = when {
                header.date.isEqual(LocalDate.now()) -> context.getString(R.string.date_today)
                header.date.isEqual(LocalDate.now().minusDays(1)) -> context.getString(R.string.date_yesterday)
                else -> header.date.toString()
            }
        }
    }

    class MealViewHolder(private val binding: ItemMealBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mealItem: MealListItem.Meal) {
            val context = binding.root.context
            val meal = mealItem.refeicao
            binding.textViewDescription.text = meal.descricao

            // ✅ REATORADO: Usa a função de extensão para obter o nome traduzido
            binding.textViewMealType.text = try {
                TipoRefeicao.valueOf(meal.tipo).getDisplayName(context)
            } catch (e: Exception) {
                context.getString(R.string.meal_type_default)
            }

            binding.textViewCalories.text = meal.calorias?.let { context.getString(R.string.value_kcal, it.toString()) } ?: ""
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MealListItem>() {
        override fun areItemsTheSame(oldItem: MealListItem, newItem: MealListItem): Boolean {
            return if (oldItem is MealListItem.Meal && newItem is MealListItem.Meal) {
                oldItem.refeicao.id == newItem.refeicao.id
            } else if (oldItem is MealListItem.Header && newItem is MealListItem.Header) {
                oldItem.date.isEqual(newItem.date)
            } else {
                false
            }
        }

        override fun areContentsTheSame(oldItem: MealListItem, newItem: MealListItem): Boolean {
            return oldItem == newItem
        }
    }
}