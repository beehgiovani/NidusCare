package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.DocumentRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AppModule_ProvideDocumentRepositoryFactory implements Factory<DocumentRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private AppModule_ProvideDocumentRepositoryFactory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseStorage> storageProvider) {
    this.dbProvider = dbProvider;
    this.storageProvider = storageProvider;
  }

  @Override
  public DocumentRepository get() {
    return provideDocumentRepository(dbProvider.get(), storageProvider.get());
  }

  public static AppModule_ProvideDocumentRepositoryFactory create(
      Provider<FirebaseFirestore> dbProvider, Provider<FirebaseStorage> storageProvider) {
    return new AppModule_ProvideDocumentRepositoryFactory(dbProvider, storageProvider);
  }

  public static DocumentRepository provideDocumentRepository(FirebaseFirestore db,
      FirebaseStorage storage) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDocumentRepository(db, storage));
  }
}
