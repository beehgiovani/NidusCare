package com.developersbeeh.medcontrol.ui.wellbeing.timer;

import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.UserRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class ActivityTimerService_MembersInjector implements MembersInjector<ActivityTimerService> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private ActivityTimerService_MembersInjector(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
  }

  @Override
  public void injectMembers(ActivityTimerService instance) {
    injectFirestoreRepository(instance, firestoreRepositoryProvider.get());
    injectUserRepository(instance, userRepositoryProvider.get());
    injectUserPreferences(instance, userPreferencesProvider.get());
  }

  public static MembersInjector<ActivityTimerService> create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider) {
    return new ActivityTimerService_MembersInjector(firestoreRepositoryProvider, userRepositoryProvider, userPreferencesProvider);
  }

  @InjectedFieldSignature("com.developersbeeh.medcontrol.ui.wellbeing.timer.ActivityTimerService.firestoreRepository")
  public static void injectFirestoreRepository(ActivityTimerService instance,
      FirestoreRepository firestoreRepository) {
    instance.firestoreRepository = firestoreRepository;
  }

  @InjectedFieldSignature("com.developersbeeh.medcontrol.ui.wellbeing.timer.ActivityTimerService.userRepository")
  public static void injectUserRepository(ActivityTimerService instance,
      UserRepository userRepository) {
    instance.userRepository = userRepository;
  }

  @InjectedFieldSignature("com.developersbeeh.medcontrol.ui.wellbeing.timer.ActivityTimerService.userPreferences")
  public static void injectUserPreferences(ActivityTimerService instance,
      UserPreferences userPreferences) {
    instance.userPreferences = userPreferences;
  }
}
