package com.developersbeeh.medcontrol.di;

import com.developersbeeh.medcontrol.data.repository.ChatRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
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
public final class AppModule_ProvideChatRepositoryFactory implements Factory<ChatRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseFunctions> functionsProvider;

  private AppModule_ProvideChatRepositoryFactory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseFunctions> functionsProvider) {
    this.dbProvider = dbProvider;
    this.functionsProvider = functionsProvider;
  }

  @Override
  public ChatRepository get() {
    return provideChatRepository(dbProvider.get(), functionsProvider.get());
  }

  public static AppModule_ProvideChatRepositoryFactory create(
      Provider<FirebaseFirestore> dbProvider, Provider<FirebaseFunctions> functionsProvider) {
    return new AppModule_ProvideChatRepositoryFactory(dbProvider, functionsProvider);
  }

  public static ChatRepository provideChatRepository(FirebaseFirestore db,
      FirebaseFunctions functions) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideChatRepository(db, functions));
  }
}
