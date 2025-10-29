package com.developersbeeh.medcontrol.ui.geriatric;

import com.developersbeeh.medcontrol.data.repository.EducationRepository;
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
public final class GeriatricCareViewModel_Factory implements Factory<GeriatricCareViewModel> {
  private final Provider<EducationRepository> educationRepositoryProvider;

  private final Provider<ReminderRepository> reminderRepositoryProvider;

  private final Provider<FirestoreRepository> firestoreRepositoryProvider;

  private GeriatricCareViewModel_Factory(Provider<EducationRepository> educationRepositoryProvider,
      Provider<ReminderRepository> reminderRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    this.educationRepositoryProvider = educationRepositoryProvider;
    this.reminderRepositoryProvider = reminderRepositoryProvider;
    this.firestoreRepositoryProvider = firestoreRepositoryProvider;
  }

  @Override
  public GeriatricCareViewModel get() {
    return newInstance(educationRepositoryProvider.get(), reminderRepositoryProvider.get(), firestoreRepositoryProvider.get());
  }

  public static GeriatricCareViewModel_Factory create(
      Provider<EducationRepository> educationRepositoryProvider,
      Provider<ReminderRepository> reminderRepositoryProvider,
      Provider<FirestoreRepository> firestoreRepositoryProvider) {
    return new GeriatricCareViewModel_Factory(educationRepositoryProvider, reminderRepositoryProvider, firestoreRepositoryProvider);
  }

  public static GeriatricCareViewModel newInstance(EducationRepository educationRepository,
      ReminderRepository reminderRepository, FirestoreRepository firestoreRepository) {
    return new GeriatricCareViewModel(educationRepository, reminderRepository, firestoreRepository);
  }
}
