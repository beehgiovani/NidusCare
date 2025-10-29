package com.developersbeeh.medcontrol.ui.reminders;

import android.app.Application;
import com.developersbeeh.medcontrol.data.repository.FirestoreRepository;
import com.developersbeeh.medcontrol.data.repository.ReminderRepository;
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
public final class RemindersViewModel_Factory implements Factory<RemindersViewModel> {
  private final Provider<ReminderRepository> reminderRepositoryProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private final Provider<Application> applicationProvider;

  private RemindersViewModel_Factory(Provider<ReminderRepository> reminderRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    this.reminderRepositoryProvider = reminderRepositoryProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public RemindersViewModel get() {
    return newInstance(reminderRepositoryProvider.get(), firestoreRepositoryProvider.get(), applicationProvider.get());
  }

  public static RemindersViewModel_Factory create(
      Provider<ReminderRepository> reminderRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider,
      Provider<Application> applicationProvider) {
    return new RemindersViewModel_Factory(reminderRepositoryProvider, firestoreRepositoryProvider, applicationProvider);
  }

  public static RemindersViewModel newInstance(ReminderRepository reminderRepository,
      FirestoreRepository firestoreRepository, Application application) {
    return new RemindersViewModel(reminderRepository, firestoreRepository, application);
  }
}
