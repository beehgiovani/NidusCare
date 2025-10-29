package com.developersbeeh.medcontrol.ui.profile;

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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<FirebaseAuth> authProvider;

  private ProfileViewModel_Factory(Provider<UserRepository> userRepositoryProvider,
      Provider<FirebaseAuth> authProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
    this.authProvider = authProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(userRepositoryProvider.get(), authProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<UserRepository> userRepositoryProvider,
      Provider<FirebaseAuth> authProvider) {
    return new ProfileViewModel_Factory(userRepositoryProvider, authProvider);
  }

  public static ProfileViewModel newInstance(UserRepository userRepository, FirebaseAuth auth) {
    return new ProfileViewModel(userRepository, auth);
  }
}
