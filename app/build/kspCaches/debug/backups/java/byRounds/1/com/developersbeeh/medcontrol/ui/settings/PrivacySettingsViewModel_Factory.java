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
public final class PrivacySettingsViewModel_Factory implements Factory<PrivacySettingsViewModel> {
  private final Provider<Application> applicationProvider;

  private PrivacySettingsViewModel_Factory(Provider<Application> applicationProvider) {
    this.applicationProvider = applicationProvider;
  }

  @Override
  public PrivacySettingsViewModel get() {
    return newInstance(applicationProvider.get());
  }

  public static PrivacySettingsViewModel_Factory create(Provider<Application> applicationProvider) {
    return new PrivacySettingsViewModel_Factory(applicationProvider);
  }

  public static PrivacySettingsViewModel newInstance(Application application) {
    return new PrivacySettingsViewModel(application);
  }
}
