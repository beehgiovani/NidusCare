package com.developersbeeh.medcontrol.ui.scan

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class ScanAndConfirmFragmentArgs(
  public val dependentId: String?,
  public val dependentName: String?,
  public val title: String = "Escanear Medicamento",
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("dependentId", this.dependentId)
    result.putString("dependentName", this.dependentName)
    result.putString("title", this.title)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("dependentId", this.dependentId)
    result.set("dependentName", this.dependentName)
    result.set("title", this.title)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): ScanAndConfirmFragmentArgs {
      bundle.setClassLoader(ScanAndConfirmFragmentArgs::class.java.classLoader)
      val __dependentId : String?
      if (bundle.containsKey("dependentId")) {
        __dependentId = bundle.getString("dependentId")
      } else {
        throw IllegalArgumentException("Required argument \"dependentId\" is missing and does not have an android:defaultValue")
      }
      val __dependentName : String?
      if (bundle.containsKey("dependentName")) {
        __dependentName = bundle.getString("dependentName")
      } else {
        throw IllegalArgumentException("Required argument \"dependentName\" is missing and does not have an android:defaultValue")
      }
      val __title : String?
      if (bundle.containsKey("title")) {
        __title = bundle.getString("title")
        if (__title == null) {
          throw IllegalArgumentException("Argument \"title\" is marked as non-null but was passed a null value.")
        }
      } else {
        __title = "Escanear Medicamento"
      }
      return ScanAndConfirmFragmentArgs(__dependentId, __dependentName, __title)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        ScanAndConfirmFragmentArgs {
      val __dependentId : String?
      if (savedStateHandle.contains("dependentId")) {
        __dependentId = savedStateHandle["dependentId"]
      } else {
        throw IllegalArgumentException("Required argument \"dependentId\" is missing and does not have an android:defaultValue")
      }
      val __dependentName : String?
      if (savedStateHandle.contains("dependentName")) {
        __dependentName = savedStateHandle["dependentName"]
      } else {
        throw IllegalArgumentException("Required argument \"dependentName\" is missing and does not have an android:defaultValue")
      }
      val __title : String?
      if (savedStateHandle.contains("title")) {
        __title = savedStateHandle["title"]
        if (__title == null) {
          throw IllegalArgumentException("Argument \"title\" is marked as non-null but was passed a null value")
        }
      } else {
        __title = "Escanear Medicamento"
      }
      return ScanAndConfirmFragmentArgs(__dependentId, __dependentName, __title)
    }
  }
}
