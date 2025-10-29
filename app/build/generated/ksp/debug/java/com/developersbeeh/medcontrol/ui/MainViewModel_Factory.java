package com.developersbeeh.medcontrol.ui;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<FirebaseAuth> authProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private MainViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider, Provider<FirebaseAuth> authProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.authProvider = authProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), userRepositoryProvider.get(), authProvider.get(), userPreferencesProvider.get());
  }

  public static MainViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider, Provider<FirebaseAuth> authProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new MainViewModel_Factory(firestoreRepositoryProvider, userRepositoryProvider, authProvider, userPreferencesProvider);
  }

  public static MainViewModel newInstance(FirestoreRepository firestoreRepository,
      UserRepository userRepository, FirebaseAuth auth, UserPreferences userPreferences) {
    return new MainViewModel(firestoreRepository, userRepository, auth, userPreferences);
  }
}
