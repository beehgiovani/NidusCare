package com.developersbeeh.medcontrol.ui.listmedicamentos

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Boolean
import kotlin.String
import kotlin.jvm.JvmStatic

public data class ListMedicamentosFragmentArgs(
  public val dependentId: String,
  public val isCaregiver: Boolean = true,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("dependentId", this.dependentId)
    result.putBoolean("isCaregiver", this.isCaregiver)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("dependentId", this.dependentId)
    result.set("isCaregiver", this.isCaregiver)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): ListMedicamentosFragmentArgs {
      bundle.setClassLoader(ListMedicamentosFragmentArgs::class.java.classLoader)
      val __dependentId : String?
      if (bundle.containsKey("dependentId")) {
        __dependentId = bundle.getString("dependentId")
        if (__dependentId == null) {
          throw IllegalArgumentException("Argument \"dependentId\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"dependentId\" is missing and does not have an android:defaultValue")
      }
      val __isCaregiver : Boolean
      if (bundle.containsKey("isCaregiver")) {
        __isCaregiver = bundle.getBoolean("isCaregiver")
      } else {
        __isCaregiver = true
      }
      return ListMedicamentosFragmentArgs(__dependentId, __isCaregiver)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        ListMedicamentosFragmentArgs {
      val __dependentId : String?
      if (savedStateHandle.contains("dependentId")) {
        __dependentId = savedStateHandle["dependentId"]
        if (__dependentId == null) {
          throw IllegalArgumentException("Argument \"dependentId\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"dependentId\" is missing and does not have an android:defaultValue")
      }
      val __isCaregiver : Boolean?
      if (savedStateHandle.contains("isCaregiver")) {
        __isCaregiver = savedStateHandle["isCaregiver"]
        if (__isCaregiver == null) {
          throw IllegalArgumentException("Argument \"isCaregiver\" of type boolean does not support null values")
        }
      } else {
        __isCaregiver = true
      }
      return ListMedicamentosFragmentArgs(__dependentId, __isCaregiver)
    }
  }
}
