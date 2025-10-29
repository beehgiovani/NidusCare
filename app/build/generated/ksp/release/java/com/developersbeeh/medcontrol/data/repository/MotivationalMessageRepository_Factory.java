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
public final class MotivationalMessageRepository_Factory implements Factory<MotivationalMessageRepository> {
  @Override
  public MotivationalMessageRepository get() {
    return newInstance();
  }

  public static MotivationalMessageRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MotivationalMessageRepository newInstance() {
    return new MotivationalMessageRepository();
  }

  private static final class InstanceHolder {
    static final MotivationalMessageRepository_Factory INSTANCE = new MotivationalMessageRepository_Factory();
  }
}
