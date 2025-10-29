package com.developersbeeh.medcontrol.di;

import android.content.Context;
import com.developersbeeh.medcontrol.data.remote.GooglePlacesApiService;
import com.developersbeeh.medcontrol.data.repository.PlacesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvidePlacesRepositoryFactory implements Factory<PlacesRepository> {
  private final Provider<GooglePlacesApiService> apiServiceProvider;

  private final Provider<Context> contextProvider;

  private AppModule_ProvidePlacesRepositoryFactory(
      Provider<GooglePlacesApiService> apiServiceProvider, Provider<Context> contextProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public PlacesRepository get() {
    return providePlacesRepository(apiServiceProvider.get(), contextProvider.get());
  }

  public static AppModule_ProvidePlacesRepositoryFactory create(
      Provider<GooglePlacesApiService> apiServiceProvider, Provider<Context> contextProvider) {
    return new AppModule_ProvidePlacesRepositoryFactory(apiServiceProvider, contextProvider);
  }

  public static PlacesRepository providePlacesRepository(GooglePlacesApiService apiService,
      Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePlacesRepository(apiService, context));
  }
}
