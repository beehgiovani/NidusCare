package com.developersbeeh.medcontrol.di;

import com.google.firebase.storage.FirebaseStorage;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideFirebaseStorageFactory implements Factory<FirebaseStorage> {
  @Override
  public FirebaseStorage get() {
    return provideFirebaseStorage();
  }

  public static AppModule_ProvideFirebaseStorageFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FirebaseStorage provideFirebaseStorage() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFirebaseStorage());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideFirebaseStorageFactory INSTANCE = new AppModule_ProvideFirebaseStorageFactory();
  }
}
