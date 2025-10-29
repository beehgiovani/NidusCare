package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
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
public final class AppModule_ProvideFirestoreRepositoryFactory implements Factory<FirestoreRepository> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirebaseFunctions> functionsProvider;

  private AppModule_ProvideFirestoreRepositoryFactory(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseStorage> storageProvider, Provider<FirebaseFunctions> functionsProvider) {
    this.authProvider = authProvider;
    this.storageProvider = storageProvider;
    this.functionsProvider = functionsProvider;
  }

  @Override
  public FirestoreRepository get() {
    return provideFirestoreRepository(authProvider.get(), storageProvider.get(), functionsProvider.get());
  }

  public static AppModule_ProvideFirestoreRepositoryFactory create(
      Provider<FirebaseAuth> authProvider, Provider<FirebaseStorage> storageProvider,
      Provider<FirebaseFunctions> functionsProvider) {
    return new AppModule_ProvideFirestoreRepositoryFactory(authProvider, storageProvider, functionsProvider);
  }

  public static FirestoreRepository provideFirestoreRepository(FirebaseAuth auth,
      FirebaseStorage storage, FirebaseFunctions functions) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFirestoreRepository(auth, storage, functions));
  }
}
