package com.developersbeeh.medcontrol.ui.schedule

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import com.developersbeeh.medcontrol.`data`.model.AgendamentoSaude
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmStatic

public data class AddEditScheduleFragmentArgs(
  public val dependentId: String,
  public val dependentName: String,
  public val agendamento: AgendamentoSaude? = null,
) : NavArgs {
  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("dependentId", this.dependentId)
    result.putString("dependentName", this.dependentName)
    if (Parcelable::class.java.isAssignableFrom(AgendamentoSaude::class.java)) {
      result.putParcelable("agendamento", this.agendamento as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(AgendamentoSaude::class.java)) {
      result.putSerializable("agendamento", this.agendamento as Serializable?)
    }
    return result
  }

  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("dependentId", this.dependentId)
    result.set("dependentName", this.dependentName)
    if (Parcelable::class.java.isAssignableFrom(AgendamentoSaude::class.java)) {
      result.set("agendamento", this.agendamento as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(AgendamentoSaude::class.java)) {
      result.set("agendamento", this.agendamento as Serializable?)
    }
    return result
  }

  public companion object {
    @JvmStatic
    @Suppress("DEPRECATION")
    public fun fromBundle(bundle: Bundle): AddEditScheduleFragmentArgs {
      bundle.setClassLoader(AddEditScheduleFragmentArgs::class.java.classLoader)
      val __dependentId : String?
      if (bundle.containsKey("dependentId")) {
        __dependentId = bundle.getString("dependentId")
        if (__dependentId == null) {
          throw IllegalArgumentException("Argument \"dependentId\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"dependentId\" is missing and does not have an android:defaultValue")
      }
      val __dependentName : String?
      if (bundle.containsKey("dependentName")) {
        __dependentName = bundle.getString("dependentName")
        if (__dependentName == null) {
          throw IllegalArgumentException("Argument \"dependentName\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"dependentName\" is missing and does not have an android:defaultValue")
      }
      val __agendamento : AgendamentoSaude?
      if (bundle.containsKey("agendamento")) {
        if (Parcelable::class.java.isAssignableFrom(AgendamentoSaude::class.java) ||
            Serializable::class.java.isAssignableFrom(AgendamentoSaude::class.java)) {
          __agendamento = bundle.get("agendamento") as AgendamentoSaude?
        } else {
          throw UnsupportedOperationException(AgendamentoSaude::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __agendamento = null
      }
      return AddEditScheduleFragmentArgs(__dependentId, __dependentName, __agendamento)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        AddEditScheduleFragmentArgs {
      val __dependentId : String?
      if (savedStateHandle.contains("dependentId")) {
        __dependentId = savedStateHandle["dependentId"]
        if (__dependentId == null) {
          throw IllegalArgumentException("Argument \"dependentId\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"dependentId\" is missing and does not have an android:defaultValue")
      }
      val __dependentName : String?
      if (savedStateHandle.contains("dependentName")) {
        __dependentName = savedStateHandle["dependentName"]
        if (__dependentName == null) {
          throw IllegalArgumentException("Argument \"dependentName\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"dependentName\" is missing and does not have an android:defaultValue")
      }
      val __agendamento : AgendamentoSaude?
      if (savedStateHandle.contains("agendamento")) {
        if (Parcelable::class.java.isAssignableFrom(AgendamentoSaude::class.java) ||
            Serializable::class.java.isAssignableFrom(AgendamentoSaude::class.java)) {
          __agendamento = savedStateHandle.get<AgendamentoSaude?>("agendamento")
        } else {
          throw UnsupportedOperationException(AgendamentoSaude::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __agendamento = null
      }
      return AddEditScheduleFragmentArgs(__dependentId, __dependentName, __agendamento)
    }
  }
}
