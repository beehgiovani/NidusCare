package com.developersbeeh.medcontrol.data.repository;

import android.content.Context;
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
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class MedicationRepositoryEnhanced_Factory implements Factory<MedicationRepositoryEnhanced> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<AchievementRepository> achievementRepositoryProvider;

  private final Provider<AuditLogger> auditLoggerProvider;

  private final Provider<Context> contextProvider;

  private MedicationRepositoryEnhanced_Factory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<AuditLogger> auditLoggerProvider, Provider<Context> contextProvider) {
    this.dbProvider = dbProvider;
    this.authProvider = authProvider;
    this.achievementRepositoryProvider = achievementRepositoryProvider;
    this.auditLoggerProvider = auditLoggerProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public MedicationRepositoryEnhanced get() {
    return newInstance(dbProvider.get(), authProvider.get(), achievementRepositoryProvider.get(), auditLoggerProvider.get(), contextProvider.get());
  }

  public static MedicationRepositoryEnhanced_Factory create(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<AuditLogger> auditLoggerProvider, Provider<Context> contextProvider) {
    return new MedicationRepositoryEnhanced_Factory(dbProvider, authProvider, achievementRepositoryProvider, auditLoggerProvider, contextProvider);
  }

  public static MedicationRepositoryEnhanced newInstance(FirebaseFirestore db, FirebaseAuth auth,
      AchievementRepository achievementRepository, AuditLogger auditLogger, Context context) {
    return new MedicationRepositoryEnhanced(db, auth, achievementRepository, auditLogger, context);
  }
}
