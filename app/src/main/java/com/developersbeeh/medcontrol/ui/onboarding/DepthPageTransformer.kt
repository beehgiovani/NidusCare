// src/main/java/com/developersbeeh/medcontrol/ui/onboarding/DepthPageTransformer.kt
package com.developersbeeh.medcontrol.ui.onboarding

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

private const val MIN_SCALE = 0.75f

class DepthPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            when {
                position < -1 -> { // [-Infinity,-1)
                    // Esta página está bem fora da tela à esquerda.
                    alpha = 0f
                }
                position <= 0 -> { // [-1,0]
                    // Usa a página padrão para deslizar para a esquerda
                    alpha = 1f
                    translationX = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> { // (0,1]
                    // Desaparece e escala a página para dentro
                    alpha = 1 - position

                    // Contrapeso o movimento de rolagem padrão
                    translationX = pageWidth * -position

                    // Escala a página para dentro (0.75 = MIN_SCALE)
                    val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> { // (1,+Infinity]
                    // Esta página está bem fora da tela à direita.
                    alpha = 0f
                }
            }
        }
    }
}