package com.developersbeeh.medcontrol.ui.scan;

import android.app.Application;
import com.developersbeeh.medcontrol.data.repository.ImageAnalysisRepository;
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
public final class PrescriptionAnalysisViewModel_Factory implements Factory<PrescriptionAnalysisViewModel> {
  private final Provider<ImageAnalysisRepository> imageAnalysisRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<Application> applicationProvider;

  private PrescriptionAnalysisViewModel_Factory(
      Provider<ImageAnalysisRepository> imageAnalysisRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.imageAnalysisRepositoryProvider = imageAnalysisRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public PrescriptionAnalysisViewModel get() {
    return newInstance(imageAnalysisRepositoryProvider.get(), medicationRepositoryProvider.get(), applicationProvider.get());
  }

  public static PrescriptionAnalysisViewModel_Factory create(
      Provider<ImageAnalysisRepository> imageAnalysisRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new PrescriptionAnalysisViewModel_Factory(imageAnalysisRepositoryProvider, medicationRepositoryProvider, applicationProvider);
  }

  public static PrescriptionAnalysisViewModel newInstance(
      ImageAnalysisRepository imageAnalysisRepository, MedicationRepository medicationRepository,
      Application application) {
    return new PrescriptionAnalysisViewModel(imageAnalysisRepository, medicationRepository, application);
  }
}
