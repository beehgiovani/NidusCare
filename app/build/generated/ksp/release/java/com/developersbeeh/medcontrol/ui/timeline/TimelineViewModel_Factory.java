package com.developersbeeh.medcontrol.ui.timeline;

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
public final class TimelineViewModel_Factory implements Factory<TimelineViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private TimelineViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
  }

  @Override
  public TimelineViewModel get() {
    return newInstance(firestoreRepositoryProvider.get());
  }

  public static TimelineViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    return new TimelineViewModel_Factory(firestoreRepositoryProvider);
  }

  public static TimelineViewModel newInstance(FirestoreRepository firestoreRepository) {
    return new TimelineViewModel(firestoreRepository);
  }
}
