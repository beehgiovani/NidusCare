package com.developersbeeh.medcontrol.ui.dosehistory;

import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
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
public final class DoseHistoryViewModel_Factory implements Factory<DoseHistoryViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private DoseHistoryViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
  }

  @Override
  public DoseHistoryViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), medicationRepositoryProvider.get());
  }

  public static DoseHistoryViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider) {
    return new DoseHistoryViewModel_Factory(firestoreRepositoryProvider, medicationRepositoryProvider);
  }

  public static DoseHistoryViewModel newInstance(FirestoreRepository firestoreRepository,
      MedicationRepository medicationRepository) {
    return new DoseHistoryViewModel(firestoreRepository, medicationRepository);
  }
}
