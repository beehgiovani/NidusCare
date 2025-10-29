package com.developersbeeh.medcontrol.ui.caregiver

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.developersbeeh.medcontrol.NavGraphDirections
import com.developersbeeh.medcontrol.R
import com.developersbeeh.medcontrol.`data`.model.Dependente
import com.developersbeeh.medcontrol.`data`.model.Medicamento
import java.io.Serializable
import java.lang.UnsupportedOperationException
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress

public class CaregiverDashboardFragmentDirections private constructor() {
  private data class ActionCaregiverDashboardFragmentToDashboardDependenteFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int =
        R.id.action_caregiverDashboardFragment_to_dashboardDependenteFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionCaregiverDashboardFragmentToListMedicamentosFragment(
    public val dependentId: String,
    public val isCaregiver: Boolean = true,
  ) : NavDirections {
    public override val actionId: Int =
        R.id.action_caregiverDashboardFragment_toListMedicamentosFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putBoolean("isCaregiver", this.isCaregiver)
        return result
      }
  }

  private data class ActionCaregiverDashboardFragmentToInsightsFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_caregiverDashboardFragment_to_insightsFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionCaregiverDashboardFragmentToAddEditDependentFragment(
    public val dependente: Dependente? = null,
  ) : NavDirections {
    public override val actionId: Int =
        R.id.action_caregiverDashboardFragment_to_addEditDependentFragment

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

  private data class ActionCaregiverDashboardFragmentToAnalysisHistoryFragment(
    public val dependentId: String,
    public val dependentName: String,
  ) : NavDirections {
    public override val actionId: Int =
        R.id.action_caregiverDashboardFragment_to_analysisHistoryFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("dependentId", this.dependentId)
        result.putString("dependentName", this.dependentName)
        return result
      }
  }

  private data class ActionCaregiverDashboardFragmentToManageCaregiversFragment(
    public val dependente: Dependente?,
  ) : NavDirections {
    public override val actionId: Int =
        R.id.action_caregiverDashboardFragment_to_manageCaregiversFragment

    public override val arguments: Bundle
      @Suppress("CAST_NEVER_SUCCEEDS")
      get() {
        val result = Bundle()
        if (Parcelable::class.java.isAssignableFrom(Dependente::class.java)) {
          result.putParcelable("dependente", this.dependente as Parcelable?)
        } else if (Serializable::class.java.isAssignableFrom(Dependente::class.java)) {
          result.putSerializable("dependente", this.dependente as Serializable?)
        } else {
          throw UnsupportedOperationException(Dependente::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
        return result
      }
  }

  public companion object {
    public fun actionCaregiverDashboardFragmentToDashboardDependenteFragment(dependentId: String,
        dependentName: String): NavDirections =
        ActionCaregiverDashboardFragmentToDashboardDependenteFragment(dependentId, dependentName)

    public fun actionCaregiverDashboardFragmentToListMedicamentosFragment(dependentId: String,
        isCaregiver: Boolean = true): NavDirections =
        ActionCaregiverDashboardFragmentToListMedicamentosFragment(dependentId, isCaregiver)

    public fun actionCaregiverDashboardFragmentToInsightsFragment(dependentId: String,
        dependentName: String): NavDirections =
        ActionCaregiverDashboardFragmentToInsightsFragment(dependentId, dependentName)

    public fun actionCaregiverDashboardFragmentToAddEditDependentFragment(dependente: Dependente? =
        null): NavDirections =
        ActionCaregiverDashboardFragmentToAddEditDependentFragment(dependente)

    public fun actionCaregiverDashboardFragmentToAnalysisHistoryFragment(dependentId: String,
        dependentName: String): NavDirections =
        ActionCaregiverDashboardFragmentToAnalysisHistoryFragment(dependentId, dependentName)

    public fun actionCaregiverDashboardFragmentToManageCaregiversFragment(dependente: Dependente?):
        NavDirections = ActionCaregiverDashboardFragmentToManageCaregiversFragment(dependente)

    public fun actionCaregiverDashboardFragmentToReceivedInvitesFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_caregiverDashboardFragment_to_receivedInvitesFragment)

    public fun actionGlobalToHydrationTrackerFragment(dependentId: String): NavDirections =
        NavGraphDirections.actionGlobalToHydrationTrackerFragment(dependentId)

    public fun actionGlobalToMealTrackerFragment(dependentId: String): NavDirections =
        NavGraphDirections.actionGlobalToMealTrackerFragment(dependentId)

    public fun actionGlobalToWeightTrackerFragment(dependentId: String): NavDirections =
        NavGraphDirections.actionGlobalToWeightTrackerFragment(dependentId)

    public fun actionGlobalToPhysicalActivityTrackerFragment(dependentId: String): NavDirections =
        NavGraphDirections.actionGlobalToPhysicalActivityTrackerFragment(dependentId)

    public fun actionGlobalToSleepTrackerFragment(dependentId: String): NavDirections =
        NavGraphDirections.actionGlobalToSleepTrackerFragment(dependentId)

    public fun actionGlobalToPharmacySelectionFragment(): NavDirections =
        NavGraphDirections.actionGlobalToPharmacySelectionFragment()

    public fun actionGlobalToHealthGoalsFragment(dependentId: String): NavDirections =
        NavGraphDirections.actionGlobalToHealthGoalsFragment(dependentId)

    public fun actionSplashFragmentToDashboardDependenteFragment(dependentId: String,
        dependentName: String): NavDirections =
        NavGraphDirections.actionSplashFragmentToDashboardDependenteFragment(dependentId,
        dependentName)

    public fun actionGlobalToWellbeingDiaryFragment(dependentId: String, dependentName: String):
        NavDirections = NavGraphDirections.actionGlobalToWellbeingDiaryFragment(dependentId,
        dependentName)

    public fun actionGlobalToGeriatricCareFragment(dependentId: String, dependentName: String):
        NavDirections = NavGraphDirections.actionGlobalToGeriatricCareFragment(dependentId,
        dependentName)

    public fun actionGlobalToChatFragment(dependentId: String, dependentName: String): NavDirections
        = NavGraphDirections.actionGlobalToChatFragment(dependentId, dependentName)

    public fun actionGlobalToVaccinationCardFragment(
      dependentId: String,
      dependentName: String,
      dependentDob: String,
    ): NavDirections = NavGraphDirections.actionGlobalToVaccinationCardFragment(dependentId,
        dependentName, dependentDob)

    public fun actionGlobalToEducationCenterFragment(): NavDirections =
        NavGraphDirections.actionGlobalToEducationCenterFragment()

    public fun actionGlobalToDependentDiaryFragment(dependentId: String, dependentName: String):
        NavDirections = NavGraphDirections.actionGlobalToDependentDiaryFragment(dependentId,
        dependentName)

    public fun actionGlobalToAchievementsFragment(dependentId: String, dependentName: String):
        NavDirections = NavGraphDirections.actionGlobalToAchievementsFragment(dependentId,
        dependentName)

    public fun actionGlobalToAdherenceReportFragment(dependentId: String, dependentName: String):
        NavDirections = NavGraphDirections.actionGlobalToAdherenceReportFragment(dependentId,
        dependentName)

    public fun actionGlobalToCycleTrackerFragment(dependentId: String, dependentName: String):
        NavDirections = NavGraphDirections.actionGlobalToCycleTrackerFragment(dependentId,
        dependentName)

    public fun actionLoginFragmentToCompleteProfileFragment(): NavDirections =
        NavGraphDirections.actionLoginFragmentToCompleteProfileFragment()

    public fun actionGlobalToCompleteProfileFragment(): NavDirections =
        NavGraphDirections.actionGlobalToCompleteProfileFragment()

    public fun actionGlobalToAddEditDependentFragment(dependente: Dependente? = null): NavDirections
        = NavGraphDirections.actionGlobalToAddEditDependentFragment(dependente)

    public fun actionGlobalToPrescriptionScannerFragment(dependentId: String,
        dependentName: String): NavDirections =
        NavGraphDirections.actionGlobalToPrescriptionScannerFragment(dependentId, dependentName)

    public fun actionGlobalToAnalysisResultFragment(
      dependentId: String,
      dependentName: String,
      prompt: String? = null,
      analysisResult: String? = null,
    ): NavDirections = NavGraphDirections.actionGlobalToAnalysisResultFragment(dependentId,
        dependentName, prompt, analysisResult)

    public fun actionGlobalToManageFamilyFragment(): NavDirections =
        NavGraphDirections.actionGlobalToManageFamilyFragment()

    public fun actionGlobalToArchivedMedicationsFragment(dependentId: String,
        dependentName: String): NavDirections =
        NavGraphDirections.actionGlobalToArchivedMedicationsFragment(dependentId, dependentName)

    public fun actionGlobalToReportsFragment(dependentId: String, dependentName: String):
        NavDirections = NavGraphDirections.actionGlobalToReportsFragment(dependentId, dependentName)

    public fun actionGlobalToFarmacinhaFragment(dependentId: String? = null, dependentName: String?
        = null): NavDirections = NavGraphDirections.actionGlobalToFarmacinhaFragment(dependentId,
        dependentName)

    public fun actionGlobalToManageCaregiversFragment(dependente: Dependente? = null): NavDirections
        = NavGraphDirections.actionGlobalToManageCaregiversFragment(dependente)

    public fun actionGlobalToHealthScheduleFragment(dependentId: String, dependentName: String):
        NavDirections = NavGraphDirections.actionGlobalToHealthScheduleFragment(dependentId,
        dependentName)

    public fun actionGlobalToPremiumPlansFragment(): NavDirections =
        NavGraphDirections.actionGlobalToPremiumPlansFragment()

    public fun actionGlobalLogout(): NavDirections = NavGraphDirections.actionGlobalLogout()

    public fun actionGlobalToDashboard(): NavDirections =
        NavGraphDirections.actionGlobalToDashboard()

    public fun actionGlobalToMedications(dependentId: String, isCaregiver: Boolean = true):
        NavDirections = NavGraphDirections.actionGlobalToMedications(dependentId, isCaregiver)

    public fun actionGlobalToReminders(dependentId: String): NavDirections =
        NavGraphDirections.actionGlobalToReminders(dependentId)

    public fun actionGlobalToHealthNotes(dependentId: String, dependentName: String): NavDirections
        = NavGraphDirections.actionGlobalToHealthNotes(dependentId, dependentName)

    public fun actionGlobalToProfile(): NavDirections = NavGraphDirections.actionGlobalToProfile()

    public fun actionGlobalToSettings(): NavDirections = NavGraphDirections.actionGlobalToSettings()

    public fun actionGlobalToAnalytics(dependentId: String, dependentName: String): NavDirections =
        NavGraphDirections.actionGlobalToAnalytics(dependentId, dependentName)

    public fun actionGlobalToDoseHistoryFragment(dependentId: String): NavDirections =
        NavGraphDirections.actionGlobalToDoseHistoryFragment(dependentId)

    public fun actionGlobalToAddMedicamentoFragment(
      dependentId: String,
      dependentName: String,
      isCaregiver: Boolean,
      medicamento: Medicamento? = null,
    ): NavDirections = NavGraphDirections.actionGlobalToAddMedicamentoFragment(dependentId,
        dependentName, isCaregiver, medicamento)

    public fun actionGlobalToTimelineFragment(dependentId: String, dependentName: String):
        NavDirections = NavGraphDirections.actionGlobalToTimelineFragment(dependentId,
        dependentName)

    public fun actionGlobalToHealthDocumentsFragment(dependentId: String, dependentName: String):
        NavDirections = NavGraphDirections.actionGlobalToHealthDocumentsFragment(dependentId,
        dependentName)
  }
}
