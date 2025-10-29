package com.developersbeeh.medcontrol.notifications;

import android.content.Context;
import androidx.work.WorkerParameters;
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
public final class StockExpiryWorker_Factory {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private StockExpiryWorker_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
  }

  public StockExpiryWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, firestoreRepositoryProvider.get(), medicationRepositoryProvider.get());
  }

  public static StockExpiryWorker_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider) {
    return new StockExpiryWorker_Factory(firestoreRepositoryProvider, medicationRepositoryProvider);
  }

  public static StockExpiryWorker newInstance(Context appContext, WorkerParameters workerParams,
      FirestoreRepository firestoreRepository, MedicationRepository medicationRepository) {
    return new StockExpiryWorker(appContext, workerParams, firestoreRepository, medicationRepository);
  }
}
