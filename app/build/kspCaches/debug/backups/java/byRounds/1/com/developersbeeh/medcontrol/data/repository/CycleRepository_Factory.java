package com.developersbeeh.medcontrol.data.repository;

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
public final class CycleRepository_Factory implements Factory<CycleRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private CycleRepository_Factory(Provider<FirebaseFirestore> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CycleRepository get() {
    return newInstance(dbProvider.get());
  }

  public static CycleRepository_Factory create(Provider<FirebaseFirestore> dbProvider) {
    return new CycleRepository_Factory(dbProvider);
  }

  public static CycleRepository newInstance(FirebaseFirestore db) {
    return new CycleRepository(db);
  }
}
