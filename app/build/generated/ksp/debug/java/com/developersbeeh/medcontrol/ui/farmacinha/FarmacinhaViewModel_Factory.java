package com.developersbeeh.medcontrol.ui.farmacinha;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository;
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
public final class FarmacinhaViewModel_Factory implements Factory<FarmacinhaViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<ActivityLogRepository> activityLogRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private FarmacinhaViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.activityLogRepositoryProvider = activityLogRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public FarmacinhaViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), medicationRepositoryProvider.get(), activityLogRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static FarmacinhaViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new FarmacinhaViewModel_Factory(firestoreRepositoryProvider, medicationRepositoryProvider, activityLogRepositoryProvider, userPreferencesProvider);
  }

  public static FarmacinhaViewModel newInstance(FirestoreRepository firestoreRepository,
      MedicationRepository medicationRepository, ActivityLogRepository activityLogRepository,
      UserPreferences userPreferences) {
    return new FarmacinhaViewModel(firestoreRepository, medicationRepository, activityLogRepository, userPreferences);
  }
}
