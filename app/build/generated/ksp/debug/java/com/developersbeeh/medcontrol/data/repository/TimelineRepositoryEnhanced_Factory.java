package com.developersbeeh.medcontrol.data.repository;

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
public final class TimelineRepositoryEnhanced_Factory implements Factory<TimelineRepositoryEnhanced> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseAuth> authProvider;

  private TimelineRepositoryEnhanced_Factory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider) {
    this.dbProvider = dbProvider;
    this.authProvider = authProvider;
  }

  @Override
  public TimelineRepositoryEnhanced get() {
    return newInstance(dbProvider.get(), authProvider.get());
  }

  public static TimelineRepositoryEnhanced_Factory create(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider) {
    return new TimelineRepositoryEnhanced_Factory(dbProvider, authProvider);
  }

  public static TimelineRepositoryEnhanced newInstance(FirebaseFirestore db, FirebaseAuth auth) {
    return new TimelineRepositoryEnhanced(db, auth);
  }
}
