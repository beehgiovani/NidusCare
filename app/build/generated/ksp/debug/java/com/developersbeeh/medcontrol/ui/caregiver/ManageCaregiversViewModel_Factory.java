package com.developersbeeh.medcontrol.ui.caregiver;

import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
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
public final class ManageCaregiversViewModel_Factory implements Factory<ManageCaregiversViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private ManageCaregiversViewModel_Factory(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public ManageCaregiversViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static ManageCaregiversViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new ManageCaregiversViewModel_Factory(firestoreRepositoryProvider, userRepositoryProvider);
  }

  public static ManageCaregiversViewModel newInstance(FirestoreRepository firestoreRepository,
      UserRepository userRepository) {
    return new ManageCaregiversViewModel(firestoreRepository, userRepository);
  }
}
