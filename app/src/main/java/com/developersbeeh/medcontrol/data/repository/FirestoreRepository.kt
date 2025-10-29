package com.developersbeeh.medcontrol.data.repository

import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.developersbeeh.medcontrol.data.model.*
import com.developersbeeh.medcontrol.util.InvalidIdException
import com.developersbeeh.medcontrol.ui.timeline.TimelineFilter
import com.developersbeeh.medcontrol.util.InvalidLinkingCodeException
import com.developersbeeh.medcontrol.util.UserNotAuthenticatedException
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random


private const val TAG = "FirestoreRepository"
private const val PAGE_SIZE = 20

// ✅ ADIÇÃO: PagingSource para o Histórico de Doses
class DoseHistoryPagingSource(
    private val query: Query
) : PagingSource<QuerySnapshot, DoseHistory>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, DoseHistory>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, DoseHistory> {
        return try {
            val currentPage = params.key ?: query.limit(PAGE_SIZE.toLong()).get().await()
            if (currentPage.isEmpty) {
                return LoadResult.Page(emptyList(), null, null)
            }

            val lastVisibleDocument = currentPage.documents.lastOrNull()
            val nextPage = if (lastVisibleDocument != null) {
                query.startAfter(lastVisibleDocument).limit(PAGE_SIZE.toLong()).get().await()
            } else {
                null
            }

            LoadResult.Page(
                data = currentPage.toObjects(DoseHistory::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar página do histórico de doses: ", e)
            LoadResult.Error(e)
        }
    }
}


class TimelinePagingSource(
    private val query: Query
) : PagingSource<QuerySnapshot, TimelineEvent>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, TimelineEvent>): QuerySnapshot? {
        return null // A atualização sempre recarrega do início
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, TimelineEvent> {
        return try {
            val currentPage = params.key ?: query.limit(PAGE_SIZE.toLong()).get().await()
            if (currentPage.isEmpty) {
                return LoadResult.Page(emptyList(), null, null)
            }

            val lastVisibleDocument = currentPage.documents.lastOrNull()
            val nextPage = if (lastVisibleDocument != null) {
                query.startAfter(lastVisibleDocument).limit(PAGE_SIZE.toLong()).get().await()
            } else {
                null
            }

            LoadResult.Page(
                data = currentPage.toObjects(TimelineEvent::class.java),
                prevKey = null, // Apenas paginação para frente
                nextKey = nextPage
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar página da timeline: ", e)
            LoadResult.Error(e)
        }
    }
}


@Singleton
class FirestoreRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val functions: FirebaseFunctions
) {
    private val db = FirebaseFirestore.getInstance()

    // ✅ ADIÇÃO: Nova função que retorna um Pager para o Histórico de Doses
    fun getDoseHistoryPager(dependentId: String, medicationId: String? = null): Pager<QuerySnapshot, DoseHistory> {
        if (dependentId.isBlank()) {
            throw InvalidIdException("ID do dependente é inválido.")
        }
        var query: Query = db.collection("dependentes").document(dependentId)
            .collection("historico_doses")
            .orderBy("timestampString", Query.Direction.DESCENDING)

        if (medicationId != null) {
            query = query.whereEqualTo("medicamentoId", medicationId)
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            pagingSourceFactory = { DoseHistoryPagingSource(query) }
        )
    }


    fun isCaregiver(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun getDependente(dependentId: String): Dependente? {
        if (dependentId.isBlank()) return null
        return try {
            db.collection("dependentes").document(dependentId).get().await()
                .toObject(Dependente::class.java)?.apply { id = dependentId }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar dependente: ${e.message}", e)
            null
        }
    }

    fun getTimelinePager(dependentId: String, filter: TimelineFilter): Pager<QuerySnapshot, TimelineEvent> {
        if (dependentId.isBlank()) {
            throw InvalidIdException("ID do dependente é inválido.")
        }

        // Constrói a query base
        var query: Query = db.collection("dependentes").document(dependentId)
            .collection("timeline")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        // ✅ CORREÇÃO: Adicionado o filtro WELLBEING
        when (filter) {
            TimelineFilter.DOSE -> query = query.whereEqualTo("type", "DOSE")
            TimelineFilter.NOTE -> query = query.whereEqualTo("type", "NOTE")
            TimelineFilter.ACTIVITY -> query = query.whereEqualTo("type", "ACTIVITY")
            TimelineFilter.INSIGHT -> query = query.whereEqualTo("type", "INSIGHT")
            TimelineFilter.WELLBEING -> query = query.whereEqualTo("type", "WELLBEING") // <-- ADICIONADO
            TimelineFilter.ALL -> { /* Não aplica filtro adicional */ }
            // O 'else' não é necessário se o 'when' cobrir todos os casos do enum.
        }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE),
            // O PagingSource agora recebe a query já filtrada
            pagingSourceFactory = { TimelinePagingSource(query) }
        )
    }

    suspend fun saveActivityLog(dependentId: String, activity: Atividade): Result<Unit> {
        return try {
            db.collection("dependentes").document(dependentId)
                .collection("atividades")
                .add(activity)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar log de atividade para o dependente $dependentId", e)
            Result.failure(e)
        }
    }

    suspend fun deleteDependentAndAllData(dependentId: String): Result<Unit> {
        if (dependentId.isBlank()) {
            return Result.failure(InvalidIdException("ID do dependente é inválido."))
        }
        return try {
            db.collection("dependentes").document(dependentId).delete().await()
            Log.i(TAG, "Documento do dependente $dependentId excluído. O gatilho do backend cuidará da limpeza.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao excluir documento do dependente: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getInsights(dependentId: String): Flow<List<Insight>> = callbackFlow {
        val listener = db.collection("dependentes").document(dependentId)
            .collection("insights")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Insight::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveInsight(dependentId: String, insight: Insight): Result<Unit> {
        if (dependentId.isBlank()) {
            return Result.failure(InvalidIdException("ID do dependente é inválido."))
        }
        return try {
            db.collection("dependentes").document(dependentId)
                .collection("insights")
                .add(insight)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar insight para o dependente $dependentId", e)
            Result.failure(e)
        }
    }

    suspend fun markInsightsAsRead(dependentId: String): Result<Unit> {
        return try {
            val insightsRef = db.collection("dependentes").document(dependentId).collection("insights")
            val unreadInsights = insightsRef.whereEqualTo("isRead", false).get().await()

            if (unreadInsights.isEmpty) return Result.success(Unit)

            val batch = db.batch()
            unreadInsights.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao marcar insights como lidos para o dependente $dependentId", e)
            Result.failure(e)
        }
    }

    fun getAnalysisHistory(dependentId: String): Flow<List<AnalysisHistory>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException("ID do dependente é inválido."))
            return@callbackFlow
        }
        val listener = db.collection("dependentes").document(dependentId)
            .collection("analysis_history")
            .orderBy("timestampString", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val history = snapshot.toObjects(AnalysisHistory::class.java)
                    trySend(history)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveAnalysisHistory(analysisHistory: AnalysisHistory): Result<Unit> {
        return try {
            db.collection("dependentes")
                .document(analysisHistory.dependentId)
                .collection("analysis_history")
                .document(analysisHistory.id)
                .set(analysisHistory)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar histórico de análise", e)
            Result.failure(e)
        }
    }

    fun getDependentes(): Flow<List<Dependente>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "Erro ao listar dependentes: Usuário não autenticado.")
            close(UserNotAuthenticatedException())
            return@callbackFlow
        }
        Log.d(TAG, "Buscando dependentes para o cuidador ID: $userId")

        val listener = db.collection("dependentes")
            .whereArrayContains("cuidadorIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erro ao listar dependentes: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val dependents = snapshot.documents.mapNotNull { document ->
                        document.toObject(Dependente::class.java)?.apply { id = document.id }
                    }
                    Log.d(TAG, "${dependents.size} dependentes encontrados.")
                    trySend(dependents)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getDependentCountForCurrentUser(): Int {
        val userId = getCurrentUserId() ?: return 0
        return try {
            val snapshot = db.collection("dependentes")
                .whereArrayContains("cuidadorIds", userId)
                .get().await()

            if (snapshot.isEmpty) return 0

            val dependents = snapshot.toObjects(Dependente::class.java)
            dependents.count { !it.isSelfCareProfile }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao contar dependentes: ${e.message}", e)
            0
        }
    }

    suspend fun createDependent(dependente: Dependente): Result<Dependente> {
        val cuidadorId = auth.currentUser?.uid
            ?: return Result.failure(UserNotAuthenticatedException("Utilizador cuidador não autenticado."))

        return try {
            val codigoDeVinculo = generateLinkingCode()
            val senha = generateNumericPassword()

            val finalDependente = dependente.copy(
                cuidadorIds = listOf(cuidadorId),
                codigoDeVinculo = codigoDeVinculo,
                senha = senha
            ).apply {
                this.dataCriacaoLocalDateTime = LocalDateTime.now()
            }

            val documentRef = db.collection("dependentes").add(finalDependente).await()
            val createdDependente = finalDependente.copy(id = documentRef.id)

            Log.i(TAG, "Dependente '${createdDependente.nome}' criado com sucesso.")
            Result.success(createdDependente)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar dependente: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateDependent(dependente: Dependente): Result<Unit> {
        if (dependente.id.isBlank()) {
            return Result.failure(InvalidIdException("ID do dependente é inválido para atualização."))
        }
        return try {
            db.collection("dependentes").document(dependente.id).set(dependente).await()
            Log.i(TAG, "Dependente '${dependente.nome}' atualizado com sucesso.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar dependente: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun generateNumericPassword(): String {
        return (1..6).map { Random.nextInt(0, 10) }.joinToString("")
    }

    private fun generateLinkingCode(): String {
        val prefix = "med"
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val randomPart = (1..5)
            .map { chars.random() }
            .joinToString("")
        return prefix + randomPart
    }

    suspend fun loginDependente(code: String, pass: String): Result<Dependente> {
        return try {
            Log.d(TAG, "Tentando login com o código: $code")

            val snapshot = db.collection("dependentes")
                .whereEqualTo("codigoDeVinculo", code.lowercase())
                .limit(1)
                .get().await()

            if (snapshot.isEmpty) {
                Log.e(TAG, "Código de vínculo inválido: $code")
                return Result.failure(InvalidLinkingCodeException("Código ou senha inválidos."))
            }

            val document = snapshot.documents.first()
            val dependent = document.toObject(Dependente::class.java)?.copy(id = document.id)
                ?: return Result.failure(Exception("Erro ao processar dados do dependente."))

            if (dependent.senha == pass) {
                Log.i(TAG, "Login do dependente '${dependent.nome}' bem-sucedido!")
                Result.success(dependent)
            } else {
                Log.e(TAG, "Senha incorreta para o código: $code")
                return Result.failure(InvalidLinkingCodeException("Código ou senha inválidos."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer login do dependente: ${e.message}", e)
            Result.failure(e)
        }
    }


    suspend fun deleteDependent(dependentId: String): Result<Unit> {
        if (dependentId.isBlank()) {
            return Result.failure(InvalidIdException("ID do dependente é inválido."))
        }
        return try {
            Log.i(TAG, "Iniciando exclusão do dependente ID: $dependentId e todos os seus dados.")
            val batch = db.batch()
            val dependentDocRef = db.collection("dependentes").document(dependentId)

            val healthDocs = dependentDocRef.collection("documentos_saude").get().await()
            for (doc in healthDocs) {
                val documentoSaude = doc.toObject(DocumentoSaude::class.java)
                if (documentoSaude.fileUrl.isNotBlank()) {
                    try {
                        val storageRef = storage.getReferenceFromUrl(documentoSaude.fileUrl)
                        storageRef.delete().await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Falha ao apagar arquivo do Storage: ${documentoSaude.fileUrl}", e)
                    }
                }
            }
            Log.d(TAG, "Agendada exclusão de arquivos do Storage para ${healthDocs.size()} documentos.")


            val collectionsToDelete = listOf(
                "medicamentos",
                "historico_doses",
                "reminders",
                "health_notes",
                "documentos_saude",
                "agendamentos",
                "atividades",
                "insights",
                "analysis_history"
            )

            for (collection in collectionsToDelete) {
                val docs = dependentDocRef.collection(collection).get().await()
                for (doc in docs) {
                    batch.delete(doc.reference)
                }
                Log.d(TAG, "Agendada exclusão de ${docs.size()} documentos de '$collection'.")
            }

            batch.delete(dependentDocRef)
            batch.commit().await()

            Log.i(TAG, "Dependente ID: $dependentId e todos os seus dados foram excluídos com sucesso.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao excluir dependente: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateDependentName(dependentId: String, newName: String): Result<Unit> {
        if (dependentId.isBlank() || newName.isBlank()) {
            return Result.failure(IllegalArgumentException("ID ou nome não podem ser vazios."))
        }
        return try {
            db.collection("dependentes").document(dependentId)
                .update("nome", newName)
                .await()
            Log.i(TAG, "Nome do dependente $dependentId atualizado para $newName")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar nome do dependente: ${e.message}", e)
            Result.failure(e)
        }
    }
    fun listenToDependentProfile(dependentId: String): Flow<Dependente?> = callbackFlow {
        if (dependentId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = db.collection("dependentes").document(dependentId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val dependent = snapshot.toObject(Dependente::class.java)
                trySend(dependent)
            } else {
                trySend(null)
            }
        }

        awaitClose { listener.remove() }
    }
    suspend fun updateUsaAlarmeTelaCheia(dependentId: String, usaAlarme: Boolean): Result<Unit> {
        if (dependentId.isBlank()) {
            return Result.failure(InvalidIdException("ID do dependente é inválido."))
        }
        return try {
            db.collection("dependentes").document(dependentId)
                .update("usaAlarmeTelaCheia", usaAlarme)
                .await()
            Log.i(TAG, "Configuração de alarme do dependente $dependentId atualizada para $usaAlarme")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar configuração de alarme: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveHealthNote(dependentId: String, note: HealthNote): Result<Unit> {
        if (dependentId.isBlank()) {
            return Result.failure(IllegalArgumentException("ID do dependente é obrigatório."))
        }
        return try {
            db.collection("dependentes")
                .document(dependentId)
                .collection("health_notes")
                .document(note.id)
                .set(note)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getHealthNotes(dependentId: String): Flow<List<HealthNote>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException("ID do dependente é inválido ou nulo."))
            return@callbackFlow
        }
        val listener = db.collection("dependentes").document(dependentId)
            .collection("health_notes")
            .orderBy("timestampString", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Erro ao listar notas de saúde: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notes = snapshot.toObjects(HealthNote::class.java)
                    trySend(notes)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun deleteHealthNote(dependentId: String, noteId: String): Result<Unit> {
        if (dependentId.isBlank() || noteId.isBlank()) {
            return Result.failure(IllegalArgumentException("IDs do dependente e da anotação são obrigatórios."))
        }
        return try {
            db.collection("dependentes")
                .document(dependentId)
                .collection("health_notes")
                .document(noteId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPredictiveAnalysis(
        dependentId: String,
        symptoms: String,
        startDate: LocalDate,
        endDate: LocalDate,
        includeDoseHistory: Boolean,
        includeHealthNotes: Boolean,
        includeContinuousMeds: Boolean,
        dependente: Dependente?
    ): Result<String> {
        val data = hashMapOf(
            "dependentId" to dependentId,
            "symptoms" to symptoms,
            "startDateString" to startDate.toString(),
            "endDateString" to endDate.toString(),
            "includeDoseHistory" to includeDoseHistory,
            "includeHealthNotes" to includeHealthNotes,
            "includeContinuousMeds" to includeContinuousMeds,
            "healthProfile" to mapOf(
                "alergias" to (dependente?.alergias ?: ""),
                "condicoesPreexistentes" to (dependente?.condicoesPreexistentes ?: ""),
                "observacoesMedicas" to (dependente?.observacoesMedicas ?: ""),
                "peso" to (dependente?.peso ?: ""),
                "altura" to (dependente?.altura ?: "")
            )
        )

        return try {
            val result = functions
                .getHttpsCallable("gerarAnalisePreditiva")
                .call(data)
                .await()

            @Suppress("UNCHECKED_CAST")
            val resultMap = result.data as? Map<String, Any>
            val analysisText = resultMap?.get("analysis") as? String

            if (analysisText != null) {
                Result.success(analysisText)
            } else {
                Result.failure(Exception("A resposta da função não continha o campo 'analysis'."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao chamar a Cloud Function 'gerarAnalisePreditiva'", e)
            Result.failure(e)
        }
    }
    suspend fun saveHidratacaoRecord(dependentId: String, hidratacao: Hidratacao): Result<Unit> {
        return try {
            db.collection("dependentes").document(dependentId)
                .collection("hidratacao")
                .document(hidratacao.id)
                .set(hidratacao)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar registro de hidratação", e)
            Result.failure(e)
        }
    }

    fun getHidratacaoHistory(dependentId: String, date: LocalDate): Flow<List<Hidratacao>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException("ID do dependente é inválido."))
            return@callbackFlow
        }

        val dateQueryString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val listener = db.collection("dependentes").document(dependentId)
            .collection("hidratacao")
            .whereEqualTo("dateString", dateQueryString) // Filtra pelo dia exato
            .orderBy("timestampString", Query.Direction.DESCENDING) // Ordena pela hora
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Hidratacao::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveAtividadeFisicaRecord(dependentId: String, atividade: AtividadeFisica): Result<Unit> {
        return try {
            db.collection("dependentes").document(dependentId)
                .collection("atividades_fisicas")
                .document(atividade.id)
                .set(atividade) // Agora 'atividade' contém o 'dateString'
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar registro de atividade física", e)
            Result.failure(e)
        }
    }

    fun getAtividadeFisicaHistory(dependentId: String, date: LocalDate): Flow<List<AtividadeFisica>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException("ID do dependente é inválido."))
            return@callbackFlow
        }

        val dateQueryString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val listener = db.collection("dependentes").document(dependentId)
            .collection("atividades_fisicas")
            .whereEqualTo("dateString", dateQueryString) // Filtra pelo dia exato
            .orderBy("timestampString", Query.Direction.DESCENDING) // Ordena pela hora
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(AtividadeFisica::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveRefeicaoRecord(dependentId: String, refeicao: Refeicao): Result<Unit> {
        return try {
            db.collection("dependentes").document(dependentId)
                .collection("refeicoes")
                .document(refeicao.id)
                .set(refeicao) // Agora 'refeicao' contém o 'dateString'
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar registro de refeição", e)
            Result.failure(e)
        }
    }

    fun getRefeicoesHistory(dependentId: String, date: LocalDate): Flow<List<Refeicao>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException("ID do dependente é inválido."))
            return@callbackFlow
        }

        val dateQueryString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val listener = db.collection("dependentes").document(dependentId)
            .collection("refeicoes")
            .whereEqualTo("dateString", dateQueryString) // Filtra pelo dia exato
            .orderBy("timestampString", Query.Direction.DESCENDING) // Ordena pela hora
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Refeicao::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveSonoRecord(dependentId: String, registroSono: RegistroSono): Result<Unit> {
        return try {
            db.collection("dependentes").document(dependentId)
                .collection("sono_registros")
                .document(registroSono.id)
                .set(registroSono)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar registro de sono", e)
            Result.failure(e)
        }
    }

    fun getSonoHistory(dependentId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<RegistroSono>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException("ID do dependente é inválido."))
            return@callbackFlow
        }
        val listener = db.collection("dependentes").document(dependentId)
            .collection("sono_registros")
            .whereGreaterThanOrEqualTo("data", startDate.toString())
            .whereLessThanOrEqualTo("data", endDate.toString())
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(RegistroSono::class.java))
                }
            }
        awaitClose { listener.remove() }
    }
    suspend fun triggerEmergencyAlert(dependentId: String): Result<Unit> {
        return try {
            val data = hashMapOf("dependentId" to dependentId)
            functions
                .getHttpsCallable("sendEmergencyAlert")
                .call(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao chamar a Cloud Function 'sendEmergencyAlert'", e)
            Result.failure(e)
        }
    }

    fun getHidratacaoHistory(dependentId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<Hidratacao>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException("ID do dependente é inválido."))
            return@callbackFlow
        }

        val startTimestamp = Timestamp(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
        val endTimestamp = Timestamp(Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))

        val listener = db.collection("dependentes").document(dependentId)
            .collection("hidratacao")
            .whereGreaterThanOrEqualTo("timestampString", startDate.toString())
            .whereLessThan("timestampString", endDate.plusDays(1).toString())
            .orderBy("timestampString", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Hidratacao::class.java))
                }
            }
        awaitClose { listener.remove() }
    }


    fun getAtividadeFisicaHistory(dependentId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<AtividadeFisica>> = callbackFlow {
        if (dependentId.isBlank()) {
            close(InvalidIdException("ID do dependente é inválido."))
            return@callbackFlow
        }

        val listener = db.collection("dependentes").document(dependentId)
            .collection("atividades_fisicas")
            .whereGreaterThanOrEqualTo("timestampString", startDate.toString())
            .whereLessThan("timestampString", endDate.plusDays(1).toString())
            .orderBy("timestampString", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(AtividadeFisica::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    fun getRefeicoesHistoryForTimeline(dependentId: String, startDate: LocalDate): Flow<List<Refeicao>> = callbackFlow {
        val listener = db.collection("dependentes").document(dependentId)
            .collection("refeicoes")
            .whereGreaterThanOrEqualTo("timestampString", startDate.toString())
            .orderBy("timestampString", Query.Direction.DESCENDING)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                snapshot?.let { trySend(it.toObjects(Refeicao::class.java)) }
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateDependentWeight(dependentId: String, newWeight: String): Result<Unit> {
        if (dependentId.isBlank() || newWeight.isBlank()) {
            return Result.failure(IllegalArgumentException("ID do dependente e novo peso não podem ser vazios."))
        }
        return try {
            db.collection("dependentes").document(dependentId)
                .update("peso", newWeight)
                .await()
            Log.i(TAG, "Peso do dependente $dependentId atualizado para $newWeight")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar peso do dependente: ${e.message}", e)
            Result.failure(e)
        }
    }
}