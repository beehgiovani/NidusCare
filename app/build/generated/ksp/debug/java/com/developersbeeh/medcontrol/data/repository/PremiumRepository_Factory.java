package com.developersbeeh.medcontrol.data.repository;

import com.google.firebase.auth.FirebaseAuth;
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
public final class PremiumRepository_Factory implements Factory<PremiumRepository> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseFunctions> functionsProvider;

  private final Provider<FirebaseAuth> authProvider;

  private PremiumRepository_Factory(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseFunctions> functionsProvider, Provider<FirebaseAuth> authProvider) {
    this.firestoreProvider = firestoreProvider;
    this.functionsProvider = functionsProvider;
    this.authProvider = authProvider;
  }

  @Override
  public PremiumRepository get() {
    return newInstance(firestoreProvider.get(), functionsProvider.get(), authProvider.get());
  }

  public static PremiumRepository_Factory create(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseFunctions> functionsProvider, Provider<FirebaseAuth> authProvider) {
    return new PremiumRepository_Factory(firestoreProvider, functionsProvider, authProvider);
  }

  public static PremiumRepository newInstance(FirebaseFirestore firestore,
      FirebaseFunctions functions, FirebaseAuth auth) {
    return new PremiumRepository(firestore, functions, auth);
  }
}
