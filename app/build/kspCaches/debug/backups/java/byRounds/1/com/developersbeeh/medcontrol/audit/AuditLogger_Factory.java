package com.developersbeeh.medcontrol.audit;

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
public final class AuditLogger_Factory implements Factory<AuditLogger> {
  private final Provider<FirebaseFirestore> dbProvider;

  private AuditLogger_Factory(Provider<FirebaseFirestore> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AuditLogger get() {
    return newInstance(dbProvider.get());
  }

  public static AuditLogger_Factory create(Provider<FirebaseFirestore> dbProvider) {
    return new AuditLogger_Factory(dbProvider);
  }

  public static AuditLogger newInstance(FirebaseFirestore db) {
    return new AuditLogger(db);
  }
}
