package com.developersbeeh.medcontrol.ui.analytics;

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
public final class MedicationAnalyticsViewModel_Factory implements Factory<MedicationAnalyticsViewModel> {
  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private MedicationAnalyticsViewModel_Factory(
      Provider<MedicationRepository> medicationRepositoryProvider) {
    this.medicationRepositoryProvider = medicationRepositoryProvider;
  }

  @Override
  public MedicationAnalyticsViewModel get() {
    return newInstance(medicationRepositoryProvider.get());
  }

  public static MedicationAnalyticsViewModel_Factory create(
      Provider<MedicationRepository> medicationRepositoryProvider) {
    return new MedicationAnalyticsViewModel_Factory(medicationRepositoryProvider);
  }

  public static MedicationAnalyticsViewModel newInstance(
      MedicationRepository medicationRepository) {
    return new MedicationAnalyticsViewModel(medicationRepository);
  }
}
