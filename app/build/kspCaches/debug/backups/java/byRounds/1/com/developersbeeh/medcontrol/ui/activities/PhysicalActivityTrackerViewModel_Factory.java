package com.developersbeeh.medcontrol.ui.activities;

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
public final class PhysicalActivityTrackerViewModel_Factory implements Factory<PhysicalActivityTrackerViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<Application> applicationProvider;

  private PhysicalActivityTrackerViewModel_Factory(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public PhysicalActivityTrackerViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), applicationProvider.get());
  }

  public static PhysicalActivityTrackerViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new PhysicalActivityTrackerViewModel_Factory(firestoreRepositoryProvider, applicationProvider);
  }

  public static PhysicalActivityTrackerViewModel newInstance(
      FirestoreRepository firestoreRepository, Application application) {
    return new PhysicalActivityTrackerViewModel(firestoreRepository, application);
  }
}
