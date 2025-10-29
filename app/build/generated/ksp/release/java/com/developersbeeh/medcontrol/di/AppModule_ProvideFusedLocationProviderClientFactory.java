package com.developersbeeh.medcontrol.di;

import android.content.Context;
import com.google.android.gms.location.FusedLocationProviderClient;
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
public final class AppModule_ProvideFusedLocationProviderClientFactory implements Factory<FusedLocationProviderClient> {
  private final Provider<Context> contextProvider;

  private AppModule_ProvideFusedLocationProviderClientFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FusedLocationProviderClient get() {
    return provideFusedLocationProviderClient(contextProvider.get());
  }

  public static AppModule_ProvideFusedLocationProviderClientFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvideFusedLocationProviderClientFactory(contextProvider);
  }

  public static FusedLocationProviderClient provideFusedLocationProviderClient(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFusedLocationProviderClient(context));
  }
}
