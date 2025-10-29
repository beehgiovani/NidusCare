package com.developersbeeh.medcontrol.ui.settings;

import android.app.Application;
import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.UserRepository;
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
public final class NotificationSettingsViewModel_Factory implements Factory<NotificationSettingsViewModel> {
  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<Application> applicationProvider;

  private NotificationSettingsViewModel_Factory(Provider<UserPreferences> userPreferencesProvider,
      Provider<UserRepository> userRepositoryProvider, Provider<Application> applicationProvider) {
    this.userPreferencesProvider = userPreferencesProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public NotificationSettingsViewModel get() {
    return newInstance(userPreferencesProvider.get(), userRepositoryProvider.get(), applicationProvider.get());
  }

  public static NotificationSettingsViewModel_Factory create(
      Provider<UserPreferences> userPreferencesProvider,
      Provider<UserRepository> userRepositoryProvider, Provider<Application> applicationProvider) {
    return new NotificationSettingsViewModel_Factory(userPreferencesProvider, userRepositoryProvider, applicationProvider);
  }

  public static NotificationSettingsViewModel newInstance(UserPreferences userPreferences,
      UserRepository userRepository, Application application) {
    return new NotificationSettingsViewModel(userPreferences, userRepository, application);
  }
}
