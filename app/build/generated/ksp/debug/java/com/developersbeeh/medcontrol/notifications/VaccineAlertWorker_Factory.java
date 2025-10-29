package com.developersbeeh.medcontrol.notifications;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.VaccineRepository;
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
public final class VaccineAlertWorker_Factory {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<VaccineRepository> vaccineRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private VaccineAlertWorker_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<VaccineRepository> vaccineRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.vaccineRepositoryProvider = vaccineRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  public VaccineAlertWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, firestoreRepositoryProvider.get(), vaccineRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static VaccineAlertWorker_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<VaccineRepository> vaccineRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new VaccineAlertWorker_Factory(firestoreRepositoryProvider, vaccineRepositoryProvider, userPreferencesProvider);
  }

  public static VaccineAlertWorker newInstance(Context appContext, WorkerParameters workerParams,
      FirestoreRepository firestoreRepository, VaccineRepository vaccineRepository,
      UserPreferences userPreferences) {
    return new VaccineAlertWorker(appContext, workerParams, firestoreRepository, vaccineRepository, userPreferences);
  }
}
