package com.developersbeeh.medcontrol.ui.analysis;

import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.UserRepository;
import com.developersbeeh.medcontrol.util.AnalysisPdfGenerator;
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
public final class AnalysisResultViewModel_Factory implements Factory<AnalysisResultViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<AnalysisPdfGenerator> analysisPdfGeneratorProvider;

  private AnalysisResultViewModel_Factory(Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<AnalysisPdfGenerator> analysisPdfGeneratorProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.analysisPdfGeneratorProvider = analysisPdfGeneratorProvider;
  }

  @Override
  public AnalysisResultViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), userRepositoryProvider.get(), analysisPdfGeneratorProvider.get());
  }

  public static AnalysisResultViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<AnalysisPdfGenerator> analysisPdfGeneratorProvider) {
    return new AnalysisResultViewModel_Factory(firestoreRepositoryProvider, userRepositoryProvider, analysisPdfGeneratorProvider);
  }

  public static AnalysisResultViewModel newInstance(FirestoreRepository firestoreRepository,
      UserRepository userRepository, AnalysisPdfGenerator analysisPdfGenerator) {
    return new AnalysisResultViewModel(firestoreRepository, userRepository, analysisPdfGenerator);
  }
}
