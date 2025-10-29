package com.developersbeeh.medcontrol.ui.meals;

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
public final class MealTrackerViewModel_Factory implements Factory<MealTrackerViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<Application> applicationProvider;

  private MealTrackerViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public MealTrackerViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), applicationProvider.get());
  }

  public static MealTrackerViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new MealTrackerViewModel_Factory(firestoreRepositoryProvider, applicationProvider);
  }

  public static MealTrackerViewModel newInstance(FirestoreRepository firestoreRepository,
      Application application) {
    return new MealTrackerViewModel(firestoreRepository, application);
  }
}
