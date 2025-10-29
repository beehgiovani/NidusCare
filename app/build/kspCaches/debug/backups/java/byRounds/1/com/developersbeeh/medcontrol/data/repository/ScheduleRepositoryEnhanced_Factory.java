package com.developersbeeh.medcontrol.data.repository;

import com.developersbeeh.medcontrol.audit.AuditLogger;
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
public final class ScheduleRepositoryEnhanced_Factory implements Factory<ScheduleRepositoryEnhanced> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<AuditLogger> auditLoggerProvider;

  private ScheduleRepositoryEnhanced_Factory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider, Provider<AuditLogger> auditLoggerProvider) {
    this.dbProvider = dbProvider;
    this.authProvider = authProvider;
    this.auditLoggerProvider = auditLoggerProvider;
  }

  @Override
  public ScheduleRepositoryEnhanced get() {
    return newInstance(dbProvider.get(), authProvider.get(), auditLoggerProvider.get());
  }

  public static ScheduleRepositoryEnhanced_Factory create(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider, Provider<AuditLogger> auditLoggerProvider) {
    return new ScheduleRepositoryEnhanced_Factory(dbProvider, authProvider, auditLoggerProvider);
  }

  public static ScheduleRepositoryEnhanced newInstance(FirebaseFirestore db, FirebaseAuth auth,
      AuditLogger auditLogger) {
    return new ScheduleRepositoryEnhanced(db, auth, auditLogger);
  }
}
