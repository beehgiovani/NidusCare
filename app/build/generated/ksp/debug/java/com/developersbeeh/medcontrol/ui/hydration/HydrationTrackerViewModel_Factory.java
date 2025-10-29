package com.developersbeeh.medcontrol.ui.hydration;

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
public final class HydrationTrackerViewModel_Factory implements Factory<HydrationTrackerViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<Application> applicationProvider;

  private HydrationTrackerViewModel_Factory(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public HydrationTrackerViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), applicationProvider.get());
  }

  public static HydrationTrackerViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new HydrationTrackerViewModel_Factory(firestoreRepositoryProvider, applicationProvider);
  }

  public static HydrationTrackerViewModel newInstance(FirestoreRepository firestoreRepository,
      Application application) {
    return new HydrationTrackerViewModel(firestoreRepository, application);
  }
}
