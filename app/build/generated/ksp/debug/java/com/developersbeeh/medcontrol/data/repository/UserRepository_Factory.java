package com.developersbeeh.medcontrol.data.repository;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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
public final class UserRepository_Factory implements Factory<UserRepository> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirebaseFunctions> functionsProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private UserRepository_Factory(Provider<FirebaseAuth> authProvider,
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
    return newInstance(authProvider.get(), dbProvider.get(), storageProvider.get(), functionsProvider.get(), userPreferencesProvider.get());
  }

  public static UserRepository_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> dbProvider, Provider<FirebaseStorage> storageProvider,
      Provider<FirebaseFunctions> functionsProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new UserRepository_Factory(authProvider, dbProvider, storageProvider, functionsProvider, userPreferencesProvider);
  }

  public static UserRepository newInstance(FirebaseAuth auth, FirebaseFirestore db,
      FirebaseStorage storage, FirebaseFunctions functions, UserPreferences userPreferences) {
    return new UserRepository(auth, db, storage, functions, userPreferences);
  }
}
