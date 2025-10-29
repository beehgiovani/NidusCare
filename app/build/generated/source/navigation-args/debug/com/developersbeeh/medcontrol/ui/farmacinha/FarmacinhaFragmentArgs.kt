package com.developersbeeh.medcontrol.ui.farmacinha

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import kotlin.String
import kotlin.jvm.JvmStatic

public data class FarmacinhaFragmentArgs(
  public val dependentId: String? = null,
  public val dependentName: String? = null,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("dependentId", this.dependentId)
    result.putString("dependentName", this.dependentName)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("dependentId", this.dependentId)
    result.set("dependentName", this.dependentName)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): FarmacinhaFragmentArgs {
      bundle.setClassLoader(FarmacinhaFragmentArgs::class.java.classLoader)
      val __dependentId : String?
      if (bundle.containsKey("dependentId")) {
        __dependentId = bundle.getString("dependentId")
      } else {
        __dependentId = null
      }
      val __dependentName : String?
      if (bundle.containsKey("dependentName")) {
        __dependentName = bundle.getString("dependentName")
      } else {
        __dependentName = null
      }
      return FarmacinhaFragmentArgs(__dependentId, __dependentName)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): FarmacinhaFragmentArgs {
      val __dependentId : String?
      if (savedStateHandle.contains("dependentId")) {
        __dependentId = savedStateHandle["dependentId"]
      } else {
        __dependentId = null
      }
      val __dependentName : String?
      if (savedStateHandle.contains("dependentName")) {
        __dependentName = savedStateHandle["dependentName"]
      } else {
        __dependentName = null
      }
      return FarmacinhaFragmentArgs(__dependentId, __dependentName)
    }
  }
}
