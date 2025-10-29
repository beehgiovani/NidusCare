package com.developersbeeh.medcontrol.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class DocumentRepository_Factory implements Factory<DocumentRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseStorage> storageProvider;

  private DocumentRepository_Factory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseStorage> storageProvider) {
    this.dbProvider = dbProvider;
    this.storageProvider = storageProvider;
  }

  @Override
  public DocumentRepository get() {
    return newInstance(dbProvider.get(), storageProvider.get());
  }

  public static DocumentRepository_Factory create(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseStorage> storageProvider) {
    return new DocumentRepository_Factory(dbProvider, storageProvider);
  }

  public static DocumentRepository newInstance(FirebaseFirestore db, FirebaseStorage storage) {
    return new DocumentRepository(db, storage);
  }
}
