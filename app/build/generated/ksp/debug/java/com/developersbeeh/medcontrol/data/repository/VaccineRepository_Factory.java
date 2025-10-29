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
public final class VaccineRepository_Factory implements Factory<VaccineRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private VaccineRepository_Factory(Provider<FirebaseFirestore> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public VaccineRepository get() {
    return newInstance(dbProvider.get());
  }

  public static VaccineRepository_Factory create(Provider<FirebaseFirestore> dbProvider) {
    return new VaccineRepository_Factory(dbProvider);
  }

  public static VaccineRepository newInstance(FirebaseFirestore db) {
    return new VaccineRepository(db);
  }
}
