package com.developersbeeh.medcontrol.ui.profile

// Data classes para representar os dados do usuário
data class UserProfile(
    val name: String,
    val email: String,
    val isCaregiver: Boolean
)

data class UserStatistics(
    val totalMedications: Int,
    val adherenceRate: Int,
    val daysActive: Int
)