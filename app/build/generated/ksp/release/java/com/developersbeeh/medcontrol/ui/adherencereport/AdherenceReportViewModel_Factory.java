package com.developersbeeh.medcontrol.ui.adherencereport;

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
public final class AdherenceReportViewModel_Factory implements Factory<AdherenceReportViewModel> {
  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private AdherenceReportViewModel_Factory(
      Provider<MedicationRepository> medicationRepositoryProvider) {
    this.medicationRepositoryProvider = medicationRepositoryProvider;
  }

  @Override
  public AdherenceReportViewModel get() {
    return newInstance(medicationRepositoryProvider.get());
  }

  public static AdherenceReportViewModel_Factory create(
      Provider<MedicationRepository> medicationRepositoryProvider) {
    return new AdherenceReportViewModel_Factory(medicationRepositoryProvider);
  }

  public static AdherenceReportViewModel newInstance(MedicationRepository medicationRepository) {
    return new AdherenceReportViewModel(medicationRepository);
  }
}
