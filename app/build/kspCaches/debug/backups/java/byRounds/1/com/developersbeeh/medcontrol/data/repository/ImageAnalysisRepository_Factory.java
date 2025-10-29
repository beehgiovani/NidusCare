package com.developersbeeh.medcontrol.data.repository;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ImageAnalysisRepository_Factory implements Factory<ImageAnalysisRepository> {
  private final Provider<FirebaseFunctions> functionsProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<Context> contextProvider;

  private ImageAnalysisRepository_Factory(Provider<FirebaseFunctions> functionsProvider,
      Provider<FirebaseAuth> authProvider, Provider<FirebaseStorage> storageProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<Context> contextProvider) {
    this.functionsProvider = functionsProvider;
    this.authProvider = authProvider;
    this.storageProvider = storageProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public ImageAnalysisRepository get() {
    return newInstance(functionsProvider.get(), authProvider.get(), storageProvider.get(), firestoreRepositoryProvider.get(), medicationRepositoryProvider.get(), contextProvider.get());
  }

  public static ImageAnalysisRepository_Factory create(
      Provider<FirebaseFunctions> functionsProvider, Provider<FirebaseAuth> authProvider,
      Provider<FirebaseStorage> storageProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<Context> contextProvider) {
    return new ImageAnalysisRepository_Factory(functionsProvider, authProvider, storageProvider, firestoreRepositoryProvider, medicationRepositoryProvider, contextProvider);
  }

  public static ImageAnalysisRepository newInstance(FirebaseFunctions functions, FirebaseAuth auth,
      FirebaseStorage storage, FirestoreRepository firestoreRepository,
      MedicationRepository medicationRepository, Context context) {
    return new ImageAnalysisRepository(functions, auth, storage, firestoreRepository, medicationRepository, context);
  }
}
