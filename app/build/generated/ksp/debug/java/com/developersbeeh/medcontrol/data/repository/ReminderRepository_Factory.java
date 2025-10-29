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
public final class ReminderRepository_Factory implements Factory<ReminderRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private ReminderRepository_Factory(Provider<FirebaseFirestore> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ReminderRepository get() {
    return newInstance(dbProvider.get());
  }

  public static ReminderRepository_Factory create(Provider<FirebaseFirestore> dbProvider) {
    return new ReminderRepository_Factory(dbProvider);
  }

  public static ReminderRepository newInstance(FirebaseFirestore db) {
    return new ReminderRepository(db);
  }
}
