package com.developersbeeh.medcontrol.ui.weight;

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
public final class WeightTrackerViewModel_Factory implements Factory<WeightTrackerViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<Application> applicationProvider;

  private WeightTrackerViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public WeightTrackerViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), applicationProvider.get());
  }

  public static WeightTrackerViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new WeightTrackerViewModel_Factory(firestoreRepositoryProvider, applicationProvider);
  }

  public static WeightTrackerViewModel newInstance(FirestoreRepository firestoreRepository,
      Application application) {
    return new WeightTrackerViewModel(firestoreRepository, application);
  }
}
