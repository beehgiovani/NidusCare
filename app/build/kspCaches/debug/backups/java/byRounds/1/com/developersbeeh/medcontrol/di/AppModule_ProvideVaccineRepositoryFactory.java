package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.VaccineRepository;
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
public final class AppModule_ProvideVaccineRepositoryFactory implements Factory<VaccineRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private AppModule_ProvideVaccineRepositoryFactory(Provider<FirebaseFirestore> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public VaccineRepository get() {
    return provideVaccineRepository(dbProvider.get());
  }

  public static AppModule_ProvideVaccineRepositoryFactory create(
      Provider<FirebaseFirestore> dbProvider) {
    return new AppModule_ProvideVaccineRepositoryFactory(dbProvider);
  }

  public static VaccineRepository provideVaccineRepository(FirebaseFirestore db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideVaccineRepository(db));
  }
}
