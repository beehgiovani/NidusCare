package com.developersbeeh.medcontrol.ui.meals;

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
public final class MealTrackerViewModel_Factory implements Factory<MealTrackerViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private MealTrackerViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
  }

  @Override
  public MealTrackerViewModel get() {
    return newInstance(firestoreRepositoryProvider.get());
  }

  public static MealTrackerViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    return new MealTrackerViewModel_Factory(firestoreRepositoryProvider);
  }

  public static MealTrackerViewModel newInstance(FirestoreRepository firestoreRepository) {
    return new MealTrackerViewModel(firestoreRepository);
  }
}
