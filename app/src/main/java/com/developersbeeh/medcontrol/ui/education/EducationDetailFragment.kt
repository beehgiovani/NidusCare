// src/main/java/com/developersbeeh/medcontrol/ui/education/EducationDetailFragment.kt

package com.developersbeeh.medcontrol.ui.education

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import coil.load
import com.developersbeeh.medcontrol.databinding.FragmentEducationDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EducationDetailFragment : Fragment() {

    private var _binding: FragmentEducationDetailBinding? = null
    private val binding get() = _binding!!

    private val args: EducationDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEducationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val artigo = args.artigo

        // Configura a toolbar para ter o botão de voltar
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        binding.toolbar.setupWithNavController(findNavController())

        binding.collapsingToolbar.title = artigo.titulo
        binding.imageViewArticleHeader.load(artigo.imageUrl)
        binding.textViewArticleCategory.text = artigo.categoria
        binding.textViewArticleTitle.text = artigo.titulo
        binding.textViewArticleContent.text = artigo.conteudo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}