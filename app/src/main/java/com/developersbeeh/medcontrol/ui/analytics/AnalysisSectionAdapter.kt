package com.developersbeeh.medcontrol.ui.analysis

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.developersbeeh.medcontrol.databinding.ItemAnalysisSectionBinding



class AnalysisSectionAdapter : ListAdapter<AnalysisSection, AnalysisSectionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAnalysisSectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAnalysisSectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(section: AnalysisSection) {
            binding.textViewSectionTitle.text = section.title
            binding.textViewSectionContent.text = section.content
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<AnalysisSection>() {
        override fun areItemsTheSame(oldItem: AnalysisSection, newItem: AnalysisSection): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: AnalysisSection, newItem: AnalysisSection): Boolean {
            return oldItem == newItem
        }
    }
}