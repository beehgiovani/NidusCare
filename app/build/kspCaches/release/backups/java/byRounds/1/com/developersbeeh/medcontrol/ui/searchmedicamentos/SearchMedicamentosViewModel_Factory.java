package com.developersbeeh.medcontrol.ui.searchmedicamentos;

import com.developersbeeh.medcontrol.data.repository.RealtimeDatabaseRepository;
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
public final class SearchMedicamentosViewModel_Factory implements Factory<SearchMedicamentosViewModel> {
  private final Provider<RealtimeDatabaseRepository> realtimeDatabaseRepositoryProvider;

  private SearchMedicamentosViewModel_Factory(
      Provider<RealtimeDatabaseRepository> realtimeDatabaseRepositoryProvider) {
    this.realtimeDatabaseRepositoryProvider = realtimeDatabaseRepositoryProvider;
  }

  @Override
  public SearchMedicamentosViewModel get() {
    return newInstance(realtimeDatabaseRepositoryProvider.get());
  }

  public static SearchMedicamentosViewModel_Factory create(
      Provider<RealtimeDatabaseRepository> realtimeDatabaseRepositoryProvider) {
    return new SearchMedicamentosViewModel_Factory(realtimeDatabaseRepositoryProvider);
  }

  public static SearchMedicamentosViewModel newInstance(
      RealtimeDatabaseRepository realtimeDatabaseRepository) {
    return new SearchMedicamentosViewModel(realtimeDatabaseRepository);
  }
}
