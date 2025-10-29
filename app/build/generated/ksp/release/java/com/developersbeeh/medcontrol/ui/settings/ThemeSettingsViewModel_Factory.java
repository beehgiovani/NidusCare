package com.developersbeeh.medcontrol.ui.settings;

import android.app.Application;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ThemeSettingsViewModel_Factory implements Factory<ThemeSettingsViewModel> {
  private final Provider<Application> applicationProvider;

  private ThemeSettingsViewModel_Factory(Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public ThemeSettingsViewModel get() {
    return newInstance(applicationProvider.get());
  }

  public static ThemeSettingsViewModel_Factory create(Provider<Application> applicationProvider) {
    return new ThemeSettingsViewModel_Factory(applicationProvider);
  }

  public static ThemeSettingsViewModel newInstance(Application application) {
    return new ThemeSettingsViewModel(application);
  }
}
