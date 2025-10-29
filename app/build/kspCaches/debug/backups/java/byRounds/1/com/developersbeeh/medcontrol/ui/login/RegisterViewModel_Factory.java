package com.developersbeeh.medcontrol.ui.login;

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
public final class RegisterViewModel_Factory implements Factory<RegisterViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<AchievementRepository> achievementRepositoryProvider;

  private RegisterViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<AchievementRepository> achievementRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.achievementRepositoryProvider = achievementRepositoryProvider;
  }

  @Override
  public RegisterViewModel get() {
    return newInstance(userRepositoryProvider.get(), achievementRepositoryProvider.get());
  }

  public static RegisterViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<AchievementRepository> achievementRepositoryProvider) {
    return new RegisterViewModel_Factory(userRepositoryProvider, achievementRepositoryProvider);
  }

  public static RegisterViewModel newInstance(UserRepository userRepository,
      AchievementRepository achievementRepository) {
    return new RegisterViewModel(userRepository, achievementRepository);
  }
}
