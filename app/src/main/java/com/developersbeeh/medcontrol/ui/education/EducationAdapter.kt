// src/main/java/com/developersbeeh/medcontrol/ui/education/EducationAdapter.kt

package com.developersbeeh.medcontrol.ui.education

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.model.ArtigoEducativo
import com.developersbeeh.medcontrol.databinding.ItemEducationArticleBinding

class EducationAdapter(
    private val onItemClick: (ArtigoEducativo) -> Unit
) : ListAdapter<ArtigoEducativo, EducationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEducationArticleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemEducationArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(bindingAdapterPosition))
                }
            }
        }

        fun bind(artigo: ArtigoEducativo) {
            binding.imageViewArticle.load(artigo.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.placeholder_background)
                error(R.drawable.placeholder_background)
            }
            binding.chipCategory.text = artigo.categoria
            binding.textViewArticleTitle.text = artigo.titulo
            binding.textViewArticleSubtitle.text = artigo.subtitulo
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ArtigoEducativo>() {
        override fun areItemsTheSame(oldItem: ArtigoEducativo, newItem: ArtigoEducativo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ArtigoEducativo, newItem: ArtigoEducativo): Boolean {
            return oldItem == newItem
        }
    }
}