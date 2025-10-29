package com.developersbeeh.medcontrol.ui.documents;

import com.developersbeeh.medcontrol.data.repository.DocumentRepository;
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
public final class HealthDocumentsViewModel_Factory implements Factory<HealthDocumentsViewModel> {
  private final Provider<DocumentRepository> documentRepositoryProvider;

  private HealthDocumentsViewModel_Factory(
      Provider<DocumentRepository> documentRepositoryProvider) {
    this.documentRepositoryProvider = documentRepositoryProvider;
  }

  @Override
  public HealthDocumentsViewModel get() {
    return newInstance(documentRepositoryProvider.get());
  }

  public static HealthDocumentsViewModel_Factory create(
      Provider<DocumentRepository> documentRepositoryProvider) {
    return new HealthDocumentsViewModel_Factory(documentRepositoryProvider);
  }

  public static HealthDocumentsViewModel newInstance(DocumentRepository documentRepository) {
    return new HealthDocumentsViewModel(documentRepository);
  }
}
