package com.developersbeeh.medcontrol

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.developersbeeh.medcontrol.`data`.model.Dependente
import com.developersbeeh.medcontrol.`data`.model.Medicamento
import java.io.Serializable
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress

public class NavGraphDirections private constructor() {
  private data class ActionGlobalToHydrationTrackerFragment(
    public val dependentId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_hydrationTrackerFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        return result
      }
  }

  private data class ActionGlobalToMealTrackerFragment(
    public val dependentId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_mealTrackerFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        return result
      }
  }

  private data class ActionGlobalToWeightTrackerFragment(
    public val dependentId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_weightTrackerFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        return result
      }
  }

  private data class ActionGlobalToPhysicalActivityTrackerFragment(
    public val dependentId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_physicalActivityTrackerFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        return result
      }
  }

  private data class ActionGlobalToSleepTrackerFragment(
    public val dependentId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_sleepTrackerFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        return result
      }
  }

  private data class ActionGlobalToHealthGoalsFragment(
    public val dependentId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_healthGoalsFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        return result
      }
  }

  private data class ActionSplashFragmentToDashboardDependenteFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_splashFragment_to_dashboardDependenteFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToWellbeingDiaryFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_wellbeingDiaryFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToGeriatricCareFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_geriatricCareFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToChatFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_chatFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToVaccinationCardFragment(
    public val dependentId: String,
    public val dependentName: String,
    public val dependentDob: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_vaccinationCardFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        result.putString("dependentDob", this.dependentDob)
        return result
      }
  }

  private data class ActionGlobalToDependentDiaryFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_dependentDiaryFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToAchievementsFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_achievementsFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToAdherenceReportFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_adherenceReportFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToCycleTrackerFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_cycleTrackerFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToAddEditDependentFragment(
    public val dependente: Dependente? = null,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_addEditDependentFragment

    public override val arguments: Bundle
      @Suppress("CAST_NEVER_SUCCEEDS")
      get() {
        val result = Bundle()
        if (Parcelable::class.java.isAssignableFrom(Dependente::class.java)) {
          result.putParcelable("dependente", this.dependente as Parcelable?)
        } else if (Serializable::class.java.isAssignableFrom(Dependente::class.java)) {
          result.putSerializable("dependente", this.dependente as Serializable?)
        }
        return result
      }
  }

  private data class ActionGlobalToPrescriptionScannerFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_prescriptionScannerFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToAnalysisResultFragment(
    public val dependentId: String,
    public val dependentName: String,
    public val prompt: String? = null,
    public val analysisResult: String? = null,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_analysisResultFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        result.putString("prompt", this.prompt)
        result.putString("analysisResult", this.analysisResult)
        return result
      }
  }

  private data class ActionGlobalToArchivedMedicationsFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_archivedMedicationsFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToReportsFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_reportsFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToFarmacinhaFragment(
    public val dependentId: String? = null,
    public val dependentName: String? = null,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_farmacinhaFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToManageCaregiversFragment(
    public val dependente: Dependente? = null,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_manageCaregiversFragment

    public override val arguments: Bundle
      @Suppress("CAST_NEVER_SUCCEEDS")
      get() {
        val result = Bundle()
        if (Parcelable::class.java.isAssignableFrom(Dependente::class.java)) {
          result.putParcelable("dependente", this.dependente as Parcelable?)
        } else if (Serializable::class.java.isAssignableFrom(Dependente::class.java)) {
          result.putSerializable("dependente", this.dependente as Serializable?)
        }
        return result
      }
  }

  private data class ActionGlobalToHealthScheduleFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_healthScheduleFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToMedications(
    public val dependentId: String,
    public val isCaregiver: Boolean = true,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_medications

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putBoolean("isCaregiver", this.isCaregiver)
        return result
      }
  }

  private data class ActionGlobalToReminders(
    public val dependentId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_reminders

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        return result
      }
  }

  private data class ActionGlobalToHealthNotes(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_health_notes

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToAnalytics(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_analytics

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToDoseHistoryFragment(
    public val dependentId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_doseHistoryFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        return result
      }
  }

  private data class ActionGlobalToAddMedicamentoFragment(
    public val dependentId: String,
    public val dependentName: String,
    public val isCaregiver: Boolean,
    public val medicamento: Medicamento? = null,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_addMedicamentoFragment

    public override val arguments: Bundle
      @Suppress("CAST_NEVER_SUCCEEDS")
      get() {
        val result = Bundle()
        if (Parcelable::class.java.isAssignableFrom(Medicamento::class.java)) {
          result.putParcelable("medicamento", this.medicamento as Parcelable?)
        } else if (Serializable::class.java.isAssignableFrom(Medicamento::class.java)) {
          result.putSerializable("medicamento", this.medicamento as Serializable?)
        }
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        result.putBoolean("isCaregiver", this.isCaregiver)
        return result
      }
  }

  private data class ActionGlobalToTimelineFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_timelineFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionGlobalToHealthDocumentsFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_global_to_healthDocumentsFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  public companion object {
    public fun actionGlobalToHydrationTrackerFragment(dependentId: String): NavDirections =
        ActionGlobalToHydrationTrackerFragment(dependentId)

    public fun actionGlobalToMealTrackerFragment(dependentId: String): NavDirections =
        ActionGlobalToMealTrackerFragment(dependentId)

    public fun actionGlobalToWeightTrackerFragment(dependentId: String): NavDirections =
        ActionGlobalToWeightTrackerFragment(dependentId)

    public fun actionGlobalToPhysicalActivityTrackerFragment(dependentId: String): NavDirections =
        ActionGlobalToPhysicalActivityTrackerFragment(dependentId)

    public fun actionGlobalToSleepTrackerFragment(dependentId: String): NavDirections =
        ActionGlobalToSleepTrackerFragment(dependentId)

    public fun actionGlobalToPharmacySelectionFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_global_to_pharmacySelectionFragment)

    public fun actionGlobalToHealthGoalsFragment(dependentId: String): NavDirections =
        ActionGlobalToHealthGoalsFragment(dependentId)

    public fun actionSplashFragmentToDashboardDependenteFragment(dependentId: String,
        dependentName: String): NavDirections =
        ActionSplashFragmentToDashboardDependenteFragment(dependentId, dependentName)

    public fun actionGlobalToWellbeingDiaryFragment(dependentId: String, dependentName: String):
        NavDirections = ActionGlobalToWellbeingDiaryFragment(dependentId, dependentName)

    public fun actionGlobalToGeriatricCareFragment(dependentId: String, dependentName: String):
        NavDirections = ActionGlobalToGeriatricCareFragment(dependentId, dependentName)

    public fun actionGlobalToChatFragment(dependentId: String, dependentName: String): NavDirections
        = ActionGlobalToChatFragment(dependentId, dependentName)

    public fun actionGlobalToVaccinationCardFragment(
      dependentId: String,
      dependentName: String,
      dependentDob: String,
    ): NavDirections = ActionGlobalToVaccinationCardFragment(dependentId, dependentName,
        dependentDob)

    public fun actionGlobalToEducationCenterFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_global_to_educationCenterFragment)

    public fun actionGlobalToDependentDiaryFragment(dependentId: String, dependentName: String):
        NavDirections = ActionGlobalToDependentDiaryFragment(dependentId, dependentName)

    public fun actionGlobalToAchievementsFragment(dependentId: String, dependentName: String):
        NavDirections = ActionGlobalToAchievementsFragment(dependentId, dependentName)

    public fun actionGlobalToAdherenceReportFragment(dependentId: String, dependentName: String):
        NavDirections = ActionGlobalToAdherenceReportFragment(dependentId, dependentName)

    public fun actionGlobalToCycleTrackerFragment(dependentId: String, dependentName: String):
        NavDirections = ActionGlobalToCycleTrackerFragment(dependentId, dependentName)

    public fun actionLoginFragmentToCompleteProfileFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_loginFragment_to_completeProfileFragment)

    public fun actionGlobalToCompleteProfileFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_global_to_completeProfileFragment)

    public fun actionGlobalToAddEditDependentFragment(dependente: Dependente? = null): NavDirections
        = ActionGlobalToAddEditDependentFragment(dependente)

    public fun actionGlobalToPrescriptionScannerFragment(dependentId: String,
        dependentName: String): NavDirections =
        ActionGlobalToPrescriptionScannerFragment(dependentId, dependentName)

    public fun actionGlobalToAnalysisResultFragment(
      dependentId: String,
      dependentName: String,
      prompt: String? = null,
      analysisResult: String? = null,
    ): NavDirections = ActionGlobalToAnalysisResultFragment(dependentId, dependentName, prompt,
        analysisResult)

    public fun actionGlobalToManageFamilyFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_global_to_manageFamilyFragment)

    public fun actionGlobalToArchivedMedicationsFragment(dependentId: String,
        dependentName: String): NavDirections =
        ActionGlobalToArchivedMedicationsFragment(dependentId, dependentName)

    public fun actionGlobalToReportsFragment(dependentId: String, dependentName: String):
        NavDirections = ActionGlobalToReportsFragment(dependentId, dependentName)

    public fun actionGlobalToFarmacinhaFragment(dependentId: String? = null, dependentName: String?
        = null): NavDirections = ActionGlobalToFarmacinhaFragment(dependentId, dependentName)

    public fun actionGlobalToManageCaregiversFragment(dependente: Dependente? = null): NavDirections
        = ActionGlobalToManageCaregiversFragment(dependente)

    public fun actionGlobalToHealthScheduleFragment(dependentId: String, dependentName: String):
        NavDirections = ActionGlobalToHealthScheduleFragment(dependentId, dependentName)

    public fun actionGlobalToPremiumPlansFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_global_to_premiumPlansFragment)

    public fun actionGlobalLogout(): NavDirections =
        ActionOnlyNavDirections(R.id.action_global_logout)

    public fun actionGlobalToDashboard(): NavDirections =
        ActionOnlyNavDirections(R.id.action_global_to_dashboard)

    public fun actionGlobalToMedications(dependentId: String, isCaregiver: Boolean = true):
        NavDirections = ActionGlobalToMedications(dependentId, isCaregiver)

    public fun actionGlobalToReminders(dependentId: String): NavDirections =
        ActionGlobalToReminders(dependentId)

    public fun actionGlobalToHealthNotes(dependentId: String, dependentName: String): NavDirections
        = ActionGlobalToHealthNotes(dependentId, dependentName)

    public fun actionGlobalToProfile(): NavDirections =
        ActionOnlyNavDirections(R.id.action_global_to_profile)

    public fun actionGlobalToSettings(): NavDirections =
        ActionOnlyNavDirections(R.id.action_global_to_settings)

    public fun actionGlobalToAnalytics(dependentId: String, dependentName: String): NavDirections =
        ActionGlobalToAnalytics(dependentId, dependentName)

    public fun actionGlobalToDoseHistoryFragment(dependentId: String): NavDirections =
        ActionGlobalToDoseHistoryFragment(dependentId)

    public fun actionGlobalToAddMedicamentoFragment(
      dependentId: String,
      dependentName: String,
      isCaregiver: Boolean,
      medicamento: Medicamento? = null,
    ): NavDirections = ActionGlobalToAddMedicamentoFragment(dependentId, dependentName, isCaregiver,
        medicamento)

    public fun actionGlobalToTimelineFragment(dependentId: String, dependentName: String):
        NavDirections = ActionGlobalToTimelineFragment(dependentId, dependentName)

    public fun actionGlobalToHealthDocumentsFragment(dependentId: String, dependentName: String):
        NavDirections = ActionGlobalToHealthDocumentsFragment(dependentId, dependentName)
  }
}
