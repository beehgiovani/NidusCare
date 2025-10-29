package com.developersbeeh.medcontrol.ui.analytics;

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
public final class HealthChartsViewModel_Factory implements Factory<HealthChartsViewModel> {
  private final Provider<FirestoreRepository> repositoryProvider;

  private HealthChartsViewModel_Factory(Provider<FirestoreRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public HealthChartsViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static HealthChartsViewModel_Factory create(
      Provider<FirestoreRepository> repositoryProvider) {
    return new HealthChartsViewModel_Factory(repositoryProvider);
  }

  public static HealthChartsViewModel newInstance(FirestoreRepository repository) {
    return new HealthChartsViewModel(repository);
  }
}
