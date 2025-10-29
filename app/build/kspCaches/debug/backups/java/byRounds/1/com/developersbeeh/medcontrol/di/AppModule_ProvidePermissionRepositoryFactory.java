package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.PermissionRepository;
import com.google.firebase.firestore.FirebaseFirestore;
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
public final class AppModule_ProvidePermissionRepositoryFactory implements Factory<PermissionRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private AppModule_ProvidePermissionRepositoryFactory(Provider<FirebaseFirestore> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PermissionRepository get() {
    return providePermissionRepository(dbProvider.get());
  }

  public static AppModule_ProvidePermissionRepositoryFactory create(
      Provider<FirebaseFirestore> dbProvider) {
    return new AppModule_ProvidePermissionRepositoryFactory(dbProvider);
  }

  public static PermissionRepository providePermissionRepository(FirebaseFirestore db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePermissionRepository(db));
  }
}
