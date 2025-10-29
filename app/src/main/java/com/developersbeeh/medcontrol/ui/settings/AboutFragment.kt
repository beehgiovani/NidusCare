// com/developersbeeh/niduscare/ui/settings/AboutFragment.kt
package com.developersbeeh.medcontrol.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.developersbeeh.medcontrol.databinding.FragmentAboutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textViewAppVersion.text = "Versão ${getAppVersion()}"

        binding.buttonSendFeedback.setOnClickListener {
            sendFeedbackEmail()
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            // CORREÇÃO: Usamos o operador Elvis para garantir que não seja nulo
            packageInfo.versionName ?: "N/A"
        } catch (e: PackageManager.NameNotFoundException) {
            "N/A"
        }
    }

    private fun sendFeedbackEmail() {
        val recipientEmail = "developersbeeh@gmail.com"
        val subject = "Feedback - NidusCare App v${getAppVersion()}"
        val body = """
            Descreva seu feedback ou problema aqui...


            --------------------
            Informações do Dispositivo (Não apague)
            Versão do App: ${getAppVersion()}
            Versão do Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            Dispositivo: ${Build.MANUFACTURER} ${Build.MODEL}
            --------------------
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(Intent.createChooser(intent, "Enviar e-mail usando..."))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Nenhum aplicativo de e-mail encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}