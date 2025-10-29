package com.developersbeeh.medcontrol.ui.profile;

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
public final class EditSelfProfileHealthViewModel_Factory implements Factory<EditSelfProfileHealthViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  private EditSelfProfileHealthViewModel_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public EditSelfProfileHealthViewModel get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static EditSelfProfileHealthViewModel_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new EditSelfProfileHealthViewModel_Factory(userRepositoryProvider);
  }

  public static EditSelfProfileHealthViewModel newInstance(UserRepository userRepository) {
    return new EditSelfProfileHealthViewModel(userRepository);
  }
}
