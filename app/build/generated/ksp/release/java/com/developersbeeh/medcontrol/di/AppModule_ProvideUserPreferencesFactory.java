package com.developersbeeh.medcontrol.di;

import android.content.Context;
import com.developersbeeh.medcontrol.data.UserPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvideUserPreferencesFactory implements Factory<UserPreferences> {
  private final Provider<Context> contextProvider;

  private AppModule_ProvideUserPreferencesFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UserPreferences get() {
    return provideUserPreferences(contextProvider.get());
  }

  public static AppModule_ProvideUserPreferencesFactory create(Provider<Context> contextProvider) {
    return new AppModule_ProvideUserPreferencesFactory(contextProvider);
  }

  public static UserPreferences provideUserPreferences(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideUserPreferences(context));
  }
}
