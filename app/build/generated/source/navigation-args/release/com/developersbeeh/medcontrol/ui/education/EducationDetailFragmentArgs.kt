package com.developersbeeh.medcontrol.ui.education

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import com.developersbeeh.medcontrol.`data`.model.ArtigoEducativo
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import kotlin.Suppress
import kotlin.jvm.JvmStatic

public data class EducationDetailFragmentArgs(
  public val artigo: ArtigoEducativo,
) : NavArgs {
  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toBundle(): Bundle {
    val result = Bundle()
    if (Parcelable::class.java.isAssignableFrom(ArtigoEducativo::class.java)) {
      result.putParcelable("artigo", this.artigo as Parcelable)
    } else if (Serializable::class.java.isAssignableFrom(ArtigoEducativo::class.java)) {
      result.putSerializable("artigo", this.artigo as Serializable)
    } else {
      throw UnsupportedOperationException(ArtigoEducativo::class.java.name +
          " must implement Parcelable or Serializable or must be an Enum.")
    }
    return result
  }

  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    if (Parcelable::class.java.isAssignableFrom(ArtigoEducativo::class.java)) {
      result.set("artigo", this.artigo as Parcelable)
    } else if (Serializable::class.java.isAssignableFrom(ArtigoEducativo::class.java)) {
      result.set("artigo", this.artigo as Serializable)
    } else {
      throw UnsupportedOperationException(ArtigoEducativo::class.java.name +
          " must implement Parcelable or Serializable or must be an Enum.")
    }
    return result
  }

  public companion object {
    @JvmStatic
    @Suppress("DEPRECATION")
    public fun fromBundle(bundle: Bundle): EducationDetailFragmentArgs {
      bundle.setClassLoader(EducationDetailFragmentArgs::class.java.classLoader)
      val __artigo : ArtigoEducativo?
      if (bundle.containsKey("artigo")) {
        if (Parcelable::class.java.isAssignableFrom(ArtigoEducativo::class.java) ||
            Serializable::class.java.isAssignableFrom(ArtigoEducativo::class.java)) {
          __artigo = bundle.get("artigo") as ArtigoEducativo?
        } else {
          throw UnsupportedOperationException(ArtigoEducativo::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
        if (__artigo == null) {
          throw IllegalArgumentException("Argument \"artigo\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"artigo\" is missing and does not have an android:defaultValue")
      }
      return EducationDetailFragmentArgs(__artigo)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        EducationDetailFragmentArgs {
      val __artigo : ArtigoEducativo?
      if (savedStateHandle.contains("artigo")) {
        if (Parcelable::class.java.isAssignableFrom(ArtigoEducativo::class.java) ||
            Serializable::class.java.isAssignableFrom(ArtigoEducativo::class.java)) {
          __artigo = savedStateHandle.get<ArtigoEducativo?>("artigo")
        } else {
          throw UnsupportedOperationException(ArtigoEducativo::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
        if (__artigo == null) {
          throw IllegalArgumentException("Argument \"artigo\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"artigo\" is missing and does not have an android:defaultValue")
      }
      return EducationDetailFragmentArgs(__artigo)
    }
  }
}
