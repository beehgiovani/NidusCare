package com.developersbeeh.medcontrol;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.developersbeeh.medcontrol.audit.AuditLogger;
import com.developersbeeh.medcontrol.billing.BillingClientWrapper;
import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.remote.GooglePlacesApiService;
import com.developersbeeh.medcontrol.data.repository.AchievementRepository;
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository;
import com.developersbeeh.medcontrol.data.repository.ChatRepository;
import com.developersbeeh.medcontrol.data.repository.CycleRepository;
import com.developersbeeh.medcontrol.data.repository.DocumentRepository;
import com.developersbeeh.medcontrol.data.repository.EducationRepository;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.ImageAnalysisRepository;
import com.developersbeeh.medcontrol.data.repository.MealAnalysisRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepositoryEnhanced;
import com.developersbeeh.medcontrol.data.repository.PlacesRepository;
import com.developersbeeh.medcontrol.data.repository.RealtimeDatabaseRepository;
import com.developersbeeh.medcontrol.data.repository.ReminderRepository;
import com.developersbeeh.medcontrol.data.repository.ScheduleRepository;
import com.developersbeeh.medcontrol.data.repository.StorageRepository;
import com.developersbeeh.medcontrol.data.repository.UserRepository;
import com.developersbeeh.medcontrol.data.repository.VaccineRepository;
import com.developersbeeh.medcontrol.di.AppModule_ProvideAchievementRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideActivityLogRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideAnalysisPdfGeneratorFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideBillingClientWrapperFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideChatRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideCycleRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideDocumentRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideEducationRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideFirebaseAuthFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideFirebaseFirestoreFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideFirebaseFunctionsFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideFirebaseStorageFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideFirestoreRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideFusedLocationProviderClientFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideGooglePlacesApiServiceFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideImageAnalysisRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideMealAnalysisRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideMedicationRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideMoshiFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvidePlacesRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideRealtimeDatabaseRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideReminderRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideRetrofitFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideScheduleRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideUserPreferencesFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideUserRepositoryFactory;
import com.developersbeeh.medcontrol.di.AppModule_ProvideVaccineRepositoryFactory;
import com.developersbeeh.medcontrol.notifications.AlarmActivity;
import com.developersbeeh.medcontrol.notifications.BootReceiver;
import com.developersbeeh.medcontrol.notifications.EmergencyAlertActivity;
import com.developersbeeh.medcontrol.notifications.NotificationBroadcastReceiver;
import com.developersbeeh.medcontrol.ui.MainViewModel;
import com.developersbeeh.medcontrol.ui.MainViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.MainViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.MainViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.achievements.AchievementsFragment;
import com.developersbeeh.medcontrol.ui.achievements.AchievementsViewModel;
import com.developersbeeh.medcontrol.ui.achievements.AchievementsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.achievements.AchievementsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.achievements.AchievementsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.activities.PhysicalActivityTrackerFragment;
import com.developersbeeh.medcontrol.ui.activities.PhysicalActivityTrackerViewModel;
import com.developersbeeh.medcontrol.ui.activities.PhysicalActivityTrackerViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.activities.PhysicalActivityTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.activities.PhysicalActivityTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoFragment;
import com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoViewModel;
import com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoViewModelEnhanced;
import com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoViewModelEnhanced_HiltModules;
import com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoViewModelEnhanced_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoViewModelEnhanced_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.adherencereport.AdherenceReportFragment;
import com.developersbeeh.medcontrol.ui.adherencereport.AdherenceReportViewModel;
import com.developersbeeh.medcontrol.ui.adherencereport.AdherenceReportViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.adherencereport.AdherenceReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.adherencereport.AdherenceReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.analysis.AnalysisHistoryFragment;
import com.developersbeeh.medcontrol.ui.analysis.AnalysisHistoryViewModel;
import com.developersbeeh.medcontrol.ui.analysis.AnalysisHistoryViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.analysis.AnalysisHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.analysis.AnalysisHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.analysis.AnalysisResultFragment;
import com.developersbeeh.medcontrol.ui.analysis.AnalysisResultViewModel;
import com.developersbeeh.medcontrol.ui.analysis.AnalysisResultViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.analysis.AnalysisResultViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.analysis.AnalysisResultViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.analytics.HealthChartsFragment;
import com.developersbeeh.medcontrol.ui.analytics.HealthChartsViewModel;
import com.developersbeeh.medcontrol.ui.analytics.HealthChartsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.analytics.HealthChartsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.analytics.HealthChartsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.analytics.MedicationAnalyticsFragment;
import com.developersbeeh.medcontrol.ui.analytics.MedicationAnalyticsViewModel;
import com.developersbeeh.medcontrol.ui.analytics.MedicationAnalyticsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.analytics.MedicationAnalyticsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.analytics.MedicationAnalyticsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.archive.ArchivedMedicationsFragment;
import com.developersbeeh.medcontrol.ui.archive.ArchivedMedicationsViewModel;
import com.developersbeeh.medcontrol.ui.archive.ArchivedMedicationsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.archive.ArchivedMedicationsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.archive.ArchivedMedicationsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.caregiver.AddEditDependentFragment;
import com.developersbeeh.medcontrol.ui.caregiver.AddEditDependentViewModel;
import com.developersbeeh.medcontrol.ui.caregiver.AddEditDependentViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.caregiver.AddEditDependentViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.caregiver.AddEditDependentViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.caregiver.CaregiverDashboardFragment;
import com.developersbeeh.medcontrol.ui.caregiver.CaregiverDashboardViewModel;
import com.developersbeeh.medcontrol.ui.caregiver.CaregiverDashboardViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.caregiver.CaregiverDashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.caregiver.CaregiverDashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.caregiver.ManageCaregiversFragment;
import com.developersbeeh.medcontrol.ui.caregiver.ManageCaregiversViewModel;
import com.developersbeeh.medcontrol.ui.caregiver.ManageCaregiversViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.caregiver.ManageCaregiversViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.caregiver.ManageCaregiversViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.caregiver.ReceivedInvitesFragment;
import com.developersbeeh.medcontrol.ui.chat.ChatFragment;
import com.developersbeeh.medcontrol.ui.chat.ChatViewModel;
import com.developersbeeh.medcontrol.ui.chat.ChatViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.chat.ChatViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.chat.ChatViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.cycletracker.CycleHistoryFragment;
import com.developersbeeh.medcontrol.ui.cycletracker.CycleTrackerFragment;
import com.developersbeeh.medcontrol.ui.cycletracker.CycleTrackerViewModel;
import com.developersbeeh.medcontrol.ui.cycletracker.CycleTrackerViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.cycletracker.CycleTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.cycletracker.CycleTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.dashboard.DashboardDependenteFragment;
import com.developersbeeh.medcontrol.ui.dashboard.DashboardDependenteViewModel;
import com.developersbeeh.medcontrol.ui.dashboard.DashboardDependenteViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.dashboard.DashboardDependenteViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.dashboard.DashboardDependenteViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.dependents.DependentDiaryFragment;
import com.developersbeeh.medcontrol.ui.dependents.DependentDiaryViewModel;
import com.developersbeeh.medcontrol.ui.dependents.DependentDiaryViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.dependents.DependentDiaryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.dependents.DependentDiaryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.dependents.LinkDependentFragment;
import com.developersbeeh.medcontrol.ui.dependents.LinkDependentViewModel;
import com.developersbeeh.medcontrol.ui.dependents.LinkDependentViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.dependents.LinkDependentViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.dependents.LinkDependentViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.documents.AddEditDocumentFragment;
import com.developersbeeh.medcontrol.ui.documents.AddEditDocumentViewModel;
import com.developersbeeh.medcontrol.ui.documents.AddEditDocumentViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.documents.AddEditDocumentViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.documents.AddEditDocumentViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.documents.DocumentViewerFragment;
import com.developersbeeh.medcontrol.ui.documents.HealthDocumentsFragment;
import com.developersbeeh.medcontrol.ui.documents.HealthDocumentsViewModel;
import com.developersbeeh.medcontrol.ui.documents.HealthDocumentsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.documents.HealthDocumentsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.documents.HealthDocumentsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.dosehistory.DoseHistoryFragment;
import com.developersbeeh.medcontrol.ui.dosehistory.DoseHistoryViewModel;
import com.developersbeeh.medcontrol.ui.dosehistory.DoseHistoryViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.dosehistory.DoseHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.dosehistory.DoseHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.education.EducationCenterFragment;
import com.developersbeeh.medcontrol.ui.education.EducationDetailFragment;
import com.developersbeeh.medcontrol.ui.education.EducationViewModel;
import com.developersbeeh.medcontrol.ui.education.EducationViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.education.EducationViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.education.EducationViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.family.ManageFamilyFragment;
import com.developersbeeh.medcontrol.ui.family.ManageFamilyViewModel;
import com.developersbeeh.medcontrol.ui.family.ManageFamilyViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.family.ManageFamilyViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.family.ManageFamilyViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.farmacinha.FarmacinhaFragment;
import com.developersbeeh.medcontrol.ui.farmacinha.FarmacinhaViewModel;
import com.developersbeeh.medcontrol.ui.farmacinha.FarmacinhaViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.farmacinha.FarmacinhaViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.farmacinha.FarmacinhaViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.geriatric.GeriatricCareFragment;
import com.developersbeeh.medcontrol.ui.geriatric.GeriatricCareViewModel;
import com.developersbeeh.medcontrol.ui.geriatric.GeriatricCareViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.geriatric.GeriatricCareViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.geriatric.GeriatricCareViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.goals.HealthGoalsFragment;
import com.developersbeeh.medcontrol.ui.goals.HealthGoalsViewModel;
import com.developersbeeh.medcontrol.ui.goals.HealthGoalsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.goals.HealthGoalsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.goals.HealthGoalsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.healthnotes.AddHealthNoteFragment;
import com.developersbeeh.medcontrol.ui.healthnotes.HealthNotesFragment;
import com.developersbeeh.medcontrol.ui.healthnotes.HealthNotesViewModel;
import com.developersbeeh.medcontrol.ui.healthnotes.HealthNotesViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.healthnotes.HealthNotesViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.healthnotes.HealthNotesViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.hydration.HydrationTrackerFragment;
import com.developersbeeh.medcontrol.ui.hydration.HydrationTrackerViewModel;
import com.developersbeeh.medcontrol.ui.hydration.HydrationTrackerViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.hydration.HydrationTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.hydration.HydrationTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.insights.InsightsFragment;
import com.developersbeeh.medcontrol.ui.insights.InsightsViewModel;
import com.developersbeeh.medcontrol.ui.insights.InsightsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.insights.InsightsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.insights.InsightsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.listmedicamentos.ListMedicamentosFragment;
import com.developersbeeh.medcontrol.ui.listmedicamentos.ListMedicamentosViewModel;
import com.developersbeeh.medcontrol.ui.listmedicamentos.ListMedicamentosViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.listmedicamentos.ListMedicamentosViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.listmedicamentos.ListMedicamentosViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.login.LoginFragment;
import com.developersbeeh.medcontrol.ui.login.LoginViewModel;
import com.developersbeeh.medcontrol.ui.login.LoginViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.login.LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.login.LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.login.RegisterFragment;
import com.developersbeeh.medcontrol.ui.login.RegisterViewModel;
import com.developersbeeh.medcontrol.ui.login.RegisterViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.login.RegisterViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.login.RegisterViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.meals.MealTrackerFragment;
import com.developersbeeh.medcontrol.ui.meals.MealTrackerViewModel;
import com.developersbeeh.medcontrol.ui.meals.MealTrackerViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.meals.MealTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.meals.MealTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.onboarding.OnboardingFragment;
import com.developersbeeh.medcontrol.ui.onboarding.OnboardingViewModel;
import com.developersbeeh.medcontrol.ui.onboarding.OnboardingViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.onboarding.OnboardingViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.onboarding.OnboardingViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.pharmacy.PharmacyMedicationSelectionFragment;
import com.developersbeeh.medcontrol.ui.pharmacy.PharmacyMedicationSelectionViewModel;
import com.developersbeeh.medcontrol.ui.pharmacy.PharmacyMedicationSelectionViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.pharmacy.PharmacyMedicationSelectionViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.pharmacy.PharmacyMedicationSelectionViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.pharmacy.PharmacySelectionFragment;
import com.developersbeeh.medcontrol.ui.pharmacy.PharmacySelectionViewModel;
import com.developersbeeh.medcontrol.ui.pharmacy.PharmacySelectionViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.pharmacy.PharmacySelectionViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.pharmacy.PharmacySelectionViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.premium.PremiumPlansFragment;
import com.developersbeeh.medcontrol.ui.premium.PremiumPlansViewModel;
import com.developersbeeh.medcontrol.ui.premium.PremiumPlansViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.premium.PremiumPlansViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.premium.PremiumPlansViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.profile.CompleteProfileFragment;
import com.developersbeeh.medcontrol.ui.profile.CompleteProfileViewModel;
import com.developersbeeh.medcontrol.ui.profile.CompleteProfileViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.profile.CompleteProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.profile.CompleteProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.profile.EditSelfProfileHealthFragment;
import com.developersbeeh.medcontrol.ui.profile.EditSelfProfileHealthViewModel;
import com.developersbeeh.medcontrol.ui.profile.EditSelfProfileHealthViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.profile.EditSelfProfileHealthViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.profile.EditSelfProfileHealthViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.profile.ProfileEditFragment;
import com.developersbeeh.medcontrol.ui.profile.ProfileEditViewModel;
import com.developersbeeh.medcontrol.ui.profile.ProfileEditViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.profile.ProfileEditViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.profile.ProfileEditViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.profile.ProfileFragment;
import com.developersbeeh.medcontrol.ui.profile.ProfileViewModel;
import com.developersbeeh.medcontrol.ui.profile.ProfileViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.profile.ProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.profile.ProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.reminders.RemindersFragment;
import com.developersbeeh.medcontrol.ui.reminders.RemindersViewModel;
import com.developersbeeh.medcontrol.ui.reminders.RemindersViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.reminders.RemindersViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.reminders.RemindersViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.reports.ReportsFragment;
import com.developersbeeh.medcontrol.ui.reports.ReportsViewModel;
import com.developersbeeh.medcontrol.ui.reports.ReportsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.reports.ReportsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.reports.ReportsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.role.RoleSelectionFragment;
import com.developersbeeh.medcontrol.ui.scan.PrescriptionAnalysisFragment;
import com.developersbeeh.medcontrol.ui.scan.PrescriptionAnalysisViewModel;
import com.developersbeeh.medcontrol.ui.scan.PrescriptionAnalysisViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.scan.PrescriptionAnalysisViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.scan.PrescriptionAnalysisViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.scan.PrescriptionScannerFragment;
import com.developersbeeh.medcontrol.ui.scan.ScanAndConfirmFragment;
import com.developersbeeh.medcontrol.ui.scan.ScanAndConfirmViewModel;
import com.developersbeeh.medcontrol.ui.scan.ScanAndConfirmViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.scan.ScanAndConfirmViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.scan.ScanAndConfirmViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.schedule.AddEditScheduleFragment;
import com.developersbeeh.medcontrol.ui.schedule.AddEditScheduleViewModel;
import com.developersbeeh.medcontrol.ui.schedule.AddEditScheduleViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.schedule.AddEditScheduleViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.schedule.AddEditScheduleViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.schedule.HealthScheduleFragment;
import com.developersbeeh.medcontrol.ui.schedule.HealthScheduleViewModel;
import com.developersbeeh.medcontrol.ui.schedule.HealthScheduleViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.schedule.HealthScheduleViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.schedule.HealthScheduleViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.searchmedicamentos.MedicamentoDetailFragment;
import com.developersbeeh.medcontrol.ui.searchmedicamentos.SearchMedicamentosFragment;
import com.developersbeeh.medcontrol.ui.searchmedicamentos.SearchMedicamentosViewModel;
import com.developersbeeh.medcontrol.ui.searchmedicamentos.SearchMedicamentosViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.searchmedicamentos.SearchMedicamentosViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.searchmedicamentos.SearchMedicamentosViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.settings.AboutFragment;
import com.developersbeeh.medcontrol.ui.settings.NotificationSettingsFragment;
import com.developersbeeh.medcontrol.ui.settings.NotificationSettingsViewModel;
import com.developersbeeh.medcontrol.ui.settings.NotificationSettingsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.settings.NotificationSettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.settings.NotificationSettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.settings.PrivacySettingsFragment;
import com.developersbeeh.medcontrol.ui.settings.PrivacySettingsViewModel;
import com.developersbeeh.medcontrol.ui.settings.PrivacySettingsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.settings.PrivacySettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.settings.PrivacySettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.settings.SettingsFragment;
import com.developersbeeh.medcontrol.ui.settings.SettingsViewModel;
import com.developersbeeh.medcontrol.ui.settings.SettingsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.settings.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.settings.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.settings.ThemeSettingsFragment;
import com.developersbeeh.medcontrol.ui.settings.ThemeSettingsViewModel;
import com.developersbeeh.medcontrol.ui.settings.ThemeSettingsViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.settings.ThemeSettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.settings.ThemeSettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.sleep.SleepTrackerFragment;
import com.developersbeeh.medcontrol.ui.sleep.SleepTrackerViewModel;
import com.developersbeeh.medcontrol.ui.sleep.SleepTrackerViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.sleep.SleepTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.sleep.SleepTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.splash.SplashFragment;
import com.developersbeeh.medcontrol.ui.splash.SplashViewModel;
import com.developersbeeh.medcontrol.ui.splash.SplashViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.splash.SplashViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.splash.SplashViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.timeline.TimelineFragment;
import com.developersbeeh.medcontrol.ui.timeline.TimelineViewModel;
import com.developersbeeh.medcontrol.ui.timeline.TimelineViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.timeline.TimelineViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.timeline.TimelineViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.vaccination.VaccinationCardFragment;
import com.developersbeeh.medcontrol.ui.vaccination.VaccinationViewModel;
import com.developersbeeh.medcontrol.ui.vaccination.VaccinationViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.vaccination.VaccinationViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.vaccination.VaccinationViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.weight.WeightTrackerFragment;
import com.developersbeeh.medcontrol.ui.weight.WeightTrackerViewModel;
import com.developersbeeh.medcontrol.ui.weight.WeightTrackerViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.weight.WeightTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.weight.WeightTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.wellbeing.BemEstarViewModel;
import com.developersbeeh.medcontrol.ui.wellbeing.BemEstarViewModel_HiltModules;
import com.developersbeeh.medcontrol.ui.wellbeing.BemEstarViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.developersbeeh.medcontrol.ui.wellbeing.BemEstarViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.developersbeeh.medcontrol.ui.wellbeing.WellbeingDiaryFragment;
import com.developersbeeh.medcontrol.ui.wellbeing.timer.ActivityTimerService;
import com.developersbeeh.medcontrol.ui.wellbeing.timer.ActivityTimerService_MembersInjector;
import com.developersbeeh.medcontrol.util.AnalysisPdfGenerator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.moshi.Moshi;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideApplicationFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DaggerMedControlApplication_HiltComponents_SingletonC {
  private DaggerMedControlApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public MedControlApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements MedControlApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public MedControlApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements MedControlApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public MedControlApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements MedControlApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public MedControlApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements MedControlApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MedControlApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements MedControlApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public MedControlApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements MedControlApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public MedControlApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements MedControlApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public MedControlApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends MedControlApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends MedControlApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    FragmentCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public void injectAchievementsFragment(AchievementsFragment arg0) {
    }

    @Override
    public void injectPhysicalActivityTrackerFragment(PhysicalActivityTrackerFragment arg0) {
    }

    @Override
    public void injectAddMedicamentoFragment(AddMedicamentoFragment arg0) {
    }

    @Override
    public void injectAdherenceReportFragment(AdherenceReportFragment arg0) {
    }

    @Override
    public void injectAnalysisHistoryFragment(AnalysisHistoryFragment arg0) {
    }

    @Override
    public void injectAnalysisResultFragment(AnalysisResultFragment arg0) {
    }

    @Override
    public void injectHealthChartsFragment(HealthChartsFragment arg0) {
    }

    @Override
    public void injectMedicationAnalyticsFragment(MedicationAnalyticsFragment arg0) {
    }

    @Override
    public void injectArchivedMedicationsFragment(ArchivedMedicationsFragment arg0) {
    }

    @Override
    public void injectAddEditDependentFragment(AddEditDependentFragment arg0) {
    }

    @Override
    public void injectCaregiverDashboardFragment(CaregiverDashboardFragment arg0) {
    }

    @Override
    public void injectManageCaregiversFragment(ManageCaregiversFragment arg0) {
    }

    @Override
    public void injectReceivedInvitesFragment(ReceivedInvitesFragment arg0) {
    }

    @Override
    public void injectChatFragment(ChatFragment arg0) {
    }

    @Override
    public void injectCycleHistoryFragment(CycleHistoryFragment arg0) {
    }

    @Override
    public void injectCycleTrackerFragment(CycleTrackerFragment arg0) {
    }

    @Override
    public void injectDashboardDependenteFragment(DashboardDependenteFragment arg0) {
    }

    @Override
    public void injectDependentDiaryFragment(DependentDiaryFragment arg0) {
    }

    @Override
    public void injectLinkDependentFragment(LinkDependentFragment arg0) {
    }

    @Override
    public void injectAddEditDocumentFragment(AddEditDocumentFragment arg0) {
    }

    @Override
    public void injectDocumentViewerFragment(DocumentViewerFragment arg0) {
    }

    @Override
    public void injectHealthDocumentsFragment(HealthDocumentsFragment arg0) {
    }

    @Override
    public void injectDoseHistoryFragment(DoseHistoryFragment arg0) {
    }

    @Override
    public void injectEducationCenterFragment(EducationCenterFragment arg0) {
    }

    @Override
    public void injectEducationDetailFragment(EducationDetailFragment arg0) {
    }

    @Override
    public void injectManageFamilyFragment(ManageFamilyFragment arg0) {
    }

    @Override
    public void injectFarmacinhaFragment(FarmacinhaFragment arg0) {
    }

    @Override
    public void injectGeriatricCareFragment(GeriatricCareFragment arg0) {
    }

    @Override
    public void injectHealthGoalsFragment(HealthGoalsFragment arg0) {
    }

    @Override
    public void injectAddHealthNoteFragment(AddHealthNoteFragment arg0) {
    }

    @Override
    public void injectHealthNotesFragment(HealthNotesFragment arg0) {
    }

    @Override
    public void injectHydrationTrackerFragment(HydrationTrackerFragment arg0) {
    }

    @Override
    public void injectInsightsFragment(InsightsFragment arg0) {
    }

    @Override
    public void injectListMedicamentosFragment(ListMedicamentosFragment arg0) {
    }

    @Override
    public void injectLoginFragment(LoginFragment arg0) {
    }

    @Override
    public void injectRegisterFragment(RegisterFragment arg0) {
    }

    @Override
    public void injectMealTrackerFragment(MealTrackerFragment arg0) {
    }

    @Override
    public void injectOnboardingFragment(OnboardingFragment arg0) {
    }

    @Override
    public void injectPharmacyMedicationSelectionFragment(
        PharmacyMedicationSelectionFragment arg0) {
    }

    @Override
    public void injectPharmacySelectionFragment(PharmacySelectionFragment arg0) {
    }

    @Override
    public void injectPremiumPlansFragment(PremiumPlansFragment arg0) {
    }

    @Override
    public void injectCompleteProfileFragment(CompleteProfileFragment arg0) {
    }

    @Override
    public void injectEditSelfProfileHealthFragment(EditSelfProfileHealthFragment arg0) {
    }

    @Override
    public void injectProfileEditFragment(ProfileEditFragment arg0) {
    }

    @Override
    public void injectProfileFragment(ProfileFragment arg0) {
    }

    @Override
    public void injectRemindersFragment(RemindersFragment arg0) {
    }

    @Override
    public void injectReportsFragment(ReportsFragment arg0) {
    }

    @Override
    public void injectRoleSelectionFragment(RoleSelectionFragment arg0) {
    }

    @Override
    public void injectPrescriptionAnalysisFragment(PrescriptionAnalysisFragment arg0) {
    }

    @Override
    public void injectPrescriptionScannerFragment(PrescriptionScannerFragment arg0) {
    }

    @Override
    public void injectScanAndConfirmFragment(ScanAndConfirmFragment arg0) {
    }

    @Override
    public void injectAddEditScheduleFragment(AddEditScheduleFragment arg0) {
    }

    @Override
    public void injectHealthScheduleFragment(HealthScheduleFragment arg0) {
    }

    @Override
    public void injectMedicamentoDetailFragment(MedicamentoDetailFragment arg0) {
    }

    @Override
    public void injectSearchMedicamentosFragment(SearchMedicamentosFragment arg0) {
    }

    @Override
    public void injectAboutFragment(AboutFragment arg0) {
    }

    @Override
    public void injectNotificationSettingsFragment(NotificationSettingsFragment arg0) {
    }

    @Override
    public void injectPrivacySettingsFragment(PrivacySettingsFragment arg0) {
    }

    @Override
    public void injectSettingsFragment(SettingsFragment arg0) {
    }

    @Override
    public void injectThemeSettingsFragment(ThemeSettingsFragment arg0) {
    }

    @Override
    public void injectSleepTrackerFragment(SleepTrackerFragment arg0) {
    }

    @Override
    public void injectSplashFragment(SplashFragment arg0) {
    }

    @Override
    public void injectTimelineFragment(TimelineFragment arg0) {
    }

    @Override
    public void injectVaccinationCardFragment(VaccinationCardFragment arg0) {
    }

    @Override
    public void injectWeightTrackerFragment(WeightTrackerFragment arg0) {
    }

    @Override
    public void injectWellbeingDiaryFragment(WellbeingDiaryFragment arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends MedControlApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends MedControlApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    ActivityCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public void injectAlarmActivity(AlarmActivity arg0) {
    }

    @Override
    public void injectEmergencyAlertActivity(EmergencyAlertActivity arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>builderWithExpectedSize(59).put(AchievementsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AchievementsViewModel_HiltModules.KeyModule.provide()).put(AddEditDependentViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddEditDependentViewModel_HiltModules.KeyModule.provide()).put(AddEditDocumentViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddEditDocumentViewModel_HiltModules.KeyModule.provide()).put(AddEditScheduleViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddEditScheduleViewModel_HiltModules.KeyModule.provide()).put(AddMedicamentoViewModelEnhanced_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddMedicamentoViewModelEnhanced_HiltModules.KeyModule.provide()).put(AddMedicamentoViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AddMedicamentoViewModel_HiltModules.KeyModule.provide()).put(AdherenceReportViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AdherenceReportViewModel_HiltModules.KeyModule.provide()).put(AnalysisHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AnalysisHistoryViewModel_HiltModules.KeyModule.provide()).put(AnalysisResultViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AnalysisResultViewModel_HiltModules.KeyModule.provide()).put(ArchivedMedicationsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ArchivedMedicationsViewModel_HiltModules.KeyModule.provide()).put(BemEstarViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, BemEstarViewModel_HiltModules.KeyModule.provide()).put(CaregiverDashboardViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CaregiverDashboardViewModel_HiltModules.KeyModule.provide()).put(ChatViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ChatViewModel_HiltModules.KeyModule.provide()).put(CompleteProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CompleteProfileViewModel_HiltModules.KeyModule.provide()).put(CycleTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CycleTrackerViewModel_HiltModules.KeyModule.provide()).put(DashboardDependenteViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DashboardDependenteViewModel_HiltModules.KeyModule.provide()).put(DependentDiaryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DependentDiaryViewModel_HiltModules.KeyModule.provide()).put(DoseHistoryViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, DoseHistoryViewModel_HiltModules.KeyModule.provide()).put(EditSelfProfileHealthViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, EditSelfProfileHealthViewModel_HiltModules.KeyModule.provide()).put(EducationViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, EducationViewModel_HiltModules.KeyModule.provide()).put(FarmacinhaViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, FarmacinhaViewModel_HiltModules.KeyModule.provide()).put(GeriatricCareViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, GeriatricCareViewModel_HiltModules.KeyModule.provide()).put(HealthChartsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HealthChartsViewModel_HiltModules.KeyModule.provide()).put(HealthDocumentsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HealthDocumentsViewModel_HiltModules.KeyModule.provide()).put(HealthGoalsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HealthGoalsViewModel_HiltModules.KeyModule.provide()).put(HealthNotesViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HealthNotesViewModel_HiltModules.KeyModule.provide()).put(HealthScheduleViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HealthScheduleViewModel_HiltModules.KeyModule.provide()).put(HydrationTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, HydrationTrackerViewModel_HiltModules.KeyModule.provide()).put(InsightsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, InsightsViewModel_HiltModules.KeyModule.provide()).put(LinkDependentViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LinkDependentViewModel_HiltModules.KeyModule.provide()).put(ListMedicamentosViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ListMedicamentosViewModel_HiltModules.KeyModule.provide()).put(LoginViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, LoginViewModel_HiltModules.KeyModule.provide()).put(MainViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MainViewModel_HiltModules.KeyModule.provide()).put(ManageCaregiversViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ManageCaregiversViewModel_HiltModules.KeyModule.provide()).put(ManageFamilyViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ManageFamilyViewModel_HiltModules.KeyModule.provide()).put(MealTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MealTrackerViewModel_HiltModules.KeyModule.provide()).put(MedicationAnalyticsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, MedicationAnalyticsViewModel_HiltModules.KeyModule.provide()).put(NotificationSettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, NotificationSettingsViewModel_HiltModules.KeyModule.provide()).put(OnboardingViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, OnboardingViewModel_HiltModules.KeyModule.provide()).put(PharmacyMedicationSelectionViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PharmacyMedicationSelectionViewModel_HiltModules.KeyModule.provide()).put(PharmacySelectionViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PharmacySelectionViewModel_HiltModules.KeyModule.provide()).put(PhysicalActivityTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PhysicalActivityTrackerViewModel_HiltModules.KeyModule.provide()).put(PremiumPlansViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PremiumPlansViewModel_HiltModules.KeyModule.provide()).put(PrescriptionAnalysisViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PrescriptionAnalysisViewModel_HiltModules.KeyModule.provide()).put(PrivacySettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PrivacySettingsViewModel_HiltModules.KeyModule.provide()).put(ProfileEditViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ProfileEditViewModel_HiltModules.KeyModule.provide()).put(ProfileViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ProfileViewModel_HiltModules.KeyModule.provide()).put(RegisterViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RegisterViewModel_HiltModules.KeyModule.provide()).put(RemindersViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, RemindersViewModel_HiltModules.KeyModule.provide()).put(ReportsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ReportsViewModel_HiltModules.KeyModule.provide()).put(ScanAndConfirmViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ScanAndConfirmViewModel_HiltModules.KeyModule.provide()).put(SearchMedicamentosViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SearchMedicamentosViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).put(SleepTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SleepTrackerViewModel_HiltModules.KeyModule.provide()).put(SplashViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SplashViewModel_HiltModules.KeyModule.provide()).put(ThemeSettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, ThemeSettingsViewModel_HiltModules.KeyModule.provide()).put(TimelineViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TimelineViewModel_HiltModules.KeyModule.provide()).put(VaccinationViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, VaccinationViewModel_HiltModules.KeyModule.provide()).put(WeightTrackerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, WeightTrackerViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends MedControlApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    Provider<AchievementsViewModel> achievementsViewModelProvider;

    Provider<AddEditDependentViewModel> addEditDependentViewModelProvider;

    Provider<AddEditDocumentViewModel> addEditDocumentViewModelProvider;

    Provider<AddEditScheduleViewModel> addEditScheduleViewModelProvider;

    Provider<AddMedicamentoViewModelEnhanced> addMedicamentoViewModelEnhancedProvider;

    Provider<AddMedicamentoViewModel> addMedicamentoViewModelProvider;

    Provider<AdherenceReportViewModel> adherenceReportViewModelProvider;

    Provider<AnalysisHistoryViewModel> analysisHistoryViewModelProvider;

    Provider<AnalysisResultViewModel> analysisResultViewModelProvider;

    Provider<ArchivedMedicationsViewModel> archivedMedicationsViewModelProvider;

    Provider<BemEstarViewModel> bemEstarViewModelProvider;

    Provider<CaregiverDashboardViewModel> caregiverDashboardViewModelProvider;

    Provider<ChatViewModel> chatViewModelProvider;

    Provider<CompleteProfileViewModel> completeProfileViewModelProvider;

    Provider<CycleTrackerViewModel> cycleTrackerViewModelProvider;

    Provider<DashboardDependenteViewModel> dashboardDependenteViewModelProvider;

    Provider<DependentDiaryViewModel> dependentDiaryViewModelProvider;

    Provider<DoseHistoryViewModel> doseHistoryViewModelProvider;

    Provider<EditSelfProfileHealthViewModel> editSelfProfileHealthViewModelProvider;

    Provider<EducationViewModel> educationViewModelProvider;

    Provider<FarmacinhaViewModel> farmacinhaViewModelProvider;

    Provider<GeriatricCareViewModel> geriatricCareViewModelProvider;

    Provider<HealthChartsViewModel> healthChartsViewModelProvider;

    Provider<HealthDocumentsViewModel> healthDocumentsViewModelProvider;

    Provider<HealthGoalsViewModel> healthGoalsViewModelProvider;

    Provider<HealthNotesViewModel> healthNotesViewModelProvider;

    Provider<HealthScheduleViewModel> healthScheduleViewModelProvider;

    Provider<HydrationTrackerViewModel> hydrationTrackerViewModelProvider;

    Provider<InsightsViewModel> insightsViewModelProvider;

    Provider<LinkDependentViewModel> linkDependentViewModelProvider;

    Provider<ListMedicamentosViewModel> listMedicamentosViewModelProvider;

    Provider<LoginViewModel> loginViewModelProvider;

    Provider<MainViewModel> mainViewModelProvider;

    Provider<ManageCaregiversViewModel> manageCaregiversViewModelProvider;

    Provider<ManageFamilyViewModel> manageFamilyViewModelProvider;

    Provider<MealTrackerViewModel> mealTrackerViewModelProvider;

    Provider<MedicationAnalyticsViewModel> medicationAnalyticsViewModelProvider;

    Provider<NotificationSettingsViewModel> notificationSettingsViewModelProvider;

    Provider<OnboardingViewModel> onboardingViewModelProvider;

    Provider<PharmacyMedicationSelectionViewModel> pharmacyMedicationSelectionViewModelProvider;

    Provider<PharmacySelectionViewModel> pharmacySelectionViewModelProvider;

    Provider<PhysicalActivityTrackerViewModel> physicalActivityTrackerViewModelProvider;

    Provider<PremiumPlansViewModel> premiumPlansViewModelProvider;

    Provider<PrescriptionAnalysisViewModel> prescriptionAnalysisViewModelProvider;

    Provider<PrivacySettingsViewModel> privacySettingsViewModelProvider;

    Provider<ProfileEditViewModel> profileEditViewModelProvider;

    Provider<ProfileViewModel> profileViewModelProvider;

    Provider<RegisterViewModel> registerViewModelProvider;

    Provider<RemindersViewModel> remindersViewModelProvider;

    Provider<ReportsViewModel> reportsViewModelProvider;

    Provider<ScanAndConfirmViewModel> scanAndConfirmViewModelProvider;

    Provider<SearchMedicamentosViewModel> searchMedicamentosViewModelProvider;

    Provider<SettingsViewModel> settingsViewModelProvider;

    Provider<SleepTrackerViewModel> sleepTrackerViewModelProvider;

    Provider<SplashViewModel> splashViewModelProvider;

    Provider<ThemeSettingsViewModel> themeSettingsViewModelProvider;

    Provider<TimelineViewModel> timelineViewModelProvider;

    Provider<VaccinationViewModel> vaccinationViewModelProvider;

    Provider<WeightTrackerViewModel> weightTrackerViewModelProvider;

    ViewModelCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        SavedStateHandle savedStateHandleParam, ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);
      initialize2(savedStateHandleParam, viewModelLifecycleParam);
      initialize3(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.achievementsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.addEditDependentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.addEditDocumentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.addEditScheduleViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.addMedicamentoViewModelEnhancedProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.addMedicamentoViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.adherenceReportViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.analysisHistoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.analysisResultViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.archivedMedicationsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
      this.bemEstarViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 10);
      this.caregiverDashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 11);
      this.chatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 12);
      this.completeProfileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 13);
      this.cycleTrackerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 14);
      this.dashboardDependenteViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 15);
      this.dependentDiaryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 16);
      this.doseHistoryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 17);
      this.editSelfProfileHealthViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 18);
      this.educationViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 19);
      this.farmacinhaViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 20);
      this.geriatricCareViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 21);
      this.healthChartsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 22);
      this.healthDocumentsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 23);
      this.healthGoalsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 24);
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.healthNotesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 25);
      this.healthScheduleViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 26);
      this.hydrationTrackerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 27);
      this.insightsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 28);
      this.linkDependentViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 29);
      this.listMedicamentosViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 30);
      this.loginViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 31);
      this.mainViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 32);
      this.manageCaregiversViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 33);
      this.manageFamilyViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 34);
      this.mealTrackerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 35);
      this.medicationAnalyticsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 36);
      this.notificationSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 37);
      this.onboardingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 38);
      this.pharmacyMedicationSelectionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 39);
      this.pharmacySelectionViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 40);
      this.physicalActivityTrackerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 41);
      this.premiumPlansViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 42);
      this.prescriptionAnalysisViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 43);
      this.privacySettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 44);
      this.profileEditViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 45);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 46);
      this.registerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 47);
      this.remindersViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 48);
      this.reportsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 49);
    }

    @SuppressWarnings("unchecked")
    private void initialize3(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.scanAndConfirmViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 50);
      this.searchMedicamentosViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 51);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 52);
      this.sleepTrackerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 53);
      this.splashViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 54);
      this.themeSettingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 55);
      this.timelineViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 56);
      this.vaccinationViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 57);
      this.weightTrackerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 58);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>builderWithExpectedSize(59).put(AchievementsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (achievementsViewModelProvider))).put(AddEditDependentViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (addEditDependentViewModelProvider))).put(AddEditDocumentViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (addEditDocumentViewModelProvider))).put(AddEditScheduleViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (addEditScheduleViewModelProvider))).put(AddMedicamentoViewModelEnhanced_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (addMedicamentoViewModelEnhancedProvider))).put(AddMedicamentoViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (addMedicamentoViewModelProvider))).put(AdherenceReportViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (adherenceReportViewModelProvider))).put(AnalysisHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (analysisHistoryViewModelProvider))).put(AnalysisResultViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (analysisResultViewModelProvider))).put(ArchivedMedicationsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (archivedMedicationsViewModelProvider))).put(BemEstarViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (bemEstarViewModelProvider))).put(CaregiverDashboardViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (caregiverDashboardViewModelProvider))).put(ChatViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (chatViewModelProvider))).put(CompleteProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (completeProfileViewModelProvider))).put(CycleTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (cycleTrackerViewModelProvider))).put(DashboardDependenteViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (dashboardDependenteViewModelProvider))).put(DependentDiaryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (dependentDiaryViewModelProvider))).put(DoseHistoryViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (doseHistoryViewModelProvider))).put(EditSelfProfileHealthViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (editSelfProfileHealthViewModelProvider))).put(EducationViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (educationViewModelProvider))).put(FarmacinhaViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (farmacinhaViewModelProvider))).put(GeriatricCareViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (geriatricCareViewModelProvider))).put(HealthChartsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (healthChartsViewModelProvider))).put(HealthDocumentsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (healthDocumentsViewModelProvider))).put(HealthGoalsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (healthGoalsViewModelProvider))).put(HealthNotesViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (healthNotesViewModelProvider))).put(HealthScheduleViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (healthScheduleViewModelProvider))).put(HydrationTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (hydrationTrackerViewModelProvider))).put(InsightsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (insightsViewModelProvider))).put(LinkDependentViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (linkDependentViewModelProvider))).put(ListMedicamentosViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (listMedicamentosViewModelProvider))).put(LoginViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (loginViewModelProvider))).put(MainViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (mainViewModelProvider))).put(ManageCaregiversViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (manageCaregiversViewModelProvider))).put(ManageFamilyViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (manageFamilyViewModelProvider))).put(MealTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (mealTrackerViewModelProvider))).put(MedicationAnalyticsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (medicationAnalyticsViewModelProvider))).put(NotificationSettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (notificationSettingsViewModelProvider))).put(OnboardingViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (onboardingViewModelProvider))).put(PharmacyMedicationSelectionViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (pharmacyMedicationSelectionViewModelProvider))).put(PharmacySelectionViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (pharmacySelectionViewModelProvider))).put(PhysicalActivityTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (physicalActivityTrackerViewModelProvider))).put(PremiumPlansViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (premiumPlansViewModelProvider))).put(PrescriptionAnalysisViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (prescriptionAnalysisViewModelProvider))).put(PrivacySettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (privacySettingsViewModelProvider))).put(ProfileEditViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (profileEditViewModelProvider))).put(ProfileViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (profileViewModelProvider))).put(RegisterViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (registerViewModelProvider))).put(RemindersViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (remindersViewModelProvider))).put(ReportsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (reportsViewModelProvider))).put(ScanAndConfirmViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (scanAndConfirmViewModelProvider))).put(SearchMedicamentosViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (searchMedicamentosViewModelProvider))).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (settingsViewModelProvider))).put(SleepTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (sleepTrackerViewModelProvider))).put(SplashViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (splashViewModelProvider))).put(ThemeSettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (themeSettingsViewModelProvider))).put(TimelineViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (timelineViewModelProvider))).put(VaccinationViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (vaccinationViewModelProvider))).put(WeightTrackerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) (weightTrackerViewModelProvider))).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.developersbeeh.medcontrol.ui.achievements.AchievementsViewModel
          return (T) new AchievementsViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get(), singletonCImpl.provideActivityLogRepositoryProvider.get(), singletonCImpl.provideUserRepositoryProvider.get());

          case 1: // com.developersbeeh.medcontrol.ui.caregiver.AddEditDependentViewModel
          return (T) new AddEditDependentViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideUserRepositoryProvider.get());

          case 2: // com.developersbeeh.medcontrol.ui.documents.AddEditDocumentViewModel
          return (T) new AddEditDocumentViewModel(singletonCImpl.provideDocumentRepositoryProvider.get(), singletonCImpl.provideAchievementRepositoryProvider.get(), singletonCImpl.provideActivityLogRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.developersbeeh.medcontrol.ui.schedule.AddEditScheduleViewModel
          return (T) new AddEditScheduleViewModel(singletonCImpl.provideScheduleRepositoryProvider.get(), singletonCImpl.provideActivityLogRepositoryProvider.get());

          case 4: // com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoViewModelEnhanced
          return (T) new AddMedicamentoViewModelEnhanced(singletonCImpl.medicationRepositoryEnhancedProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 5: // com.developersbeeh.medcontrol.ui.addmedicamento.AddMedicamentoViewModel
          return (T) new AddMedicamentoViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideAchievementRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 6: // com.developersbeeh.medcontrol.ui.adherencereport.AdherenceReportViewModel
          return (T) new AdherenceReportViewModel(singletonCImpl.provideMedicationRepositoryProvider.get());

          case 7: // com.developersbeeh.medcontrol.ui.analysis.AnalysisHistoryViewModel
          return (T) new AnalysisHistoryViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get());

          case 8: // com.developersbeeh.medcontrol.ui.analysis.AnalysisResultViewModel
          return (T) new AnalysisResultViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideAnalysisPdfGeneratorProvider.get());

          case 9: // com.developersbeeh.medcontrol.ui.archive.ArchivedMedicationsViewModel
          return (T) new ArchivedMedicationsViewModel(singletonCImpl.provideMedicationRepositoryProvider.get(), singletonCImpl.provideActivityLogRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 10: // com.developersbeeh.medcontrol.ui.wellbeing.BemEstarViewModel
          return (T) new BemEstarViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.provideMealAnalysisRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 11: // com.developersbeeh.medcontrol.ui.caregiver.CaregiverDashboardViewModel
          return (T) new CaregiverDashboardViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get(), singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideScheduleRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 12: // com.developersbeeh.medcontrol.ui.chat.ChatViewModel
          return (T) new ChatViewModel(singletonCImpl.provideChatRepositoryProvider.get());

          case 13: // com.developersbeeh.medcontrol.ui.profile.CompleteProfileViewModel
          return (T) new CompleteProfileViewModel(singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideAchievementRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 14: // com.developersbeeh.medcontrol.ui.cycletracker.CycleTrackerViewModel
          return (T) new CycleTrackerViewModel(singletonCImpl.provideCycleRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 15: // com.developersbeeh.medcontrol.ui.dashboard.DashboardDependenteViewModel
          return (T) new DashboardDependenteViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get(), singletonCImpl.provideScheduleRepositoryProvider.get(), singletonCImpl.provideReminderRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 16: // com.developersbeeh.medcontrol.ui.dependents.DependentDiaryViewModel
          return (T) new DependentDiaryViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideActivityLogRepositoryProvider.get());

          case 17: // com.developersbeeh.medcontrol.ui.dosehistory.DoseHistoryViewModel
          return (T) new DoseHistoryViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get());

          case 18: // com.developersbeeh.medcontrol.ui.profile.EditSelfProfileHealthViewModel
          return (T) new EditSelfProfileHealthViewModel(singletonCImpl.provideUserRepositoryProvider.get());

          case 19: // com.developersbeeh.medcontrol.ui.education.EducationViewModel
          return (T) new EducationViewModel(singletonCImpl.provideEducationRepositoryProvider.get());

          case 20: // com.developersbeeh.medcontrol.ui.farmacinha.FarmacinhaViewModel
          return (T) new FarmacinhaViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get(), singletonCImpl.provideActivityLogRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 21: // com.developersbeeh.medcontrol.ui.geriatric.GeriatricCareViewModel
          return (T) new GeriatricCareViewModel(singletonCImpl.provideEducationRepositoryProvider.get(), singletonCImpl.provideReminderRepositoryProvider.get(), singletonCImpl.provideFirestoreRepositoryProvider.get());

          case 22: // com.developersbeeh.medcontrol.ui.analytics.HealthChartsViewModel
          return (T) new HealthChartsViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get());

          case 23: // com.developersbeeh.medcontrol.ui.documents.HealthDocumentsViewModel
          return (T) new HealthDocumentsViewModel(singletonCImpl.provideDocumentRepositoryProvider.get());

          case 24: // com.developersbeeh.medcontrol.ui.goals.HealthGoalsViewModel
          return (T) new HealthGoalsViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get());

          case 25: // com.developersbeeh.medcontrol.ui.healthnotes.HealthNotesViewModel
          return (T) new HealthNotesViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideActivityLogRepositoryProvider.get());

          case 26: // com.developersbeeh.medcontrol.ui.schedule.HealthScheduleViewModel
          return (T) new HealthScheduleViewModel(singletonCImpl.provideScheduleRepositoryProvider.get());

          case 27: // com.developersbeeh.medcontrol.ui.hydration.HydrationTrackerViewModel
          return (T) new HydrationTrackerViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 28: // com.developersbeeh.medcontrol.ui.insights.InsightsViewModel
          return (T) new InsightsViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get());

          case 29: // com.developersbeeh.medcontrol.ui.dependents.LinkDependentViewModel
          return (T) new LinkDependentViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 30: // com.developersbeeh.medcontrol.ui.listmedicamentos.ListMedicamentosViewModel
          return (T) new ListMedicamentosViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideReminderRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get(), singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.provideActivityLogRepositoryProvider.get(), singletonCImpl.provideFirebaseFunctionsProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 31: // com.developersbeeh.medcontrol.ui.login.LoginViewModel
          return (T) new LoginViewModel(singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideUserRepositoryProvider.get());

          case 32: // com.developersbeeh.medcontrol.ui.MainViewModel
          return (T) new MainViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 33: // com.developersbeeh.medcontrol.ui.caregiver.ManageCaregiversViewModel
          return (T) new ManageCaregiversViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideUserRepositoryProvider.get());

          case 34: // com.developersbeeh.medcontrol.ui.family.ManageFamilyViewModel
          return (T) new ManageFamilyViewModel(singletonCImpl.provideUserRepositoryProvider.get());

          case 35: // com.developersbeeh.medcontrol.ui.meals.MealTrackerViewModel
          return (T) new MealTrackerViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 36: // com.developersbeeh.medcontrol.ui.analytics.MedicationAnalyticsViewModel
          return (T) new MedicationAnalyticsViewModel(singletonCImpl.provideMedicationRepositoryProvider.get());

          case 37: // com.developersbeeh.medcontrol.ui.settings.NotificationSettingsViewModel
          return (T) new NotificationSettingsViewModel(singletonCImpl.provideUserPreferencesProvider.get(), singletonCImpl.provideUserRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 38: // com.developersbeeh.medcontrol.ui.onboarding.OnboardingViewModel
          return (T) new OnboardingViewModel();

          case 39: // com.developersbeeh.medcontrol.ui.pharmacy.PharmacyMedicationSelectionViewModel
          return (T) new PharmacyMedicationSelectionViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get());

          case 40: // com.developersbeeh.medcontrol.ui.pharmacy.PharmacySelectionViewModel
          return (T) new PharmacySelectionViewModel(singletonCImpl.providePlacesRepositoryProvider.get(), singletonCImpl.provideFusedLocationProviderClientProvider.get());

          case 41: // com.developersbeeh.medcontrol.ui.activities.PhysicalActivityTrackerViewModel
          return (T) new PhysicalActivityTrackerViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 42: // com.developersbeeh.medcontrol.ui.premium.PremiumPlansViewModel
          return (T) new PremiumPlansViewModel(singletonCImpl.provideBillingClientWrapperProvider.get());

          case 43: // com.developersbeeh.medcontrol.ui.scan.PrescriptionAnalysisViewModel
          return (T) new PrescriptionAnalysisViewModel(singletonCImpl.provideImageAnalysisRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 44: // com.developersbeeh.medcontrol.ui.settings.PrivacySettingsViewModel
          return (T) new PrivacySettingsViewModel(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 45: // com.developersbeeh.medcontrol.ui.profile.ProfileEditViewModel
          return (T) new ProfileEditViewModel(singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 46: // com.developersbeeh.medcontrol.ui.profile.ProfileViewModel
          return (T) new ProfileViewModel(singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideFirebaseAuthProvider.get());

          case 47: // com.developersbeeh.medcontrol.ui.login.RegisterViewModel
          return (T) new RegisterViewModel(singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideAchievementRepositoryProvider.get());

          case 48: // com.developersbeeh.medcontrol.ui.reminders.RemindersViewModel
          return (T) new RemindersViewModel(singletonCImpl.provideReminderRepositoryProvider.get(), singletonCImpl.provideFirestoreRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 49: // com.developersbeeh.medcontrol.ui.reports.ReportsViewModel
          return (T) new ReportsViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get(), singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideScheduleRepositoryProvider.get(), singletonCImpl.provideCycleRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 50: // com.developersbeeh.medcontrol.ui.scan.ScanAndConfirmViewModel
          return (T) new ScanAndConfirmViewModel(singletonCImpl.storageRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 51: // com.developersbeeh.medcontrol.ui.searchmedicamentos.SearchMedicamentosViewModel
          return (T) new SearchMedicamentosViewModel(singletonCImpl.provideRealtimeDatabaseRepositoryProvider.get());

          case 52: // com.developersbeeh.medcontrol.ui.settings.SettingsViewModel
          return (T) new SettingsViewModel(singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 53: // com.developersbeeh.medcontrol.ui.sleep.SleepTrackerViewModel
          return (T) new SleepTrackerViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 54: // com.developersbeeh.medcontrol.ui.splash.SplashViewModel
          return (T) new SplashViewModel(singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 55: // com.developersbeeh.medcontrol.ui.settings.ThemeSettingsViewModel
          return (T) new ThemeSettingsViewModel(ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          case 56: // com.developersbeeh.medcontrol.ui.timeline.TimelineViewModel
          return (T) new TimelineViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get());

          case 57: // com.developersbeeh.medcontrol.ui.vaccination.VaccinationViewModel
          return (T) new VaccinationViewModel(singletonCImpl.provideVaccineRepositoryProvider.get(), singletonCImpl.provideActivityLogRepositoryProvider.get());

          case 58: // com.developersbeeh.medcontrol.ui.weight.WeightTrackerViewModel
          return (T) new WeightTrackerViewModel(singletonCImpl.provideFirestoreRepositoryProvider.get(), ApplicationContextModule_ProvideApplicationFactory.provideApplication(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends MedControlApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends MedControlApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectActivityTimerService(ActivityTimerService arg0) {
      injectActivityTimerService2(arg0);
    }

    @CanIgnoreReturnValue
    private ActivityTimerService injectActivityTimerService2(ActivityTimerService instance) {
      ActivityTimerService_MembersInjector.injectFirestoreRepository(instance, singletonCImpl.provideFirestoreRepositoryProvider.get());
      ActivityTimerService_MembersInjector.injectUserRepository(instance, singletonCImpl.provideUserRepositoryProvider.get());
      ActivityTimerService_MembersInjector.injectUserPreferences(instance, singletonCImpl.provideUserPreferencesProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends MedControlApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    Provider<FirebaseAuth> provideFirebaseAuthProvider;

    Provider<FirebaseStorage> provideFirebaseStorageProvider;

    Provider<FirebaseFunctions> provideFirebaseFunctionsProvider;

    Provider<FirestoreRepository> provideFirestoreRepositoryProvider;

    Provider<FirebaseFirestore> provideFirebaseFirestoreProvider;

    Provider<AchievementRepository> provideAchievementRepositoryProvider;

    Provider<MedicationRepository> provideMedicationRepositoryProvider;

    Provider<ReminderRepository> provideReminderRepositoryProvider;

    Provider<UserPreferences> provideUserPreferencesProvider;

    Provider<ActivityLogRepository> provideActivityLogRepositoryProvider;

    Provider<UserRepository> provideUserRepositoryProvider;

    Provider<DocumentRepository> provideDocumentRepositoryProvider;

    Provider<ScheduleRepository> provideScheduleRepositoryProvider;

    Provider<AuditLogger> auditLoggerProvider;

    Provider<MedicationRepositoryEnhanced> medicationRepositoryEnhancedProvider;

    Provider<AnalysisPdfGenerator> provideAnalysisPdfGeneratorProvider;

    Provider<Moshi> provideMoshiProvider;

    Provider<MealAnalysisRepository> provideMealAnalysisRepositoryProvider;

    Provider<ChatRepository> provideChatRepositoryProvider;

    Provider<CycleRepository> provideCycleRepositoryProvider;

    Provider<EducationRepository> provideEducationRepositoryProvider;

    Provider<Retrofit> provideRetrofitProvider;

    Provider<GooglePlacesApiService> provideGooglePlacesApiServiceProvider;

    Provider<PlacesRepository> providePlacesRepositoryProvider;

    Provider<FusedLocationProviderClient> provideFusedLocationProviderClientProvider;

    Provider<BillingClientWrapper> provideBillingClientWrapperProvider;

    Provider<ImageAnalysisRepository> provideImageAnalysisRepositoryProvider;

    Provider<StorageRepository> storageRepositoryProvider;

    Provider<RealtimeDatabaseRepository> provideRealtimeDatabaseRepositoryProvider;

    Provider<VaccineRepository> provideVaccineRepositoryProvider;

    SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);
      initialize2(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideFirebaseAuthProvider = DoubleCheck.provider(new SwitchingProvider<FirebaseAuth>(singletonCImpl, 1));
      this.provideFirebaseStorageProvider = DoubleCheck.provider(new SwitchingProvider<FirebaseStorage>(singletonCImpl, 2));
      this.provideFirebaseFunctionsProvider = DoubleCheck.provider(new SwitchingProvider<FirebaseFunctions>(singletonCImpl, 3));
      this.provideFirestoreRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FirestoreRepository>(singletonCImpl, 0));
      this.provideFirebaseFirestoreProvider = DoubleCheck.provider(new SwitchingProvider<FirebaseFirestore>(singletonCImpl, 5));
      this.provideAchievementRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AchievementRepository>(singletonCImpl, 6));
      this.provideMedicationRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MedicationRepository>(singletonCImpl, 4));
      this.provideReminderRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ReminderRepository>(singletonCImpl, 7));
      this.provideUserPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<UserPreferences>(singletonCImpl, 9));
      this.provideActivityLogRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ActivityLogRepository>(singletonCImpl, 8));
      this.provideUserRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<UserRepository>(singletonCImpl, 10));
      this.provideDocumentRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DocumentRepository>(singletonCImpl, 11));
      this.provideScheduleRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ScheduleRepository>(singletonCImpl, 12));
      this.auditLoggerProvider = DoubleCheck.provider(new SwitchingProvider<AuditLogger>(singletonCImpl, 14));
      this.medicationRepositoryEnhancedProvider = DoubleCheck.provider(new SwitchingProvider<MedicationRepositoryEnhanced>(singletonCImpl, 13));
      this.provideAnalysisPdfGeneratorProvider = DoubleCheck.provider(new SwitchingProvider<AnalysisPdfGenerator>(singletonCImpl, 15));
      this.provideMoshiProvider = DoubleCheck.provider(new SwitchingProvider<Moshi>(singletonCImpl, 17));
      this.provideMealAnalysisRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MealAnalysisRepository>(singletonCImpl, 16));
      this.provideChatRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ChatRepository>(singletonCImpl, 18));
      this.provideCycleRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<CycleRepository>(singletonCImpl, 19));
      this.provideEducationRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<EducationRepository>(singletonCImpl, 20));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 23));
      this.provideGooglePlacesApiServiceProvider = DoubleCheck.provider(new SwitchingProvider<GooglePlacesApiService>(singletonCImpl, 22));
      this.providePlacesRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PlacesRepository>(singletonCImpl, 21));
      this.provideFusedLocationProviderClientProvider = DoubleCheck.provider(new SwitchingProvider<FusedLocationProviderClient>(singletonCImpl, 24));
    }

    @SuppressWarnings("unchecked")
    private void initialize2(final ApplicationContextModule applicationContextModuleParam) {
      this.provideBillingClientWrapperProvider = DoubleCheck.provider(new SwitchingProvider<BillingClientWrapper>(singletonCImpl, 25));
      this.provideImageAnalysisRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ImageAnalysisRepository>(singletonCImpl, 26));
      this.storageRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<StorageRepository>(singletonCImpl, 27));
      this.provideRealtimeDatabaseRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RealtimeDatabaseRepository>(singletonCImpl, 28));
      this.provideVaccineRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<VaccineRepository>(singletonCImpl, 29));
    }

    @Override
    public void injectMedControlApplication(MedControlApplication arg0) {
    }

    @Override
    public FirestoreRepository getFirestoreRepository() {
      return provideFirestoreRepositoryProvider.get();
    }

    @Override
    public MedicationRepository getMedicationRepository() {
      return provideMedicationRepositoryProvider.get();
    }

    @Override
    public ReminderRepository getReminderRepository() {
      return provideReminderRepositoryProvider.get();
    }

    @Override
    public void injectBootReceiver(BootReceiver arg0) {
    }

    @Override
    public void injectNotificationBroadcastReceiver(NotificationBroadcastReceiver arg0) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @Override
      @SuppressWarnings("unchecked")
      public T get() {
        switch (id) {
          case 0: // com.developersbeeh.medcontrol.data.repository.FirestoreRepository
          return (T) AppModule_ProvideFirestoreRepositoryFactory.provideFirestoreRepository(singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideFirebaseStorageProvider.get(), singletonCImpl.provideFirebaseFunctionsProvider.get());

          case 1: // com.google.firebase.auth.FirebaseAuth
          return (T) AppModule_ProvideFirebaseAuthFactory.provideFirebaseAuth();

          case 2: // com.google.firebase.storage.FirebaseStorage
          return (T) AppModule_ProvideFirebaseStorageFactory.provideFirebaseStorage();

          case 3: // com.google.firebase.functions.FirebaseFunctions
          return (T) AppModule_ProvideFirebaseFunctionsFactory.provideFirebaseFunctions();

          case 4: // com.developersbeeh.medcontrol.data.repository.MedicationRepository
          return (T) AppModule_ProvideMedicationRepositoryFactory.provideMedicationRepository(singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideAchievementRepositoryProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.google.firebase.firestore.FirebaseFirestore
          return (T) AppModule_ProvideFirebaseFirestoreFactory.provideFirebaseFirestore();

          case 6: // com.developersbeeh.medcontrol.data.repository.AchievementRepository
          return (T) AppModule_ProvideAchievementRepositoryFactory.provideAchievementRepository(singletonCImpl.provideFirebaseFirestoreProvider.get());

          case 7: // com.developersbeeh.medcontrol.data.repository.ReminderRepository
          return (T) AppModule_ProvideReminderRepositoryFactory.provideReminderRepository(singletonCImpl.provideFirebaseFirestoreProvider.get());

          case 8: // com.developersbeeh.medcontrol.data.repository.ActivityLogRepository
          return (T) AppModule_ProvideActivityLogRepositoryFactory.provideActivityLogRepository(singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 9: // com.developersbeeh.medcontrol.data.UserPreferences
          return (T) AppModule_ProvideUserPreferencesFactory.provideUserPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 10: // com.developersbeeh.medcontrol.data.repository.UserRepository
          return (T) AppModule_ProvideUserRepositoryFactory.provideUserRepository(singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.provideFirebaseStorageProvider.get(), singletonCImpl.provideFirebaseFunctionsProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 11: // com.developersbeeh.medcontrol.data.repository.DocumentRepository
          return (T) AppModule_ProvideDocumentRepositoryFactory.provideDocumentRepository(singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.provideFirebaseStorageProvider.get());

          case 12: // com.developersbeeh.medcontrol.data.repository.ScheduleRepository
          return (T) AppModule_ProvideScheduleRepositoryFactory.provideScheduleRepository(singletonCImpl.provideFirebaseFirestoreProvider.get());

          case 13: // com.developersbeeh.medcontrol.data.repository.MedicationRepositoryEnhanced
          return (T) new MedicationRepositoryEnhanced(singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideAchievementRepositoryProvider.get(), singletonCImpl.auditLoggerProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 14: // com.developersbeeh.medcontrol.audit.AuditLogger
          return (T) new AuditLogger(singletonCImpl.provideFirebaseFirestoreProvider.get());

          case 15: // com.developersbeeh.medcontrol.util.AnalysisPdfGenerator
          return (T) AppModule_ProvideAnalysisPdfGeneratorFactory.provideAnalysisPdfGenerator(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 16: // com.developersbeeh.medcontrol.data.repository.MealAnalysisRepository
          return (T) AppModule_ProvideMealAnalysisRepositoryFactory.provideMealAnalysisRepository(singletonCImpl.provideFirebaseFunctionsProvider.get(), singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideFirebaseStorageProvider.get(), singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideMoshiProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 17: // com.squareup.moshi.Moshi
          return (T) AppModule_ProvideMoshiFactory.provideMoshi();

          case 18: // com.developersbeeh.medcontrol.data.repository.ChatRepository
          return (T) AppModule_ProvideChatRepositoryFactory.provideChatRepository(singletonCImpl.provideFirebaseFirestoreProvider.get(), singletonCImpl.provideFirebaseFunctionsProvider.get());

          case 19: // com.developersbeeh.medcontrol.data.repository.CycleRepository
          return (T) AppModule_ProvideCycleRepositoryFactory.provideCycleRepository(singletonCImpl.provideFirebaseFirestoreProvider.get());

          case 20: // com.developersbeeh.medcontrol.data.repository.EducationRepository
          return (T) AppModule_ProvideEducationRepositoryFactory.provideEducationRepository();

          case 21: // com.developersbeeh.medcontrol.data.repository.PlacesRepository
          return (T) AppModule_ProvidePlacesRepositoryFactory.providePlacesRepository(singletonCImpl.provideGooglePlacesApiServiceProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 22: // com.developersbeeh.medcontrol.data.remote.GooglePlacesApiService
          return (T) AppModule_ProvideGooglePlacesApiServiceFactory.provideGooglePlacesApiService(singletonCImpl.provideRetrofitProvider.get());

          case 23: // retrofit2.Retrofit
          return (T) AppModule_ProvideRetrofitFactory.provideRetrofit();

          case 24: // com.google.android.gms.location.FusedLocationProviderClient
          return (T) AppModule_ProvideFusedLocationProviderClientFactory.provideFusedLocationProviderClient(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 25: // com.developersbeeh.medcontrol.billing.BillingClientWrapper
          return (T) AppModule_ProvideBillingClientWrapperFactory.provideBillingClientWrapper(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideUserRepositoryProvider.get(), singletonCImpl.provideUserPreferencesProvider.get());

          case 26: // com.developersbeeh.medcontrol.data.repository.ImageAnalysisRepository
          return (T) AppModule_ProvideImageAnalysisRepositoryFactory.provideImageAnalysisRepository(singletonCImpl.provideFirebaseFunctionsProvider.get(), singletonCImpl.provideFirebaseAuthProvider.get(), singletonCImpl.provideFirebaseStorageProvider.get(), singletonCImpl.provideFirestoreRepositoryProvider.get(), singletonCImpl.provideMedicationRepositoryProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 27: // com.developersbeeh.medcontrol.data.repository.StorageRepository
          return (T) new StorageRepository(singletonCImpl.provideFirebaseStorageProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 28: // com.developersbeeh.medcontrol.data.repository.RealtimeDatabaseRepository
          return (T) AppModule_ProvideRealtimeDatabaseRepositoryFactory.provideRealtimeDatabaseRepository();

          case 29: // com.developersbeeh.medcontrol.data.repository.VaccineRepository
          return (T) AppModule_ProvideVaccineRepositoryFactory.provideVaccineRepository(singletonCImpl.provideFirebaseFirestoreProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
