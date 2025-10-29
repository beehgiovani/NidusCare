package com.developersbeeh.medcontrol.data.repository;

import android.content.Context;
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
public final class MedicationRepository_Factory implements Factory<MedicationRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<AchievementRepository> achievementRepositoryProvider;

  private final Provider<Context> contextProvider;

  private MedicationRepository_Factory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<Context> contextProvider) {
    this.dbProvider = dbProvider;
    this.authProvider = authProvider;
    this.achievementRepositoryProvider = achievementRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public MedicationRepository get() {
    return newInstance(dbProvider.get(), authProvider.get(), achievementRepositoryProvider.get(), contextProvider.get());
  }

  public static MedicationRepository_Factory create(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseAuth> authProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<Context> contextProvider) {
    return new MedicationRepository_Factory(dbProvider, authProvider, achievementRepositoryProvider, contextProvider);
  }

  public static MedicationRepository newInstance(FirebaseFirestore db, FirebaseAuth auth,
      AchievementRepository achievementRepository, Context context) {
    return new MedicationRepository(db, auth, achievementRepository, context);
  }
}
