package com.developersbeeh.medcontrol.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class DocumentoSaude(
    @DocumentId
    var id: String = UUID.randomUUID().toString(),
    var dependentId: String = "",
    var titulo: String = "",
    var tipo: TipoDocumento = TipoDocumento.OUTRO,
    var dataDocumento: String = "", // Armazenaremos como String no formato "yyyy-MM-dd"
    var medicoSolicitante: String? = null,
    var laboratorio: String? = null,
    var anotacoes: String? = null,
    var fileUrl: String = "", // URL do arquivo no Firebase Storage
    var fileName: String = "" // Nome do arquivo original para referência
) : Parcelable