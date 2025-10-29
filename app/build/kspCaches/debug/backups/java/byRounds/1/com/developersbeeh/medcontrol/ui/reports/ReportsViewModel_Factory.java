package com.developersbeeh.medcontrol.ui.reports;

import android.app.Application;
import com.developersbeeh.medcontrol.data.repository.CycleRepository;
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
public final class ReportsViewModel_Factory implements Factory<ReportsViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<ScheduleRepository> scheduleRepositoryProvider;

  private final Provider<CycleRepository> cycleRepositoryProvider;

  private final Provider<Application> applicationProvider;

  private ReportsViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<ScheduleRepository> scheduleRepositoryProvider,
      Provider<CycleRepository> cycleRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.scheduleRepositoryProvider = scheduleRepositoryProvider;
    this.cycleRepositoryProvider = cycleRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public ReportsViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), medicationRepositoryProvider.get(), userRepositoryProvider.get(), scheduleRepositoryProvider.get(), cycleRepositoryProvider.get(), applicationProvider.get());
  }

  public static ReportsViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<ScheduleRepository> scheduleRepositoryProvider,
      Provider<CycleRepository> cycleRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new ReportsViewModel_Factory(firestoreRepositoryProvider, medicationRepositoryProvider, userRepositoryProvider, scheduleRepositoryProvider, cycleRepositoryProvider, applicationProvider);
  }

  public static ReportsViewModel newInstance(FirestoreRepository firestoreRepository,
      MedicationRepository medicationRepository, UserRepository userRepository,
      ScheduleRepository scheduleRepository, CycleRepository cycleRepository,
      Application application) {
    return new ReportsViewModel(firestoreRepository, medicationRepository, userRepository, scheduleRepository, cycleRepository, application);
  }
}
