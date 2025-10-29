package com.developersbeeh.medcontrol.ui.schedule;

import com.developersbeeh.medcontrol.data.repository.ScheduleRepository;
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
public final class HealthScheduleViewModel_Factory implements Factory<HealthScheduleViewModel> {
  private final Provider<ScheduleRepository> scheduleRepositoryProvider;

  private HealthScheduleViewModel_Factory(Provider<ScheduleRepository> scheduleRepositoryProvider) {
    this.scheduleRepositoryProvider = scheduleRepositoryProvider;
  }

  @Override
  public HealthScheduleViewModel get() {
    return newInstance(scheduleRepositoryProvider.get());
  }

  public static HealthScheduleViewModel_Factory create(
      Provider<ScheduleRepository> scheduleRepositoryProvider) {
    return new HealthScheduleViewModel_Factory(scheduleRepositoryProvider);
  }

  public static HealthScheduleViewModel newInstance(ScheduleRepository scheduleRepository) {
    return new HealthScheduleViewModel(scheduleRepository);
  }
}
