package com.developersbeeh.medcontrol.ui.cycletracker;

import com.developersbeeh.medcontrol.data.repository.CycleRepository;
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
public final class CycleTrackerViewModel_Factory implements Factory<CycleTrackerViewModel> {
  private final Provider<CycleRepository> cycleRepositoryProvider;

  private CycleTrackerViewModel_Factory(Provider<CycleRepository> cycleRepositoryProvider) {
    this.cycleRepositoryProvider = cycleRepositoryProvider;
  }

  @Override
  public CycleTrackerViewModel get() {
    return newInstance(cycleRepositoryProvider.get());
  }

  public static CycleTrackerViewModel_Factory create(
      Provider<CycleRepository> cycleRepositoryProvider) {
    return new CycleTrackerViewModel_Factory(cycleRepositoryProvider);
  }

  public static CycleTrackerViewModel newInstance(CycleRepository cycleRepository) {
    return new CycleTrackerViewModel(cycleRepository);
  }
}
