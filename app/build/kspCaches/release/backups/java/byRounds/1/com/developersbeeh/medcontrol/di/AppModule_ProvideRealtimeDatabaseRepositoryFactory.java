package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.RealtimeDatabaseRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideRealtimeDatabaseRepositoryFactory implements Factory<RealtimeDatabaseRepository> {
  @Override
  public RealtimeDatabaseRepository get() {
    return provideRealtimeDatabaseRepository();
  }

  public static AppModule_ProvideRealtimeDatabaseRepositoryFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RealtimeDatabaseRepository provideRealtimeDatabaseRepository() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideRealtimeDatabaseRepository());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideRealtimeDatabaseRepositoryFactory INSTANCE = new AppModule_ProvideRealtimeDatabaseRepositoryFactory();
  }
}
