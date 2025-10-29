// src/main/java/com/developersbeeh/niduscare/util/AnalysisPdfGenerator.kt

package com.developersbeeh.medcontrol.util

import android.R.attr.colorPrimary
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.Usuario
import com.developersbeeh.medcontrol.ui.analysis.ParsedAnalysis

import com.google.android.material.color.MaterialColors
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalysisPdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Constantes de Layout
    private val PAGE_WIDTH = 595
    private val PAGE_HEIGHT = 842
    private val LEFT_MARGIN = 40f
    private val RIGHT_MARGIN = (PAGE_WIDTH - 40).toFloat()
    private val TOP_MARGIN = 40f
    private val BOTTOM_MARGIN = (PAGE_HEIGHT - 40).toFloat()
    private val CONTENT_WIDTH = (RIGHT_MARGIN - LEFT_MARGIN).toInt()

    // Variáveis de Estado
    private lateinit var document: PdfDocument
    private lateinit var currentPage: PdfDocument.Page
    private lateinit var canvas: Canvas
    private var yPosition = 0f
    private var pageNumber = 0

    // Dados Globais do Relatório
    private lateinit var currentDependente: Dependente
    private lateinit var currentCuidador: Usuario
    private lateinit var currentLogo: Bitmap

    // Paints
    private val titlePaint = TextPaint().apply { color = MaterialColors.getColor(context, colorPrimary, Color.BLUE); textSize = 18f; isFakeBoldText = true }
    private val subtitlePaint = TextPaint().apply { color = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY); textSize = 10f }
    private val sectionTitlePaint = TextPaint().apply { color = Color.BLACK; textSize = 14f; isFakeBoldText = true }
    private val bodyPaint = TextPaint().apply { color = Color.DKGRAY; textSize = 11f }
    private val disclaimerPaint = TextPaint().apply { color = Color.GRAY; textSize = 9f; isFakeBoldText = true }
    private val footerPaint = Paint().apply { color = Color.LTGRAY; textSize = 9f; textAlign = Paint.Align.CENTER }
    private val highlightBoxPaint = Paint().apply { color = 0xFFF0F0F0.toInt(); style = Paint.Style.FILL } // Cinza claro
    private val highlightTitlePaint = TextPaint().apply { color = MaterialColors.getColor(context, colorPrimary, Color.BLUE); textSize = 14f; isFakeBoldText = true }

    fun createReport(
        analysis: ParsedAnalysis,
        dependente: Dependente,
        cuidador: Usuario,
        logoBitmap: Bitmap
    ): File {
        document = PdfDocument()
        pageNumber = 0

        this.currentDependente = dependente
        this.currentCuidador = cuidador
        this.currentLogo = logoBitmap

        startNewPage()

        val sections = mapOf(
            "Correlações" to analysis.correlations,
            "Interações Medicamentosas" to analysis.interactions,
            "Efeitos Colaterais" to analysis.sideEffects,
            "Observações Adicionais" to analysis.observations
        )

        drawHighlightedSection("Nível de Urgência", analysis.urgencyLevel)
        drawHighlightedSection("Pontos para Discussão Médica", analysis.discussionPoints)

        for ((title, content) in sections) {
            drawSection(title, content)
        }

        val disclaimerHeight = calculateTextHeight(analysis.disclaimer, disclaimerPaint)
        checkAndAddNewPage(disclaimerHeight + 20f)
        drawWrappedText(analysis.disclaimer, disclaimerPaint)

        drawFooter() // Desenha o rodapé na última página
        document.finishPage(currentPage)

        val file = File(context.cacheDir, "analysis_report_temp.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    private fun startNewPage() {
        if (this::currentPage.isInitialized) {
            drawFooter()
            document.finishPage(currentPage)
        }
        pageNumber++
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        currentPage = document.startPage(pageInfo)
        canvas = currentPage.canvas
        yPosition = TOP_MARGIN
        drawHeader()
    }

    private fun checkAndAddNewPage(requiredHeight: Float) {
        if (yPosition + requiredHeight > BOTTOM_MARGIN) {
            startNewPage()
        }
    }

    private fun drawHeader() {
        val logoSize = 50f
        val logoDestRect = RectF(LEFT_MARGIN, yPosition, LEFT_MARGIN + logoSize, yPosition + logoSize)
        canvas.drawBitmap(currentLogo, null, logoDestRect, null)

        val headerTextX = LEFT_MARGIN + logoSize + 15f
        canvas.drawText("Análise Preditiva de Saúde", headerTextX, yPosition + (logoSize / 2) + 7f, titlePaint)
        yPosition += logoSize + 15f

        canvas.drawText("Dependente: ${currentDependente.nome}", LEFT_MARGIN, yPosition, subtitlePaint)
        yPosition += 15f
        canvas.drawText("Cuidador Responsável: ${currentCuidador.name}", LEFT_MARGIN, yPosition, subtitlePaint)
        yPosition += 30f
    }

    private fun drawFooter() {
        val footerText = "NidusCare - Relatório de Análise - Página $pageNumber"
        canvas.drawText(footerText, (PAGE_WIDTH / 2).toFloat(), PAGE_HEIGHT - 20f, footerPaint)
    }

    private fun drawSection(title: String, content: String) {
        val contentHeight = calculateTextHeight(content, bodyPaint)
        val sectionHeight = 20f + contentHeight + 25f
        checkAndAddNewPage(sectionHeight)

        canvas.drawText(title, LEFT_MARGIN, yPosition, sectionTitlePaint)
        yPosition += 20f

        drawWrappedText(content.ifBlank { "Nenhuma informação disponível." }, bodyPaint)
        yPosition += 25f
    }

    private fun drawHighlightedSection(title: String, content: String) {
        val contentText = content.ifBlank { "Nenhuma informação disponível." }
        val contentHeight = calculateTextHeight(contentText, bodyPaint)
        val sectionHeight = 20f + contentHeight + 40f // Mais padding
        checkAndAddNewPage(sectionHeight)

        // Desenha a caixa de fundo
        canvas.drawRoundRect(LEFT_MARGIN - 10f, yPosition - 5, RIGHT_MARGIN + 10f, yPosition + sectionHeight - 20f, 15f, 15f, highlightBoxPaint)

        canvas.drawText(title, LEFT_MARGIN, yPosition + 15f, highlightTitlePaint)
        yPosition += 40f

        drawWrappedText(contentText, bodyPaint)
        yPosition += 25f
    }

    private fun drawWrappedText(text: String, paint: TextPaint) {
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, CONTENT_WIDTH)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1f)
            .setIncludePad(true)
            .build()

        canvas.save()
        canvas.translate(LEFT_MARGIN, yPosition)
        staticLayout.draw(canvas)
        canvas.restore()

        yPosition += staticLayout.height
    }

    private fun calculateTextHeight(text: String, paint: TextPaint): Float {
        if (text.isBlank()) return 20f // Altura mínima para "Nenhuma informação"
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, CONTENT_WIDTH)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1f)
            .setIncludePad(true)
            .build()
        return staticLayout.height.toFloat()
    }
}