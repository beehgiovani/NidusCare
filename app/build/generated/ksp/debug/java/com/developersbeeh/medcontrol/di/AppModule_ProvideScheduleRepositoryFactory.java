package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.ScheduleRepository;
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
public final class AppModule_ProvideScheduleRepositoryFactory implements Factory<ScheduleRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private AppModule_ProvideScheduleRepositoryFactory(Provider<FirebaseFirestore> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ScheduleRepository get() {
    return provideScheduleRepository(dbProvider.get());
  }

  public static AppModule_ProvideScheduleRepositoryFactory create(
      Provider<FirebaseFirestore> dbProvider) {
    return new AppModule_ProvideScheduleRepositoryFactory(dbProvider);
  }

  public static ScheduleRepository provideScheduleRepository(FirebaseFirestore db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideScheduleRepository(db));
  }
}
