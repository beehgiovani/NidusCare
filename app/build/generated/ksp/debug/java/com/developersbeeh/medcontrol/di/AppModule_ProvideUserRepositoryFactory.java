package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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
public final class AppModule_ProvideUserRepositoryFactory implements Factory<UserRepository> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirebaseFunctions> functionsProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private AppModule_ProvideUserRepositoryFactory(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> dbProvider, Provider<FirebaseStorage> storageProvider,
      Provider<FirebaseFunctions> functionsProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.authProvider = authProvider;
    this.dbProvider = dbProvider;
    this.storageProvider = storageProvider;
    this.functionsProvider = functionsProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public UserRepository get() {
    return provideUserRepository(authProvider.get(), dbProvider.get(), storageProvider.get(), functionsProvider.get(), userPreferencesProvider.get());
  }

  public static AppModule_ProvideUserRepositoryFactory create(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> dbProvider, Provider<FirebaseStorage> storageProvider,
      Provider<FirebaseFunctions> functionsProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new AppModule_ProvideUserRepositoryFactory(authProvider, dbProvider, storageProvider, functionsProvider, userPreferencesProvider);
  }

  public static UserRepository provideUserRepository(FirebaseAuth auth, FirebaseFirestore db,
      FirebaseStorage storage, FirebaseFunctions functions, UserPreferences userPreferences) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideUserRepository(auth, db, storage, functions, userPreferences));
  }
}
