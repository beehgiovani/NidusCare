
// Template de Verificações Null Seguras

// ❌ EVITAR
val user = auth.currentUser!!
val name = user.displayName!!

// ✅ USAR
val user = auth.currentUser ?: run {
    Log.w(TAG, "Usuário não autenticado")
    return
}
val name = user.displayName ?: "Usuário"

// ❌ EVITAR
val firstItem = list.first()

// ✅ USAR
val firstItem = list.firstOrNull() ?: run {
    Log.w(TAG, "Lista vazia")
    return
}

// ❌ EVITAR
val value = map["key"]!!

// ✅ USAR
val value = map["key"] ?: defaultValue

// ❌ EVITAR
viewModel.data.value!!.process()

// ✅ USAR
viewModel.data.value?.let { data ->
    data.process()
} ?: run {
    Log.w(TAG, "Dados não disponíveis")
}
