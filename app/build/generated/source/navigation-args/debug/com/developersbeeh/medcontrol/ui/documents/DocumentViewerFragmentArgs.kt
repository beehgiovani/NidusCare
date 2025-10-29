package com.developersbeeh.medcontrol.ui.documents

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class DocumentViewerFragmentArgs(
  public val documentoUrl: String,
  public val documentoTitulo: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("documentoUrl", this.documentoUrl)
    result.putString("documentoTitulo", this.documentoTitulo)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("documentoUrl", this.documentoUrl)
    result.set("documentoTitulo", this.documentoTitulo)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): DocumentViewerFragmentArgs {
      bundle.setClassLoader(DocumentViewerFragmentArgs::class.java.classLoader)
      val __documentoUrl : String?
      if (bundle.containsKey("documentoUrl")) {
        __documentoUrl = bundle.getString("documentoUrl")
        if (__documentoUrl == null) {
          throw IllegalArgumentException("Argument \"documentoUrl\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"documentoUrl\" is missing and does not have an android:defaultValue")
      }
      val __documentoTitulo : String?
      if (bundle.containsKey("documentoTitulo")) {
        __documentoTitulo = bundle.getString("documentoTitulo")
        if (__documentoTitulo == null) {
          throw IllegalArgumentException("Argument \"documentoTitulo\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"documentoTitulo\" is missing and does not have an android:defaultValue")
      }
      return DocumentViewerFragmentArgs(__documentoUrl, __documentoTitulo)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        DocumentViewerFragmentArgs {
      val __documentoUrl : String?
      if (savedStateHandle.contains("documentoUrl")) {
        __documentoUrl = savedStateHandle["documentoUrl"]
        if (__documentoUrl == null) {
          throw IllegalArgumentException("Argument \"documentoUrl\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"documentoUrl\" is missing and does not have an android:defaultValue")
      }
      val __documentoTitulo : String?
      if (savedStateHandle.contains("documentoTitulo")) {
        __documentoTitulo = savedStateHandle["documentoTitulo"]
        if (__documentoTitulo == null) {
          throw IllegalArgumentException("Argument \"documentoTitulo\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"documentoTitulo\" is missing and does not have an android:defaultValue")
      }
      return DocumentViewerFragmentArgs(__documentoUrl, __documentoTitulo)
    }
  }
}
