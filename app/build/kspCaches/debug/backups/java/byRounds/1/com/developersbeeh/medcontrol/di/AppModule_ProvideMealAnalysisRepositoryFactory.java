package com.developersbeeh.medcontrol.di;

import android.content.Context;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.MealAnalysisRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.moshi.Moshi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideMealAnalysisRepositoryFactory implements Factory<MealAnalysisRepository> {
  private final Provider<FirebaseFunctions> functionsProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<Moshi> moshiProvider;

  private final Provider<Context> contextProvider;

  private AppModule_ProvideMealAnalysisRepositoryFactory(
      Provider<FirebaseFunctions> functionsProvider, Provider<FirebaseAuth> authProvider,
      Provider<FirebaseStorage> storageProvider,
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
    return provideMealAnalysisRepository(functionsProvider.get(), authProvider.get(), storageProvider.get(), firestoreRepositoryProvider.get(), moshiProvider.get(), contextProvider.get());
  }

  public static AppModule_ProvideMealAnalysisRepositoryFactory create(
      Provider<FirebaseFunctions> functionsProvider, Provider<FirebaseAuth> authProvider,
      Provider<FirebaseStorage> storageProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider, Provider<Moshi> moshiProvider,
      Provider<Context> contextProvider) {
    return new AppModule_ProvideMealAnalysisRepositoryFactory(functionsProvider, authProvider, storageProvider, firestoreRepositoryProvider, moshiProvider, contextProvider);
  }

  public static MealAnalysisRepository provideMealAnalysisRepository(FirebaseFunctions functions,
      FirebaseAuth auth, FirebaseStorage storage, FirestoreRepository firestoreRepository,
      Moshi moshi, Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMealAnalysisRepository(functions, auth, storage, firestoreRepository, moshi, context));
  }
}
