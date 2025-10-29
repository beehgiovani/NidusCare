package com.developersbeeh.medcontrol.ui.vaccination;

import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository;
import com.developersbeeh.medcontrol.data.repository.VaccineRepository;
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
public final class VaccinationViewModel_Factory implements Factory<VaccinationViewModel> {
  private final Provider<VaccineRepository> vaccineRepositoryProvider;

  private final Provider<ActivityLogRepository> activityLogRepositoryProvider;

  private VaccinationViewModel_Factory(Provider<VaccineRepository> vaccineRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider) {
    this.vaccineRepositoryProvider = vaccineRepositoryProvider;
    this.activityLogRepositoryProvider = activityLogRepositoryProvider;
  }

  @Override
  public VaccinationViewModel get() {
    return newInstance(vaccineRepositoryProvider.get(), activityLogRepositoryProvider.get());
  }

  public static VaccinationViewModel_Factory create(
      Provider<VaccineRepository> vaccineRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider) {
    return new VaccinationViewModel_Factory(vaccineRepositoryProvider, activityLogRepositoryProvider);
  }

  public static VaccinationViewModel newInstance(VaccineRepository vaccineRepository,
      ActivityLogRepository activityLogRepository) {
    return new VaccinationViewModel(vaccineRepository, activityLogRepository);
  }
}
