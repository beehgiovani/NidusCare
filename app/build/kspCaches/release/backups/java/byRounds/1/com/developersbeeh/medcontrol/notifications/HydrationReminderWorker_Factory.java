package com.developersbeeh.medcontrol.notifications;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
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
public final class HydrationReminderWorker_Factory {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private HydrationReminderWorker_Factory(
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
  }

  public HydrationReminderWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, firestoreRepositoryProvider.get());
  }

  public static HydrationReminderWorker_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    return new HydrationReminderWorker_Factory(firestoreRepositoryProvider);
  }

  public static HydrationReminderWorker newInstance(Context appContext,
      WorkerParameters workerParams, FirestoreRepository firestoreRepository) {
    return new HydrationReminderWorker(appContext, workerParams, firestoreRepository);
  }
}
