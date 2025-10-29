package com.developersbeeh.medcontrol.ui.achievements;

import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
import com.developersbeeh.medcontrol.data.repository.UserRepository;
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
public final class AchievementsViewModel_Factory implements Factory<AchievementsViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<ActivityLogRepository> activityLogRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private AchievementsViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.activityLogRepositoryProvider = activityLogRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public AchievementsViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), medicationRepositoryProvider.get(), activityLogRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static AchievementsViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new AchievementsViewModel_Factory(firestoreRepositoryProvider, medicationRepositoryProvider, activityLogRepositoryProvider, userRepositoryProvider);
  }

  public static AchievementsViewModel newInstance(FirestoreRepository firestoreRepository,
      MedicationRepository medicationRepository, ActivityLogRepository activityLogRepository,
      UserRepository userRepository) {
    return new AchievementsViewModel(firestoreRepository, medicationRepository, activityLogRepository, userRepository);
  }
}
