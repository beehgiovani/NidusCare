package com.developersbeeh.medcontrol.ui.dependents;

import android.app.Application;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
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
public final class LinkDependentViewModel_Factory implements Factory<LinkDependentViewModel> {
  private final Provider<FirestoreRepository> repositoryProvider;

  private final Provider<Application> applicationProvider;

  private LinkDependentViewModel_Factory(Provider<FirestoreRepository> repositoryProvider,
      Provider<Application> applicationProvider) {
    this.repositoryProvider = repositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public LinkDependentViewModel get() {
    return newInstance(repositoryProvider.get(), applicationProvider.get());
  }

  public static LinkDependentViewModel_Factory create(
      Provider<FirestoreRepository> repositoryProvider, Provider<Application> applicationProvider) {
    return new LinkDependentViewModel_Factory(repositoryProvider, applicationProvider);
  }

  public static LinkDependentViewModel newInstance(FirestoreRepository repository,
      Application application) {
    return new LinkDependentViewModel(repository, application);
  }
}
