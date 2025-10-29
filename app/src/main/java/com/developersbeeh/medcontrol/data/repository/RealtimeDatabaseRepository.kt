package com.developersbeeh.medcontrol.data.repository

import android.util.Log
import com.developersbeeh.medcontrol.data.model.MedicamentoDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "RealtimeDatabaseRepository"


class RealtimeDatabaseRepository constructor() {

    private val database = FirebaseDatabase.getInstance("https://medcontrol-1ceb9-default-rtdb.firebaseio.com/")
    private val medicamentosRef = database.getReference()

    fun searchMedicamentos(query: String, orderByField: String): Flow<List<MedicamentoDatabase>> = callbackFlow {
        if (query.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        Log.d(TAG, "Buscando medicamentos com query: '$query' no campo: $orderByField")

        val startLetter = query.substring(0, 1).lowercase()

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val medicamentos = mutableListOf<MedicamentoDatabase>()
                for (childSnapshot in snapshot.children) {
                    try {
                        val medicamento = childSnapshot.getValue(MedicamentoDatabase::class.java)
                        medicamento?.let { med ->
                            medicamentos.add(med.copy(id = childSnapshot.key ?: ""))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao converter medicamento: ${e.message}", e)
                    }
                }
                Log.d(TAG, "Firebase retornou ${medicamentos.size} medicamentos candidatos.")
                trySend(medicamentos)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Erro na busca de medicamentos: ${error.message}", error.toException())
                close(error.toException())
            }
        }

        val dbQuery = medicamentosRef.orderByChild(orderByField)
            .startAt(startLetter)
            .endAt(startLetter + "\uf8ff")

        dbQuery.addValueEventListener(listener)

        awaitClose {
            dbQuery.removeEventListener(listener)
        }
    }

    suspend fun getMedicamentoById(id: String): MedicamentoDatabase? {
        return try {
            Log.d(TAG, "Buscando medicamento por ID: $id")
            val snapshot = medicamentosRef.child(id).get().await()
            val medicamento = snapshot.getValue(MedicamentoDatabase::class.java)
            medicamento?.copy(id = id)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar medicamento por ID: ${e.message}", e)
            null
        }
    }
}