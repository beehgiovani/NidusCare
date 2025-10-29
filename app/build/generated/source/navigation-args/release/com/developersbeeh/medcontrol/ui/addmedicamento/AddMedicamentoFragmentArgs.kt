package com.developersbeeh.medcontrol.ui.addmedicamento

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import com.developersbeeh.medcontrol.`data`.model.Medicamento
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmStatic

public data class AddMedicamentoFragmentArgs(
  public val dependentId: String,
  public val dependentName: String,
  public val isCaregiver: Boolean,
  public val medicamento: Medicamento? = null,
) : NavArgs {
  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toBundle(): Bundle {
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

  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    if (Parcelable::class.java.isAssignableFrom(Medicamento::class.java)) {
      result.set("medicamento", this.medicamento as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(Medicamento::class.java)) {
      result.set("medicamento", this.medicamento as Serializable?)
    }
    result.set("dependentId", this.dependentId)
    result.set("dependentName", this.dependentName)
    result.set("isCaregiver", this.isCaregiver)
    return result
  }

  public companion object {
    @JvmStatic
    @Suppress("DEPRECATION")
    public fun fromBundle(bundle: Bundle): AddMedicamentoFragmentArgs {
      bundle.setClassLoader(AddMedicamentoFragmentArgs::class.java.classLoader)
      val __medicamento : Medicamento?
      if (bundle.containsKey("medicamento")) {
        if (Parcelable::class.java.isAssignableFrom(Medicamento::class.java) ||
            Serializable::class.java.isAssignableFrom(Medicamento::class.java)) {
          __medicamento = bundle.get("medicamento") as Medicamento?
        } else {
          throw UnsupportedOperationException(Medicamento::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __medicamento = null
      }
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
      val __isCaregiver : Boolean
      if (bundle.containsKey("isCaregiver")) {
        __isCaregiver = bundle.getBoolean("isCaregiver")
      } else {
        throw IllegalArgumentException("Required argument \"isCaregiver\" is missing and does not have an android:defaultValue")
      }
      return AddMedicamentoFragmentArgs(__dependentId, __dependentName, __isCaregiver,
          __medicamento)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        AddMedicamentoFragmentArgs {
      val __medicamento : Medicamento?
      if (savedStateHandle.contains("medicamento")) {
        if (Parcelable::class.java.isAssignableFrom(Medicamento::class.java) ||
            Serializable::class.java.isAssignableFrom(Medicamento::class.java)) {
          __medicamento = savedStateHandle.get<Medicamento?>("medicamento")
        } else {
          throw UnsupportedOperationException(Medicamento::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __medicamento = null
      }
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
      val __isCaregiver : Boolean?
      if (savedStateHandle.contains("isCaregiver")) {
        __isCaregiver = savedStateHandle["isCaregiver"]
        if (__isCaregiver == null) {
          throw IllegalArgumentException("Argument \"isCaregiver\" of type boolean does not support null values")
        }
      } else {
        throw IllegalArgumentException("Required argument \"isCaregiver\" is missing and does not have an android:defaultValue")
      }
      return AddMedicamentoFragmentArgs(__dependentId, __dependentName, __isCaregiver,
          __medicamento)
    }
  }
}
