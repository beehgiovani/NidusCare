package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.MotivationalMessageRepository;
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
public final class AppModule_ProvideMotivationalMessageRepositoryFactory implements Factory<MotivationalMessageRepository> {
  @Override
  public MotivationalMessageRepository get() {
    return provideMotivationalMessageRepository();
  }

  public static AppModule_ProvideMotivationalMessageRepositoryFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MotivationalMessageRepository provideMotivationalMessageRepository() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMotivationalMessageRepository());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideMotivationalMessageRepositoryFactory INSTANCE = new AppModule_ProvideMotivationalMessageRepositoryFactory();
  }
}
