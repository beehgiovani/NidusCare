package com.developersbeeh.medcontrol.ui.family;

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
public final class ManageFamilyViewModel_Factory implements Factory<ManageFamilyViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private ManageFamilyViewModel_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public ManageFamilyViewModel get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static ManageFamilyViewModel_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new ManageFamilyViewModel_Factory(userRepositoryProvider);
  }

  public static ManageFamilyViewModel newInstance(UserRepository userRepository) {
    return new ManageFamilyViewModel(userRepository);
  }
}
