package com.developersbeeh.medcontrol.ui.addmedicamento;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.AchievementRepository;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
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
public final class AddMedicamentoViewModel_Factory implements Factory<AddMedicamentoViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<AchievementRepository> achievementRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private AddMedicamentoViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.achievementRepositoryProvider = achievementRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public AddMedicamentoViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), achievementRepositoryProvider.get(), medicationRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static AddMedicamentoViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new AddMedicamentoViewModel_Factory(firestoreRepositoryProvider, achievementRepositoryProvider, medicationRepositoryProvider, userPreferencesProvider);
  }

  public static AddMedicamentoViewModel newInstance(FirestoreRepository firestoreRepository,
      AchievementRepository achievementRepository, MedicationRepository medicationRepository,
      UserPreferences userPreferences) {
    return new AddMedicamentoViewModel(firestoreRepository, achievementRepository, medicationRepository, userPreferences);
  }
}
