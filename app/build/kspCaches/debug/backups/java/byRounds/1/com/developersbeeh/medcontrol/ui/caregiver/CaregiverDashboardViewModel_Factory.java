package com.developersbeeh.medcontrol.ui.caregiver;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
import com.developersbeeh.medcontrol.data.repository.ScheduleRepository;
import com.developersbeeh.medcontrol.data.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
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
public final class CaregiverDashboardViewModel_Factory implements Factory<CaregiverDashboardViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<ScheduleRepository> scheduleRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private CaregiverDashboardViewModel_Factory(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<ScheduleRepository> scheduleRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.scheduleRepositoryProvider = scheduleRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public CaregiverDashboardViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), medicationRepositoryProvider.get(), userRepositoryProvider.get(), scheduleRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static CaregiverDashboardViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<ScheduleRepository> scheduleRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new CaregiverDashboardViewModel_Factory(firestoreRepositoryProvider, medicationRepositoryProvider, userRepositoryProvider, scheduleRepositoryProvider, userPreferencesProvider);
  }

  public static CaregiverDashboardViewModel newInstance(FirestoreRepository firestoreRepository,
      MedicationRepository medicationRepository, UserRepository userRepository,
      ScheduleRepository scheduleRepository, UserPreferences userPreferences) {
    return new CaregiverDashboardViewModel(firestoreRepository, medicationRepository, userRepository, scheduleRepository, userPreferences);
  }
}
