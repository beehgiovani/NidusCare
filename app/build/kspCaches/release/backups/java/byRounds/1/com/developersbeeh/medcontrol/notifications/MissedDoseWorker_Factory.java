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
public final class MissedDoseWorker_Factory {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private MissedDoseWorker_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
  }

  public MissedDoseWorker get(Context appContext, WorkerParameters workerParams) {
    return newInstance(appContext, workerParams, firestoreRepositoryProvider.get(), medicationRepositoryProvider.get());
  }

  public static MissedDoseWorker_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider) {
    return new MissedDoseWorker_Factory(firestoreRepositoryProvider, medicationRepositoryProvider);
  }

  public static MissedDoseWorker newInstance(Context appContext, WorkerParameters workerParams,
      FirestoreRepository firestoreRepository, MedicationRepository medicationRepository) {
    return new MissedDoseWorker(appContext, workerParams, firestoreRepository, medicationRepository);
  }
}
