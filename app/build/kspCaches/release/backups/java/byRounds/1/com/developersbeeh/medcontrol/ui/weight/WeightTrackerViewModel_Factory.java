package com.developersbeeh.medcontrol.ui.weight;

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

  private WeightTrackerViewModel_Factory(
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
  }

  @Override
  public WeightTrackerViewModel get() {
    return newInstance(firestoreRepositoryProvider.get());
  }

  public static WeightTrackerViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    return new WeightTrackerViewModel_Factory(firestoreRepositoryProvider);
  }

  public static WeightTrackerViewModel newInstance(FirestoreRepository firestoreRepository) {
    return new WeightTrackerViewModel(firestoreRepository);
  }
}
