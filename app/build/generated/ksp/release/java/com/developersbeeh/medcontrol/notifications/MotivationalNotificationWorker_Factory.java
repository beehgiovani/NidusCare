package com.developersbeeh.medcontrol.notifications;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.MotivationalMessageRepository;
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
public final class MotivationalNotificationWorker_Factory {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MotivationalMessageRepository> messageRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private MotivationalNotificationWorker_Factory(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MotivationalMessageRepository> messageRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  public MotivationalNotificationWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, firestoreRepositoryProvider.get(), messageRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static MotivationalNotificationWorker_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MotivationalMessageRepository> messageRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new MotivationalNotificationWorker_Factory(firestoreRepositoryProvider, messageRepositoryProvider, userPreferencesProvider);
  }

  public static MotivationalNotificationWorker newInstance(Context appContext,
      WorkerParameters workerParams, FirestoreRepository firestoreRepository,
      MotivationalMessageRepository messageRepository, UserPreferences userPreferences) {
    return new MotivationalNotificationWorker(appContext, workerParams, firestoreRepository, messageRepository, userPreferences);
  }
}
