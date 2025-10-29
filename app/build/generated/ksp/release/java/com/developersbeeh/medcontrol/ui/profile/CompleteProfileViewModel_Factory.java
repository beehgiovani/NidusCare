package com.developersbeeh.medcontrol.ui.profile;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.AchievementRepository;
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
public final class CompleteProfileViewModel_Factory implements Factory<CompleteProfileViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<AchievementRepository> achievementRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private CompleteProfileViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.achievementRepositoryProvider = achievementRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public CompleteProfileViewModel get() {
    return newInstance(userRepositoryProvider.get(), achievementRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static CompleteProfileViewModel_Factory create(
      Provider<UserRepository> userRepositoryProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new CompleteProfileViewModel_Factory(userRepositoryProvider, achievementRepositoryProvider, userPreferencesProvider);
  }

  public static CompleteProfileViewModel newInstance(UserRepository userRepository,
      AchievementRepository achievementRepository, UserPreferences userPreferences) {
    return new CompleteProfileViewModel(userRepository, achievementRepository, userPreferences);
  }
}
