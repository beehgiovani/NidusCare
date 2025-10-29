package com.developersbeeh.medcontrol.di;

import android.content.Context;
import com.developersbeeh.medcontrol.data.repository.AchievementRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideMedicationRepositoryFactory implements Factory<MedicationRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<AchievementRepository> achievementRepositoryProvider;

  private final Provider<Context> contextProvider;

  private AppModule_ProvideMedicationRepositoryFactory(Provider<FirebaseFirestore> dbProvider,
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
    return provideMedicationRepository(dbProvider.get(), authProvider.get(), achievementRepositoryProvider.get(), contextProvider.get());
  }

  public static AppModule_ProvideMedicationRepositoryFactory create(
      Provider<FirebaseFirestore> dbProvider, Provider<FirebaseAuth> authProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<Context> contextProvider) {
    return new AppModule_ProvideMedicationRepositoryFactory(dbProvider, authProvider, achievementRepositoryProvider, contextProvider);
  }

  public static MedicationRepository provideMedicationRepository(FirebaseFirestore db,
      FirebaseAuth auth, AchievementRepository achievementRepository, Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMedicationRepository(db, auth, achievementRepository, context));
  }
}
