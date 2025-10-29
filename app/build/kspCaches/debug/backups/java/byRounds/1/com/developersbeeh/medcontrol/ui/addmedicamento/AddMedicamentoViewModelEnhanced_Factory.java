package com.developersbeeh.medcontrol.ui.addmedicamento;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.MedicationRepositoryEnhanced;
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
public final class AddMedicamentoViewModelEnhanced_Factory implements Factory<AddMedicamentoViewModelEnhanced> {
  private final Provider<MedicationRepositoryEnhanced> repositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private AddMedicamentoViewModelEnhanced_Factory(
      Provider<MedicationRepositoryEnhanced> repositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.repositoryProvider = repositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public AddMedicamentoViewModelEnhanced get() {
    return newInstance(repositoryProvider.get(), userPreferencesProvider.get());
  }

  public static AddMedicamentoViewModelEnhanced_Factory create(
      Provider<MedicationRepositoryEnhanced> repositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new AddMedicamentoViewModelEnhanced_Factory(repositoryProvider, userPreferencesProvider);
  }

  public static AddMedicamentoViewModelEnhanced newInstance(MedicationRepositoryEnhanced repository,
      UserPreferences userPreferences) {
    return new AddMedicamentoViewModelEnhanced(repository, userPreferences);
  }
}
