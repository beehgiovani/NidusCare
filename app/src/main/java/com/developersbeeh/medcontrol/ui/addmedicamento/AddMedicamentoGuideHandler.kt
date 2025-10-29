// src/main/java/com/developersbeeh/medcontrol/ui/addmedicamento/AddMedicamentoGuideHandler.kt
package com.developersbeeh.medcontrol.ui.addmedicamento

import android.app.Activity
import androidx.core.view.doOnPreDraw
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.databinding.FragmentAddMedicamentoBinding
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.getkeepsafe.taptargetview.TapTargetView

class AddMedicamentoGuideHandler(
    private val fragment: AddMedicamentoFragment,
    private val binding: FragmentAddMedicamentoBinding,
    private val userPreferences: UserPreferences
) {

    fun showGuideIfFirstTime() {
        // Mostra o guia apenas se for a primeira vez e se não for o modo de edição
        if (!userPreferences.hasSeenAddMedGuide() && fragment.isEditing().not()) {
            // Usamos doOnPreDraw para garantir que as views já foram medidas e posicionadas na tela
            binding.root.doOnPreDraw {
                createAndShowGuideSequence()
            }
        }
    }

    private fun createAndShowGuideSequence() {
        val activity = fragment.activity ?: return

        val targets = listOf(
            TapTarget.forView(binding.tilNome, "Nome do Medicamento", "Comece a digitar o nome do medicamento. Faremos uma busca automática para ajudar no preenchimento.")
                .style(),
            TapTarget.forView(binding.cardStep2, "Frequência e Horários", "Defina como o medicamento será administrado: diariamente, em dias específicos da semana ou em intervalos de dias.")
                .style(),
            TapTarget.forView(binding.radioGroupTermino, "Duração do Tratamento", "Marque 'Uso Contínuo' se o tratamento não tiver uma data para terminar ou defina uma duração específica.")
                .style(),
            TapTarget.forView(binding.cardStep4, "Detalhes Adicionais", "Controle o estoque, datas de validade e locais de aplicação (para injetáveis/tópicos) aqui.")
                .style(),
            TapTarget.forView(binding.buttonSalvar, "Salvar", "Quando terminar de preencher todas as informações, clique aqui para salvar o medicamento.")
                .style()
        )

        TapTargetSequence(activity)
            .targets(targets)
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                    // Marca que o guia foi visto para não mostrar novamente
                    userPreferences.setAddMedGuideSeen(true)
                }
                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {}
                override fun onSequenceCanceled(lastTarget: TapTarget?) {
                    userPreferences.setAddMedGuideSeen(true)
                }
            })
            .continueOnCancel(true)
            .start()
    }

    // Função de extensão para padronizar o estilo dos alvos do guia
    private fun TapTarget.style(): TapTarget {
        return this
            .outerCircleColor(R.color.md_theme_primary)
            .targetCircleColor(R.color.white)
            .titleTextColor(R.color.white)
            .descriptionTextColor(R.color.white)
            .cancelable(false)
            .tintTarget(false)
    }
}