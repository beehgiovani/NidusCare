package com.developersbeeh.medcontrol.ui.login;

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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private LoginViewModel_Factory(Provider<FirebaseAuth> authProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.authProvider = authProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(authProvider.get(), userRepositoryProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new LoginViewModel_Factory(authProvider, userRepositoryProvider);
  }

  public static LoginViewModel newInstance(FirebaseAuth auth, UserRepository userRepository) {
    return new LoginViewModel(auth, userRepository);
  }
}
