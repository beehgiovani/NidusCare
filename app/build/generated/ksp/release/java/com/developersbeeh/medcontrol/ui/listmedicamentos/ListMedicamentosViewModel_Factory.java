package com.developersbeeh.medcontrol.ui.listmedicamentos;

import android.app.Application;
import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.MedicationRepository;
import com.developersbeeh.medcontrol.data.repository.ReminderRepository;
import com.developersbeeh.medcontrol.data.repository.UserRepository;
import com.google.firebase.functions.FirebaseFunctions;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ListMedicamentosViewModel_Factory implements Factory<ListMedicamentosViewModel> {
  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<ReminderRepository> reminderRepositoryProvider;

  private final Provider<MedicationRepository> medicationRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<ActivityLogRepository> activityLogRepositoryProvider;

  private final Provider<FirebaseFunctions> functionsProvider;

  private final Provider<Application> applicationProvider;

  private ListMedicamentosViewModel_Factory(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<ReminderRepository> reminderRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider,
      Provider<FirebaseFunctions> functionsProvider, Provider<Application> applicationProvider) {
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.reminderRepositoryProvider = reminderRepositoryProvider;
    this.medicationRepositoryProvider = medicationRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.activityLogRepositoryProvider = activityLogRepositoryProvider;
    this.functionsProvider = functionsProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public ListMedicamentosViewModel get() {
    return newInstance(firestoreRepositoryProvider.get(), reminderRepositoryProvider.get(), medicationRepositoryProvider.get(), userRepositoryProvider.get(), userPreferencesProvider.get(), activityLogRepositoryProvider.get(), functionsProvider.get(), applicationProvider.get());
  }

  public static ListMedicamentosViewModel_Factory create(
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<ReminderRepository> reminderRepositoryProvider,
      Provider<MedicationRepository> medicationRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider,
      Provider<FirebaseFunctions> functionsProvider, Provider<Application> applicationProvider) {
    return new ListMedicamentosViewModel_Factory(firestoreRepositoryProvider, reminderRepositoryProvider, medicationRepositoryProvider, userRepositoryProvider, userPreferencesProvider, activityLogRepositoryProvider, functionsProvider, applicationProvider);
  }

  public static ListMedicamentosViewModel newInstance(FirestoreRepository firestoreRepository,
      ReminderRepository reminderRepository, MedicationRepository medicationRepository,
      UserRepository userRepository, UserPreferences userPreferences,
      ActivityLogRepository activityLogRepository, FirebaseFunctions functions,
      Application application) {
    return new ListMedicamentosViewModel(firestoreRepository, reminderRepository, medicationRepository, userRepository, userPreferences, activityLogRepository, functions, application);
  }
}
