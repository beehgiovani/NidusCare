package com.developersbeeh.medcontrol.di;

import android.content.Context;
import com.developersbeeh.medcontrol.util.AnalysisPdfGenerator;
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
public final class AppModule_ProvideAnalysisPdfGeneratorFactory implements Factory<AnalysisPdfGenerator> {
  private final Provider<Context> contextProvider;

  private AppModule_ProvideAnalysisPdfGeneratorFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AnalysisPdfGenerator get() {
    return provideAnalysisPdfGenerator(contextProvider.get());
  }

  public static AppModule_ProvideAnalysisPdfGeneratorFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvideAnalysisPdfGeneratorFactory(contextProvider);
  }

  public static AnalysisPdfGenerator provideAnalysisPdfGenerator(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAnalysisPdfGenerator(context));
  }
}
