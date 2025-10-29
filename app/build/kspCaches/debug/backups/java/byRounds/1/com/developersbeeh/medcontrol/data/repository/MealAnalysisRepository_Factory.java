package com.developersbeeh.medcontrol.data.repository;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.moshi.Moshi;
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
public final class MealAnalysisRepository_Factory implements Factory<MealAnalysisRepository> {
  private final Provider<FirebaseFunctions> functionsProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<Moshi> moshiProvider;

  private final Provider<Context> contextProvider;

  private MealAnalysisRepository_Factory(Provider<FirebaseFunctions> functionsProvider,
      Provider<FirebaseAuth> authProvider, Provider<FirebaseStorage> storageProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider, Provider<Moshi> moshiProvider,
      Provider<Context> contextProvider) {
    this.functionsProvider = functionsProvider;
    this.authProvider = authProvider;
    this.storageProvider = storageProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.moshiProvider = moshiProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public MealAnalysisRepository get() {
    return newInstance(functionsProvider.get(), authProvider.get(), storageProvider.get(), firestoreRepositoryProvider.get(), moshiProvider.get(), contextProvider.get());
  }

  public static MealAnalysisRepository_Factory create(Provider<FirebaseFunctions> functionsProvider,
      Provider<FirebaseAuth> authProvider, Provider<FirebaseStorage> storageProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider, Provider<Moshi> moshiProvider,
      Provider<Context> contextProvider) {
    return new MealAnalysisRepository_Factory(functionsProvider, authProvider, storageProvider, firestoreRepositoryProvider, moshiProvider, contextProvider);
  }

  public static MealAnalysisRepository newInstance(FirebaseFunctions functions, FirebaseAuth auth,
      FirebaseStorage storage, FirestoreRepository firestoreRepository, Moshi moshi,
      Context context) {
    return new MealAnalysisRepository(functions, auth, storage, firestoreRepository, moshi, context);
  }
}
