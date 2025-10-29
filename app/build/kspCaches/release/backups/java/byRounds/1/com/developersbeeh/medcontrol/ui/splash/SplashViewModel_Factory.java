package com.developersbeeh.medcontrol.ui.splash;

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
public final class SplashViewModel_Factory implements Factory<SplashViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private SplashViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public SplashViewModel get() {
    return newInstance(userRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static SplashViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new SplashViewModel_Factory(userRepositoryProvider, userPreferencesProvider);
  }

  public static SplashViewModel newInstance(UserRepository userRepository,
      UserPreferences userPreferences) {
    return new SplashViewModel(userRepository, userPreferences);
  }
}
