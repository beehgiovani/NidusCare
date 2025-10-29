package com.developersbeeh.medcontrol.ui.insights;

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
public final class InsightsViewModel_Factory implements Factory<InsightsViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private InsightsViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
  }

  @Override
  public InsightsViewModel get() {
    return newInstance(firestoreRepositoryProvider.get());
  }

  public static InsightsViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    return new InsightsViewModel_Factory(firestoreRepositoryProvider);
  }

  public static InsightsViewModel newInstance(FirestoreRepository firestoreRepository) {
    return new InsightsViewModel(firestoreRepository);
  }
}
