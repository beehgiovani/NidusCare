package com.developersbeeh.medcontrol.ui.education;

import com.developersbeeh.medcontrol.data.repository.EducationRepository;
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
public final class EducationViewModel_Factory implements Factory<EducationViewModel> {
  private final Provider<EducationRepository> repositoryProvider;

  private EducationViewModel_Factory(Provider<EducationRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public EducationViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static EducationViewModel_Factory create(
      Provider<EducationRepository> repositoryProvider) {
    return new EducationViewModel_Factory(repositoryProvider);
  }

  public static EducationViewModel newInstance(EducationRepository repository) {
    return new EducationViewModel(repository);
  }
}
