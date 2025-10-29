package com.developersbeeh.medcontrol.ui.vaccination

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class VaccinationCardFragmentArgs(
  public val dependentId: String,
  public val dependentName: String,
  public val dependentDob: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("dependentId", this.dependentId)
    result.putString("dependentName", this.dependentName)
    result.putString("dependentDob", this.dependentDob)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("dependentId", this.dependentId)
    result.set("dependentName", this.dependentName)
    result.set("dependentDob", this.dependentDob)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): VaccinationCardFragmentArgs {
      bundle.setClassLoader(VaccinationCardFragmentArgs::class.java.classLoader)
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
      val __dependentDob : String?
      if (bundle.containsKey("dependentDob")) {
        __dependentDob = bundle.getString("dependentDob")
        if (__dependentDob == null) {
          throw IllegalArgumentException("Argument \"dependentDob\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"dependentDob\" is missing and does not have an android:defaultValue")
      }
      return VaccinationCardFragmentArgs(__dependentId, __dependentName, __dependentDob)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        VaccinationCardFragmentArgs {
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
      val __dependentDob : String?
      if (savedStateHandle.contains("dependentDob")) {
        __dependentDob = savedStateHandle["dependentDob"]
        if (__dependentDob == null) {
          throw IllegalArgumentException("Argument \"dependentDob\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"dependentDob\" is missing and does not have an android:defaultValue")
      }
      return VaccinationCardFragmentArgs(__dependentId, __dependentName, __dependentDob)
    }
  }
}
