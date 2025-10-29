package com.developersbeeh.medcontrol.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class EducationRepository_Factory implements Factory<EducationRepository> {
  @Override
  public EducationRepository get() {
    return newInstance();
  }

  public static EducationRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static EducationRepository newInstance() {
    return new EducationRepository();
  }

  private static final class InstanceHolder {
    static final EducationRepository_Factory INSTANCE = new EducationRepository_Factory();
  }
}
