package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.EducationRepository;
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
public final class AppModule_ProvideEducationRepositoryFactory implements Factory<EducationRepository> {
  @Override
  public EducationRepository get() {
    return provideEducationRepository();
  }

  public static AppModule_ProvideEducationRepositoryFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static EducationRepository provideEducationRepository() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideEducationRepository());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideEducationRepositoryFactory INSTANCE = new AppModule_ProvideEducationRepositoryFactory();
  }
}
