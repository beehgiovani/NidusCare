package com.developersbeeh.medcontrol.ui.settings;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private SettingsViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<FirebaseAuth> authProvider, Provider<UserPreferences> userPreferencesProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.authProvider = authProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(userRepositoryProvider.get(), authProvider.get(), userPreferencesProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<FirebaseAuth> authProvider, Provider<UserPreferences> userPreferencesProvider) {
    return new SettingsViewModel_Factory(userRepositoryProvider, authProvider, userPreferencesProvider);
  }

  public static SettingsViewModel newInstance(UserRepository userRepository, FirebaseAuth auth,
      UserPreferences userPreferences) {
    return new SettingsViewModel(userRepository, auth, userPreferences);
  }
}
