package com.developersbeeh.medcontrol.ui.caregiver

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import com.developersbeeh.medcontrol.`data`.model.Dependente
import java.io.Serializable
import java.lang.UnsupportedOperationException
import kotlin.Suppress
import kotlin.jvm.JvmStatic

public data class AddEditDependentFragmentArgs(
  public val dependente: Dependente? = null,
) : NavArgs {
  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toBundle(): Bundle {
    val result = Bundle()
    if (Parcelable::class.java.isAssignableFrom(Dependente::class.java)) {
      result.putParcelable("dependente", this.dependente as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(Dependente::class.java)) {
      result.putSerializable("dependente", this.dependente as Serializable?)
    }
    return result
  }

  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    if (Parcelable::class.java.isAssignableFrom(Dependente::class.java)) {
      result.set("dependente", this.dependente as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(Dependente::class.java)) {
      result.set("dependente", this.dependente as Serializable?)
    }
    return result
  }

  public companion object {
    @JvmStatic
    @Suppress("DEPRECATION")
    public fun fromBundle(bundle: Bundle): AddEditDependentFragmentArgs {
      bundle.setClassLoader(AddEditDependentFragmentArgs::class.java.classLoader)
      val __dependente : Dependente?
      if (bundle.containsKey("dependente")) {
        if (Parcelable::class.java.isAssignableFrom(Dependente::class.java) ||
            Serializable::class.java.isAssignableFrom(Dependente::class.java)) {
          __dependente = bundle.get("dependente") as Dependente?
        } else {
          throw UnsupportedOperationException(Dependente::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __dependente = null
      }
      return AddEditDependentFragmentArgs(__dependente)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        AddEditDependentFragmentArgs {
      val __dependente : Dependente?
      if (savedStateHandle.contains("dependente")) {
        if (Parcelable::class.java.isAssignableFrom(Dependente::class.java) ||
            Serializable::class.java.isAssignableFrom(Dependente::class.java)) {
          __dependente = savedStateHandle.get<Dependente?>("dependente")
        } else {
          throw UnsupportedOperationException(Dependente::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __dependente = null
      }
      return AddEditDependentFragmentArgs(__dependente)
    }
  }
}
