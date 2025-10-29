package com.developersbeeh.medcontrol.ui.profile;

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
public final class ProfileEditViewModel_Factory implements Factory<ProfileEditViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private ProfileEditViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public ProfileEditViewModel get() {
    return newInstance(userRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static ProfileEditViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new ProfileEditViewModel_Factory(userRepositoryProvider, userPreferencesProvider);
  }

  public static ProfileEditViewModel newInstance(UserRepository userRepository,
      UserPreferences userPreferences) {
    return new ProfileEditViewModel(userRepository, userPreferences);
  }
}
