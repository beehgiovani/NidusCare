package com.developersbeeh.medcontrol.billing;

import android.content.Context;
import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.UserRepository;
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
public final class BillingClientWrapper_Factory implements Factory<BillingClientWrapper> {
  private final Provider<Context> contextProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private BillingClientWrapper_Factory(Provider<Context> contextProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.contextProvider = contextProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public BillingClientWrapper get() {
    return newInstance(contextProvider.get(), userRepositoryProvider.get(), userPreferencesProvider.get());
  }

  public static BillingClientWrapper_Factory create(Provider<Context> contextProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new BillingClientWrapper_Factory(contextProvider, userRepositoryProvider, userPreferencesProvider);
  }

  public static BillingClientWrapper newInstance(Context context, UserRepository userRepository,
      UserPreferences userPreferences) {
    return new BillingClientWrapper(context, userRepository, userPreferences);
  }
}
