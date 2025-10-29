package com.developersbeeh.medcontrol.ui.scan

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class PrescriptionAnalysisFragmentArgs(
  public val dependentId: String,
  public val dependentName: String,
  public val imageUri: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("dependentId", this.dependentId)
    result.putString("dependentName", this.dependentName)
    result.putString("imageUri", this.imageUri)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("dependentId", this.dependentId)
    result.set("dependentName", this.dependentName)
    result.set("imageUri", this.imageUri)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): PrescriptionAnalysisFragmentArgs {
      bundle.setClassLoader(PrescriptionAnalysisFragmentArgs::class.java.classLoader)
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
      val __imageUri : String?
      if (bundle.containsKey("imageUri")) {
        __imageUri = bundle.getString("imageUri")
        if (__imageUri == null) {
          throw IllegalArgumentException("Argument \"imageUri\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"imageUri\" is missing and does not have an android:defaultValue")
      }
      return PrescriptionAnalysisFragmentArgs(__dependentId, __dependentName, __imageUri)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        PrescriptionAnalysisFragmentArgs {
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
      val __imageUri : String?
      if (savedStateHandle.contains("imageUri")) {
        __imageUri = savedStateHandle["imageUri"]
        if (__imageUri == null) {
          throw IllegalArgumentException("Argument \"imageUri\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"imageUri\" is missing and does not have an android:defaultValue")
      }
      return PrescriptionAnalysisFragmentArgs(__dependentId, __dependentName, __imageUri)
    }
  }
}
