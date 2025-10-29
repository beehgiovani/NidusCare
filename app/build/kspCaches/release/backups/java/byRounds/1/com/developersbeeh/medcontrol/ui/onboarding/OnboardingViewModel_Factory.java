package com.developersbeeh.medcontrol.ui.onboarding;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  @Override
  public OnboardingViewModel get() {
    return newInstance();
  }

  public static OnboardingViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static OnboardingViewModel newInstance() {
    return new OnboardingViewModel();
  }

  private static final class InstanceHolder {
    static final OnboardingViewModel_Factory INSTANCE = new OnboardingViewModel_Factory();
  }
}
