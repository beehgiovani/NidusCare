// src/main/java/com/developersbeeh/medcontrol/data/repository/UserRepository.kt
package com.developersbeeh.medcontrol.data.repository

import android.net.Uri
import android.util.Log
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.model.Convite
import com.developersbeeh.medcontrol.data.model.Dependente
import com.developersbeeh.medcontrol.data.model.Familia
import com.developersbeeh.medcontrol.data.model.Sexo
import com.developersbeeh.medcontrol.data.model.StatusConvite
import com.developersbeeh.medcontrol.data.model.TipoSanguineo
import com.developersbeeh.medcontrol.data.model.Usuario
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.snapshots
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

sealed class LoginResult {
    data class Success(val user: FirebaseUser) : LoginResult()
    data class NewUser(val user: FirebaseUser) : LoginResult()
}

@Singleton
class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val functions: FirebaseFunctions,
    private val userPreferences: UserPreferences
) {

    /**
     * Verifica o status premium real do usuário no Firestore e sincroniza com as preferências locais.
     * Esta é a fonte da verdade para o status de assinatura do usuário.
     * @return Retorna 'true' se o usuário for premium (individualmente ou por família), 'false' caso contrário.
     */
    suspend fun syncAndCheckPremiumStatus(): Boolean {
        val userId = auth.currentUser?.uid ?: return false // Se não há usuário, não é premium

        return try {
            val userProfileResult = getUserProfile(userId)
            if (userProfileResult.isFailure) {
                // Se falhar ao buscar o perfil, mantém o status local e retorna-o
                return userPreferences.isPremium()
            }

            val user = userProfileResult.getOrThrow()
            var isPremium = user.premium
            var expiryDate: Timestamp? = null // Inicializa como nulo

            // Prioriza a data de expiração da família se existir
            if (!user.familyId.isNullOrBlank()) {
                val family = getFamilyById(user.familyId)
                if (family != null) {
                    isPremium = true // A adesão à família concede o status premium
                    expiryDate = family.subscriptionExpiryDate
                } else {
                    // Se o ID da família está presente mas a família não existe, corrige os dados do usuário
                    updateUserProfileData(userId, mapOf("familyId" to null, "premium" to false))
                    isPremium = false
                }
            } else {
                expiryDate = user.subscriptionExpiryDate
            }

            // Verifica a data de expiração, se houver
            if (isPremium && expiryDate != null && expiryDate.toDate().before(Date())) {
                isPremium = false
                // O backend deve lidar com isso, mas esta é uma segurança adicional do lado do cliente
                updatePremiumStatus(false)
            }

            // Sincroniza o status final com as preferências locais
            userPreferences.saveIsPremium(isPremium)
            isPremium

        } catch (e: Exception) {
            // Em caso de erro de rede, confia no último status salvo localmente
            Log.e("UserRepository", "Erro ao sincronizar status premium. Usando valor local.", e)
            userPreferences.isPremium()
        }
    }


    private fun saveFcmToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (!token.isNullOrEmpty()) {
                    db.collection("users").document(userId)
                        .update("fcmToken", token)
                }
            }
        }
    }

    suspend fun updatePremiumStatus(isPremium: Boolean): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated."))
        return try {
            db.collection("users").document(userId)
                .update("premium", isPremium)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getReceivedInvites(userEmail: String): Flow<List<Convite>> = callbackFlow {
        val subscription = db.collection("convites")
            .whereEqualTo("destinatarioEmail", userEmail)
            .whereEqualTo("status", StatusConvite.PENDENTE.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Convite::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun savePurchaseDetails(purchase: com.android.billingclient.api.Purchase): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated."))
        return try {
            val purchaseData = mapOf(
                "userId" to userId,
                "purchaseToken" to purchase.purchaseToken,
                "productIds" to purchase.products,
                "purchaseTime" to com.google.firebase.Timestamp(purchase.purchaseTime / 1000, 0),
                "orderId" to purchase.orderId
            )

            db.collection("users").document(userId)
                .collection("purchases")
                .document(purchase.purchaseToken)
                .set(purchaseData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPendingInvitesForDependent(dependenteId: String): Flow<List<Convite>> = callbackFlow {
        val subscription = db.collection("convites")
            .whereEqualTo("dependenteId", dependenteId)
            .whereEqualTo("status", StatusConvite.PENDENTE.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(Convite::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun acceptInvite(invite: Convite): Result<Unit> {
        val newCaregiverId = auth.currentUser?.uid ?: return Result.failure(Exception("Usuário não autenticado."))
        return try {
            db.runTransaction { transaction ->
                val dependentRef = db.collection("dependentes").document(invite.dependenteId)
                val inviteRef = db.collection("convites").document(invite.id)

                transaction.update(dependentRef, "cuidadorIds", FieldValue.arrayUnion(newCaregiverId))
                transaction.update(inviteRef, "status", StatusConvite.ACEITO)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelInvite(inviteId: String): Result<Unit> {
        return try {
            db.collection("convites").document(inviteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeCaregiver(dependentId: String, caregiverIdToRemove: String): Result<Unit> {
        return try {
            val dependentRef = db.collection("dependentes").document(dependentId)
            dependentRef.update("cuidadorIds", FieldValue.arrayRemove(caregiverIdToRemove)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserStatusFlow(userId: String): Flow<Boolean> {
        val userDocRef = db.collection("users").document(userId)
        return callbackFlow {
            val listener = userDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Em caso de erro, consideramos não-premium e fechamos o fluxo
                    trySend(false)
                    close(error)
                    return@addSnapshotListener
                }
                // Se o snapshot for válido, pega o valor do campo "premium"
                val isPremium = snapshot?.getBoolean("premium") ?: false
                trySend(isPremium)
            }
            // Garante que o listener seja removido quando o fluxo for cancelado
            awaitClose { listener.remove() }
        }
    }

    suspend fun getUsersFromIds(userIds: List<String>): Result<List<Usuario>> {
        if (userIds.isEmpty()) {
            return Result.success(emptyList())
        }
        return try {
            val snapshot = db.collection("users").whereIn("id", userIds).get().await()
            Result.success(snapshot.toObjects(Usuario::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findUserByEmail(email: String): Result<Usuario?> {
        return try {
            val snapshot = db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            if (snapshot.isEmpty) {
                Result.success(null)
            } else {
                Result.success(snapshot.documents.first().toObject(Usuario::class.java))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadDependentPhoto(dependentId: String, imageUri: Uri): Result<String> {
        return try {
            val storageRef = storage.reference.child("dependent_photos/$dependentId.jpg")
            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadUserProfilePhoto(userId: String, imageUri: Uri): Result<String> {
        return try {
            val storageRef = storage.reference.child("user_photos/$userId.jpg")
            val uploadTask = storageRef.putFile(imageUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun getUserProfile(userId: String): Result<Usuario> {
        return try {
            val document = db.collection("users").document(userId).get().await()
            val user = document.toObject(Usuario::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Perfil de usuário não encontrado."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: Usuario): Result<Unit> {
        return try {
            val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                displayName = user.name
                photoUri = user.photoUrl?.let { Uri.parse(it) }
            }
            auth.currentUser?.updateProfile(profileUpdates)?.await()
            db.collection("users").document(user.id).set(user).await()

            saveFcmToken(user.id)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ FUNÇÃO ATUALIZADA PARA RETORNAR O ID DO PERFIL CRIADO
    private suspend fun createSelfCareDependentProfile(userProfile: Usuario): String {
        val selfCareDependent = Dependente(
            nome = userProfile.name,
            cuidadorIds = listOf(userProfile.id),
            isSelfCareProfile = true,
            dataDeNascimento = userProfile.dataDeNascimento,
            sexo = userProfile.sexo,
            tipoSanguineo = userProfile.tipoSanguineo,
            peso = userProfile.peso,
            altura = userProfile.altura,
            condicoesPreexistentes = userProfile.condicoesPreexistentes,
            alergias = userProfile.alergias,
            photoUrl = userProfile.photoUrl
        )
        val documentRef = db.collection("dependentes").add(selfCareDependent).await()
        return documentRef.id
    }

    // ✅ FUNÇÃO ATUALIZADA PARA RETORNAR O ID DO PERFIL JUNTO COM O USUÁRIO
    suspend fun createUser(
        name: String, email: String, password: String,
        dataNascimento: String, sexo: String, tipoSanguineo: String,
        peso: String, altura: String, condicoes: String, alergias: String
    ): Result<Pair<FirebaseUser, String>> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user!!

            val sexoEnum = Sexo.values().firstOrNull { it.displayName == sexo } ?: Sexo.NAO_INFORMADO
            val tipoSanguineoEnum = TipoSanguineo.values().firstOrNull { it.displayName == tipoSanguineo } ?: TipoSanguineo.NAO_SABE

            val userProfile = Usuario(
                id = firebaseUser.uid,
                name = name,
                email = email,
                email_lowercase = email.lowercase(),
                dataDeNascimento = dataNascimento,
                sexo = sexoEnum.name,
                tipoSanguineo = tipoSanguineoEnum.name,
                peso = peso,
                altura = altura,
                condicoesPreexistentes = condicoes,
                alergias = alergias,
                profileIncomplete = false
            )
            db.collection("users").document(firebaseUser.uid).set(userProfile).await()

            val selfCareProfileId = createSelfCareDependentProfile(userProfile)

            saveFcmToken(firebaseUser.uid)
            Result.success(Pair(firebaseUser, selfCareProfileId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Usuário não encontrado."))

            saveFcmToken(firebaseUser.uid)

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfileData(userId: String, data: Map<String, Any?>): Result<Unit> {
        return try {
            db.collection("users").document(userId).update(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithSocial(credential: AuthCredential): Result<LoginResult> {
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user!!
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

            if (isNewUser) {
                // Se for um usuário completamente novo no Firebase Auth
                val userEmail = firebaseUser.email ?: ""
                val userName = firebaseUser.displayName ?: "Usuário"
                val userProfile = Usuario(
                    id = firebaseUser.uid,
                    name = userName,
                    email = userEmail,
                    email_lowercase = userEmail.lowercase(),
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    profileIncomplete = true // Marca o perfil como incompleto
                )
                db.collection("users").document(firebaseUser.uid).set(userProfile).await()
                Result.success(LoginResult.NewUser(firebaseUser))
            } else {
                // Se já é um usuário existente, verifica se por acaso o perfil dele está incompleto
                val userProfileResult = getUserProfile(firebaseUser.uid)
                if (userProfileResult.isSuccess && userProfileResult.getOrThrow().profileIncomplete) {
                    Result.success(LoginResult.NewUser(firebaseUser))
                } else {
                    saveFcmToken(firebaseUser.uid)
                    Result.success(LoginResult.Success(firebaseUser))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updateSelfProfileHealthData(
        userId: String,
        dataNascimento: String, sexo: String, tipoSanguineo: String,
        peso: String, altura: String, condicoes: String, alergias: String
    ): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(userId)
            val selfCareProfileQuery = db.collection("dependentes")
                .whereEqualTo("isSelfCareProfile", true)
                .whereArrayContains("cuidadorIds", userId)
                .limit(1)
                .get()
                .await()

            if (selfCareProfileQuery.isEmpty) {
                throw Exception("Perfil de autocuidado não encontrado.")
            }
            val dependentDocRef = selfCareProfileQuery.documents.first().reference

            val dataToUpdate = mapOf(
                "dataDeNascimento" to dataNascimento,
                "sexo" to sexo,
                "tipoSanguineo" to tipoSanguineo,
                "peso" to peso,
                "altura" to altura,
                "condicoesPreexistentes" to condicoes,
                "alergias" to alergias
            )

            db.runBatch { batch ->
                batch.update(userRef, dataToUpdate)
                batch.update(dependentDocRef, dataToUpdate)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finalizeNewGoogleUser(
        user: FirebaseUser,
        password: String,
        dataNascimento: String, sexo: String, tipoSanguineo: String,
        peso: String, altura: String, condicoes: String, alergias: String
    ): Result<Unit> {
        return try {
            val hasPasswordProvider = user.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }

            if (!hasPasswordProvider) {
                val credential = EmailAuthProvider.getCredential(user.email!!, password)
                user.linkWithCredential(credential).await()
            }
            val sexoEnum = Sexo.values().firstOrNull { it.displayName == sexo } ?: Sexo.NAO_INFORMADO
            val tipoSanguineoEnum = TipoSanguineo.values().firstOrNull { it.displayName == tipoSanguineo } ?: TipoSanguineo.NAO_SABE

            val userProfileUpdates = Usuario(
                id = user.uid,
                name = user.displayName ?: "Usuário",
                email = user.email ?: "",
                email_lowercase = user.email?.lowercase() ?: "",
                photoUrl = user.photoUrl?.toString(),
                dataDeNascimento = dataNascimento,
                sexo = sexoEnum.name,
                tipoSanguineo = tipoSanguineoEnum.name,
                peso = peso,
                altura = altura,
                condicoesPreexistentes = condicoes,
                alergias = alergias,
                profileIncomplete = false
            )

            db.collection("users").document(user.uid).set(userProfileUpdates).await()
            createSelfCareDependentProfile(userProfileUpdates)
            saveFcmToken(user.uid)

            Result.success(Unit)
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthRecentLoginRequiredException -> "Sua sessão expirou. Por favor, volte e faça o login com o Google novamente para continuar."
                is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                    if (e.errorCode == "ERROR_CREDENTIAL_ALREADY_IN_USE") {
                        "Este e-mail já está vinculado a outra conta."
                    } else {
                        "Um erro de colisão de usuário ocorreu."
                    }
                }
                else -> e.message ?: "Ocorreu um erro desconhecido ao salvar o perfil."
            }
            Result.failure(Exception(errorMessage))
        }
    }


    suspend fun reauthenticateAndDeleteAllData(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Usuário não está logado."))
            val userId = user.uid
            val userEmail = user.email ?: return Result.failure(Exception("E-mail do usuário não encontrado."))

            Log.d("UserRepository", "Iniciando reautenticação para exclusão de conta.")
            val credential = EmailAuthProvider.getCredential(userEmail, password)
            user.reauthenticate(credential).await()
            Log.d("UserRepository", "Reautenticação bem-sucedida.")

            withContext(Dispatchers.IO) {
                Log.d("UserRepository", "Buscando dependentes associados ao usuário $userId.")
                val dependentsQuery = db.collection("dependentes").whereArrayContains("cuidadorIds", userId).get().await()
                val batch = db.batch()

                for (document in dependentsQuery.documents) {
                    val dependent = document.toObject(Dependente::class.java)?.copy(id = document.id)
                    if (dependent != null) {
                        val dependentRef = db.collection("dependentes").document(dependent.id)

                        if (dependent.cuidadorIds.size == 1 && dependent.cuidadorIds.contains(userId)) {
                            Log.i("UserRepository", "Agendando exclusão do dependente ${dependent.id} (último cuidador).")
                            batch.delete(dependentRef)
                        } else {
                            Log.i("UserRepository", "Removendo cuidador $userId do dependente ${dependent.id}.")
                            batch.update(dependentRef, "cuidadorIds", FieldValue.arrayRemove(userId))
                        }
                    }
                }

                Log.d("UserRepository", "Agendando exclusão do perfil do usuário $userId no Firestore.")
                batch.delete(db.collection("users").document(userId))
                batch.commit().await()
                Log.d("UserRepository", "Operações do Firestore concluídas. Excluindo usuário do Firebase Auth.")
                user.delete().await()
                Log.i("UserRepository", "Conta de usuário $userId excluída com sucesso de todos os serviços.")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Falha na exclusão da conta: ${e.message}", e)
            val errorMessage = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "A senha informada está incorreta. A reautenticação falhou."
                else -> "Ocorreu um erro ao excluir a conta. Tente novamente."
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun getFamilyById(familyId: String): Familia? {
        if (familyId.isBlank()) return null
        return try {
            db.collection("families").document(familyId).get().await()
                .toObject(Familia::class.java)
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun inviteFamilyMember(familyId: String, email: String): Result<String> {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("UserRepository", "ERRO CRÍTICO: Tentativa de convidar membro da família sem um usuário logado.")
            return Result.failure(Exception("Sua sessão não é válida. Por favor, faça o login novamente."))
        }

        return try {
            Log.d("UserRepository", "Forçando a atualização do token de autenticação...")
            currentUser.getIdToken(true).await()
            Log.d("UserRepository", "Token atualizado com sucesso. Chamando a Cloud Function.")

            val data = hashMapOf(
                "familyId" to familyId,
                "email" to email
            )
            val result = functions.getHttpsCallable("inviteFamilyMember").call(data).await()
            val message = (result.data as? Map<String, Any>)?.get("message") as? String ?: "Convite enviado com sucesso"
            Result.success(message)
        } catch (e: Exception) {
            var errorMessage = "Ocorreu um erro desconhecido. Tente novamente."
            if (e is FirebaseFunctionsException) {
                val details = e.details
                errorMessage = if (details is String) {
                    details
                } else {
                    "Erro: ${e.code}. Por favor, tente novamente mais tarde."
                }
            }
            Log.e("UserRepository", "Falha na chamada da Cloud Function 'inviteFamilyMember'. Code: ${ (e as? FirebaseFunctionsException)?.code } Message: ${e.message} Details: ${(e as? FirebaseFunctionsException)?.details}", e)
            Result.failure(Exception(errorMessage))
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun removeFamilyMember(familyId: String, memberId: String): Result<String> {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("UserRepository", "ERRO CRÍTICO: Tentativa de remover membro da família sem um usuário logado.")
            return Result.failure(Exception("Sua sessão não é válida. Por favor, faça o login novamente."))
        }

        return try {
            currentUser.getIdToken(true).await()

            val data = hashMapOf(
                "familyId" to familyId,
                "memberId" to memberId
            )
            val result = functions.getHttpsCallable("removeFamilyMember").call(data).await()
            val message = (result.data as? Map<String, Any>)?.get("message") as? String ?: "Membro removido com sucesso"
            Result.success(message)
        } catch (e: Exception) {
            var errorMessage = "Ocorreu um erro desconhecido. Tente novamente."
            if (e is FirebaseFunctionsException) {
                val details = e.details
                errorMessage = if (details is String) { details } else { "Erro: ${e.code}. Tente novamente." }
            }
            Log.e("UserRepository", "Falha na chamada da Cloud Function 'removeFamilyMember'. Code: ${ (e as? FirebaseFunctionsException)?.code } Message: ${e.message} Details: ${(e as? FirebaseFunctionsException)?.details}", e)
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun isUserPremium(userId: String): Boolean {
        return try {
            val userProfileResult = getUserProfile(userId)
            if (userProfileResult.isFailure) return false

            val user = userProfileResult.getOrThrow()

            if (user.premium) return true

            if (!user.familyId.isNullOrBlank()) {
                val family = getFamilyById(user.familyId)
                return family != null
            }

            false
        } catch (e: Exception) {
            false
        }
    }
    fun listenToUserProfile(userId: String): Flow<Usuario?> {
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot -> snapshot.toObject(Usuario::class.java) }
    }
    suspend fun getSelfCareProfile(userId: String): Dependente? {
        return try {
            val query = db.collection("dependentes")
                .whereEqualTo("isSelfCareProfile", true)
                .whereArrayContains("cuidadorIds", userId)
                .limit(1)
                .get()
                .await()
            query.documents.firstOrNull()?.toObject(Dependente::class.java)
        } catch (e: Exception) {
            null
        }
    }
}