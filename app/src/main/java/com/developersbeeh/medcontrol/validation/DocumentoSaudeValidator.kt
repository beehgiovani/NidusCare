package com.developersbeeh.medcontrol.validation

import android.net.Uri
import com.developersbeeh.medcontrol.data.model.DocumentoSaude
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Validador especializado para objetos DocumentoSaude.
 * Implementa validações de dados, arquivos e lógica de negócio.
 */
class DocumentoSaudeValidator {

    companion object {
        private const val MIN_TITULO_LENGTH = 3
        private const val MAX_TITULO_LENGTH = 200
        private const val MAX_ANOTACOES_LENGTH = 2000
        private const val MAX_FILE_SIZE_MB = 20
        private const val MAX_IMAGE_SIZE_MB = 10
        
        // Tipos de arquivo permitidos
        val ALLOWED_DOCUMENT_TYPES = setOf(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/msword" // .doc
        )
        
        val ALLOWED_EXTENSIONS = setOf(
            "pdf", "jpg", "jpeg", "png", "docx", "doc"
        )
    }

    /**
     * Valida um documento completo para salvamento.
     */
    fun validate(documento: DocumentoSaude): ValidationResult {
        val builder = ValidationResultBuilder()

        validateBasicInfo(documento, builder)
        validateDates(documento, builder)
        validateFile(documento, builder)

        return builder.build()
    }

    /**
     * Valida apenas informações básicas do documento.
     */
    fun validateBasicInfo(documento: DocumentoSaude): ValidationResult {
        val builder = ValidationResultBuilder()
        validateBasicInfo(documento, builder)
        return builder.build()
    }

    /**
     * Valida um arquivo antes do upload.
     */
    fun validateFileForUpload(fileUri: Uri?, fileName: String?, fileSizeBytes: Long?, mimeType: String?): ValidationResult {
        val builder = ValidationResultBuilder()

        // Verificar se arquivo foi fornecido
        builder.addErrorIf(
            fileUri == null,
            "arquivo",
            "Nenhum arquivo foi selecionado",
            Severity.CRITICAL
        )

        // Verificar nome do arquivo
        builder.addErrorIf(
            fileName.isNullOrBlank(),
            "arquivo",
            "Nome do arquivo inválido",
            Severity.CRITICAL
        )

        // Verificar extensão do arquivo
        fileName?.let { name ->
            val extension = name.substringAfterLast('.', "").lowercase()
            builder.addErrorIf(
                extension.isBlank() || !ALLOWED_EXTENSIONS.contains(extension),
                "arquivo",
                "Tipo de arquivo não permitido. Tipos aceitos: ${ALLOWED_EXTENSIONS.joinToString(", ")}"
            )
        }

        // Verificar tipo MIME
        mimeType?.let { type ->
            builder.addWarningIf(
                !ALLOWED_DOCUMENT_TYPES.contains(type),
                "arquivo",
                "Tipo MIME do arquivo pode não ser suportado: $type"
            )
        }

        // Verificar tamanho do arquivo
        fileSizeBytes?.let { size ->
            val sizeMB = size / (1024.0 * 1024.0)
            
            // Limite específico para imagens
            if (mimeType?.startsWith("image/") == true) {
                builder.addErrorIf(
                    sizeMB > MAX_IMAGE_SIZE_MB,
                    "arquivo",
                    "Imagem muito grande (${String.format("%.2f", sizeMB)} MB). Tamanho máximo: $MAX_IMAGE_SIZE_MB MB"
                )
            } else {
                builder.addErrorIf(
                    sizeMB > MAX_FILE_SIZE_MB,
                    "arquivo",
                    "Arquivo muito grande (${String.format("%.2f", sizeMB)} MB). Tamanho máximo: $MAX_FILE_SIZE_MB MB"
                )
            }

            // Warning para arquivos grandes (mas dentro do limite)
            builder.addWarningIf(
                sizeMB > 5.0 && sizeMB <= MAX_FILE_SIZE_MB,
                "arquivo",
                "Arquivo grande (${String.format("%.2f", sizeMB)} MB). O upload pode demorar."
            )
        }

        return builder.build()
    }

    /**
     * Valida informações básicas (título, tipo, anotações).
     */
    private fun validateBasicInfo(documento: DocumentoSaude, builder: ValidationResultBuilder) {
        // Validação do título
        builder.addErrorIf(
            documento.titulo.isBlank(),
            "titulo",
            "Título do documento é obrigatório",
            Severity.CRITICAL
        )

        builder.addErrorIf(
            documento.titulo.length < MIN_TITULO_LENGTH,
            "titulo",
            "Título deve ter pelo menos $MIN_TITULO_LENGTH caracteres"
        )

        builder.addErrorIf(
            documento.titulo.length > MAX_TITULO_LENGTH,
            "titulo",
            "Título não pode exceder $MAX_TITULO_LENGTH caracteres"
        )

        // Validação de anotações
        documento.anotacoes?.let { anotacoes ->
            builder.addErrorIf(
                anotacoes.length > MAX_ANOTACOES_LENGTH,
                "anotacoes",
                "Anotações não podem exceder $MAX_ANOTACOES_LENGTH caracteres"
            )
        }

        // Validação de médico solicitante
        documento.medicoSolicitante?.let { medico ->
            builder.addWarningIf(
                medico.length < 3,
                "medicoSolicitante",
                "Nome do médico parece muito curto"
            )
        }

        // Validação de laboratório
        documento.laboratorio?.let { lab ->
            builder.addWarningIf(
                lab.length < 3,
                "laboratorio",
                "Nome do laboratório parece muito curto"
            )
        }

        // Validação do dependentId
        builder.addErrorIf(
            documento.dependentId.isBlank(),
            "dependentId",
            "ID do dependente é obrigatório",
            Severity.CRITICAL
        )
    }

    /**
     * Valida datas do documento.
     */
    private fun validateDates(documento: DocumentoSaude, builder: ValidationResultBuilder) {
        if (documento.dataDocumento.isNotBlank()) {
            try {
                val dataDoc = LocalDate.parse(documento.dataDocumento, DateTimeFormatter.ISO_LOCAL_DATE)
                val hoje = LocalDate.now()

                // Data do documento não pode ser muito no futuro
                builder.addWarningIf(
                    dataDoc.isAfter(hoje.plusDays(7)),
                    "dataDocumento",
                    "Data do documento está no futuro"
                )

                // Warning se documento for muito antigo (mais de 10 anos)
                builder.addWarningIf(
                    dataDoc.isBefore(hoje.minusYears(10)),
                    "dataDocumento",
                    "Documento muito antigo (mais de 10 anos). Verifique se a data está correta."
                )
            } catch (e: Exception) {
                builder.addError(
                    "dataDocumento",
                    "Formato de data inválido. Use o formato AAAA-MM-DD"
                )
            }
        } else {
            builder.addWarning(
                "dataDocumento",
                "Data do documento não foi informada"
            )
        }
    }

    /**
     * Valida informações do arquivo.
     */
    private fun validateFile(documento: DocumentoSaude, builder: ValidationResultBuilder) {
        // Verificar se arquivo foi fornecido
        builder.addWarningIf(
            documento.fileUrl.isBlank(),
            "arquivo",
            "Documento sem arquivo anexado"
        )

        // Verificar se nome do arquivo foi fornecido
        builder.addWarningIf(
            documento.fileName.isBlank(),
            "arquivo",
            "Nome do arquivo não foi informado"
        )

        // Validar extensão do arquivo pelo nome
        if (documento.fileName.isNotBlank()) {
            val extension = documento.fileName.substringAfterLast('.', "").lowercase()
            builder.addWarningIf(
                extension.isBlank() || !ALLOWED_EXTENSIONS.contains(extension),
                "arquivo",
                "Extensão do arquivo pode não ser suportada: $extension"
            )
        }

        // Validar URL do arquivo
        if (documento.fileUrl.isNotBlank()) {
            builder.addWarningIf(
                !documento.fileUrl.startsWith("http://") && !documento.fileUrl.startsWith("https://"),
                "arquivo",
                "URL do arquivo parece inválida"
            )
        }
    }

    /**
     * Valida se um documento pode ser deletado.
     */
    fun validateForDeletion(documento: DocumentoSaude): ValidationResult {
        val builder = ValidationResultBuilder()

        builder.addErrorIf(
            documento.id.isBlank(),
            "id",
            "ID do documento é inválido",
            Severity.CRITICAL
        )

        builder.addErrorIf(
            documento.dependentId.isBlank(),
            "dependentId",
            "ID do dependente é inválido",
            Severity.CRITICAL
        )

        return builder.build()
    }

    /**
     * Valida se um documento pode ser compartilhado.
     */
    fun validateForSharing(documento: DocumentoSaude): ValidationResult {
        val builder = ValidationResultBuilder()

        builder.addErrorIf(
            documento.fileUrl.isBlank(),
            "arquivo",
            "Documento não possui arquivo para compartilhar",
            Severity.CRITICAL
        )

        builder.addErrorIf(
            documento.id.isBlank(),
            "id",
            "ID do documento é inválido",
            Severity.CRITICAL
        )

        return builder.build()
    }
}

