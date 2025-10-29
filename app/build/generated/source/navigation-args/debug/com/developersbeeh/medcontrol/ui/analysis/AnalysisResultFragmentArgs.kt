package com.developersbeeh.medcontrol.ui.analysis

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class AnalysisResultFragmentArgs(
  public val prompt: String,
  public val dependentId: String,
  public val dependentName: String,
  public val analysisResult: String? = null,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("prompt", this.prompt)
    result.putString("analysisResult", this.analysisResult)
    result.putString("dependentId", this.dependentId)
    result.putString("dependentName", this.dependentName)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("prompt", this.prompt)
    result.set("analysisResult", this.analysisResult)
    result.set("dependentId", this.dependentId)
    result.set("dependentName", this.dependentName)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): AnalysisResultFragmentArgs {
      bundle.setClassLoader(AnalysisResultFragmentArgs::class.java.classLoader)
      val __prompt : String?
      if (bundle.containsKey("prompt")) {
        __prompt = bundle.getString("prompt")
        if (__prompt == null) {
          throw IllegalArgumentException("Argument \"prompt\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"prompt\" is missing and does not have an android:defaultValue")
      }
      val __analysisResult : String?
      if (bundle.containsKey("analysisResult")) {
        __analysisResult = bundle.getString("analysisResult")
      } else {
        __analysisResult = null
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
      return AnalysisResultFragmentArgs(__prompt, __dependentId, __dependentName, __analysisResult)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        AnalysisResultFragmentArgs {
      val __prompt : String?
      if (savedStateHandle.contains("prompt")) {
        __prompt = savedStateHandle["prompt"]
        if (__prompt == null) {
          throw IllegalArgumentException("Argument \"prompt\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"prompt\" is missing and does not have an android:defaultValue")
      }
      val __analysisResult : String?
      if (savedStateHandle.contains("analysisResult")) {
        __analysisResult = savedStateHandle["analysisResult"]
      } else {
        __analysisResult = null
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
      return AnalysisResultFragmentArgs(__prompt, __dependentId, __dependentName, __analysisResult)
    }
  }
}
