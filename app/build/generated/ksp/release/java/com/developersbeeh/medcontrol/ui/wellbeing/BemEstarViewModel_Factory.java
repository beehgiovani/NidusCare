package com.developersbeeh.medcontrol.ui.wellbeing;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.MealAnalysisRepository;
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
public final class BemEstarViewModel_Factory implements Factory<BemEstarViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<MealAnalysisRepository> mealAnalysisRepositoryProvider;

  private BemEstarViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<MealAnalysisRepository> mealAnalysisRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.mealAnalysisRepositoryProvider = mealAnalysisRepositoryProvider;
  }

  @Override
  public BemEstarViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), userRepositoryProvider.get(), userPreferencesProvider.get(), mealAnalysisRepositoryProvider.get());
  }

  public static BemEstarViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<MealAnalysisRepository> mealAnalysisRepositoryProvider) {
    return new BemEstarViewModel_Factory(firestoreRepositoryProvider, userRepositoryProvider, userPreferencesProvider, mealAnalysisRepositoryProvider);
  }

  public static BemEstarViewModel newInstance(FirestoreRepository firestoreRepository,
      UserRepository userRepository, UserPreferences userPreferences,
      MealAnalysisRepository mealAnalysisRepository) {
    return new BemEstarViewModel(firestoreRepository, userRepository, userPreferences, mealAnalysisRepository);
  }
}
