package com.developersbeeh.medcontrol.ui.analysis;

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
public final class AnalysisHistoryViewModel_Factory implements Factory<AnalysisHistoryViewModel> {
  private final Provider<FirestoreRepository> repositoryProvider;

  private AnalysisHistoryViewModel_Factory(Provider<FirestoreRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AnalysisHistoryViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static AnalysisHistoryViewModel_Factory create(
      Provider<FirestoreRepository> repositoryProvider) {
    return new AnalysisHistoryViewModel_Factory(repositoryProvider);
  }

  public static AnalysisHistoryViewModel newInstance(FirestoreRepository repository) {
    return new AnalysisHistoryViewModel(repository);
  }
}
