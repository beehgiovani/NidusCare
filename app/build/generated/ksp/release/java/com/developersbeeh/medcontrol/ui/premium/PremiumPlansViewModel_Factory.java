package com.developersbeeh.medcontrol.ui.premium;

import com.developersbeeh.medcontrol.billing.BillingClientWrapper;
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
public final class PremiumPlansViewModel_Factory implements Factory<PremiumPlansViewModel> {
  private final Provider<BillingClientWrapper> billingClientWrapperProvider;

  private PremiumPlansViewModel_Factory(
      Provider<BillingClientWrapper> billingClientWrapperProvider) {
    this.billingClientWrapperProvider = billingClientWrapperProvider;
  }

  @Override
  public PremiumPlansViewModel get() {
    return newInstance(billingClientWrapperProvider.get());
  }

  public static PremiumPlansViewModel_Factory create(
      Provider<BillingClientWrapper> billingClientWrapperProvider) {
    return new PremiumPlansViewModel_Factory(billingClientWrapperProvider);
  }

  public static PremiumPlansViewModel newInstance(BillingClientWrapper billingClientWrapper) {
    return new PremiumPlansViewModel(billingClientWrapper);
  }
}
