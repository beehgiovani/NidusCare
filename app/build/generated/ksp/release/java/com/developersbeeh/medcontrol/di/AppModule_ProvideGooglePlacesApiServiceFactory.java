package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.remote.GooglePlacesApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideGooglePlacesApiServiceFactory implements Factory<GooglePlacesApiService> {
  private final Provider<Retrofit> retrofitProvider;

  private AppModule_ProvideGooglePlacesApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public GooglePlacesApiService get() {
    return provideGooglePlacesApiService(retrofitProvider.get());
  }

  public static AppModule_ProvideGooglePlacesApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new AppModule_ProvideGooglePlacesApiServiceFactory(retrofitProvider);
  }

  public static GooglePlacesApiService provideGooglePlacesApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGooglePlacesApiService(retrofit));
  }
}
