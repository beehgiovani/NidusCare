package com.developersbeeh.medcontrol.di

import android.content.Context
import android.util.Log
import com.developersbeeh.medcontrol.billing.BillingClientWrapper
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.remote.GooglePlacesApiService
import com.developersbeeh.medcontrol.data.repository.*
import com.developersbeeh.medcontrol.util.AnalysisPdfGenerator
import com.developersbeeh.medcontrol.util.PdfReportGenerator
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.google.firebase.storage.FirebaseStorage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 🔐 Firebase Authentication
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // 📍 Localização (GPS)
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
        LocationServices.getFusedLocationProviderClient(context)

    // 🌐 Retrofit - Google Places API
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGooglePlacesApiService(retrofit: Retrofit): GooglePlacesApiService {
        return retrofit.create(GooglePlacesApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlacesRepository(
        apiService: GooglePlacesApiService,
        @ApplicationContext context: Context
    ): PlacesRepository {
        return PlacesRepository(apiService, context)
    }

    // 🔥 Firestore com persistência local habilitada
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
        } catch (e: IllegalStateException) {
            Log.w("AppModule", "Firestore settings already applied.")
        }
        return db
    }

    // ⚙️ Firebase Functions - Região correta confirmada (us-central1)
    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions {
        val functions = Firebase.functions("us-central1")
        Log.d("AppModule", "Firebase Functions configurado para região: ")
        return functions
    }

    // ☁️ Firebase Storage
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    // ⚙️ User Preferences (DataStore)
    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    // 👤 Repositório de Usuário
    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore,
        storage: FirebaseStorage,
        functions: FirebaseFunctions,
        userPreferences: UserPreferences
    ): UserRepository {
        return UserRepository(auth, db, storage, functions, userPreferences)
    }

    // 🧩 Moshi JSON Parser
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // 🍽️ Análise de Refeição (IA)
    @Provides
    @Singleton
    fun provideMealAnalysisRepository(
        functions: FirebaseFunctions,
        auth: FirebaseAuth,
        storage: FirebaseStorage,
        firestoreRepository: FirestoreRepository,
        moshi: Moshi,
        @ApplicationContext context: Context
    ): MealAnalysisRepository {
        return MealAnalysisRepository(functions, auth, storage, firestoreRepository, moshi, context)
    }

    // 💬 Chat com IA / Assistente
    @Provides
    @Singleton
    fun provideChatRepository(db: FirebaseFirestore, functions: FirebaseFunctions): ChatRepository {
        return ChatRepository(db, functions)
    }

    // ⏰ Lembretes
    @Provides
    @Singleton
    fun provideReminderRepository(db: FirebaseFirestore): ReminderRepository {
        return ReminderRepository(db)
    }

    // 💊 Medicamentos
    @Provides
    @Singleton
    fun provideMedicationRepository(
        db: FirebaseFirestore,
        auth: FirebaseAuth,
        achievementRepository: AchievementRepository,
        @ApplicationContext context: Context
    ): MedicationRepository {
        return MedicationRepository(db, auth, achievementRepository, context)
    }

    // 🔥 Firestore Repository (Funções, Storage e Auth)
    @Provides
    @Singleton
    fun provideFirestoreRepository(
        auth: FirebaseAuth,
        storage: FirebaseStorage,
        functions: FirebaseFunctions
    ): FirestoreRepository {
        return FirestoreRepository(auth, storage, functions)
    }

    // 📄 Documentos
    @Provides
    @Singleton
    fun provideDocumentRepository(db: FirebaseFirestore, storage: FirebaseStorage): DocumentRepository {
        return DocumentRepository(db, storage)
    }

    // 🔐 Permissões
    @Provides
    @Singleton
    fun providePermissionRepository(db: FirebaseFirestore): PermissionRepository {
        return PermissionRepository(db)
    }

    // 🕒 Agenda / Ciclos
    @Provides
    @Singleton
    fun provideScheduleRepository(db: FirebaseFirestore): ScheduleRepository {
        return ScheduleRepository(db)
    }

    // 🔄 Realtime Database
    @Provides
    @Singleton
    fun provideRealtimeDatabaseRepository(): RealtimeDatabaseRepository {
        return RealtimeDatabaseRepository()
    }

    // 🔁 Ciclos
    @Provides
    @Singleton
    fun provideCycleRepository(db: FirebaseFirestore): CycleRepository {
        return CycleRepository(db)
    }

    // 🏆 Conquistas
    @Provides
    @Singleton
    fun provideAchievementRepository(db: FirebaseFirestore): AchievementRepository {
        return AchievementRepository(db)
    }

    // 📚 Educação / Conteúdo informativo
    @Provides
    @Singleton
    fun provideEducationRepository(): EducationRepository {
        return EducationRepository()
    }

    // 💉 Vacinas
    @Provides
    @Singleton
    fun provideVaccineRepository(db: FirebaseFirestore): VaccineRepository {
        return VaccineRepository(db)
    }

    // 💬 Mensagens motivacionais
    @Provides
    @Singleton
    fun provideMotivationalMessageRepository(): MotivationalMessageRepository {
        return MotivationalMessageRepository()
    }

    // 💳 Billing / Assinaturas
    @Provides
    @Singleton
    fun provideBillingClientWrapper(
        @ApplicationContext context: Context,
        userRepository: UserRepository,
        userPreferences: UserPreferences
    ): BillingClientWrapper {
        return BillingClientWrapper(context, userRepository, userPreferences)
    }

    // 📊 Log de Atividades
    @Provides
    @Singleton
    fun provideActivityLogRepository(
        db: FirebaseFirestore,
        auth: FirebaseAuth,
        userPreferences: UserPreferences
    ): ActivityLogRepository {
        return ActivityLogRepository(db, auth, userPreferences)
    }

    // 🧠 Análise de Imagens (IA)
    @Provides
    @Singleton
    fun provideImageAnalysisRepository(
        functions: FirebaseFunctions,
        auth: FirebaseAuth,
        storage: FirebaseStorage,
        firestoreRepository: FirestoreRepository,
        medicationRepository: MedicationRepository,
        @ApplicationContext context: Context
    ): ImageAnalysisRepository {
        return ImageAnalysisRepository(functions, auth, storage, firestoreRepository, medicationRepository, context)
    }

    // 🧾 PDF Generator (Relatórios)
    @Provides
    @Singleton
    fun providePdfReportGenerator(@ApplicationContext context: Context): PdfReportGenerator {
        return PdfReportGenerator(context)
    }

    // 📄 PDF Generator (Análises)
    @Provides
    @Singleton
    fun provideAnalysisPdfGenerator(@ApplicationContext context: Context): AnalysisPdfGenerator {
        return AnalysisPdfGenerator(context)
    }
}
