package com.developersbeeh.medcontrol.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
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
public final class ChatRepository_Factory implements Factory<ChatRepository> {
  private final Provider<FirebaseFirestore> dbProvider;

  private final Provider<FirebaseFunctions> functionsProvider;

  private ChatRepository_Factory(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseFunctions> functionsProvider) {
    this.dbProvider = dbProvider;
    this.functionsProvider = functionsProvider;
  }

  @Override
  public ChatRepository get() {
    return newInstance(dbProvider.get(), functionsProvider.get());
  }

  public static ChatRepository_Factory create(Provider<FirebaseFirestore> dbProvider,
      Provider<FirebaseFunctions> functionsProvider) {
    return new ChatRepository_Factory(dbProvider, functionsProvider);
  }

  public static ChatRepository newInstance(FirebaseFirestore db, FirebaseFunctions functions) {
    return new ChatRepository(db, functions);
  }
}
