package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.CycleRepository;
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
public final class AppModule_ProvideCycleRepositoryFactory implements Factory<CycleRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private AppModule_ProvideCycleRepositoryFactory(Provider<FirebaseFirestore> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CycleRepository get() {
    return provideCycleRepository(dbProvider.get());
  }

  public static AppModule_ProvideCycleRepositoryFactory create(
      Provider<FirebaseFirestore> dbProvider) {
    return new AppModule_ProvideCycleRepositoryFactory(dbProvider);
  }

  public static CycleRepository provideCycleRepository(FirebaseFirestore db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideCycleRepository(db));
  }
}
