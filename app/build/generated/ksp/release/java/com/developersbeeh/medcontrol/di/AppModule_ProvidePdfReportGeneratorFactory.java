package com.developersbeeh.medcontrol.di;

import android.content.Context;
import com.developersbeeh.medcontrol.util.PdfReportGenerator;
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
public final class AppModule_ProvidePdfReportGeneratorFactory implements Factory<PdfReportGenerator> {
  private final Provider<Context> contextProvider;

  private AppModule_ProvidePdfReportGeneratorFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PdfReportGenerator get() {
    return providePdfReportGenerator(contextProvider.get());
  }

  public static AppModule_ProvidePdfReportGeneratorFactory create(
      Provider<Context> contextProvider) {
    return new AppModule_ProvidePdfReportGeneratorFactory(contextProvider);
  }

  public static PdfReportGenerator providePdfReportGenerator(Context context) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePdfReportGenerator(context));
  }
}
