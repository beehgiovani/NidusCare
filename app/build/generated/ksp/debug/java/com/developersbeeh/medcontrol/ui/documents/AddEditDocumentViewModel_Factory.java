package com.developersbeeh.medcontrol.ui.documents;

import android.content.Context;
import com.developersbeeh.medcontrol.data.UserPreferences;
import com.developersbeeh.medcontrol.data.repository.AchievementRepository;
import com.developersbeeh.medcontrol.data.repository.ActivityLogRepository;
import com.developersbeeh.medcontrol.data.repository.DocumentRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AddEditDocumentViewModel_Factory implements Factory<AddEditDocumentViewModel> {
  private final Provider<DocumentRepository> documentRepositoryProvider;

  private final Provider<AchievementRepository> achievementRepositoryProvider;

  private final Provider<ActivityLogRepository> activityLogRepositoryProvider;

  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<Context> contextProvider;

  private AddEditDocumentViewModel_Factory(Provider<DocumentRepository> documentRepositoryProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider, Provider<Context> contextProvider) {
    this.documentRepositoryProvider = documentRepositoryProvider;
    this.achievementRepositoryProvider = achievementRepositoryProvider;
    this.activityLogRepositoryProvider = activityLogRepositoryProvider;
    this.userPreferencesProvider = userPreferencesProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public AddEditDocumentViewModel get() {
    return newInstance(documentRepositoryProvider.get(), achievementRepositoryProvider.get(), activityLogRepositoryProvider.get(), userPreferencesProvider.get(), contextProvider.get());
  }

  public static AddEditDocumentViewModel_Factory create(
      Provider<DocumentRepository> documentRepositoryProvider,
      Provider<AchievementRepository> achievementRepositoryProvider,
      Provider<ActivityLogRepository> activityLogRepositoryProvider,
      Provider<UserPreferences> userPreferencesProvider, Provider<Context> contextProvider) {
    return new AddEditDocumentViewModel_Factory(documentRepositoryProvider, achievementRepositoryProvider, activityLogRepositoryProvider, userPreferencesProvider, contextProvider);
  }

  public static AddEditDocumentViewModel newInstance(DocumentRepository documentRepository,
      AchievementRepository achievementRepository, ActivityLogRepository activityLogRepository,
      UserPreferences userPreferences, Context context) {
    return new AddEditDocumentViewModel(documentRepository, achievementRepository, activityLogRepository, userPreferences, context);
  }
}
