package com.developersbeeh.medcontrol.ui.pharmacy;

import com.developersbeeh.medcontrol.data.repository.PlacesRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
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
public final class PharmacySelectionViewModel_Factory implements Factory<PharmacySelectionViewModel> {
  private final Provider<PlacesRepository> placesRepositoryProvider;

  private final Provider<FusedLocationProviderClient> locationProvider;

  private PharmacySelectionViewModel_Factory(Provider<PlacesRepository> placesRepositoryProvider,
      Provider<FusedLocationProviderClient> locationProvider) {
    this.placesRepositoryProvider = placesRepositoryProvider;
    this.locationProvider = locationProvider;
  }

  @Override
  public PharmacySelectionViewModel get() {
    return newInstance(placesRepositoryProvider.get(), locationProvider.get());
  }

  public static PharmacySelectionViewModel_Factory create(
      Provider<PlacesRepository> placesRepositoryProvider,
      Provider<FusedLocationProviderClient> locationProvider) {
    return new PharmacySelectionViewModel_Factory(placesRepositoryProvider, locationProvider);
  }

  public static PharmacySelectionViewModel newInstance(PlacesRepository placesRepository,
      FusedLocationProviderClient locationProvider) {
    return new PharmacySelectionViewModel(placesRepository, locationProvider);
  }
}
