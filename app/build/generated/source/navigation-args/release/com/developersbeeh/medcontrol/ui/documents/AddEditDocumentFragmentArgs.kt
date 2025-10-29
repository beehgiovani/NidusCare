package com.developersbeeh.medcontrol.ui.documents

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import com.developersbeeh.medcontrol.`data`.model.DocumentoSaude
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmStatic

public data class AddEditDocumentFragmentArgs(
  public val dependentId: String,
  public val dependentName: String,
  public val documento: DocumentoSaude? = null,
) : NavArgs {
  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("dependentId", this.dependentId)
    result.putString("dependentName", this.dependentName)
    if (Parcelable::class.java.isAssignableFrom(DocumentoSaude::class.java)) {
      result.putParcelable("documento", this.documento as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(DocumentoSaude::class.java)) {
      result.putSerializable("documento", this.documento as Serializable?)
    }
    return result
  }

  @Suppress("CAST_NEVER_SUCCEEDS")
  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("dependentId", this.dependentId)
    result.set("dependentName", this.dependentName)
    if (Parcelable::class.java.isAssignableFrom(DocumentoSaude::class.java)) {
      result.set("documento", this.documento as Parcelable?)
    } else if (Serializable::class.java.isAssignableFrom(DocumentoSaude::class.java)) {
      result.set("documento", this.documento as Serializable?)
    }
    return result
  }

  public companion object {
    @JvmStatic
    @Suppress("DEPRECATION")
    public fun fromBundle(bundle: Bundle): AddEditDocumentFragmentArgs {
      bundle.setClassLoader(AddEditDocumentFragmentArgs::class.java.classLoader)
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
      val __documento : DocumentoSaude?
      if (bundle.containsKey("documento")) {
        if (Parcelable::class.java.isAssignableFrom(DocumentoSaude::class.java) ||
            Serializable::class.java.isAssignableFrom(DocumentoSaude::class.java)) {
          __documento = bundle.get("documento") as DocumentoSaude?
        } else {
          throw UnsupportedOperationException(DocumentoSaude::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __documento = null
      }
      return AddEditDocumentFragmentArgs(__dependentId, __dependentName, __documento)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        AddEditDocumentFragmentArgs {
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
      val __documento : DocumentoSaude?
      if (savedStateHandle.contains("documento")) {
        if (Parcelable::class.java.isAssignableFrom(DocumentoSaude::class.java) ||
            Serializable::class.java.isAssignableFrom(DocumentoSaude::class.java)) {
          __documento = savedStateHandle.get<DocumentoSaude?>("documento")
        } else {
          throw UnsupportedOperationException(DocumentoSaude::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
      } else {
        __documento = null
      }
      return AddEditDocumentFragmentArgs(__dependentId, __dependentName, __documento)
    }
  }
}
