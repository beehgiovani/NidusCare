package com.developersbeeh.medcontrol.ui.archive;

import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
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
public final class ArchivedMedicationsViewModel_Factory implements Factory<ArchivedMedicationsViewModel> {
  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<ActivityLogRepository> activityLogRepositoryProvider;

  private ArchivedMedicationsViewModel_Factory(
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider) {
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.activityLogRepositoryProvider = activityLogRepositoryProvider;
  }

  @Override
  public ArchivedMedicationsViewModel get() {
    return newInstance(medicationRepositoryProvider.get(), activityLogRepositoryProvider.get());
  }

  public static ArchivedMedicationsViewModel_Factory create(
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider) {
    return new ArchivedMedicationsViewModel_Factory(medicationRepositoryProvider, activityLogRepositoryProvider);
  }

  public static ArchivedMedicationsViewModel newInstance(MedicationRepository medicationRepository,
      ActivityLogRepository activityLogRepository) {
    return new ArchivedMedicationsViewModel(medicationRepository, activityLogRepository);
  }
}
