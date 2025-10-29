package com.developersbeeh.medcontrol.ui.timeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.databinding.ItemTimelineDateHeaderBinding
import com.developersbeeh.medcontrol.databinding.ItemTimelineLogBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class TimelineAdapter : PagingDataAdapter<TimelineListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_LOG = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimelineListItem.HeaderItem -> VIEW_TYPE_HEADER
            is TimelineListItem.LogItem -> VIEW_TYPE_LOG
            null -> throw IllegalStateException("Item nulo em uma posição válida")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> DateHeaderViewHolder(ItemTimelineDateHeaderBinding.inflate(inflater, parent, false))
            VIEW_TYPE_LOG -> LogViewHolder(ItemTimelineLogBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Tipo de view desconhecido")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            when (it) {
                is TimelineListItem.HeaderItem -> (holder as DateHeaderViewHolder).bind(it)
                is TimelineListItem.LogItem -> (holder as LogViewHolder).bind(it)
            }
        }
    }

    class DateHeaderViewHolder(private val binding: ItemTimelineDateHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        private val locale = Locale("pt", "BR")
        private val dateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", locale)

        fun bind(header: TimelineListItem.HeaderItem) {
            val context = binding.root.context
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            val dateText = when (header.date) {
                today -> context.getString(R.string.timeline_header_today, header.date.format(dateFormatter))
                yesterday -> context.getString(R.string.timeline_header_yesterday, header.date.format(dateFormatter))
                else -> {
                    val dayOfWeek = header.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
                        .replaceFirstChar { it.uppercase() }
                    context.getString(R.string.timeline_header_format, dayOfWeek, header.date.format(dateFormatter))
                }
            }
            binding.textViewDate.text = dateText
        }
    }

    class LogViewHolder(private val binding: ItemTimelineLogBinding) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun bind(item: TimelineListItem.LogItem) {
            val context = binding.root.context
            val log = item.log
            binding.textViewTime.text = log.timestamp.format(timeFormatter)
            binding.textViewDetails.text = log.description


            if (log.category == TimelineItemCategory.ACTIVITY) {
                binding.textViewAutor.visibility = View.GONE
            } else {

                binding.textViewAutor.visibility = View.VISIBLE
                if (log.author.equals("Nidus AI", ignoreCase = true) || log.author.equals("Sistema", ignoreCase = true)) {
                    binding.textViewAutor.text = log.author
                } else {
                    binding.textViewAutor.text = context.getString(R.string.timeline_author_prefix, log.author)
                }
            }

            binding.timelineIcon.setImageResource(log.iconRes)
            binding.timelineIcon.setColorFilter(ContextCompat.getColor(context, log.iconTintRes))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TimelineListItem>() {
        override fun areItemsTheSame(oldItem: TimelineListItem, newItem: TimelineListItem): Boolean {
            return (oldItem is TimelineListItem.LogItem && newItem is TimelineListItem.LogItem && oldItem.log.id == newItem.log.id) ||
                    (oldItem is TimelineListItem.HeaderItem && newItem is TimelineListItem.HeaderItem && oldItem.date == newItem.date)
        }
        override fun areContentsTheSame(oldItem: TimelineListItem, newItem: TimelineListItem): Boolean {
            return oldItem == newItem
        }
    }
}