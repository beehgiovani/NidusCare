package com.developersbeeh.medcontrol.data.repository;

import android.content.Context;
import com.developersbeeh.medcontrol.data.remote.GooglePlacesApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class PlacesRepository_Factory implements Factory<PlacesRepository> {
  private final Provider<GooglePlacesApiService> apiServiceProvider;

  private final Provider<Context> contextProvider;

  private PlacesRepository_Factory(Provider<GooglePlacesApiService> apiServiceProvider,
      Provider<Context> contextProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public PlacesRepository get() {
    return newInstance(apiServiceProvider.get(), contextProvider.get());
  }

  public static PlacesRepository_Factory create(Provider<GooglePlacesApiService> apiServiceProvider,
      Provider<Context> contextProvider) {
    return new PlacesRepository_Factory(apiServiceProvider, contextProvider);
  }

  public static PlacesRepository newInstance(GooglePlacesApiService apiService, Context context) {
    return new PlacesRepository(apiService, context);
  }
}
