package com.developersbeeh.medcontrol.ui.pharmacy

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class PharmacyMedicationSelectionFragmentArgs(
  public val pharmacyName: String,
  public val pharmacyPhoneNumber: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("pharmacy_name", this.pharmacyName)
    result.putString("pharmacy_phone_number", this.pharmacyPhoneNumber)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("pharmacy_name", this.pharmacyName)
    result.set("pharmacy_phone_number", this.pharmacyPhoneNumber)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): PharmacyMedicationSelectionFragmentArgs {
      bundle.setClassLoader(PharmacyMedicationSelectionFragmentArgs::class.java.classLoader)
      val __pharmacyName : String?
      if (bundle.containsKey("pharmacy_name")) {
        __pharmacyName = bundle.getString("pharmacy_name")
        if (__pharmacyName == null) {
          throw IllegalArgumentException("Argument \"pharmacy_name\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"pharmacy_name\" is missing and does not have an android:defaultValue")
      }
      val __pharmacyPhoneNumber : String?
      if (bundle.containsKey("pharmacy_phone_number")) {
        __pharmacyPhoneNumber = bundle.getString("pharmacy_phone_number")
        if (__pharmacyPhoneNumber == null) {
          throw IllegalArgumentException("Argument \"pharmacy_phone_number\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"pharmacy_phone_number\" is missing and does not have an android:defaultValue")
      }
      return PharmacyMedicationSelectionFragmentArgs(__pharmacyName, __pharmacyPhoneNumber)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle):
        PharmacyMedicationSelectionFragmentArgs {
      val __pharmacyName : String?
      if (savedStateHandle.contains("pharmacy_name")) {
        __pharmacyName = savedStateHandle["pharmacy_name"]
        if (__pharmacyName == null) {
          throw IllegalArgumentException("Argument \"pharmacy_name\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"pharmacy_name\" is missing and does not have an android:defaultValue")
      }
      val __pharmacyPhoneNumber : String?
      if (savedStateHandle.contains("pharmacy_phone_number")) {
        __pharmacyPhoneNumber = savedStateHandle["pharmacy_phone_number"]
        if (__pharmacyPhoneNumber == null) {
          throw IllegalArgumentException("Argument \"pharmacy_phone_number\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"pharmacy_phone_number\" is missing and does not have an android:defaultValue")
      }
      return PharmacyMedicationSelectionFragmentArgs(__pharmacyName, __pharmacyPhoneNumber)
    }
  }
}
