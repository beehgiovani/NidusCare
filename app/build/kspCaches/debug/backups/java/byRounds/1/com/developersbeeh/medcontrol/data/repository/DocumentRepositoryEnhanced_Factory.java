package com.developersbeeh.medcontrol.data.repository;

import com.developersbeeh.medcontrol.audit.AuditLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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
public final class DocumentRepositoryEnhanced_Factory implements Factory<DocumentRepositoryEnhanced> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<AuditLogger> auditLoggerProvider;

  private DocumentRepositoryEnhanced_Factory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseStorage> storageProvider, Provider<FirebaseAuth> authProvider,
      Provider<AuditLogger> auditLoggerProvider) {
    this.dbProvider = dbProvider;
    this.storageProvider = storageProvider;
    this.authProvider = authProvider;
    this.auditLoggerProvider = auditLoggerProvider;
  }

  @Override
  public DocumentRepositoryEnhanced get() {
    return newInstance(dbProvider.get(), storageProvider.get(), authProvider.get(), auditLoggerProvider.get());
  }

  public static DocumentRepositoryEnhanced_Factory create(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseStorage> storageProvider, Provider<FirebaseAuth> authProvider,
      Provider<AuditLogger> auditLoggerProvider) {
    return new DocumentRepositoryEnhanced_Factory(dbProvider, storageProvider, authProvider, auditLoggerProvider);
  }

  public static DocumentRepositoryEnhanced newInstance(FirebaseFirestore db,
      FirebaseStorage storage, FirebaseAuth auth, AuditLogger auditLogger) {
    return new DocumentRepositoryEnhanced(db, storage, auth, auditLogger);
  }
}
