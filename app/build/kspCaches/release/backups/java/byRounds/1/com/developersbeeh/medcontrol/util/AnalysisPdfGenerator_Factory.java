package com.developersbeeh.medcontrol.util;

import android.content.Context;
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
public final class AnalysisPdfGenerator_Factory implements Factory<AnalysisPdfGenerator> {
  private final Provider<Context> contextProvider;

  private AnalysisPdfGenerator_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AnalysisPdfGenerator get() {
    return newInstance(contextProvider.get());
  }

  public static AnalysisPdfGenerator_Factory create(Provider<Context> contextProvider) {
    return new AnalysisPdfGenerator_Factory(contextProvider);
  }

  public static AnalysisPdfGenerator newInstance(Context context) {
    return new AnalysisPdfGenerator(context);
  }
}
