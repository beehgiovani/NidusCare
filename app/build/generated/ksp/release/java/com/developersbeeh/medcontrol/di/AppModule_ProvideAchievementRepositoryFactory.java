package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.AchievementRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideAchievementRepositoryFactory implements Factory<AchievementRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private AppModule_ProvideAchievementRepositoryFactory(Provider<FirebaseFirestore> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AchievementRepository get() {
    return provideAchievementRepository(dbProvider.get());
  }

  public static AppModule_ProvideAchievementRepositoryFactory create(
      Provider<FirebaseFirestore> dbProvider) {
    return new AppModule_ProvideAchievementRepositoryFactory(dbProvider);
  }

  public static AchievementRepository provideAchievementRepository(FirebaseFirestore db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAchievementRepository(db));
  }
}
