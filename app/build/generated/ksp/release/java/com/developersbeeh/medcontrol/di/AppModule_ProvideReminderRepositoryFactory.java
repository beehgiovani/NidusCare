package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.ReminderRepository;
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
public final class AppModule_ProvideReminderRepositoryFactory implements Factory<ReminderRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private AppModule_ProvideReminderRepositoryFactory(Provider<FirebaseFirestore> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ReminderRepository get() {
    return provideReminderRepository(dbProvider.get());
  }

  public static AppModule_ProvideReminderRepositoryFactory create(
      Provider<FirebaseFirestore> dbProvider) {
    return new AppModule_ProvideReminderRepositoryFactory(dbProvider);
  }

  public static ReminderRepository provideReminderRepository(FirebaseFirestore db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideReminderRepository(db));
  }
}
