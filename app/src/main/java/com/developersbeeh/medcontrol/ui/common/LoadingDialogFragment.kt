// src/main/java/com/developersbeeh/medcontrol/ui/common/LoadingDialogFragment.kt
package com.developersbeeh.medcontrol.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.developersbeeh.medcontrol.databinding.DialogLoadingBinding

class LoadingDialogFragment : DialogFragment() {

    private var _binding: DialogLoadingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false // Impede o usuário de fechar o diálogo
        val message = arguments?.getString(ARG_MESSAGE) ?: "Processando..."
        binding.textViewLoadingMessage.text = message
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "LoadingDialog"
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(message: String): LoadingDialogFragment {
            val fragment = LoadingDialogFragment()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }
}