package com.developersbeeh.medcontrol.ui.healthnotes;

import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
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
public final class HealthNotesViewModel_Factory implements Factory<HealthNotesViewModel> {
  private final Provider<FirestoreRepository> repositoryProvider;

  private final Provider<ActivityLogRepository> activityLogRepositoryProvider;

  private HealthNotesViewModel_Factory(Provider<FirestoreRepository> repositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.activityLogRepositoryProvider = activityLogRepositoryProvider;
  }

  @Override
  public HealthNotesViewModel get() {
    return newInstance(repositoryProvider.get(), activityLogRepositoryProvider.get());
  }

  public static HealthNotesViewModel_Factory create(
      Provider<FirestoreRepository> repositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider) {
    return new HealthNotesViewModel_Factory(repositoryProvider, activityLogRepositoryProvider);
  }

  public static HealthNotesViewModel newInstance(FirestoreRepository repository,
      ActivityLogRepository activityLogRepository) {
    return new HealthNotesViewModel(repository, activityLogRepository);
  }
}
