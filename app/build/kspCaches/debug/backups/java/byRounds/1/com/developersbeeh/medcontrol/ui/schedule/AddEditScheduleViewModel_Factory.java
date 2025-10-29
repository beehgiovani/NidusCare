package com.developersbeeh.medcontrol.ui.schedule;

import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository;
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
public final class AddEditScheduleViewModel_Factory implements Factory<AddEditScheduleViewModel> {
  private final Provider<ScheduleRepository> scheduleRepositoryProvider;

  private final Provider<ActivityLogRepository> activityLogRepositoryProvider;

  private AddEditScheduleViewModel_Factory(Provider<ScheduleRepository> scheduleRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider) {
    this.scheduleRepositoryProvider = scheduleRepositoryProvider;
    this.activityLogRepositoryProvider = activityLogRepositoryProvider;
  }

  @Override
  public AddEditScheduleViewModel get() {
    return newInstance(scheduleRepositoryProvider.get(), activityLogRepositoryProvider.get());
  }

  public static AddEditScheduleViewModel_Factory create(
      Provider<ScheduleRepository> scheduleRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider) {
    return new AddEditScheduleViewModel_Factory(scheduleRepositoryProvider, activityLogRepositoryProvider);
  }

  public static AddEditScheduleViewModel newInstance(ScheduleRepository scheduleRepository,
      ActivityLogRepository activityLogRepository) {
    return new AddEditScheduleViewModel(scheduleRepository, activityLogRepository);
  }
}
