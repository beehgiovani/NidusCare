package com.developersbeeh.medcontrol.notifications;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.AchievementRepository;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
import dagger.internal.DaggerGenerated;
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
public final class AdherenceStreakWorker_Factory {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<AchievementRepository> achievementRepositoryProvider;

  private AdherenceStreakWorker_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<AchievementRepository> achievementRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.achievementRepositoryProvider = achievementRepositoryProvider;
  }

  public AdherenceStreakWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, firestoreRepositoryProvider.get(), medicationRepositoryProvider.get(), userPreferencesProvider.get(), achievementRepositoryProvider.get());
  }

  public static AdherenceStreakWorker_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<AchievementRepository> achievementRepositoryProvider) {
    return new AdherenceStreakWorker_Factory(firestoreRepositoryProvider, medicationRepositoryProvider, userPreferencesProvider, achievementRepositoryProvider);
  }

  public static AdherenceStreakWorker newInstance(Context appContext, WorkerParameters workerParams,
      FirestoreRepository firestoreRepository, MedicationRepository medicationRepository,
      UserPreferences userPreferences, AchievementRepository achievementRepository) {
    return new AdherenceStreakWorker(appContext, workerParams, firestoreRepository, medicationRepository, userPreferences, achievementRepository);
  }
}
