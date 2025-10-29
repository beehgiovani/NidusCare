package com.developersbeeh.medcontrol.di;

import com.google.firebase.functions.FirebaseFunctions;
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
public final class AppModule_ProvideFirebaseFunctionsFactory implements Factory<FirebaseFunctions> {
  @Override
  public FirebaseFunctions get() {
    return provideFirebaseFunctions();
  }

  public static AppModule_ProvideFirebaseFunctionsFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FirebaseFunctions provideFirebaseFunctions() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFirebaseFunctions());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideFirebaseFunctionsFactory INSTANCE = new AppModule_ProvideFirebaseFunctionsFactory();
  }
}
