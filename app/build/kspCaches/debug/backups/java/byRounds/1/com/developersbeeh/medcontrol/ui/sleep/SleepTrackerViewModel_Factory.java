package com.developersbeeh.medcontrol.ui.sleep;

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
public final class SleepTrackerViewModel_Factory implements Factory<SleepTrackerViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<Application> applicationProvider;

  private SleepTrackerViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public SleepTrackerViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), applicationProvider.get());
  }

  public static SleepTrackerViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new SleepTrackerViewModel_Factory(firestoreRepositoryProvider, applicationProvider);
  }

  public static SleepTrackerViewModel newInstance(FirestoreRepository firestoreRepository,
      Application application) {
    return new SleepTrackerViewModel(firestoreRepository, application);
  }
}
