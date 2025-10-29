package com.developersbeeh.medcontrol.data.repository;

import android.content.Context;
import com.google.firebase.storage.FirebaseStorage;
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
public final class StorageRepository_Factory implements Factory<StorageRepository> {
  private final Provider<FirebaseStorage> storageProvider;

  private final Provider<Context> contextProvider;

  private StorageRepository_Factory(Provider<FirebaseStorage> storageProvider,
      Provider<Context> contextProvider) {
    this.storageProvider = storageProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public StorageRepository get() {
    return newInstance(storageProvider.get(), contextProvider.get());
  }

  public static StorageRepository_Factory create(Provider<FirebaseStorage> storageProvider,
      Provider<Context> contextProvider) {
    return new StorageRepository_Factory(storageProvider, contextProvider);
  }

  public static StorageRepository newInstance(FirebaseStorage storage, Context context) {
    return new StorageRepository(storage, context);
  }
}
