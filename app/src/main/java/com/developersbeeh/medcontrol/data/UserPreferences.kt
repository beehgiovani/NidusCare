// src/main/java/com/developersbeeh/medcontrol/data/UserPreferences.kt
package com.developersbeeh.medcontrol.data

import android.content.Context
import android.content.SharedPreferences
import com.developersbeeh.medcontrol.data.model.Medicamento
import com.developersbeeh.medcontrol.data.model.PermissaoTipo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHOTO_URL = "user_photo_url"
        private const val KEY_IS_CAREGIVER = "is_caregiver"
        private const val KEY_DEPENDENT_ID = "dependent_id"
        private const val KEY_SELECTED_DEPENDENT_NAME = "selected_dependent_name"
        private const val KEY_THEME = "theme_preference"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_HAS_SEEN_DASHBOARD_GUIDE = "has_seen_dashboard_guide"
        private const val KEY_HAS_SEEN_MED_LIST_GUIDE = "has_seen_med_list_guide"
        private const val KEY_DATA_COLLECTION_ENABLED = "data_collection_enabled"
        private const val KEY_DEPENDENT_PERMISSIONS = "dependent_permissions"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_LAST_ANALYSIS_DATE = "last_analysis_date"
        private const val KEY_ANALYSIS_COUNT_TODAY = "analysis_count_today"
        private const val KEY_ADHERENCE_STREAK_COUNT = "adherence_streak_count"
        private const val KEY_LAST_ADHERENCE_CHECK_DATE = "last_adherence_check_date"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_dose_reminders_enabled"
        private const val KEY_MISSED_DOSE_ALERTS_ENABLED = "notifications_missed_dose_enabled"
        private const val KEY_LOW_STOCK_ALERTS_ENABLED = "notifications_low_stock_enabled"
        private const val KEY_EXPIRY_ALERTS_ENABLED = "notifications_expiry_enabled"
        private const val KEY_APPOINTMENT_REMINDERS_ENABLED = "notifications_appointment_enabled"
        private const val KEY_VACCINE_ALERTS_ENABLED = "notifications_vaccine_alerts_enabled"
        private const val KEY_DAILY_SUMMARY_ENABLED = "daily_summary_enabled"
        private const val KEY_DAILY_SUMMARY_TIME = "daily_summary_time"
        private const val KEY_MOTIVATIONAL_NOTIFICATIONS_ENABLED = "motivational_notifications_enabled"
        private const val KEY_HYDRATION_REMINDERS_ENABLED = "hydration_reminders_enabled"
        private const val KEY_MEDICATION_DRAFT = "medication_draft"
        private const val KEY_HAS_SEEN_ADD_MED_GUIDE = "has_seen_add_med_guide"
        private const val KEY_HAS_SEEN_REPORTS_GUIDE = "has_seen_reports_guide"
        private const val KEY_HAS_SEEN_SCANNER_GUIDE = "has_seen_scanner_guide"
        private const val KEY_HAS_SEEN_DOCUMENTS_GUIDE = "has_seen_documents_guide" // ✅ NOVA CHAVE
        private const val KEY_HAS_SEEN_SCHEDULE_GUIDE = "has_seen_schedule_guide" // ✅ NOVA CHAVE
    }

    fun saveUserPhotoUrl(url: String?) {
        sharedPreferences.edit().putString(KEY_USER_PHOTO_URL, url).apply()
    }

    fun getUserPhotoUrl(): String? {
        return sharedPreferences.getString(KEY_USER_PHOTO_URL, null)
    }

    fun setMotivationalNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_MOTIVATIONAL_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isMotivationalNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_MOTIVATIONAL_NOTIFICATIONS_ENABLED, true)
    }

    fun saveAdherenceStreak(dependentId: String, count: Int) {
        sharedPreferences.edit().putInt("${KEY_ADHERENCE_STREAK_COUNT}_${dependentId}", count).apply()
    }

    fun getAdherenceStreak(dependentId: String): Int {
        return sharedPreferences.getInt("${KEY_ADHERENCE_STREAK_COUNT}_${dependentId}", 0)
    }

    fun saveLastAdherenceCheckDate(dependentId: String, date: String) {
        sharedPreferences.edit().putString("${KEY_LAST_ADHERENCE_CHECK_DATE}_${dependentId}", date).apply()
    }

    fun getLastAdherenceCheckDate(dependentId: String): String? {
        return sharedPreferences.getString("${KEY_LAST_ADHERENCE_CHECK_DATE}_${dependentId}", null)
    }

    fun getAnalysisCountToday(): Int {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val lastAnalysisDate = sharedPreferences.getString(KEY_LAST_ANALYSIS_DATE, null)

        return if (todayStr == lastAnalysisDate) {
            sharedPreferences.getInt(KEY_ANALYSIS_COUNT_TODAY, 0)
        } else {
            sharedPreferences.edit()
                .putString(KEY_LAST_ANALYSIS_DATE, todayStr)
                .putInt(KEY_ANALYSIS_COUNT_TODAY, 0)
                .apply()
            0
        }
    }

    fun isDailySummaryEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DAILY_SUMMARY_ENABLED, false)
    }

    fun setDailySummaryEnabled(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DAILY_SUMMARY_ENABLED, isEnabled).apply()
    }

    fun getDailySummaryTime(): String {
        return sharedPreferences.getString(KEY_DAILY_SUMMARY_TIME, "08:00") ?: "08:00"
    }

    fun setDailySummaryTime(time: String) {
        sharedPreferences.edit().putString(KEY_DAILY_SUMMARY_TIME, time).apply()
    }

    fun incrementAnalysisCount() {
        val currentCount = getAnalysisCountToday()
        sharedPreferences.edit().putInt(KEY_ANALYSIS_COUNT_TODAY, currentCount + 1).apply()
    }

    fun saveDependentPermissions(permissions: Map<String, Boolean>) {
        val jsonString = gson.toJson(permissions)
        sharedPreferences.edit().putString(KEY_DEPENDENT_PERMISSIONS, jsonString).apply()
    }

    private fun getPermissionsMap(): Map<String, Boolean> {
        val jsonString = sharedPreferences.getString(KEY_DEPENDENT_PERMISSIONS, null)
        return if (jsonString != null) {
            val type = object : TypeToken<Map<String, Boolean>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            emptyMap()
        }
    }

    fun temPermissao(tipo: PermissaoTipo): Boolean {
        if (getIsCaregiver()) {
            return true
        }
        return getPermissionsMap()[tipo.key] ?: false
    }

    fun saveIsPremium(isPremium: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()
    }

    fun isPremium(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PREMIUM, false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getDataCollectionEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DATA_COLLECTION_ENABLED, true)
    }

    fun setDataCollectionEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DATA_COLLECTION_ENABLED, enabled).apply()
    }

    fun hasSeenDashboardGuide(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_SEEN_DASHBOARD_GUIDE, false)
    }

    fun setDashboardGuideSeen(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_DASHBOARD_GUIDE, seen).apply()
    }

    fun hasSeenMedListGuide(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_SEEN_MED_LIST_GUIDE, false)
    }

    fun setMedListGuideSeen(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_MED_LIST_GUIDE, seen).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(): String {
        return sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
    }

    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String {
        return sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun saveIsCaregiver(isCaregiver: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_CAREGIVER, isCaregiver).apply()
    }

    fun getIsCaregiver(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_CAREGIVER, true)
    }

    fun saveDependentId(id: String) {
        sharedPreferences.edit().putString(KEY_DEPENDENT_ID, id).apply()
    }

    fun getDependentId(): String? {
        return sharedPreferences.getString(KEY_DEPENDENT_ID, null)
    }

    fun saveSelectedDependentName(name: String) {
        sharedPreferences.edit().putString(KEY_SELECTED_DEPENDENT_NAME, name).apply()
    }

    fun getSelectedDependentName(): String? {
        return sharedPreferences.getString(KEY_SELECTED_DEPENDENT_NAME, null)
    }

    fun clearSelectedDependent() {
        sharedPreferences.edit().remove(KEY_DEPENDENT_ID).remove(KEY_SELECTED_DEPENDENT_NAME).apply()
    }

    fun saveTheme(theme: String) {
        sharedPreferences.edit().putString(KEY_THEME, theme).apply()
    }

    fun getTheme(): String {
        return sharedPreferences.getString(KEY_THEME, "system") ?: "system"
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    fun getDoseRemindersEnabled(): Boolean = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    fun setDoseRemindersEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()

    fun getMissedDoseAlertsEnabled(): Boolean = sharedPreferences.getBoolean(KEY_MISSED_DOSE_ALERTS_ENABLED, true)
    fun setMissedDoseAlertsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_MISSED_DOSE_ALERTS_ENABLED, enabled).apply()

    fun getLowStockAlertsEnabled(): Boolean = sharedPreferences.getBoolean(KEY_LOW_STOCK_ALERTS_ENABLED, true)
    fun setLowStockAlertsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_LOW_STOCK_ALERTS_ENABLED, enabled).apply()

    fun getExpiryAlertsEnabled(): Boolean = sharedPreferences.getBoolean(KEY_EXPIRY_ALERTS_ENABLED, true)
    fun setExpiryAlertsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_EXPIRY_ALERTS_ENABLED, enabled).apply()

    fun getAppointmentRemindersEnabled(): Boolean = sharedPreferences.getBoolean(KEY_APPOINTMENT_REMINDERS_ENABLED, true)
    fun setAppointmentRemindersEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_APPOINTMENT_REMINDERS_ENABLED, enabled).apply()

    fun getVaccineAlertsEnabled(): Boolean = sharedPreferences.getBoolean(KEY_VACCINE_ALERTS_ENABLED, true)
    fun setVaccineAlertsEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_VACCINE_ALERTS_ENABLED, enabled).apply()

    fun isHydrationRemindersEnabled(): Boolean = sharedPreferences.getBoolean(KEY_HYDRATION_REMINDERS_ENABLED, true)
    fun setHydrationRemindersEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_HYDRATION_REMINDERS_ENABLED, enabled).apply()

    fun saveMedicationDraft(medicamento: Medicamento) {
        val jsonString = gson.toJson(medicamento)
        sharedPreferences.edit().putString(KEY_MEDICATION_DRAFT, jsonString).apply()
    }

    fun getMedicationDraft(): Medicamento? {
        val jsonString = sharedPreferences.getString(KEY_MEDICATION_DRAFT, null)
        return if (jsonString != null) {
            try {
                gson.fromJson(jsonString, Medicamento::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun clearMedicationDraft() {
        sharedPreferences.edit().remove(KEY_MEDICATION_DRAFT).apply()
    }

    fun hasSeenAddMedGuide(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_SEEN_ADD_MED_GUIDE, false)
    }

    fun setAddMedGuideSeen(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_ADD_MED_GUIDE, seen).apply()
    }

    fun hasSeenReportGuide(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_SEEN_REPORTS_GUIDE, false)
    }

    fun setReportGuideSeen(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_REPORTS_GUIDE, seen).apply()
    }

    fun hasSeenScannerGuide(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_SEEN_SCANNER_GUIDE, false)
    }

    fun setScannerGuideSeen(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_SCANNER_GUIDE, seen).apply()
    }

    fun hasSeenDocumentsGuide(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_SEEN_DOCUMENTS_GUIDE, false)
    }

    fun setDocumentsGuideSeen(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_DOCUMENTS_GUIDE, seen).apply()
    }
    // ✅ NOVAS FUNÇÕES ADICIONADAS
    fun hasSeenScheduleGuide(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_SEEN_SCHEDULE_GUIDE, false)
    }

    fun setScheduleGuideSeen(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_SCHEDULE_GUIDE, seen).apply()
    }
}