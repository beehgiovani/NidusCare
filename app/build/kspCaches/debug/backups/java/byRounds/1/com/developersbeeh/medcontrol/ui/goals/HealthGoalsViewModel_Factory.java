package com.developersbeeh.medcontrol.ui.goals;

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
public final class HealthGoalsViewModel_Factory implements Factory<HealthGoalsViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private HealthGoalsViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
  }

  @Override
  public HealthGoalsViewModel get() {
    return newInstance(firestoreRepositoryProvider.get());
  }

  public static HealthGoalsViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    return new HealthGoalsViewModel_Factory(firestoreRepositoryProvider);
  }

  public static HealthGoalsViewModel newInstance(FirestoreRepository firestoreRepository) {
    return new HealthGoalsViewModel(firestoreRepository);
  }
}
