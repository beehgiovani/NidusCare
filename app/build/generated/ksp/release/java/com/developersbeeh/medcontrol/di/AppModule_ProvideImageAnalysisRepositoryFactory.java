package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.ImageAnalysisRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideImageAnalysisRepositoryFactory implements Factory<ImageAnalysisRepository> {
  private final Provider<FirebaseFunctions> functionsProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private AppModule_ProvideImageAnalysisRepositoryFactory(
      Provider<FirebaseFunctions> functionsProvider, Provider<FirebaseAuth> authProvider,
      Provider<FirebaseStorage> storageProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider) {
    this.functionsProvider = functionsProvider;
    this.authProvider = authProvider;
    this.storageProvider = storageProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
  }

  @Override
  public ImageAnalysisRepository get() {
    return provideImageAnalysisRepository(functionsProvider.get(), authProvider.get(), storageProvider.get(), firestoreRepositoryProvider.get(), medicationRepositoryProvider.get());
  }

  public static AppModule_ProvideImageAnalysisRepositoryFactory create(
      Provider<FirebaseFunctions> functionsProvider, Provider<FirebaseAuth> authProvider,
      Provider<FirebaseStorage> storageProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider) {
    return new AppModule_ProvideImageAnalysisRepositoryFactory(functionsProvider, authProvider, storageProvider, firestoreRepositoryProvider, medicationRepositoryProvider);
  }

  public static ImageAnalysisRepository provideImageAnalysisRepository(FirebaseFunctions functions,
      FirebaseAuth auth, FirebaseStorage storage, FirestoreRepository firestoreRepository,
      MedicationRepository medicationRepository) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideImageAnalysisRepository(functions, auth, storage, firestoreRepository, medicationRepository));
  }
}
