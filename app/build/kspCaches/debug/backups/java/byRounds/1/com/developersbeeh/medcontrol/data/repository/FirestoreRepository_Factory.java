package com.developersbeeh.medcontrol.data.repository;

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
public final class FirestoreRepository_Factory implements Factory<FirestoreRepository> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirebaseFunctions> functionsProvider;

  private FirestoreRepository_Factory(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseStorage> storageProvider, Provider<FirebaseFunctions> functionsProvider) {
    this.authProvider = authProvider;
    this.storageProvider = storageProvider;
    this.functionsProvider = functionsProvider;
  }

  @Override
  public FirestoreRepository get() {
    return newInstance(authProvider.get(), storageProvider.get(), functionsProvider.get());
  }

  public static FirestoreRepository_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseStorage> storageProvider, Provider<FirebaseFunctions> functionsProvider) {
    return new FirestoreRepository_Factory(authProvider, storageProvider, functionsProvider);
  }

  public static FirestoreRepository newInstance(FirebaseAuth auth, FirebaseStorage storage,
      FirebaseFunctions functions) {
    return new FirestoreRepository(auth, storage, functions);
  }
}
