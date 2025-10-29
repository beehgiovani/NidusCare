package com.developersbeeh.medcontrol.ui.healthnotes

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import com.developersbeeh.medcontrol.`data`.model.HealthNote
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmStatic

public data class AddHealthNoteFragmentArgs(
  public val dependentId: String,
  public val dependentName: String,
  public val healthNoteToEdit: HealthNote? = null,
) : NavArgs {
  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("dependentId", this.dependentId)
    result.putString("dependentName", this.dependentName)
    if (Parcelable::class.java.isAssignableFrom(HealthNote::class.java)) {
      result.putParcelable("healthNoteToEdit", this.healthNoteToEdit as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(HealthNote::class.java)) {
      result.putSerializable("healthNoteToEdit", this.healthNoteToEdit as Serializable?)
    }
    return result
  }

  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("dependentId", this.dependentId)
    result.set("dependentName", this.dependentName)
    if (Parcelable::class.java.isAssignableFrom(HealthNote::class.java)) {
      result.set("healthNoteToEdit", this.healthNoteToEdit as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(HealthNote::class.java)) {
      result.set("healthNoteToEdit", this.healthNoteToEdit as Serializable?)
    }
    return result
  }

  public companion object {
    @JvmStatic
    @Suppress("DEPRECATION")
    public fun fromBundle(bundle: Bundle): AddHealthNoteFragmentArgs {
      bundle.setClassLoader(AddHealthNoteFragmentArgs::class.java.classLoader)
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
      val __healthNoteToEdit : HealthNote?
      if (bundle.containsKey("healthNoteToEdit")) {
        if (Parcelable::class.java.isAssignableFrom(HealthNote::class.java) ||
            Serializable::class.java.isAssignableFrom(HealthNote::class.java)) {
          __healthNoteToEdit = bundle.get("healthNoteToEdit") as HealthNote?
        } else {
          throw UnsupportedOperationException(HealthNote::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __healthNoteToEdit = null
      }
      return AddHealthNoteFragmentArgs(__dependentId, __dependentName, __healthNoteToEdit)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): AddHealthNoteFragmentArgs {
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
      val __healthNoteToEdit : HealthNote?
      if (savedStateHandle.contains("healthNoteToEdit")) {
        if (Parcelable::class.java.isAssignableFrom(HealthNote::class.java) ||
            Serializable::class.java.isAssignableFrom(HealthNote::class.java)) {
          __healthNoteToEdit = savedStateHandle.get<HealthNote?>("healthNoteToEdit")
        } else {
          throw UnsupportedOperationException(HealthNote::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __healthNoteToEdit = null
      }
      return AddHealthNoteFragmentArgs(__dependentId, __dependentName, __healthNoteToEdit)
    }
  }
}
