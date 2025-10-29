package com.developersbeeh.medcontrol.ui.pharmacy;

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
public final class PharmacyMedicationSelectionViewModel_Factory implements Factory<PharmacyMedicationSelectionViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private PharmacyMedicationSelectionViewModel_Factory(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
  }

  @Override
  public PharmacyMedicationSelectionViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), medicationRepositoryProvider.get());
  }

  public static PharmacyMedicationSelectionViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider) {
    return new PharmacyMedicationSelectionViewModel_Factory(firestoreRepositoryProvider, medicationRepositoryProvider);
  }

  public static PharmacyMedicationSelectionViewModel newInstance(
      FirestoreRepository firestoreRepository, MedicationRepository medicationRepository) {
    return new PharmacyMedicationSelectionViewModel(firestoreRepository, medicationRepository);
  }
}
