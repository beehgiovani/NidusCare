package com.developersbeeh.medcontrol.ui.dashboard;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
import com.developersbeeh.medcontrol.data.repository.ReminderRepository;
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
public final class DashboardDependenteViewModel_Factory implements Factory<DashboardDependenteViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<ScheduleRepository> scheduleRepositoryProvider;

  private final Provider<ReminderRepository> reminderRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private DashboardDependenteViewModel_Factory(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<ScheduleRepository> scheduleRepositoryProvider,
      Provider<ReminderRepository> reminderRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.scheduleRepositoryProvider = scheduleRepositoryProvider;
    this.reminderRepositoryProvider = reminderRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public DashboardDependenteViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), userRepositoryProvider.get(), medicationRepositoryProvider.get(), scheduleRepositoryProvider.get(), reminderRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static DashboardDependenteViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<ScheduleRepository> scheduleRepositoryProvider,
      Provider<ReminderRepository> reminderRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new DashboardDependenteViewModel_Factory(firestoreRepositoryProvider, userRepositoryProvider, medicationRepositoryProvider, scheduleRepositoryProvider, reminderRepositoryProvider, userPreferencesProvider);
  }

  public static DashboardDependenteViewModel newInstance(FirestoreRepository firestoreRepository,
      UserRepository userRepository, MedicationRepository medicationRepository,
      ScheduleRepository scheduleRepository, ReminderRepository reminderRepository,
      UserPreferences userPreferences) {
    return new DashboardDependenteViewModel(firestoreRepository, userRepository, medicationRepository, scheduleRepository, reminderRepository, userPreferences);
  }
}
