package com.developersbeeh.medcontrol.ui.scan;

import android.app.Application;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
import com.developersbeeh.medcontrol.data.repository.StorageRepository;
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
public final class ScanAndConfirmViewModel_Factory implements Factory<ScanAndConfirmViewModel> {
  private final Provider<StorageRepository> storageRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<Application> applicationProvider;

  private ScanAndConfirmViewModel_Factory(Provider<StorageRepository> storageRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.storageRepositoryProvider = storageRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public ScanAndConfirmViewModel get() {
    return newInstance(storageRepositoryProvider.get(), medicationRepositoryProvider.get(), applicationProvider.get());
  }

  public static ScanAndConfirmViewModel_Factory create(
      Provider<StorageRepository> storageRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new ScanAndConfirmViewModel_Factory(storageRepositoryProvider, medicationRepositoryProvider, applicationProvider);
  }

  public static ScanAndConfirmViewModel newInstance(StorageRepository storageRepository,
      MedicationRepository medicationRepository, Application application) {
    return new ScanAndConfirmViewModel(storageRepository, medicationRepository, application);
  }
}
