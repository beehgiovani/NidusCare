package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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
public final class AppModule_ProvideActivityLogRepositoryFactory implements Factory<ActivityLogRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private AppModule_ProvideActivityLogRepositoryFactory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider, Provider<UserPreferences> userPreferencesProvider) {
    this.dbProvider = dbProvider;
    this.authProvider = authProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public ActivityLogRepository get() {
    return provideActivityLogRepository(dbProvider.get(), authProvider.get(), userPreferencesProvider.get());
  }

  public static AppModule_ProvideActivityLogRepositoryFactory create(
      Provider<FirebaseFirestore> dbProvider, Provider<FirebaseAuth> authProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new AppModule_ProvideActivityLogRepositoryFactory(dbProvider, authProvider, userPreferencesProvider);
  }

  public static ActivityLogRepository provideActivityLogRepository(FirebaseFirestore db,
      FirebaseAuth auth, UserPreferences userPreferences) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideActivityLogRepository(db, auth, userPreferences));
  }
}
