package com.developersbeeh.medcontrol.data.repository;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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
public final class ActivityLogRepository_Factory implements Factory<ActivityLogRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private ActivityLogRepository_Factory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider, Provider<UserPreferences> userPreferencesProvider) {
    this.dbProvider = dbProvider;
    this.authProvider = authProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public ActivityLogRepository get() {
    return newInstance(dbProvider.get(), authProvider.get(), userPreferencesProvider.get());
  }

  public static ActivityLogRepository_Factory create(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider, Provider<UserPreferences> userPreferencesProvider) {
    return new ActivityLogRepository_Factory(dbProvider, authProvider, userPreferencesProvider);
  }

  public static ActivityLogRepository newInstance(FirebaseFirestore db, FirebaseAuth auth,
      UserPreferences userPreferences) {
    return new ActivityLogRepository(db, auth, userPreferences);
  }
}
