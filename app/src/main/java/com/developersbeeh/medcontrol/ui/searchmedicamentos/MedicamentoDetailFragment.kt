package com.developersbeeh.medcontrol.ui.searchmedicamentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.developersbeeh.medcontrol.databinding.FragmentMedicamentoDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MedicamentoDetailFragment : Fragment() {

    private var _binding: FragmentMedicamentoDetailBinding? = null
    private val binding get() = _binding!!

    private val args: MedicamentoDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicamentoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val medicamento = args.medicamento

        binding.textViewNome.text = medicamento.nome
        binding.textViewPrincipioAtivo.text = medicamento.principioAtivo
        binding.textViewClasseTerapeutica.text = medicamento.classeTerapeutica
        binding.textViewLaboratorio.text = medicamento.laboratorio
        binding.textViewRegistroAnvisa.text = medicamento.registroAnvisa
        binding.textViewApresentacao.text = medicamento.apresentacao
        binding.textViewBulaLink.text = medicamento.bulaLink
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
