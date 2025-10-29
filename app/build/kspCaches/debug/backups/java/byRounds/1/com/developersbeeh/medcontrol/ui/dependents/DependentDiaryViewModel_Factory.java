package com.developersbeeh.medcontrol.ui.dependents;

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
public final class DependentDiaryViewModel_Factory implements Factory<DependentDiaryViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<ActivityLogRepository> activityLogRepositoryProvider;

  private DependentDiaryViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.activityLogRepositoryProvider = activityLogRepositoryProvider;
  }

  @Override
  public DependentDiaryViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), activityLogRepositoryProvider.get());
  }

  public static DependentDiaryViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider) {
    return new DependentDiaryViewModel_Factory(firestoreRepositoryProvider, activityLogRepositoryProvider);
  }

  public static DependentDiaryViewModel newInstance(FirestoreRepository firestoreRepository,
      ActivityLogRepository activityLogRepository) {
    return new DependentDiaryViewModel(firestoreRepository, activityLogRepository);
  }
}
